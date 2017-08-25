package com.radojcic.login;

import com.google.gson.Gson;

public class UserDetails {
	private String firstName, lastName, userName;
	
	public UserDetails(){};

	public UserDetails(String firstName, String lastName, String userName) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
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

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return String.format("%s %s (%s)", this.firstName, this.lastName, this.userName);
	}
	
	public String toJSON() {
		return new Gson().toJson(this);
	}
}
