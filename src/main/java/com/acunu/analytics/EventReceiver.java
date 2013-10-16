package com.acunu.analytics;

import java.io.IOException;

/**
 * An entity capable of receiving events -- either a Bird instance to store
 * them, or a filter to manipulate them and send them on.
 * 
 * @author abyde
 */
public interface EventReceiver {

	/**
	 * Unique name within the set of event receivers.
	 */
	String getName();

	/**
	 * Register that an event has occurred. The event is described in a map from
	 * dimension name to value.
	 * 
	 * @throws IOException
	 *             if there's a problem storing the event in the back-end.
	 * @throws ParseException
	 *             if the values cannot be parsed.
	 */
	void submitEvent(Event event) throws IOException;

	/**
	 * Flush any pending updates to disk. This does not prevent new updates from
	 * arriving.
	 */
	void flush() throws IOException;

}
