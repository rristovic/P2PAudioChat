package com.radojcic.networking.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import com.radojcic.networking.IClientListener;
import com.radojcic.networking.IMessageSender;
import com.radojcic.util.Messages;

public class AudioMessageUtil {

	public static void downloadAudioMessage(Socket communicationSocket, IClientListener.MessageListener msgListener) {
		try {
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			int bytesRead = 0;
			while ((bytesRead = communicationSocket.getInputStream().read(buffer)) >= 0) {
				if (Messages.isEndSoundMsg(buffer))
					break;
				for (int i = 0; i < bytesRead; i++) {
					ba.write(buffer[i]);
				}
			}
			msgListener.onNewMessage("New audio message.");
			msgListener.onNewMessage(ba.toByteArray());
		} catch (IOException e) {
			System.err.println("Failed to receive audio message: " + e.getLocalizedMessage());
		}
	}

	public static void sendAudioMessage(OutputStream outputStream, byte[] message,
			String messageName) {
		PrintStream print = new PrintStream(outputStream);
		print.println(Messages.sendAudioMessageReq(messageName));
		try {
			outputStream.write(message);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			outputStream.write(Messages.soundMsgEndReq());
		} catch (IOException e) {
			System.err.println("Faild to send audio message: " + messageName);
		}
	};
}
