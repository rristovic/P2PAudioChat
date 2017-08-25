package com.radojcic.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.radojcic.networking.IClientListener.MessageReceiverListener;
import com.radojcic.util.Messages;

public class P2PServer extends Thread {

	private ServerSocket server;
	private int port = 8877;
	private InetAddress address;
	private IClientListener.MessageReceiverListener msgListener;
	private IClientListener.NewClientListener clientListener;

	private Socket clientSocket;

	private String chatBuddy;
	private BufferedReader clientInputStream;
	private PrintStream clientOutputStream;

	public P2PServer(IClientListener.MessageReceiverListener msgListener,
			IClientListener.NewClientListener clientListener) {
		this.msgListener = msgListener;
		this.clientListener = clientListener;
		try {
			server = new ServerSocket(0);
			this.port = server.getLocalPort();
			this.address = server.getInetAddress();
		} catch (IOException ex) {
			Logger.getLogger(P2PServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void run() {

		if (this.server == null) {
			System.out.println("Server p2p failed to start. Aborting.");
			return;
		}

		while (true) {
			try {
				clientSocket = server.accept();
				clientInputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				clientOutputStream = new PrintStream(clientSocket.getOutputStream());
				String msg = "";

				while ((msg = clientInputStream.readLine()) != null) {
					if (msg.startsWith("newchat::")) {
						this.chatBuddy = msg.substring(msg.indexOf("::") + 2);
						SimpleMessageSender sender = new SimpleMessageSender(clientOutputStream);
						MessageReceiverListener listener = clientListener.onNewChat(sender, "", this.chatBuddy);
						this.msgListener = listener;
					} else {
						msgListener.onNewMessage(msg);
					}
					System.out.println(msg);
				}
			} catch (IOException e) {
				System.err.println("p2p server socket closed: " + e.getLocalizedMessage());
			} finally {
				endChatting(false);
			}
		}
	}

	/**
	 * Helper method for ending current chat. Notifies other client that chat
	 * has ended if required.
	 * 
	 * @param notify
	 *            - true if server should notify other user about chat ending.
	 * @throws IOException
	 */
	public void endChatting(boolean notify) {
		System.out.println("Chat has ended.");
		if (notify) {
			System.out.println("Notifying other client about ending.");
			try {
				clientOutputStream.println(Messages.CON_END_REQ);
			} catch (Exception e) {
			}
		}
		try {
			if (clientSocket != null) {
				clientSocket.close();
				clientSocket = null;
			}
			if (clientOutputStream != null) {
				clientOutputStream.close();
				clientOutputStream = null;
			}
			if (clientInputStream != null) {
				clientInputStream.close();
				clientInputStream = null;
			}
		} catch (IOException e) {
		}
		System.out.println("Clsoing down p2p server socket.");
	}

	public void setMsgListener(IClientListener.MessageReceiverListener msgListener) {
		this.msgListener = msgListener;
	}

	public void setClientListener(IClientListener.NewClientListener clientListener) {
		this.clientListener = clientListener;
	}

	/**
	 * Returns the port on which this socket is listening for messages.
	 * 
	 * @return port number in use.
	 */
	public int getPortNum() {
		return this.port;
	}

	/**
	 * Returns the address on which this socket is listening for messages.
	 * 
	 * @return address in use.
	 */
	public InetAddress getLocalAdress() {
		return this.address;
	}
}
