package com.radojcic.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import com.radojcic.networking.error.ConnectionErrorException;

public class SimpleMessageSender implements IMessageSender {

	private Socket communicationSocket;
	private BufferedReader console;
	private PrintStream outputStream;
	private BufferedReader inputSteam;
	
	public SimpleMessageSender(String adress, int port) {
		try {
			communicationSocket = new Socket(adress, port);
			console = new BufferedReader(new InputStreamReader(System.in));
			outputStream = new PrintStream(communicationSocket.getOutputStream());
			inputSteam = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SimpleMessageSender(PrintStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public BufferedReader getInputStream() {
		return this.inputSteam;
	}

	@Override
	public void sendMessage(String message) throws ConnectionErrorException{
		if (outputStream != null)
			outputStream.println(message);
		else
			throw new ConnectionErrorException("Client has gone offline.");
	}

	@Override
	public void sendData(Object object) {
		// TODO Auto-generated method stub

	}

}
