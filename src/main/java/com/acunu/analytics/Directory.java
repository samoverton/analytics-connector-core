package com.acunu.analytics;

public interface Directory<X> {
	X lookup(String name);
}
