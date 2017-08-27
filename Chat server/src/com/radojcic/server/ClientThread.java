package com.radojcic.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.radojcic.data.AbstractClientManager;
import com.radojcic.data.Client;
import com.radojcic.data.IClientManager;
import com.radojcic.error.ClientDisconectedException;
import com.radojcic.util.Messages;

public class ClientThread extends Thread {

	interface ClientListener {

		void onClientOffline(ClientThread ct);

		void onClientUnavailable(ClientThread ct);

		void onClientAvailable(ClientThread ct);

		void onNewClient(Client client);
	}

	BufferedReader clientInputStream = null;
	PrintStream clientOutputStream = null;
	Socket communicationSocket = null;
	PrintStream reportStream = null;
	// UDP socket for recipients
	DatagramSocket datagramSocket = null;
	String mLine = "";

	// Interface for getting available clients info
	IClientManager clientManager;
	IClientMessagingService senderService;
	ClientListener clientListener;
	Client currentClient;
	private int clientChatPortNumb;
	private int clientUdpPortNumb;

	public ClientThread(Socket socket, IClientManager clientManager, IClientMessagingService senderService,
			ClientListener listener) {
		this.clientListener = listener;
		this.clientManager = clientManager;
		this.senderService = senderService;
		this.communicationSocket = socket;

		System.out.println("Client connected, port: " + socket.getPort());

		String fileName = "client_" + this.getClass().getName()
				+ new SimpleDateFormat("yyyyMMdd:HHmmss").format(Calendar.getInstance().getTime());
		File reportFile = new File(fileName + ".txt");
		int count = 0;
		while (reportFile.exists()) {
			fileName = fileName + "(" + (++count) + ")";
			reportFile = new File(fileName);
		}

		try {
			this.reportStream = new PrintStream(new FileOutputStream(reportFile), true);
		} catch (FileNotFoundException e) {
			this.reportStream = System.out;
		}
	}

	@Override
	public void run() {
		try {
			// Stream binding
			clientInputStream = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			clientOutputStream = new PrintStream(communicationSocket.getOutputStream());
			readPortNum();
			System.out
					.println(String.format("Client UDP port:%d, Chat port:%d", clientUdpPortNumb, clientChatPortNumb));

			sendMsgToClient("Dobrodosli na SukiChatting. Molimo vas unesite vase korisnicke podatke.");
			// Populate clients info
			String[] userInfo = readUserInfo();
			// Inform login success
			sendMsgToClient("loginsuccess::Uspesno ste se povezali na server. Dobrodosli!");
			// Create new client object
			this.currentClient = clientManager.createNewClient(this.communicationSocket.getInetAddress(),
					clientChatPortNumb, userInfo[0]);
			this.currentClient.setFirstName(userInfo[1]);
			this.currentClient.setLastName(userInfo[2]);
			this.clientListener.onNewClient(this.currentClient);

			Thread.currentThread().setName("ClientThread-" + this.currentClient.getUserName());

			while (true) {
				sendRecipientClients();
				String msg;

				while ((msg = readMsgFromClient()) != null) {
					msg = msg.trim();
					if(msg.startsWith(Messages.MAINFRAME_END_REQ)) {
						throw new ClientDisconectedException("Client went offline.");
					}
					if (msg.startsWith(Messages.GET_CLIENTS_REQ)) {
						sendRecipientClients();
						continue;
					}
					String client = msg.substring(msg.indexOf("::") + 2);
					Client clientToTalkWith = this.clientManager.findClientByUsername(client);
					if (clientToTalkWith == null || clientToTalkWith.equals(this.currentClient)
							|| !clientToTalkWith.isAvailable()) {
						sendMsgToClient("No user found with that name, pls try again.");
						continue;
					}
					sendClientInfo(clientToTalkWith, clientUdpPortNumb);
				}
			}

		} catch (ClientDisconectedException e) {
//			 e.printStackTrace();
		} catch (SocketException e) {
			 e.printStackTrace();
		} catch (IOException e) {
//			 e.printStackTrace();
		} catch (RuntimeException e) {
//			 e.printStackTrace();
		} finally {
			try {
				if (this.communicationSocket != null)
					this.communicationSocket.close();
			} catch (IOException e) {
			}
			if (this.clientOutputStream != null)
				this.clientOutputStream.close();

			if (this.clientInputStream != null)
				try {
					this.clientInputStream.close();
				} catch (IOException e) {
				}
			this.communicationSocket = null;
			this.clientInputStream = null;
			this.clientOutputStream = null;
			this.clientListener.onClientOffline(this);
		}
	}

	private void readPortNum() throws NumberFormatException, IOException {
		// W8 for ports to be sent
		String msg = clientInputStream.readLine();
		if (msg.startsWith("ports::"))
			msg = msg.substring(msg.indexOf("::") + 2);
		JsonElement jelement = new JsonParser().parse(msg);
		JsonObject jobject = jelement.getAsJsonObject();
		this.clientChatPortNumb = jobject.get("chatPort").getAsInt();
		this.clientUdpPortNumb = jobject.get("udpPort").getAsInt();
	}

	/**
	 * Helper method for reading clients name and user name.
	 *
	 * @throws IOException
	 * @throws RuntimeException
	 */
	private String[] readUserInfo() throws IOException, RuntimeException {
		String userName = null, firstName = null, lastName = null;

		// Localhost firewall problem hotfix
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String line = clientInputStream.readLine().trim();
		boolean success = false;
		while (!success) {
			success = true;

			JsonElement jelement = new JsonParser().parse(line.substring(line.indexOf("::") + 2));
			JsonObject jobject = jelement.getAsJsonObject();

			userName = jobject.get("userName").getAsString();
			firstName = jobject.get("firstName").getAsString();
			lastName = jobject.get("lastName").getAsString();

			if (!this.isValidName(userName)) {
				sendMsgToClient("Uneli ste nevalidno korisnicko ime. Probajte ponovo!");
				success = false;
			} else if (!this.clientManager.isAvailableUserName(userName)) {
				sendMsgToClient("Zao nam je, korisnicko ime je vec zauzeto. Probajte ponovo!");
				success = false;
			}
			if (!this.isValidName(firstName)) {
				sendMsgToClient("Uneli ste nevalidno ime. Probajte ponovo!");
				success = false;
			}
			if (!this.isValidName(lastName)) {
				sendMsgToClient("Uneli ste nevalidno prezime. Probajte ponovo!");
				success = false;
			}

			if (!success) {
				line = clientInputStream.readLine().trim();
			}
		}

		return new String[] { userName, firstName, lastName };
	}

	private String readClientMsg() throws IOException, RuntimeException {
		clientOutputStream.println("Unesite novu poruku: \n");
		mLine = clientInputStream.readLine();
		while (mLine.length() == 0 || checkExit(mLine)) {
			clientOutputStream.println("Uneli ste praznu poruku, pokusajte ponovo:");
			mLine = clientInputStream.readLine();
		}
		System.out.println(mLine);
		return mLine;
	}

	/**
	 * Send a list of recipients to the message sender client
	 *
	 * @throws IOException
	 */
	protected void sendRecipientClients() throws IOException {
		datagramSocket = new DatagramSocket();
		String[] clients = this.clientManager.getAvailableClients(this.currentClient);

		if (clients.length > 0) {
			sendMsgToClient("Izaberi korisnika sa kojim zelis da pricas.");
		} else {
			sendMsgToClient("Nazalost, trenutno nema online korisnika.");
		}
		StringBuilder sb = new StringBuilder("clients::");
		for (int i = 0; i < clients.length; i++) {
			sb.append(clients[i]);
			if (!(i + 1 == clients.length)) {
				sb.append(",");
			}
		}

		byte[] clientNames;

		// Prepare bytes
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(sb.toString());
			out.flush();
			clientNames = bos.toByteArray();
		} finally {
			try {
				bos.close();
				out.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		DatagramPacket packet = new DatagramPacket(clientNames, clientNames.length,
				communicationSocket.getInetAddress(), this.clientUdpPortNumb);
		datagramSocket.send(packet);
		datagramSocket.close();
	}

	/**
	 * Send client info so they can chat with each other.
	 *
	 * @param clientInfo
	 */
	private void sendClientInfo(Client client, int clientPortNum) throws IOException {
		datagramSocket = new DatagramSocket();

		byte[] clientInfo;

		// Prepare bytes
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject("client::{\"client\":" + client.toJSON() + ",\"username\":\""
					+ this.currentClient.getUserName() + "\"}");
			out.flush();
			clientInfo = bos.toByteArray();
		} finally {
			try {
				bos.close();
				out.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		DatagramPacket packet = new DatagramPacket(clientInfo, clientInfo.length, communicationSocket.getInetAddress(),
				clientPortNum);
		datagramSocket.send(packet);
		datagramSocket.close();
	}

	/**
	 * Notify client about closing connection. End thread running.
	 */
	private void closeConnection() {
		this.sendMsgToClient("end::");
		throw new ClientDisconectedException("Client connected to another peer. Closing connection!");
	}

	/*
	 * ============================ Helper methods
	 * ======================================
	 */
	/**
	 * Method for notifying client that sending message has failed.
	 *
	 * @param msg
	 *            - message that should have been sent.
	 * @param client
	 *            - client that should have got the message.
	 */
	protected void notifySendingFailed(String msg, String client) {
		// Failed to find recipient and send, notify sender
		this.sendMsgToClient("Neuspelo slanje. Korisnik " + client + " nije vise na mrezi.");
		reportStream.println(String.format("%s - Neuspelo slanje. Korisnik %s nije na mrezi;",
				new SimpleDateFormat("HH:mm:ss").format(new Date()), client));
	}

	/**
	 * Method for reading message from client.
	 *
	 * @throws IOException
	 */
	protected String readMsgFromClient() throws IOException {
		mLine = clientInputStream.readLine();
		return mLine.trim();
	}

	/**
	 * Helper method for sending new message to client.
	 *
	 * @param message
	 *            - message to send.
	 */
	protected void sendMsgToClient(String message) {
		clientOutputStream.println(message);
		clientOutputStream.flush();
	}

	/**
	 * Helper method for checking if name is valid.
	 *
	 * @param name
	 *            - name to check.
	 * @return true if name is valid.
	 */
	private boolean isValidName(String name) {
		name = name.trim();
		return name != null && name.length() > 0;
	}

	/**
	 * Method for checking if client wants to leave. If param contains 'exit',
	 * it will throw exception;
	 *
	 * @param string
	 *            string to check
	 * @throws RuntimeException
	 */
	private boolean checkExit(String string) throws RuntimeException {
		if (string.toLowerCase().equals("exit")) {
			clientOutputStream.println("end:Dovidjenja! Pritisnite bilo koje dugme za izlaz..");
			throw new RuntimeException("Connection reset");
		}
		return false;
	}

	/**
	 * Helper method for logging reports to report stream.
	 *
	 * @param logMsg
	 *            - message to log.
	 */
	private void log(String logMsg) {
		reportStream.println(logMsg + ";");
	}

	public Client getCurClient() {
		return this.currentClient;
	}
}
