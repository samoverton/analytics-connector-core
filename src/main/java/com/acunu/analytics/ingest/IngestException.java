package com.acunu.analytics.ingest;

/** 
 * A problem has arisen pulling data from a remote source.
 */
public class IngestException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public IngestException() {
		super();
	}

	public IngestException(String message) {
		super(message);
	}

	public IngestException(Throwable cause) {
		super(cause);
	}

	public IngestException(String message, Throwable cause) {
		super(message, cause);
	}
}
