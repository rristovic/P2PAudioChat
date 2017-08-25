package com.radojcic;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.radojcic.gui.MainConsole;
import com.radojcic.networking.ChatClient;

public class SukiMessengerApp {
		
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		ChatClient chatClient = new ChatClient();
        		chatClient.startClient();
            }
        });
	}
}
