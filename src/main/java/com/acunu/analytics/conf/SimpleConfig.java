package com.acunu.analytics.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.acunu.util.ConfigPropertiesUtils;

/**
 * An implementation of 'config' loaded from a file.
 * 
 * @author abyde
 */
public class SimpleConfig implements ConfigProperties {
	protected Map<String, Object> config;

	public SimpleConfig() {
		config = new HashMap<String, Object>();
	}

	public SimpleConfig(Map<String, ?> data) {
		// generics judo.
		config = (Map<String, Object>) ((Map)data);
	}

	public void putBoolean(String key, boolean value) {
		put(key, Boolean.valueOf(value));
	}

	public void put(String key, Object value) {
		config.put(key, value);
	}

	public void putAll(Map<String, Object> map) {
		config.putAll(map);
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		if (config.containsKey(key))
			return asBoolean(config.get(key));
		return defaultValue;
	}

	@Override
	public int getInteger(String key, int defaultValue) {
		if (config.containsKey(key))
			return asInteger(config.get(key));
		return defaultValue;
	}

	@Override
	public long getLong(String key, long defaultValue) {
		if (config.containsKey(key))
			return asLong(config.get(key));
		return defaultValue;
	}

	@Override
	public String getString(String key, String defaultValue) {
		if (config.containsKey(key))
			return asString(config.get(key));
		return defaultValue;
	}

	@Override
	public String getString(String key) {
		return asString(config.get(key));
	}

	/**
	 * Top-level properties of the map that this YAML represents.
	 */
	public Set<Entry<String, Object>> entrySet() {
		return config.entrySet();
	}

	public static String asString(Object obj) {
		if (obj == null)
			return null;
		else if (obj instanceof String)
			return (String) obj;
		else
			return obj.toString();
	}

	public static Boolean asBoolean(Object obj) {
		if (obj == null)
			return false;
		else if (obj instanceof Boolean)
			return (Boolean) obj;
		else if (obj instanceof String)
			return Boolean.valueOf((String) obj);
		else
			return asBoolean(asString(obj));
	}

	public static Integer asInteger(Object obj) {
		if (obj == null)
			return null;
		else if (obj instanceof Integer)
			return (Integer) obj;
		else if (obj instanceof Number)
			return ((Number) obj).intValue();
		else if (obj instanceof String)
			return Integer.valueOf((String) obj);
		else
			return asInteger(asString(obj));
	}

	public static Long asLong(Object obj) {
		if (obj == null)
			return null;
		else if (obj instanceof Long)
			return (Long) obj;
		else if (obj instanceof Number)
			return ((Number) obj).longValue();
		else if (obj instanceof String)
			return Long.valueOf((String) obj);
		else
			return asLong(asString(obj));
	}

	public static Double asDouble(Object obj) {
		if (obj == null)
			return null;
		else if (obj instanceof Double)
			return (Double) obj;
		else if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		else if (obj instanceof String)
			return Double.valueOf((String) obj);
		else
			return asDouble(asString(obj));
	}

	@Override
	public List<?> getArray(String key) {
		Object val = config.get(key);
		if (val == null || !(val instanceof List))
			return new ArrayList();

		return (List<?>) val;
	}

	/**
	 * @return null if there is no such key.
	 */
	@Override
	public ConfigProperties getConfig(String key) {
		return ConfigPropertiesUtils.asConfig(config.get(key));
	}

	@Override
	public Map<String, Object> asMap() {
		return config;
	}

	@Override
	public Object get(String key) {
		return config.get(key);
	}

	@Override
	public String toString() {
		return config.toString();
	}

	public static ConfigProperties fromMap(Map<String, ?> map) {
		return new SimpleConfig(map);
	}
}
