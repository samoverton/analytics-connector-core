package com.acunu.analytics.conf;

import java.util.List;
import java.util.Map;

public interface ConfigProperties {
	public Object get(String key);

	public boolean getBoolean(String key, boolean defaultValue);

	public int getInteger(String key, int defaultValue);

	public long getLong(String key, long defaultValue);

	public String getString(String key, String defaultValue);

	public String getString(String key);

	public List<?> getArray(String key);

	public ConfigProperties getConfig(String key);

	public Map<String, Object> asMap();
}
