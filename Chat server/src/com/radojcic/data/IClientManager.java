package com.radojcic.data;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

public interface IClientManager {
	/**
	 * Retrieve all currently online clients available for chatting for provided user.
	 * @param client - list of available user for this client.
	 * @return list of available client names.
	 */
	String[] getAvailableClients(Client client);
	
	/**
	 * Creates new client with provided params.
	 * @param adress client's ip adress.
	 * @param port client's port number.
	 * @return newly created client.
	 */
	Client createNewClient(InetAddress adress, int port, String userName);

	/**
	 * Indicates if desirable username is not already in use.
	 * @param username - username to check for.
	 * @return true is username is available for use.
	 */
	boolean isAvailableUserName(String username);
	
	/**
	 * Return Client object based on username.
	 * @param userName - username of client.
	 * @return Client object from db.
	 */
	Client findClientByUsername(String userName);
}