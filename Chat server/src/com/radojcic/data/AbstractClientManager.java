package com.radojcic.data;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractClientManager implements IClientManager {

	private AtomicInteger counter = new AtomicInteger(0);
	private Map<String, Client> clients;


	public AbstractClientManager() {
		this.clients = new ConcurrentHashMap<String, Client>();
	}

	@Override
	public String[] getAvailableClients(Client forClient) {
		Set<String> clients = this.clients.keySet();
		List<String> availableClients = new ArrayList<>(clients.size());

		for (String client : clients) {
			if (!client.equals(forClient.getUserName()))
				availableClients.add(client);
		}

		return availableClients.toArray(new String[availableClients.size()]);
	}

	@Override
	public Client createNewClient(InetAddress adress, int port, String username) {
		Client c = new Client(counter.incrementAndGet(), username, adress, port);
		clients.put(c.getUserName(), c);
		System.out.println(String.format("Client (%s) has been added to active client list.", username));

		return c;
	}

	@Override
	public boolean isAvailableUserName(String username) {
		for (Client client : this.clients.values()) {
			if (client.getUserName().equals(username))
				return false;
		}

		return true;
	}

	@Override
	public Client findClientByUsername(String username) {
		return this.clients.get(username);
	}

	protected void removeClient(String username) {
		this.clients.remove(username);
		System.out.println(String.format("Client (%s) has been disconected and removed from db.", username));
	}

	protected void removeClient(Client client) {
		this.removeClient(client.getUserName());
	}
}
