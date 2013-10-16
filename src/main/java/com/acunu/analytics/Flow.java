package com.acunu.analytics;

import com.acunu.analytics.conf.ConfigProperties;

/**
 * Flow defines event flow between different components.
 * 
 * {@link Ingester} -> {@link Decoder} -> {@link EventReceiver}
 * 
 * Flows are linked to Ingester. There is no meaning of Flow in the absence of
 * Ingester and EventReceivers.
 * 
 * As each Ingester can have more than one Flow, it has to decide for each event
 * which Flow should it go to. This demux operation happens on the basis of
 * Topic.
 * 
 * @author bmuppana
 */
public interface Flow {
	/**
	 * Get the name of Flow.
	 */
	public String getName();

	/**
	 * Get the ingester (has access to the flow properties).
	 */
	public String getIngesterName();

	/**
	 * Class name of the decoder for this flow.
	 */
	public String getDecoderClassName();

	/**
	 * Decoder instance for this flow (has access to the flow properties).
	 */
	@SuppressWarnings("rawtypes")
	public Decoder getDecoder();

	/**
	 * Name of the EventReceiver for this Flow.
	 */
	public String getReceiverName();

	/**
	 * EventReceiver instance for this Flow.
	 */
	public EventReceiver getReceiver();
	
	public String toAQLString();

	/**
	 * Properties available to both the decoder and the ingester.
	 * @return
	 */
	public ConfigProperties getProperties();
}
