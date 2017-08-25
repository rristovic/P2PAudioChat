package com.radojcic.data;

import java.net.InetAddress;

import com.radojcic.server.ClientThread;

public class Client {
	
	private final long id;

	private String firstName, lastName;
	private final String userName;
	private final InetAddress chatIpAdress;
	private final int chatPort;
	
	private boolean available = true;
	
	protected Client(long id, String userName, InetAddress ipAdress, int port) {
		this.id = id;
		this.userName = userName;
		this.chatIpAdress = ipAdress;
		this.chatPort = port;
	}
	
	public boolean isAvailable() {
		return available;
	}
	
	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserName() {
		return userName;
	}

	public InetAddress getIpAdress() {
		return chatIpAdress;
	}	

	public int getPort() {
		return chatPort;
	}

	public long getId() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Client))
			return false;
		Client compareWith = (Client) obj;
		return this.id == compareWith.id;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	public String toJSON() {
		StringBuilder sb = new StringBuilder("{");
		
		sb.append(String.format("\"username\":\"%s\",", this.userName));
		sb.append(String.format("\"address\":\"%s\",", this.chatIpAdress.getHostAddress()));
		sb.append(String.format("\"port\":%d", this.chatPort));
		
		sb.append("}");
		return sb.toString();
	}	
	
	
}
