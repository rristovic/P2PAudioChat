package com.radojcic.util;

import com.radojcic.login.UserDetails;

public abstract class Messages {

	public static final String welcomeMsg = "Welcome to Sukisa Instant Messeneger!";

	public static final String CON_END_REQ = "endconnection::";
	public static final String MAINFRAME_END_REQ = "mainframedisconnect::";
	public static final String GET_CLIENTS_REQ = "getclients::";
	public static final String LOGIN_REQ = "login::";
	public static final String AUDIO_MSG_REQ = "audiomessage::";
	public static final String GET_CLIENT_REQ = "client::";

	public static String loginReqMsg(UserDetails ud, int chatPort, int udpPort) {
		return String.format("%s{\"user\":%s,\"chatPort\":%d,\"udpPort\":%d}", LOGIN_REQ, ud.toJSON(), chatPort, udpPort);
	}

	public static String selectClientReqMsg(String clientName) {
		return String.format("%s%s", GET_CLIENT_REQ, clientName);
	}
	
	public static String sendAudioMessageReq(String msgName) {
		return String.format("%s%s", AUDIO_MSG_REQ, msgName);
	}
	
	private static byte[] end = new byte[8];
	static {
		end[0] = 0;
		end[1] = Byte.MIN_VALUE;
		end[2] = 0;
		end[3] = Byte.MAX_VALUE;
		end[4] = 0;
		end[5] = Byte.MIN_VALUE;
		end[6] = 0;
		end[7] = Byte.MAX_VALUE;
	}
	public static byte[] soundMsgEndReq() {
		return end;
	}
	
	public static boolean isEndSoundMsg(byte[] soundMsg) {
		for (int i = 0; i < end.length; i++) {
			if (soundMsg[i] != end[i])
				return false;
		}
		return true;
	}

}
