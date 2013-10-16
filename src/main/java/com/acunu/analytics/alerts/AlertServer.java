package com.acunu.analytics.alerts;

import java.util.Map;

public interface AlertServer {
	public static final String VAR_ALERT_DESTINATION = "alert.destination";
	public static final String VAR_ALERT_SUBMIT_TIME = "alert.submit_time";

	public void reportAlert(Map<String, Object> alertVars);
}
