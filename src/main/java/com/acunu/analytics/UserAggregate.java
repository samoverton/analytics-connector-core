package com.acunu.analytics;

import java.util.List;

import com.acunu.analytics.model.Field;
import com.acunu.util.Serialiser;

public abstract class UserAggregate<X extends Comparable<X>, CounterType> {
	protected final List<Field> fields;

	/**
	 * Override this constructor. The fields describe the table on which the
	 * accumulation occurs.
	 */
	public UserAggregate(List<Field> fields, String[] args) {
		this.fields = fields;
	}

	public int lookupField(String fieldName) {
		if (fieldName != null) {
			for (int i = 0; i < fields.size(); i++) {
				if (fieldName.equalsIgnoreCase(fields.get(i).getName())) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Produce a counter from an event.
	 */
	public abstract CounterType counterFromEvent(Comparable[] event);

	/**
	 * Immutably accumulate two counters together to give a new counter.
	 */
	public abstract CounterType accumulate(CounterType older, CounterType newer);

	/**
	 * Extract the final value from an aggregated counter.
	 */
	public abstract X evalCounter(CounterType counter);

	/**
	 * How to serialise the counters.
	 */
	public abstract Serialiser<CounterType> getSerialiser();

	/**
	 * Report whether this aggregate function is 'idempotent' -- i.e. repeatedly
	 * merging with an identical object has no effect.
	 */
	public boolean isIdempotent() {
		return false;
	}

	/**
	 * The value of nothing! So for SUM and COUNT it's 0, for double stuff it's
	 * 0.0, but for average it's null. The zero element is the one which, when
	 * aggregated to x, gives x. We display it when there are no results for a
	 * query.
	 */
	public CounterType zero() {
		return null;
	}

	/**
	 * The class of returned values -- Long.class, String.class or Double.class
	 * usually.
	 */
	public abstract Class<X> rawValueType();
}
