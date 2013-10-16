package com.acunu.analytics.alerts;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acunu.analytics.Context;
import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.util.Pair;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * An alert sink that provides multi-threaded re-delivery.
 * Extend this class to benefit from the same capabilities.
 */
public abstract class AbstractAlertSink extends AlertSink {

	private static final Logger logger = LoggerFactory.getLogger(AbstractAlertSink.class);
	public static final String VAR_ALERT_RESPONSE = "alert.response";
	public static final String VAR_ALERT_RESPONSE_CODE = "alert.response_code";
	public static final String VAR_METHOD = "method";
	private static final String APPLICATION_JSON = "application/json";

	protected ScheduledThreadPoolExecutor alertDeliveryPool;

	protected LinkedBlockingQueue<AlertDeliveryTask> hostDownPendingTasks;

	/** Whether or not a given host is up */
	protected ConcurrentMap<InetSocketAddress, Boolean> hostUp;

	protected final long maxRetries;
	protected final long retryInitialInterval;
	protected final int backoffMultiplier;
	protected final Integer maxDelayedTasks;
	protected int maxRedeliveryAttempts;
	protected int hostChecktimeout;
	protected long checkHostPeriod;

	protected final String postContentType = null;
	protected final String postAcceptHeader = APPLICATION_JSON;

	public AbstractAlertSink(String name, Context context) {
		super(name, context);
		ConfigProperties config = getConfig();

		int deliveryThreads = config.getInteger(Parameters.PARAM_DELIVERY_THREADS, Parameters.DEFAULT_DELIVERY_THREADS);

		alertDeliveryPool = new ScheduledThreadPoolExecutor(deliveryThreads, new ThreadFactoryBuilder().setNameFormat("alert-deliverer-%d").build());

		maxRetries = config.getLong(Parameters.PARAM_MAX_RETRIES, Parameters.DEFAULT_MAX_RETRIES);
		retryInitialInterval = config.getLong(Parameters.PARAM_RETRY_INITIAL_DELAY, Parameters.DEFAULT_RETRY_INITIAL_DELAY);
		backoffMultiplier = config.getInteger(Parameters.PARAM_BACKOFF_MULTIPLIER, Parameters.DEFAULT_BACKOFF_MULTIPLIER);
		maxDelayedTasks = config.getInteger(Parameters.PARAM_MAX_DELAYED_TASKS, Parameters.DEFAULT_MAX_DELAYED_TASKS);

		hostUp = new ConcurrentHashMap<InetSocketAddress, Boolean>();

		hostChecktimeout = config.getInteger(Parameters.PARAM_HOST_CHECK_TIMEOUT, Parameters.DEFAULT_HOST_CHECK_TIMEOUT);
		checkHostPeriod = config.getLong(Parameters.PARAM_CHECK_HOST_PERIOD, Parameters.DEFAULT_CHECK_HOST_PERIOD);
		maxRedeliveryAttempts = config.getInteger(Parameters.PARAM_MAX_REDELIVERY_ATTEMPTS, Parameters.DEFAULT_MAX_REDELIVERY_ATTEMPTS);

		hostDownPendingTasks = new LinkedBlockingQueue<AlertDeliveryTask>(maxDelayedTasks);
	}

	/**
	 * Start checking whether hosts who are the target of alerts are up or down.
	 */
	@Override
	public void init() throws IOException {
		alertDeliveryPool.scheduleWithFixedDelay(new CheckForAliveHosts(), 0, checkHostPeriod, TimeUnit.MILLISECONDS);
	}

	/**
	 * Implement this method to raise a new alert.
	 */
	@Override
	public abstract void sendAlert(String destination, String body, Map<String, Object> varMap) throws IOException;

	/**
	 * Use this method in subclasses to enqueue a new {@link AlertDeliveryTask}
	 */
	protected void sendAlertImpl(AlertDeliveryTask task) {
		this.alertDeliveryPool.submit(task);
	}

	/**
	 * Task to asynchronously submit alerts.
	 * 
	 * @author abyde
	 */
	abstract public class AlertDeliveryTask implements Runnable {

		protected static final int BYTES_TO_READ_FROM_RESPONSE = 128;
		protected int currentRetry;
		protected int previousAttempts;

		/**
		 * An optional host address to focus redelivery attempts when hosts come
		 * online again. If this field is null, then tasks will always get
		 * rescheduled for another delivery attempt.
		 */
		public InetSocketAddress address;

		protected Map<String, Object> alertVars;

		public AlertDeliveryTask(InetSocketAddress address, Map<String, Object> alertVars) {
			this.address = address;
			this.alertVars = alertVars;
			this.currentRetry = 0;
			this.previousAttempts = 0;
		}

		public AlertDeliveryTask(Map<String, Object> alertVars) {
			this(null, alertVars);
		}

		@Override
		public void run() {

			// Submit the event. Subclass can update alertVars.
			long alertStartTime = System.currentTimeMillis();
			final Pair<Exception, Boolean> p = trySubmissionAndReportAlert();
			final Exception exn = p!=null ? p.getKey() : null;
			final boolean retry = p!=null ? p.getValue() : false;

			if (exn != null) {
				if (logger.isErrorEnabled())
					logger.error(String.format("Sending alert failed, %sretrying: %s Alert details : %s", retry ? "" : "not ", exn, this));
			}

			// Retry is instructed to.
			if (retry) {
				alertVars.put(AlertServer.VAR_ALERT_SUBMIT_TIME, System.currentTimeMillis() - alertStartTime);
				getAlertServer().reportAlert(alertVars);
				retrySendAlert();
			}
		}

		/**
		 * Submit an event and report any response in the alertVars structure.
		 * The server will be called back with this structure on completion.
		 * 
		 * All exceptions must be handled in the method. Return null to
		 * indicate success, or a pair indicating exception and whether to retry.
		 **/
		protected abstract Pair<Exception,Boolean> trySubmissionAndReportAlert();

		private void retrySendAlert() {
			long retryIn = retryInitialInterval * Math.round(Math.pow(backoffMultiplier, currentRetry));
			if (currentRetry < maxRetries) {
				alertDeliveryPool.schedule(this, retryIn, TimeUnit.MILLISECONDS);
			} else if (previousAttempts < maxRedeliveryAttempts) {
				previousAttempts++;
				logger.warn("failed to deliver alert, exhausted retry attempts, placing on queue for delivery later: {} ", this);
				currentRetry = 0;
				try {
					hostDownPendingTasks.add(this);
				} catch (IllegalStateException ise) {
					logger.error("WARNING - QUEUE full, DROPPING alert entirely: {} ", this);
				}
			} else {
				logger.error("WARNING - failed to deliver alert, exhausted retry attempts, DROPPING alert entirely: {} ", this);
			}

			currentRetry++;
		}

		@Override
		public String toString() {
			return "maxRetries=" + maxRetries + ", retryInitialInterval=" + retryInitialInterval + ", backoffMultiplier=" + backoffMultiplier
					+ ", currentRetry=" + currentRetry + ", previousAttempts=" + previousAttempts;
		}
	}

	/**
	 * A task to periodically check whether hosts are up or down and retry
	 * failed alerts when hosts come online again.
	 * 
	 * @author mbyrd
	 * 
	 */
	class CheckForAliveHosts implements Runnable {

		public void run() {

			logger.info("Checking for hosts going up or down");
			Set<InetSocketAddress> newlyUp = null;

			for (Map.Entry<InetSocketAddress, Boolean> entry : hostUp.entrySet()) {

				InetSocketAddress host = entry.getKey();
				boolean currentUpStatus = entry.getValue();
				boolean newUpStatus = hostUP(host);
				logger.info("host : {} is up: {}", new Object[] { host, newUpStatus });

				if (currentUpStatus != newUpStatus) {
					hostUp.put(host, newUpStatus);

					if (newUpStatus) {
						logger.info("host : {} back up again ", host);
						if (newlyUp == null) {
							newlyUp = new HashSet<InetSocketAddress>();
						}
						newlyUp.add(host);
					} else {
						logger.warn("host : {} marked as down", host);

					}
				}
			}

			if (newlyUp != null && !newlyUp.isEmpty())
				attemptRedelivery(newlyUp);
			logger.info("Done checking whether hosts are up or down");
		}

		private void attemptRedelivery(Set<InetSocketAddress> newlyUp) {

			logger.info("Attempting to re-deliver any queued alerts for hosts '{}'", newlyUp);
			List<AlertDeliveryTask> toRedeliver = new ArrayList<AlertDeliveryTask>(hostDownPendingTasks.size() / 8);

			logger.info("items in queue prior to draining: {}", hostDownPendingTasks.size());
			hostDownPendingTasks.drainTo(toRedeliver);
			int schedulerForRedelivery = 0;
			int reQueuedTasks = 0;

			logger.info("Running through {} drained items to see if they can be redelivered", toRedeliver.size());
			for (AlertDeliveryTask task : toRedeliver) {

				if (task.address == null || newlyUp.contains(task.address)) {
					alertDeliveryPool.submit(task);
					schedulerForRedelivery++;
				} else {
					try {
						hostDownPendingTasks.add(task);
						reQueuedTasks++;
					} catch (IllegalStateException ise) {
						logger.error("WARNING - QUEUE full, DROPPING alert entirely: {} ", this);
					}
				}
			}
			logger.info("Submitted for redelivery {} alerts", schedulerForRedelivery);
			logger.info("Requeued on hostDownPendingTasks queue {} alerts", reQueuedTasks);
			logger.info("Size of hostDownPendingTasks queue post draining: {}", hostDownPendingTasks.size());

		}

		private boolean hostUP(InetSocketAddress host) {

			Socket socket = null;
			boolean up = false;
			try {
				socket = new Socket();
				socket.connect(host, hostChecktimeout);
				up = true;
			} catch (IOException e) {

			} catch (IllegalArgumentException iae) {
				logger.error("Illegal arg exception whilst trying to check whether host: {} is up : {}."
						+ "As a result redelivery of failed http alert devivery may not occur,"
						+ "check your configuration to ensure the host is correct.", new Object[] { host, iae });
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						logger.warn("problem closing socket, when testing for host {}", host);
					}
				}
			}

			return up;
		}
	}

	@Override
	public void shutdown() throws IOException {
	}

}
