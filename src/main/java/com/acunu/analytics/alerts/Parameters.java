package com.acunu.analytics.alerts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Parameters {

	public static final String PARAM_FREQ_SEC = "frequency_seconds";
	public static final String PARAM_DEST = "destination";
	public static final String PARAM_BODY = "body";
	public static final String PARAM_QUERY = "query";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_METHOD = "method";
	
	/** How long after an alert has failed to check again. */
	public static final String PARAM_LONG_WAIT = "LONG_WAIT";
	public static final long DEFAULT_LONG_WAIT = 1000l;
	/** How long after an alert has fired to check the next. */
	public static final String PARAM_SHORT_WAIT = "SHORT_WAIT";
	public static final long DEFAULT_SHORT_WAIT = 100l;

	/** How many threads to have in the pool delivering alerts */
	public static final String PARAM_DELIVERY_THREADS = "delivery_threads";
	public static final int DEFAULT_DELIVERY_THREADS = 4;
	/** Where to do reads from. */
	public static final String CONFIG_ANALYTICS_URL = "analytics_url";
	
	public static final String DEFAULT_PROTOCOL = "http";
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 8080;
	public static final String DEFAULT_PATH = "/analytics/api/";
	public static final String DEFAULT_ANALYTICS_URL = DEFAULT_PROTOCOL + "://" + DEFAULT_HOST + ":" + DEFAULT_PORT + DEFAULT_PATH;
		
	/** Alert delivery reporting */
	public static final String REPORTING = "reporting";
	public static final String REPORTING_URL = "url";
	public static final String REPORTING_TABLE = "table";
	public static final String REPORTING_TABLE_DEFAULT = "alert_delivery";

	/** containing key for ssl details . */
	public static final String SSL_DETAILS = "ssl_details";
	/** ssl details of analytics node */
	public static final String ANALYTICS_URL_KEY = "url";
	public static final String PARAM_KEYSTORE = "ks";
	public static final String PARAM_KEYSTORE_PW = "kspw";
	public static final String PARAM_TRUSTSTORE = "ts";
	public static final String PARAM_TRUSTSTORE_PW = "tspw";
	public static final Set<String> REQUIRED_KEYSTORE_PARAMS = new HashSet<String>(Arrays.asList(PARAM_KEYSTORE,
			PARAM_KEYSTORE_PW, PARAM_TRUSTSTORE, PARAM_TRUSTSTORE_PW));

	public static final String CONFIG_SUPER_QUERY = "super_query";
	
	/** If true, don't send alerts, just print'em. */
	public static final String CONFIG_TEST = "test";
	
	/** Globally available variables. */
	public static final String CONFIG_GLOBAL_VARS = "global";
	
	/** The field whose value is a list of alert definitions. */
	public static final String CONFIG_ALERT_DEFS = "alerts";

	/** The field whose value is a list of {@link AlertSink}s. */
	public static final String CONFIG_SINKS = "sinks";

	/** The field, per alert, whose value is the {@link AlertSink} to use for that alert. */
	public static final String CONFIG_SINK = "sink";
	
	/** The field whose value is a map of HTTP headers to send in a request. */
	public static final String CONFIG_HTTP_HEADERS = "http_headers";
	
	/** The name of configuration file. */
	public static final String CONFIG_FILE_NAME = "alert-config.yaml";

	/**
	 * The field whose value is the amount by which alert values will need to
	 * change to justify alerting. If not set, alerts will be set without regard
	 * for the last alerted value.
	 */
	public static final String CONFIG_CHANGE_THRESHOLD = "change_threshold";

	enum UnsplitJsonRow {
		TRUE,
		FALSE,
		BEST_GUESS;
	}

	public static final String UNSPLIT_ROW_JSON = "unsplit_row_json";
	public static final UnsplitJsonRow DEFAULT_UNSPLIT_ROW_JSON = UnsplitJsonRow.BEST_GUESS;

	/*
	 *  N.B The default retries will mean the last retry is
	 *  approximately 156.25 seconds after the first attempt
	 */

	/** How many times to retry a failed sending of alerts **/
	public static final String PARAM_MAX_RETRIES = "max_retries";
	public static final long DEFAULT_MAX_RETRIES = 5;

	/** The initial delay to retry a failed sending of alerts in ms**/
	public static final String PARAM_RETRY_INITIAL_DELAY = "retry_initial_delay";
	public static final long DEFAULT_RETRY_INITIAL_DELAY = 1000;

	/** The multiplier to apply to the delay to retry a failed sending of alerts **/
	public static final String PARAM_BACKOFF_MULTIPLIER = "backoff_multiplier";
	public static final int DEFAULT_BACKOFF_MULTIPLIER = 2;
	/**	The size of the queue which alerts can be put onto, awaiting re-delivery **/
	public static final int DEFAULT_MAX_DELAYED_TASKS = 100000;
	public static final String PARAM_MAX_DELAYED_TASKS = "max_delayed_tasks";
	/**	The timeout in milliseconds used when checking if a host is up **/
	public static final String PARAM_HOST_CHECK_TIMEOUT = "host_check_timeout";
	public static final int DEFAULT_HOST_CHECK_TIMEOUT = 10000;

	/**	Maximum number of times an alert will attempt to be re-delivered
	 A re-delivery occurs when a host comes back online from being dead for a period of time **/
	public static final String PARAM_MAX_REDELIVERY_ATTEMPTS = "max_redelivery_attempts";
	public static final int DEFAULT_MAX_REDELIVERY_ATTEMPTS = 3;
	/**	How often to check hosts in milliseconds to see if they are up again or not. **/
	public static final String PARAM_CHECK_HOST_PERIOD = "check_host_period";
	public static final long DEFAULT_CHECK_HOST_PERIOD = 10000;

}
