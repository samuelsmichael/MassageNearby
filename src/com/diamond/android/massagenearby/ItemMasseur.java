package com.diamond.android.massagenearby;

import java.net.Socket;

public class ItemMasseur {
	public int getmUserId() {
		return mUserId;
	}
	public void setmUserId(int mUserId) {
		this.mUserId = mUserId;
	}
	public int getmMasserId() {
		return mMasserId;
	}
	public void setmMasserId(int mMasserId) {
		this.mMasserId = mMasserId;
	}
	public String getmName() {
		return mName;
	}
	public void setmName(String mName) {
		this.mName = mName;
	}
	public String getmURL() {
		return mURL;
	}
	public void setmURL(String mURL) {
		this.mURL = mURL;
	}
	public boolean ismIsOnline() {
		return mIsOnline;
	}
	public void setmIsOnline(boolean mIsOnline) {
		this.mIsOnline = mIsOnline;
	}
	int mUserId;
	public Socket getmSocket() {
		return mSocket;
	}
	public void setmSocket(Socket mSocket) {
		this.mSocket = mSocket;
	}
	int mMasserId;
	String mName;
	String mURL;
	boolean mIsOnline;
	Socket mSocket;
	boolean mConnected=false;
	/**
	 * @return the mConnected
	 */
	public boolean ismConnected() {
		return mConnected;
	}
	/**
	 * @param mConnected the mConnected to set
	 */
	public void setmConnected(boolean mConnected) {
		this.mConnected = mConnected;
	}
}
