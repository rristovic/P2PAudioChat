package com.radojcic.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.radojcic.data.AbstractClientManager;
import com.radojcic.data.Client;

public class ChatServer extends AbstractClientManager implements IClientMessagingService, ClientThread.ClientListener {

	// static ServerThread clients[] = new ServerThread[10];
	private List<ClientThread> clients = new LinkedList<>();
	
	private final int MAX_CLIENTS = 10;

	public void start(String args[]) {
		// Initialize vars
		int portNum = 1080;

		Socket clientSocket = null;
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(portNum);
			while (true) {
				clientSocket = serverSocket.accept();

				if (clients.size() < MAX_CLIENTS) {
					ClientThread t = new ClientThread(clientSocket, this, this, this);
					t.start();

					clients.add(t);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClientOffline(ClientThread ct) {
		if (ct.currentClient != null)
			super.removeClient(ct.currentClient);
		clients.remove(ct);
		
		for (ClientThread c : clients) {
			if (c.currentClient != null)
				try {
					c.sendRecipientClients();
				} catch (Exception e) {
				}
		}
		
	}

	@Override
	public List<String> sendMsgToRecipients(ClientThread sender, List<String> recipients, String msg) {
		List<String> clientsReceived = new LinkedList<>();
		for (ClientThread receiver : this.clients) {
			for (String username : recipients) {
				if (receiver.currentClient.getUserName().trim().toLowerCase().equals(username.trim().toLowerCase())) {
					boolean success = sendMsgToClient(sender, receiver, msg);
					if (success) {
						clientsReceived.add(username);
					} else {
						sender.notifySendingFailed(msg, username);
					}
					break;
				}
			}
		}
		return clientsReceived;
	}

	/**
	 * Method for sending message to chosen client.
	 * 
	 * @param ct
	 *            - Client Thread object.
	 * @param message
	 *            - message to send.
	 */
	private boolean sendMsgToClient(ClientThread sender, ClientThread receiver, String message) {
		boolean added = false;
		try {
			receiver.sendMsgToClient("Poruka od " + sender.currentClient.getUserName() + ": " + message);
			added = true;
		} catch (Exception e) {

		}
		return added;
	}

	@Override
	public void onClientUnavailable(ClientThread ct) {
		super.findClientByUsername(ct.currentClient.getUserName()).setAvailable(false);
	}

	@Override
	public void onClientAvailable(ClientThread ct) {
		super.findClientByUsername(ct.currentClient.getUserName()).setAvailable(true);
	}

	@Override
	public void onNewClient(Client client) {
			for (ClientThread ct : clients) {
				if (ct.currentClient != null && !ct.currentClient.equals(client))
					try {
						ct.sendRecipientClients();
					} catch (IOException e) {
					}
			}
		
	}
}
