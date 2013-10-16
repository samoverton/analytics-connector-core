package com.acunu.analytics;

import com.acunu.analytics.alerts.AlertServer;
import com.acunu.analytics.client.AnalyticsClient;
import com.acunu.analytics.conf.ConfigProperties;

public interface Context {
	public static class SimpleContext implements Context {
		private final ConfigProperties config;
		private final Directory<? extends EventReceiver> directory;
		private final AnalyticsClient client;
		private final AlertServer alertServer;

		public SimpleContext(ConfigProperties config, Directory<? extends EventReceiver> directory,
				AnalyticsClient client, AlertServer alertServer) {
			this.config = config;
			this.directory = directory;
			this.client = client;
			this.alertServer = alertServer;
		}

		public ConfigProperties getConfig() {
			return config;
		}

		public Directory<? extends EventReceiver> getEventReceiverDirectory() {
			return directory;
		}

		public AnalyticsClient getClient() {
			return client;
		}

		public AlertServer getAlertServer() {
			return alertServer;
		}
	}

	ConfigProperties getConfig();

	Directory<? extends EventReceiver> getEventReceiverDirectory();

	AnalyticsClient getClient();

	AlertServer getAlertServer();
}
