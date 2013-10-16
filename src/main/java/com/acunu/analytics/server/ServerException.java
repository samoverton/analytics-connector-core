package com.acunu.analytics.server;

public class ServerException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ServerException(String msg) {
		super(msg);
	}

	public ServerException(String msg, Throwable exn) {
		super(msg, exn);
	}

	public ServerException(Exception exn) {
		super(exn);
	}
}
