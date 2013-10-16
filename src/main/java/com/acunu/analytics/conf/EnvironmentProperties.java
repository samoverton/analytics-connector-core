package com.acunu.analytics.conf;

public interface EnvironmentProperties {

	/** System property to override the location of analytics.yaml -- default /etc/acunu/conf/analytics.yaml */
	public static final String ENV_CONFIG_PATH = "analytics.config.path";
	/** System property to override the base directory of all the config files.  /etc/acunu */
	public static final String ENV_BASE_DIR = "analytics.config.dir";

}
