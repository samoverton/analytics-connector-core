package com.acunu.analytics.alerts;

import java.util.Iterator;
import java.util.Map;

import com.acunu.analytics.conf.ConfigProperties;

/**
 * Interface for the configuration settings of the Alert Monitor and Alert sinks.
 * @author tmoreton
 *
 */
public interface AlertMonitorConfig extends ConfigProperties {

	public abstract Iterator<Map.Entry<String, ConfigProperties>> enumerateAlertSinkConfigs();

	public abstract ConfigProperties getSslDetails();

	public abstract ConfigProperties getReporting();
	
	public Map<String,String> getHttpHeaders();

}