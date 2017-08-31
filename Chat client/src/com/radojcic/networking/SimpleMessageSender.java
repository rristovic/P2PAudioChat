package com.radojcic.networking;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import com.radojcic.networking.error.ConnectionErrorException;
import com.radojcic.networking.util.AudioMessageUtil;
import com.radojcic.util.Messages;

public class SimpleMessageSender implements IMessageSender {

	private BufferedReader console;
	private OutputStream outputStream;
	private PrintStream stringOutputStream;
	
	public SimpleMessageSender(OutputStream outputStream) {
		this.outputStream = outputStream;
		stringOutputStream = new PrintStream(outputStream);
	}


	@Override
	public void sendMessage(String message) throws ConnectionErrorException{
		if (outputStream != null)
			stringOutputStream.println(message);
		else
			throw new ConnectionErrorException("Client has gone offline.");
	}

	@Override
	public void sendSoundData(byte[] object, String msgName) {
		AudioMessageUtil.sendAudioMessage(outputStream, object, msgName);
	}

}
