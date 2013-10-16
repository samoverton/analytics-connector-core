package com.acunu.analytics.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * An implementation of 'config' loaded from a file.
 * 
 * @author abyde
 */
public class YamlConfig extends SimpleConfig {
	private static Logger logger = LoggerFactory.getLogger(YamlConfig.class);

	/**
	 * Read config at the specified location, optionally throwing an exception
	 * if it's not there.
	 */
	public static YamlConfig load(File file, boolean withExceptions) {
		if (withExceptions)
			return load(file);
		else {
			try {
				return load(file);
			} catch (Exception exn) {
				logger.error("Failed to load config: ", exn);
				return null;
			}
		}
	}

	/**
	 * Exception throwing config construction.
	 */
	private static YamlConfig load(File file) {
		if (file == null) {
			throw new IllegalArgumentException("Config file is null");
		}
		if (!file.exists()) {
			throw new IllegalArgumentException("Config file '" + file + "' does not exist");
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("Config file '" + file + "' cannot be read");
		}
		try {
			return new YamlConfig(file);
		} catch (IOException exn) {
			throw new IllegalArgumentException(exn);
		}
	}

	private File source;

	public YamlConfig(File source) throws IOException {
		this.source = source;
		reload();
	}

	public YamlConfig(Map<String, Object> data) {
		super(data);
	}

	public synchronized void reload() throws IOException {
		try {
			Yaml yaml = new Yaml();
			Map<String, Object> object = (Map<String, Object>) yaml.load(new FileInputStream(source));
			logger.info("Loaded config from {}", source);

			config = object;
		} catch (IOException exn) {
			throw exn;
		} catch (Exception exn) {
			logger.error("Configuration using path '{}' failed: {}", source, exn.getMessage());
			throw new RuntimeException(exn);
		}
	}
}
