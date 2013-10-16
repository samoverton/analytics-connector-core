package com.acunu.analytics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single event reaching the analytics node. Event data is
 * structured as a map of string keys to object values. The map should
 * correspond to the fields of the receiving analytics table or the parameters
 * of the receiving preprocessor.
 * 
 * In the case of preprocessors, it is possible for events to have a nested
 * structure, with string / object maps as values.
 * 
 * @author rallison
 * 
 */
public class Event implements Map<String, Object> {

	private final Map<String, Object> data;

	public Event() {
		this(new HashMap<String, Object>());
	}

	public Event(Map<String, Object> data) {
		this.data = data;
	}

	public static Event fromArray(Object[] obj) {
		Event event = new Event();
		event.put("_event_array", obj);
		return event;
	}

	public static Event fromString(String str) {
		Event event = new Event();
		event.put("_event_string", str);
		return event;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public int size() {
		return data.size();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	public Object get(Object key) {
		return data.get(key);
	}

	public Object put(String key, Object value) {
		return data.put(key, value);
	}

	public Object remove(Object key) {
		return data.remove(key);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		data.putAll(m);
	}

	public void clear() {
		data.clear();
	}

	public Set<String> keySet() {
		return data.keySet();
	}

	public Collection<Object> values() {
		return data.values();
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return data.entrySet();
	}

	/** 
	 * Deep (i.e. slow) comparison of two events by contents of map.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof Event))
			return false;

		if (data.size() != ((Event) other).data.size())
			return false;
		for (String key : data.keySet()) {
			Object value1 = data.get(key);
			Object value2 = ((Event)other).data.get(key);
			if (value1 == null)
				if (value2 != null)
					return false;
				else
					continue;
			else
				if (value2 == null)
					return false;
				else if (!value1.equals(value2))
					return false;
		}
		return true;
	}

	public int hashCode() {
		return data.hashCode();
	}

}
