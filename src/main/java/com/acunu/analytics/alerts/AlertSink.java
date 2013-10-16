package com.acunu.analytics.alerts;

import java.io.IOException;
import java.util.Map;

import com.acunu.analytics.Context;
import com.acunu.analytics.conf.ConfigProperties;

/**
 * Abstract class that an alert sink must extend.
 */
public abstract class AlertSink {

	private final String name;
	private final Context context;

	public AlertSink(String name, Context context) {
		this.name = name;
		this.context = context;
	}

	protected String getName() {
		return name;
	}

	protected AlertServer getAlertServer() {
		return context.getAlertServer();
	}

	protected ConfigProperties getConfig() {
		return context.getConfig();
	}

	/**
	 * Called to initialize the alert sink.
	 * @throws IOException
	 */
	public abstract void init() throws IOException;

	/**
	 * Name with which to register this sink.
	 * 
	 * @param destination
	 *            Where to send the alert to -- composed from the alert config.
	 * @param body
	 *            The content of the message to be sent -- composed from the
	 *            alert config.
	 * @param vars
	 *            Variables instantiated from global, local and row-specific
	 *            sources.
	 * @throws IOException
	 */
	public abstract void sendAlert(String destination, String body, Map<String, Object> vars) throws IOException;

	/**
	 * Called to shutdown the alert sink.
	 * @throws IOException
	 */
	public abstract void shutdown() throws IOException;
}
