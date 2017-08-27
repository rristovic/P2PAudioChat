package com.radojcic.util;

import com.radojcic.login.UserDetails;

public abstract class Messages {

	public static final String welcomeMsg = "Welcome to Sukisa Instant Messeneger!";

	public static final String CON_END_REQ = "endconnection::";
	public static final String MAINFRAME_END_REQ = "mainframedisconnect::";
	public static final String GET_CLIENTS_REQ = "getclients::";

	public static String loginReqMsg(UserDetails ud) {
		return String.format("login::%s", ud.toJSON());
	}

	public static String selectClientReqMsg(String clientName) {
		return String.format("client::%s", clientName);
	}

}
