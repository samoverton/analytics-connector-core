package com.acunu.analytics.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NestedConfig implements ConfigProperties {

	/**
	 * These apply from left to right, so you'd do nest(global, local, mine, todays).
	 * 
	 * config[i] over-writes configs[j] for j < i.
	 */
	public static ConfigProperties nest(ConfigProperties... configs) {
		assert configs != null;
		assert configs.length > 0;
		
		// can easily get ArrayStoreException when assigning a config
		// in nest(int, Config...): the array passed in here might
		// not actually be an array of configs!
		ConfigProperties[] actualConfigs = new ConfigProperties[configs.length];
		for(int i = 0; i < configs.length; i++ ) {
			actualConfigs[i] = configs[i];
		}
		return nest(0, actualConfigs);
	}
	
	private static ConfigProperties nest(int index, ConfigProperties[] configs) {
		assert (index < configs.length);
		if (index == configs.length-1)
			return configs[index];

		// at least 2 left...
		configs[index+1] = new NestedConfig(configs[index], configs[index+1]);

		// tail recursive, baby!
		return nest(index+1, configs);
	}

	private ConfigProperties global;
	private ConfigProperties local;

	public NestedConfig(ConfigProperties global, ConfigProperties local) {
		assert (global != null);
		assert (local != null);
		this.global = global;
		this.local = local;
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		return local.getBoolean(key, global.getBoolean(key, defaultValue));
	}

	@Override
	public int getInteger(String key, int defaultValue) {
		return local.getInteger(key, global.getInteger(key, defaultValue));
	}

	@Override
	public long getLong(String key, long defaultValue) {
		return local.getLong(key, global.getLong(key, defaultValue));
	}

	@Override
	public String getString(String key, String defaultValue) {
		return local.getString(key, global.getString(key, defaultValue));
	}

	@Override
	public List<?> getArray(String key) {
		List<?> ret = local.getArray(key);
		if (ret == null)
			return global.getArray(key);
		return ret;
	}

	@Override
	public ConfigProperties getConfig(String key) {
		ConfigProperties ret = local.getConfig(key);
		if (ret == null)
			return global.getConfig(key);
		return ret;
	}

	@Override
	public Map<String, Object> asMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(global.asMap());
		map.putAll(local.asMap());
		return map;
	}

	@Override
	public String getString(String key) {
		String localStr = local.getString(key);
		return (localStr == null) ? global.getString(key) : localStr;
	}

	@Override
	public Object get(String key) {
		Object localObj = local.get(key);
		return (localObj == null) ? global.get(key) : localObj;
	}
}
