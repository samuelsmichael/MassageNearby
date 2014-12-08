package com.diamond.android.massagenearby;

import java.util.ArrayList;

import com.diamond.android.massagenearby.model.ItemMasseur;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.ArrayAdapter;

public class ApplicationMassageNearby extends Application {
	private SharedPreferences mSharedPreferences=null;
	private SettingsManager mSettingsManager=null;
	public ItemMasseur mItemMasseur;
	public ArrayList<Object> mAllMasseurs;
	public Handler handler = new Handler();
	ArrayAdapter aa;
	public static final String PROFILE_ID = "profile_id";
	
	//parameters recognized by demo server
	public static final String FROM = "chatIdFrom";
	public static final String REG_ID = "regId";
	public static final String MSG = "msg";
	public static final String TO = "chatIdTo";	

	

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

	public ItemMasseur getItemMasseurOfMasseursHavingName(String name) {
		int x=0;
		ItemMasseur retValue=null;
		for (Object masseur: this.mAllMasseurs) {
			if(((ItemMasseur)masseur).getmName().equalsIgnoreCase(name)) {
				retValue=(ItemMasseur)masseur;
				break;
			}
			x++;
		}
			
		return retValue;
	}
}
