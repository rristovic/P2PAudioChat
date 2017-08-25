package com.radojcic;

import com.radojcic.server.ChatServer;

public class SukiServerApp {

	public static void main(String[] args) {
		ChatServer cs = new ChatServer();
		cs.start(args);
	}
}
