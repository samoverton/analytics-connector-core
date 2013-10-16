package com.acunu.analytics.ingest;

/**
 * An Exception raised during decoding.
 * 
 * @author tmoreton
 *
 */
public class DecodeException extends Exception {

	private static final long serialVersionUID = 1L;

	public DecodeException(Throwable throwable) {
		super(throwable);
	}

	public DecodeException(String string, Exception e) {
		super(string, e);
	}

	public DecodeException(String string) {
		super(string);
	}

}
