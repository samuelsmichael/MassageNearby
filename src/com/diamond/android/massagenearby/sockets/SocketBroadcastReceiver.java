package com.diamond.android.massagenearby.sockets;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import com.diamond.android.massagenearby.ApplicationMassageNearby;

import com.diamond.android.massagenearby.DataProvider;
import com.diamond.android.massagenearby.MainActivity;
import com.diamond.android.massagenearby.R;
import com.diamond.android.massagenearby.R.drawable;
import com.diamond.android.massagenearby.R.string;
import com.diamond.android.massagenearby.SettingsManager;

public class SocketBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "GcmBroadcastReceiver";
	
	private Context ctx;
	private ContentResolver cr;
	private SocketBroadcastReceiver() {}
	private SettingsManager mSettingsManager;
	public SocketBroadcastReceiver(SettingsManager sm) {
		mSettingsManager=sm;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		ctx = context;
		cr = context.getContentResolver();
		
		PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
		
		try {
			String msg = intent.getStringExtra(ApplicationMassageNearby.MSG);
			String from = intent.getStringExtra(ApplicationMassageNearby.FROM);
			String to = intent.getStringExtra(ApplicationMassageNearby.TO);
			
			//find contact
			String contactName = null;
			Cursor c = context.getContentResolver().query(
					DataProvider.CONTENT_URI_PROFILE, 
					new String[]{DataProvider.COL_NAME}, 
					DataProvider.COL_CHATID+" = ?", 
					new String[]{from}, 
					null);
			if (c != null) {
				if (c.moveToFirst()) {
					contactName = c.getString(0);
				}
				c.close();
			}
			
			//contact not found
			if (contactName == null) return;
			
			ContentValues values = new ContentValues(2);
			values.put(DataProvider.COL_MSG, msg);
			values.put(DataProvider.COL_FROM, from);
			values.put(DataProvider.COL_TO, to);
			cr.insert(DataProvider.CONTENT_URI_MESSAGES, values);
			
			if (!from.equals(mSettingsManager.getCurrentChat()) && !to.equals(mSettingsManager.getCurrentChat())) {
				if (mSettingsManager.isNotify()) 
					sendNotification(contactName+": "+msg, true);
				
				incrementMessageCount(context, from, to);
			}
			
			setResultCode(Activity.RESULT_OK);
			
		} finally {
			mWakeLock.release();
		}
	}
	
	private void sendNotification(String text, boolean launchApp) {
		NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification.Builder mBuilder = new Notification.Builder(ctx)
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(ctx.getString(R.string.app_name))
			.setContentText(text);

		if (!TextUtils.isEmpty(mSettingsManager.getRingtone())) {
			mBuilder.setSound(Uri.parse(mSettingsManager.getRingtone()));
		}
		
		if (launchApp) {
			Intent intent = new Intent(ctx, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(pi);
		}
		
		mNotificationManager.notify(1, mBuilder.getNotification());
	}
	
	private void incrementMessageCount(Context context, String from, String to) {
		String chatId;
		if (!mSettingsManager.getChatId().equals(to)) {//group
			chatId = to;
		} else {
			chatId = from;
		}
		
		String selection = DataProvider.COL_CHATID+" = ?";
		String[] selectionArgs = new String[]{chatId};
		Cursor c = cr.query(DataProvider.CONTENT_URI_PROFILE, 
				new String[]{DataProvider.COL_COUNT}, 
				selection, 
				selectionArgs, 
				null);
		
		if (c != null) {
			if (c.moveToFirst()) {
				int count = c.getInt(0);
				
				ContentValues cv = new ContentValues(1);
				cv.put(DataProvider.COL_COUNT, count+1);
				cr.update(DataProvider.CONTENT_URI_PROFILE, cv, selection, selectionArgs);
			}
			c.close();
		}
	}
}
