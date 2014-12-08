package com.diamond.android.massagenearby.model;

import java.net.Socket;

public class ItemClient {
	private String mName;
	private Socket mSocket;
	/**
	 * @return the mName
	 */
	public String getmName() {
		return mName;
	}
	/**
	 * @param mName the mName to set
	 */
	public void setmName(String mName) {
		this.mName = mName;
	}
	/**
	 * @return the mSocket
	 */
	public Socket getmSocket() {
		return mSocket;
	}
	/**
	 * @param mSocket the mSocket to set
	 */
	public void setmSocket(Socket mSocket) {
		this.mSocket = mSocket;
	}
}
