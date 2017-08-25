package com.radojcic.error;

public class ClientDisconectedException extends RuntimeException{

	private static final long serialVersionUID = -5398486296097192605L;
	
	public ClientDisconectedException(String message) {
		super(message);
	}

}
