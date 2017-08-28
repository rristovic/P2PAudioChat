package com.radojcic.networking.error;

public class ConnectionErrorException extends RuntimeException {

	public ConnectionErrorException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -5937077536626186853L;
}
