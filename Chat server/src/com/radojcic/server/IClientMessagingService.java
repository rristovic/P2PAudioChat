package com.radojcic.server;

import java.util.List;

public interface IClientMessagingService {
	/**
	 * Method for sending message to chosen recipients.
	 * 
	 * @param sender - client who sent the message.
	 * @param recipients
	 *            list of client user names to receive the
	 *            message.
	 * @param msg
	 *            message string to be sent
	 * @return list of clients who received the message
	 */
	List<String> sendMsgToRecipients(ClientThread sender, List<String> recipients, String msg);
}
