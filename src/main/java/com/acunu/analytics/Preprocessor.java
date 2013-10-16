package com.acunu.analytics;

import java.io.IOException;

public abstract class Preprocessor implements EventReceiver {
	private final String name;

	public Preprocessor(String name, Context context) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * This is where the work happens.
	 */
	public abstract void submitEvent(Event event) throws IOException;

	@Override
	public void flush() throws IOException {
	}
}
