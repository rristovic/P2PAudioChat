package com.radojcic.networking;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import com.google.gson.JsonParser;
import com.radojcic.gui.MainConsole;
import com.radojcic.login.LoginWindow;
import com.radojcic.login.LoginListener;
import com.radojcic.login.UserDetails;
import com.radojcic.networking.IClientListener.MessageReceiverListener;
import com.radojcic.networking.error.ConnectionErrorException;
import com.radojcic.util.Messages;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChatClient implements IMessageSender, IClientListener.NewClientListener,
		IClientListener.ClientChatEndListener, LoginListener {

	// GUI
	MainConsole mainConsole;
	LoginWindow loginConsole;

	// Message listener to listen on server messages, binds to main console
	IClientListener.NewClientListener clientListener;
	IClientListener.MessageReceiverListener msgListener;

	// Communication socket/streams from/to server
	private Socket communicationSocket = null;
	private PrintStream outputStream = null;
	private BufferedReader inputSteam = null;
	private BufferedReader console = null;
	final static int mainFramePort = 1080;
	static boolean end = false;

	// UDP Port used for sending and receiving object data from/to server
	private DatagramSocket datagramSocket;
	private volatile Integer datagramPortNum = -1;

	// Lock to wait for all ports to be populated
	private Object portLock;

	// Message receiver thread used to receive messages from chat buddy
	private P2PServer chatServer;
	// Indicates if this chat client's p2p server is currently beign used
	private boolean isUsingServerChat = false;
	// Message sender used to send messages to chat buddy
	private IMessageSender chatMsgSender;

	// First chat port num must be sent, then UDP
	private volatile boolean isPortSent = false;

	private UserDetails userDetails;

	public ChatClient() {
		super();
		loginConsole = new LoginWindow(this);
		loginConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginConsole.setLocationRelativeTo(null);
		this.msgListener = loginConsole;
	};

	public ChatClient(IClientListener.MessageReceiverListener msgListener,
			IClientListener.NewClientListener clientListener) {
		super();
		this.msgListener = msgListener;
		this.clientListener = clientListener;
	};

	public void startClient() {
		try {
			connectToServer("localhost", mainFramePort);
			startP2PServer();
			openUDP();
			sendPorts();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Start connection to server with provided address and port.
	 * 
	 * @param adress
	 *            - server address.
	 * @param port
	 *            - server port.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void connectToServer(String adress, int port) throws UnknownHostException, IOException {
		communicationSocket = new Socket(adress, port);
		console = new BufferedReader(new InputStreamReader(System.in));
		outputStream = new PrintStream(communicationSocket.getOutputStream());
		inputSteam = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
		System.out.println("Connection to server open on port:" + communicationSocket.getPort());

		// TCP Message listener
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("TCP Thread");
				String msg;
				try {
					while ((msg = inputSteam.readLine()) != null) {
						System.out.println(msg);
						ChatClient.this.onNewMessage(msg);
					}
				} catch (IOException e) {
					System.err.println("Connection to server closed. " + e.getLocalizedMessage());
				} finally {
					try {
						if (communicationSocket != null) {
							communicationSocket.close();
							communicationSocket = null;
						}
						if (outputStream != null) {
							outputStream.close();
							outputStream = null;
						}
						if (inputSteam != null) {
							inputSteam.close();
							inputSteam = null;
						}
					} catch (IOException e1) {
					}
					communicationSocket = null;
					outputStream = null;
					inputSteam = null;
				}
			}
		}).start();
	}

	/**
	 * Starts p2p server, and send p2p port number to main frame server.
	 */
	private void startP2PServer() {
		// Chat thread
		chatServer = new P2PServer(this.msgListener, this.clientListener);
		chatServer.start();
		// Send chat port number
		// sendMessage(Integer.toString(chatServer.getPortNum()));
		// this.isPortSent = true;

	}

	/**
	 * Start UDP socket listener and UDP port to mainframe server.
	 */
	private void openUDP() {
		if (datagramSocket == null)
			// UDP Message listener
			new Thread(new Runnable() {
				@Override
				public void run() {
					Thread.currentThread().setName("UDP Thread");
					try {
						datagramSocket = new DatagramSocket();
						datagramPortNum = datagramSocket.getLocalPort();
						// Send UDP port number
						// while (!isPortSent) {
						// try {
						// Thread.sleep(100);
						// } catch (InterruptedException e) {
						// }
						// }
						// sendMessage(datagramPortNum.toString());
						while (true)
							receive();
					} catch (SocketException e) {
						e.printStackTrace();
					}
				}
			}).start();
	}

	/**
	 * Method for receiving data from UDP socket.
	 */
	private void receive() {
		List<String> list = new ArrayList<>();
		try {

			byte[] serverData = new byte[1024];
			DatagramPacket serverPacket = new DatagramPacket(serverData, serverData.length);
			datagramSocket.receive(serverPacket);

			ByteArrayInputStream bis = new ByteArrayInputStream(serverPacket.getData());
			ObjectInput in = null;
			try {
				in = new ObjectInputStream(bis);
				String readObject = (String) in.readObject();
				System.out.println(readObject);

				if (readObject.startsWith("clients::"))
					parseClients(readObject);
				else if (readObject.startsWith("client::"))
					parseClient(readObject);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method for parsing and showing client names received from UDP
	 * connection.
	 * 
	 * @param clients
	 *            - clients names string to parse.
	 */
	private void parseClients(String clients) {
		String[] clientsArray = clients.substring(clients.indexOf("::") + 2).split(",");
		List<String> clientNames = new LinkedList<String>(Arrays.asList(clientsArray));

		StringBuilder string = new StringBuilder("");
		Iterator<String> i = clientNames.iterator();
		int counter = 1;
		while (i.hasNext()) {
			String client = i.next();
			if (client == null || client.length() == 0) {
				i.remove();
				continue;
			}
			string.append((counter++) + ". ");
			string.append(client);
			string.append("\n");
		}

		// this.onNewMessage(string.toString());
		while (this.mainConsole == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.mainConsole.setClients(clientNames);
		System.out.println(string.toString());
	}

	/**
	 * Helper method for parsing chat client. Notifies Message Listener that new
	 * chat user has been chosen with provided arguments.
	 * 
	 * @param client
	 *            - client object to parse.
	 */
	private void parseClient(String client) {
		JsonElement jelement = new JsonParser().parse(client.substring(client.indexOf("::") + 2));
		JsonObject jobject = jelement.getAsJsonObject();
		JsonObject clientJson = jobject.get("client").getAsJsonObject();
		try {
			try {
				this.chatServer.serverShutdown().join();
			} catch (InterruptedException e) {
			}

			serverDisconnect(true);
			connectToServer(clientJson.get("address").getAsString(), clientJson.get("port").getAsInt());
			this.sendMessage("newchat::" + this.userDetails.getUserName());
			this.msgListener = this.mainConsole.onChatUserChosen(this, clientJson.get("username").getAsString());
		} catch (IOException ex) {
			System.err.println("Failed to connecti to p2p server: " + ex.getLocalizedMessage());
			try {
				serverDisconnect(false);
				reconnectToMainFrame("localhost", this.mainFramePort);
				this.msgListener.onNewMessage("Busy chat client, pls try again later.");
			} catch (IOException e) {
				System.exit(-1);
			}
		}
	}

	/**
	 * Temporary message buffer for delayed messages arrived when switching from
	 * LoginForm to MainConsole.
	 */
	static class msgRec implements IClientListener.MessageReceiverListener {
		static msgRec instance = new msgRec();
		static final List<String> msgBuffer = new LinkedList();

		@Override
		public void onNewMessage(String message) {
			msgBuffer.add(message);
		}
	}

	@Override
	public void onLoginComplete(UserDetails userDetails) {
		this.msgListener = msgRec.instance;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainConsole = new MainConsole(ChatClient.this);
				mainConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainConsole.setLocationRelativeTo(null);
				mainConsole.setChatEndListener(ChatClient.this);

				// Set main message listeners
				ChatClient.this.msgListener = mainConsole;
				ChatClient.this.clientListener = mainConsole;
				// Route p2p server msg listener to main console
				ChatClient.this.chatServer.setMsgListener(mainConsole);
				ChatClient.this.chatServer.setClientListener(ChatClient.this);

				ChatClient.this.userDetails = userDetails;
				ChatClient.this.loginConsole.dispose();

				// Add delayed messages
				for (String s : msgRec.msgBuffer) {
					msgListener.onNewMessage(s);
				}
			}
		});
	}

	@Override
	public void onLogin(UserDetails userDetails) {
		sendMessage(Messages.loginReqMsg(userDetails));
	}

	@Override
	public void sendMessage(String message) throws ConnectionErrorException {
		if (outputStream != null) {
			outputStream.println(message);
			outputStream.flush();
		} else {
			if (message.equals(Messages.GET_CLIENTS_REQ)) {
				try {
					this.reconnectToMainFrame("localhost", this.mainFramePort);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			throw new ConnectionErrorException("Server not responding");
			
		}
	}

	/**
	 * Helper method to invoke message receive listener.
	 * 
	 * @param msg
	 *            - newly arrived message.
	 */
	private void onNewMessage(String msg) {
		if (this.msgListener != null) {
			this.msgListener.onNewMessage(msg);
		}
	}

	@Override
	public void onChatEnded() {
		// If this chat was hosted on our server, shut it down
		if (this.isUsingServerChat) {
			this.isUsingServerChat = false;
			// Try notifying other client
			this.chatServer.endChatting(false);
		} else {
			startP2PServer();
			// Else disconnect from other client p2p server, and connect to main
			// frame again.
			serverDisconnect(false);
		}

		try {
			reconnectToMainFrame("localhost", mainFramePort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Disconnects from the server.
	 * 
	 * @param mainframe
	 *            - true if should disconnect from main frame server.
	 */
	private void serverDisconnect(boolean mainframe) {
		if (mainframe) {
			sendMessage(Messages.MAINFRAME_END_REQ);
		}
		try {
			if (this.communicationSocket != null)
				this.communicationSocket.close();
			this.communicationSocket = null;
		} catch (IOException e) {
		}

		if (this.outputStream != null) {
			this.outputStream.close();
			this.outputStream = null;
		}
		try {
			if (this.inputSteam != null)
				this.inputSteam.close();
			this.inputSteam = null;
		} catch (IOException e) {
		}
	}

	private void reconnectToMainFrame(String adress, int port) throws UnknownHostException, IOException {
		this.msgListener = this.mainConsole;

		connectToServer("localhost", mainFramePort);
		// Send login details
		sendLoginDetails();
	}

	private void sendLoginDetails() {
		// Send ports
		sendPorts();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Send login user info
		sendMessage(Messages.loginReqMsg(this.userDetails));
	}

	private void sendPorts() {
		if (datagramPortNum == -1) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		sendMessage(
				String.format("ports::{\"chatPort\":%d, \"udpPort\":%d}", chatServer.getPortNum(), datagramPortNum));
	}

	@Override
	public void sendData(Object object) {
		throw new RuntimeException("Method not implemented.");
	}

	@Override
	public MessageReceiverListener onNewChat(IMessageSender msgSender, String message, String chatBuddyName) {
		// Intercept new chat request
		this.serverDisconnect(true);
		this.isUsingServerChat = true;
		return this.mainConsole.onNewChat(msgSender, message, chatBuddyName);
	}
}
