package com.diamond.android.massagenearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingsManager {
	private SharedPreferences mSharedPreferences;
	private Context mContext;
	
	public SettingsManager(Context context, SharedPreferences sharedPreferences) {
		mSharedPreferences=sharedPreferences;
		mContext=context;
	}
	private String getValue(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}
	private void setValue(String key, String value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(key,value);
		editor.commit();				
	}
	public String getDisplayName() {
		return getValue("display_name", "");
	}
	
	public String getChatId() {
		return getValue("chat_id", "");
	}
	public void setChatId(String chatId) {
		setValue("chat_id",chatId);
	}
	
	public String getCurrentChat() {
		return getValue("current_chat", null);
	}
	public void setCurrentChat(String chatId) {
		setValue("current_chat", chatId);
	}	
	
	public boolean isNotify() {		
		return mSharedPreferences.getBoolean("notifications_new_message", true);
	}	
	
	public String getRingtone() {
		return getValue("notifications_new_message_ringtone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
	}
	public void setCurrentUserName(String u) {
		setValue("curusenam", u);
	}
	public String getCurrentUserName() {
		return getValue("curusenam","");
	}
}
