package com.diamond.android.massagenearby;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;

public class ApplicationMassageNearby extends Application {
	private SharedPreferences mSharedPreferences=null;
	private SettingsManager mSettingsManager=null;
	ItemMasseur mItemMasseur;
	ArrayList<Object> mAllMasseurs;
	ArrayAdapter aa;
	public static final String PROFILE_ID = "profile_id";
	
	//parameters recognized by demo server
	public static final String FROM = "chatId";
	public static final String REG_ID = "regId";
	public static final String MSG = "msg";
	public static final String TO = "chatId2";	

	

	public SettingsManager getSettingsManager() {
		if(mSettingsManager==null) {
			mSettingsManager=new SettingsManager(this, getSharedPreferences());
		}
		return mSettingsManager;
	}
	
	public SharedPreferences getSharedPreferences() {
		if(mSharedPreferences==null) {
			mSharedPreferences=getSharedPreferences(getPackageName() + "_preferences", Activity.MODE_PRIVATE);
		}
		return mSharedPreferences;
	}
	String[] getAllMasseursAsStringArray() {
		if(mAllMasseurs!=null) {
			String[] allMasseurNames=new String[mAllMasseurs.size()];
			int c=0;
			for(Object masseur : mAllMasseurs) {
				allMasseurNames[c++]=((ItemMasseur)masseur).getmName();
			}
			return allMasseurNames;
		} else {
			return null;
		}
	}
}
