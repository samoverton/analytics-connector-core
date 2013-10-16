package com.acunu.analytics;

import com.acunu.analytics.ingest.IngestException;

/**
 * Ingester is just a stand alone component that can receive raw events from
 * custom sources. {@link Flow} defines actual event flow between different
 * components. A single Ingester can server multiple flows, but a Flow always
 * bound to one Ingester. Ingester owns the Flow.
 * 
 * @author bmuppana
 */
public abstract class Ingester {

	protected final String name;
	protected final Context context;

	/**
	 * Ingester's declared AQL name
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Initialize and start-up the Ingester. Even though, it has started, it
	 * can't possibly serve any events unless some Flows are defined.
	 * 
	 * @param name
	 *            The ingester name
	 * 
	 * @param context
	 *            Execution context, with properties and access to a client
	 *            object.
	 */
	public Ingester(String name, Context context) throws IngestException {
		this.name = name;
		this.context = context;
	}

	/**
	 * Add a new Flow {@link Flow}
	 */
	public abstract void addFlow(Flow flow) throws IngestException;

	/**
	 * Remove a Flow.
	 */
	public abstract void dropFlow(Flow flow) throws IngestException;

	/**
	 * Return the number of active flows, i.e. the total number of successful
	 * calls to addFlow less succesful calls to dropFlow.
	 */
	public abstract int getFlowCount();

	/**
	 * Synchronous call to clean-up everything and get ready to shutdown JVM.
	 */
	public abstract void shutdown();
}