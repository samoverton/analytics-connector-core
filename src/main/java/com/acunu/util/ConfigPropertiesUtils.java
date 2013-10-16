package com.acunu.util;

import java.util.Map;

import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.analytics.conf.SimpleConfig;

public class ConfigPropertiesUtils {

	public static ConfigProperties asConfig(Object obj) {
		if (obj == null)
			return null;
		else if (obj instanceof ConfigProperties)
			return (ConfigProperties)obj;
		else if (obj instanceof Map)
			return SimpleConfig.fromMap( (Map<String, ?>)obj);
		else
			throw new IllegalArgumentException("Cannot extract map from property " + obj);
	}
}
