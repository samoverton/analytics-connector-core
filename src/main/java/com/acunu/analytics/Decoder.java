package com.acunu.analytics;

import java.util.List;

import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.analytics.ingest.DecodeException;

/**
 * A decoder to turn a raw event of some type into an {@link Event}. Instances
 * should provide a no-args constructor and a constructor that takes a set of
 * properties, an instance of {@link ConfigProperties}.
 * 
 * @author tmoreton
 */
public interface Decoder {

	/**
	 * For requests submitted via HTTP, the key in the properties which
	 * specifies, if provided, the value of the supplied ContentType header.
	 */
	public static final String CONTENT_TYPE_KEY = "Content-Type";

	/**
	 * Return the list of values for Content-Type that this Decoder could be
	 * invoked for, and possibly decode a message. It is not a contract that
	 * they have to be able to decode those messages (it could return
	 * DecodeException), or that this decoder will be called (another
	 * registering the same type may be invoked instead).
	 */
	public List<String> getRegisterableContentTypes();

	/**
	 * Decode a raw event.
	 * 
	 * @return one or more Events, in the order they should be ingested.
	 * @throws DecodeException
	 *             when the raw event is invalid.
	 */
	public List<Event> decode(Object rawEvent) throws DecodeException;
}
