package com.radojcic.networking;

import java.net.InetAddress;

/**
 * Listener used for listening to new arrived messages from the server.
 */
public interface IClientListener {

	public interface MessageReceiverListener {
		/**
		 * Called when new message has arrived from server.
		 * 
		 * @param message
		 *            retrieved from server.
		 */
		void onNewMessage(final String message);
	}
	
	public interface ClientChatEndListener {
		/**
		 * Called when chat has ended.
		 */
		void onChatEnded();
	}

	public interface NewClientListener {
		/**
		 * Called when new client wants to chat and has sent a new message.
		 * 
		 * @param message
		 *            - new message that client has sent.
		 * @return {@link MessageReceiverListener} object which will listen to
		 *         new chat messages.
		 */
		MessageReceiverListener onNewChat(IMessageSender msgSender, final String message, final String chatBuddyName);
	}
}
