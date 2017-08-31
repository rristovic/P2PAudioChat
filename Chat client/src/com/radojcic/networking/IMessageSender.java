package com.radojcic.networking;

import com.radojcic.networking.error.ConnectionErrorException;

/**
 * Class which implements this interface will act as message sender to the server.
 */
public interface IMessageSender {
	/**
	 * Invoked when new message should be sent.
	 * @param message to send.
	 */
	void sendMessage(String message) throws ConnectionErrorException;
	
	void sendSoundData(byte[] object, String msgName) throws ConnectionErrorException;
}
