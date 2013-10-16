package com.acunu.analytics.ingest;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import com.acunu.analytics.Decoder;
import com.acunu.analytics.Event;
import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.analytics.conf.SimpleConfig;

/**
 * Helper class for building decoders.
 * 
 * @author tmoreton
 * 
 * @param <RawEventType>
 */
public abstract class AbstractDecoder implements Decoder {

	protected AbstractDecoder() {
		this.properties = new SimpleConfig();
	}

	/**
	 * Called whenever a decoder is instantiated in the context of the flow.
	 * 
	 * @param properties
	 *            a read-only reference to the flow properties.
	 */
	protected AbstractDecoder(ConfigProperties properties) {
		this.properties = properties;
	}

	protected final ConfigProperties properties;

	/**
	 * Decode a raw event.
	 * 
	 * @return one or more Events, in the order they should be ingested.
	 * @throws DecodeException
	 *             when the raw event is invalid.
	 */
	@Override
	public abstract List<Event> decode(Object rawEvent) throws DecodeException;

	/**
	 * Helper method to get a Reader over the raw event.
	 * 
	 * @throws DecodeException
	 *             If the raw event isn't a String, byte[] or char[].
	 */
	protected Reader getStringReader(Object rawEvent) throws DecodeException {
		if (rawEvent instanceof String) {
			return new StringReader((String) rawEvent);
		} else if (rawEvent instanceof byte[]) {
			return new InputStreamReader(new ByteArrayInputStream((byte[]) rawEvent));
		} else if (rawEvent instanceof char[]) {
			return new CharArrayReader((char[]) rawEvent);
		} else {
			throw new DecodeException("Cannot decode a " + rawEvent.getClass().getCanonicalName());
		}
	}
	
}
