package com.radojcic.login;

public interface LoginListener {
	void onLogin(UserDetails userDetails);
	void onLoginComplete(UserDetails userDetails);
}
