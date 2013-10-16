package com.acunu.analytics.ingest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acunu.analytics.Context;
import com.acunu.analytics.Event;
import com.acunu.analytics.Flow;
import com.acunu.analytics.Ingester;
import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.util.Pair;

/**
 * Base class for ingesters.
 * 
 */
public abstract class AbstractIngester extends Ingester {

	private static Logger logger = LoggerFactory.getLogger(AbstractIngester.class);

	/**
	 * Please give your ingester a friendly representation.
	 */
	public String toString() {
		return getClass().getSimpleName() + " " + this.getName();
	}

	/**
	 * The config we're passed on init
	 **/
	protected ConfigProperties config;

	/**
	 * Map of flow name to the associated FlowSource and the thread pool.
	 */
	protected Map<String, FlowSource<? extends AbstractIngester>> flows = new HashMap<String, FlowSource<? extends AbstractIngester>>();

	protected AtomicBoolean running = new AtomicBoolean(true);

	protected static int systemThreads = 4;

	protected int consumerThreads;

	static {
		// initialize thread pool
		try {
			systemThreads = Integer.parseInt(System.getenv("threads"));
		} catch (NumberFormatException e) {
		}
		if (systemThreads < 1)
			systemThreads = 1;
	}

	protected AtomicLong numRead = new AtomicLong(0);
	protected int[] numEvents;

	/**
	 * Pool of threads servicing events from flows.
	 */
	protected ExecutorService pool;

	/** Event queues are endpoint, object pairs */
	protected BlockingQueue<Pair<Flow, List<?>>>[] eventQueues;

	public static final int EVENT_INTERVAL = 100000;
	public static final int STATS_INTERVAL = 60;

	protected ScheduledExecutorService statsExecutor;
	protected final Runnable statsTask = new Runnable() {
		protected long lastTime = Long.MIN_VALUE;
		protected int lastTotalEvents = 0;
		protected long start = System.currentTimeMillis();

		@Override
		public void run() {
			double thisInterval;
			long end = System.currentTimeMillis();
			double sinceStart = (end - start) / 1000.0;
			if (lastTime == Long.MIN_VALUE)
				thisInterval = sinceStart;
			else
				thisInterval = (end - lastTime) / 1000.0;

			int totalEvents = 0;
			for (int i = 0; i < numEvents.length; i++)
				totalEvents += numEvents[i];
			int thisTotalEvents = totalEvents - lastTotalEvents;
			lastTotalEvents = totalEvents;

			if (logger.isInfoEnabled() && thisInterval > 0 && sinceStart > 0) {
				logger.info(String.format("%s processed %d events in last %.1f secs (%.2f events/sec). Over all time %d events (%.2f events/sec)",
						getName(), thisTotalEvents, thisInterval, thisTotalEvents / thisInterval, totalEvents, totalEvents / sinceStart));
			}

			lastTime = System.currentTimeMillis();
		}
	};

	@SuppressWarnings("unchecked")
	protected <V> V getProperty(Class<V> c, String key, V defaultValue) {
		Object o = this.config.get(key);
		if (c.isInstance(o)) {
			return (V) o;
		} else
			return defaultValue;
	}

	/**
	 * Initialize and start-up the Ingester. Even though, it has started, it
	 * can't possibly serve any events unless some Flows are defined.
	 * 
	 * @param ingesterProperties
	 *            Ingester specific properties
	 * 
	 * @param config
	 *            jBird config could be used by Ingester.
	 */
	@SuppressWarnings("unchecked")
	public AbstractIngester(String name, Context context) throws IngestException {
		super(name, context);
		this.config = context.getConfig();
		this.consumerThreads = getProperty(int.class, "consumer_threads", systemThreads);

		logger.info("Creating consumer thread pool with {} threads", consumerThreads);
		pool = Executors.newFixedThreadPool(consumerThreads);
		numEvents = new int[consumerThreads];

		// initialize event queues
		eventQueues = (BlockingQueue<Pair<Flow, List<? extends Object>>>[]) new BlockingQueue[consumerThreads];
		for (int i = 0; i < consumerThreads; i++) {
			eventQueues[i] = new LinkedBlockingQueue<Pair<Flow, List<? extends Object>>>(4096);
		}

		// initialize stats service
		statsExecutor = Executors.newScheduledThreadPool(1);

		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();

		// create consumer tasks
		for (int i = 0; i < consumerThreads; i++) {
			final int taskId = i;
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						return processEvents(taskId);
					} catch (Exception e) {
						logger.error("Error while processing events", e);
						throw e;
					}
				}
			});
		}

		// schedule stats task
		statsExecutor.scheduleAtFixedRate(statsTask, STATS_INTERVAL, STATS_INTERVAL, TimeUnit.SECONDS);

		// start consumer tasks
		for (Callable<Void> task : tasks) {
			pool.submit(task);
		}
	}

	/**
	 * Add a new Flow {@link Flow}. Build a fixed size pool of threads to
	 * service the flow.
	 */
	public void addFlow(Flow flow) throws IngestException {

		final FlowSource<? extends AbstractIngester> fs;

		synchronized (flows) {
			// Check a flow is not already registered under this name.
			if (flows.containsKey(flow.getName()))
				throw new IngestException(String.format("A flow named '%s' already exists", flow.getName()));

			// Create a new FlowSource.
			fs = createFlowSource(flow);

			// Started ok. Add the FlowSource to the map
			flows.put(flow.getName(), fs);
		}

		// Try and start the FlowSource. May raise IngestException up.
		try {
			fs.start();
		} catch (IngestException e) {
			synchronized (flows) {
				flows.remove(flow.getName());
			}
			throw e;
		}
	}

	/**
	 * Remove a Flow.
	 */
	public synchronized void dropFlow(Flow flow) throws IngestException {

		FlowSource<? extends AbstractIngester> fs;

		synchronized (flows) {
			// Retrieve the flow registered under this name.
			fs = flows.remove(flow.getName());

			if (fs == null)
				throw new IngestException(String.format("No flow named %s currently exists", flow.getName()));
		}

		try {
			fs.stop();
		} catch (InterruptedException e) {
			logger.warn(String.format("Interrupted shutting down %s", fs));
		}
	}

	public int getFlowCount() {
		synchronized (flows) {
			return this.flows.size();
		}
	}

	/**
	 * Create a new FlowSource for this flow. After registration, start() will
	 * be called on that FlowSource. Then read
	 */
	protected abstract FlowSource<? extends AbstractIngester> createFlowSource(Flow flow) throws IngestException;

	/**
	 * Push an event to an endpoint for processing on a particular thread.
	 */
	protected void enqueueEventsForFlow(int taskId, Flow flow, List<? extends Object> events) throws InterruptedException {
		eventQueues[taskId].offer(new Pair<Flow, List<?>>(flow, events));
	}

	/**
	 * Push an event to an endpoint for processing.
	 */
	protected void enqueueEventsForFlow(Flow flow, List<?> events) throws InterruptedException {
		enqueueEventsForFlow((int) ((numRead.getAndIncrement() & 0xffffffff) % consumerThreads), flow, events);
	}

	/**
	 * Task for processing events.
	 */
	protected Void processEvents(int taskId) throws InterruptedException {
		int i = 0;
		while (running.get()) {
			try {
				// Take an item off this work queue -- a Flow and a batch of
				// events.
				final Pair<Flow, List<?>> p = eventQueues[taskId].take();
				final Flow flow = p.getKey();

				// Decode and forward each event to the matching EventReceiver.
				for (final Object rawEvent : p.getValue()) {

					// Decode events
					final List<Event> events;
					try {
						events = flow.getDecoder().decode(rawEvent);
					} catch (DecodeException e) {
						logger.warn(String.format("Exception during decoding of %s: %s", rawEvent, e.getMessage()), e);
						continue;
					}

					// Take each event separately and try to submit it.
					// TODO - what are the semantics for a batch that we want
					// here?
					for (final Event event : events) {
						try {
							flow.getReceiver().submitEvent(event);
						} catch (Exception e) {
							logger.warn(String.format("Exception during processing of %s: %s", event, e.getMessage()), e);
						}
					}

					// TODO semantics of stats: raw events, processed events.
					numEvents[taskId]++;
					if (++i % EVENT_INTERVAL == 0)
						logger.debug("Thread {} processed {} raw events", taskId, i);
				}
			} catch (InterruptedException e) {
				/* do nothing. If legit, this.running will be false. */
			}
		}
		return null;
	}

	/**
	 * Shutdown all flows, then the consumer threads, finally the stats thread.
	 */
	public void shutdown() {

		if (running.getAndSet(false)) {

			logger.info(String.format("Shutting down ingester %s...", toString()));

			// Shutdown all FlowSources by calling stop(). They are responsible
			// for stopping their own workers.

			for (FlowSource<? extends AbstractIngester> flowSource : this.flows.values()) {
				try {
					flowSource.stop();
				} catch (InterruptedException e) {
					logger.warn(String.format("Interrupted shutting down %s", flowSource));
				}
			}

			// Shutdown consumer threads by interrupting, and forcing them to check this.running.
			try {
				pool.shutdownNow();
				pool.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.warn(String.format("Interrupted shutting down ingester %s", toString()));
			}

			statsTask.run();
			statsExecutor.shutdown();

			logger.info(String.format("Done shutting down ingester %s", toString()));
		}
	}

	protected void addShutdownHook() {
		// show stats on exit
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				shutdown();
			}
		}));
	}

}
