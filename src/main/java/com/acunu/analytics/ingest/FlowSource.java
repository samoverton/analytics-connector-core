package com.acunu.analytics.ingest;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acunu.analytics.Flow;

/**
 * The source of a particular flow with respect to an ingester.
 * 
 * Extend this class to implement the retrieval of a particular flow of messages
 * (e.g. an MQ or Kafka topic) from an ingester that is already initialized.
 * 
 * @author tmoreton
 * 
 * @param <I>
 */
public abstract class FlowSource<I extends AbstractIngester> {

	private static Logger logger = LoggerFactory.getLogger(FlowSource.class);

	protected AtomicBoolean running = new AtomicBoolean(true);

	/**
	 * Create a {@link FlowSource} with a particular flow specification.
	 * 
	 * @param ingester
	 *            the {@link AbstractIngester} from which events will be read.
	 * @param flow
	 *            the flow for which this is a source.
	 */
	protected FlowSource(I ingester, Flow flow) {
		this.ingester = ingester;
		this.flow = flow;
		this.pool = Executors.newFixedThreadPool(1);
	}

	protected final I ingester;

	public I getIngester() {
		return this.ingester;
	}

	protected final Flow flow;

	public Flow getFlow() {
		return this.flow;
	}

	protected ExecutorService pool;

	@Override
	public String toString() {
		return this.flow.getName();
	}

	/**
	 * Set up this particular source. Will be called once - and from that point
	 * can start processing events.
	 * 
	 * @throws IngestException
	 */
	public void start() throws IngestException {
		this.pool.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ingestLoop();
				return null;
			}
		});
	}

	/**
	 * Stop this source. Will be called once after all invocations of
	 * ingestSomeMore() have completed or thrown InterruptedException.
	 */
	public void stop() throws InterruptedException {
		if (this.running.compareAndSet(true, false)) {
			this.pool.shutdownNow();
			this.pool.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	/**
	 * Default loop for ingesting events, suitable for "pull" sources. Calls
	 * {@link #ingestSomeMore} to return a batch of events; if none are
	 * returned, sleeps for a second. Otherwise keep pulling more events.
	 * 
	 * @throws IngestException
	 */
	protected void ingestLoop() throws IngestException, InterruptedException {
		try {
			while (running.get()) {
				try {
					final List<?> events = ingestSomeMore();
					if (!events.isEmpty())
						ingester.enqueueEventsForFlow(flow, events);
					else
						Thread.sleep(100);
				} catch (InterruptedException e) {
					/* Don't worry */
				}
			}
		} catch (Exception e) {
			logger.error("Error while processing events", e);
			throw new IngestException(e);
		}
	}

	/**
	 * Main ingestion loop.
	 * 
	 * @return Zero or more events. Any events returned will be sent together to
	 *         a processing thread in the ingester.
	 */
	protected abstract List<?> ingestSomeMore() throws IngestException, InterruptedException;

}
