/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diamond.android.massagenearby.sockets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.diamond.android.massagenearby.ApplicationMassageNearby;
import com.diamond.android.massagenearby.DataProvider;
import com.diamond.android.massagenearby.MainActivity;
import com.diamond.android.massagenearby.common.GlobalStaticValues;
import com.diamond.android.massagenearby.model.ItemMasseur;


import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Semaphore;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {
	public static final int SERVERPORT = 8080;
	private static final String TAG = "ServerUtilities";

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Send a message.
     */
    public  String send(String msg, String to, ApplicationMassageNearby amn,Semaphore stick,MainActivity ac) throws Exception {
        //Log.i(TAG, "sending message (msg = " + msg + ")");
        Map<String, String> params = new HashMap<String, String>();
        params.put(ApplicationMassageNearby.MSG, msg);
        params.put(ApplicationMassageNearby.FROM, amn.getSettingsManager().getChatId());
        params.put(ApplicationMassageNearby.TO,/*bbhbb 51*/to);        
        
        return post(params, MAX_ATTEMPTS,amn, stick, ac);
    }
    
    /** Issue a POST with exponential backoff */
    private  String post(Map<String, String> params, int maxAttempts,ApplicationMassageNearby amn, Semaphore stick, MainActivity ma) throws Exception {
    	long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
    	Exception theLastIOExceptionOfTheBunch = null;
    	for (int i = 1; i <= maxAttempts; i++) {
    		Log.d(TAG, "Attempt #" + i + " to connect");
    		try {
    			String retMsg=executePost( params,amn, ma);
    			stick.release();
    			return retMsg;
    		} catch (IOException e) {
    			theLastIOExceptionOfTheBunch=e;
    			Log.e(TAG, "Failed on attempt " + i + ":" + e);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                }
                backoff *= 2;    			
    		}
    	}
		stick.release();
		throw theLastIOExceptionOfTheBunch;
    }

    
    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     * @return response
     * @throws IOException propagated from POST.
     */
    private String executePost(Map<String, String> params,ApplicationMassageNearby amn, MainActivity ma) throws IOException, InterruptedException {
    	String toId=params.get(ApplicationMassageNearby.TO);
    	ItemMasseur im = amn.getItemMasseurOfMasseursHavingUserId(Integer.valueOf(toId));
    	if(im.ismConnected()) {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(im.getmSocket()
                    .getOutputStream())), true);
            out.println(amn.mItemMasseur.getmName()+"~"+amn.mItemMasseur.getmUserId()+"~"+GlobalStaticValues.COMMAND_HERES_MY_CHAT_MSG+"~"+params.get(ApplicationMassageNearby.MSG));     	 	
    	} else {
        	Semaphore stick2=new Semaphore(0);
        	ClientThread ct=new ClientThread(im,amn,stick2,ma);
            Thread cThread = new Thread(ct);
            cThread.start();
			stick2.acquire();
			if(im.ismConnected()) {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(im.getmSocket()
                        .getOutputStream())), true);
                out.println(amn.mItemMasseur.getmName()+"~"+amn.mItemMasseur.getmUserId()+"~"+GlobalStaticValues.COMMAND_HERES_MY_CHAT_MSG+"~"+params.get(ApplicationMassageNearby.MSG));
			} else {
				throw new IOException(ct.errMessage);
			}
    	}
    	
    	/*
    	 * if allMasseurs[the one I'm trying to talk to]'s socket ==null
    	 * 		create a socket
    	 * 			and if fails, try re-getting that guy (and putting him into allMasseurs)
    	 * 				and try again
    	 *		Note: if time's out, then throw exception
    	 * otherwise, send the msg (pre-pending my Name, so other guy knows who he's talking to, and can add him to his list on the left {name, socket}
    	 */
    	return null;
      }
    public class ClientThread implements Runnable {
    	String mIpAddress;
    	ItemMasseur mMasseur;
    	ApplicationMassageNearby mAmn;
    	Semaphore mStick2;
    	MainActivity mMa;
    	String errMessage=null;
    	public ClientThread(ItemMasseur masseur,ApplicationMassageNearby amn, Semaphore stick2, MainActivity ma) {
    		mIpAddress=masseur.getmURL();
    		mMasseur=masseur;
    		mAmn=amn;
    		mStick2=stick2;
    		mMa=ma;
    	}
  
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(mIpAddress);
                Log.d("ClientActivity", "C: Connecting...");
                mMasseur.setmSocket( new Socket(serverAddr, SERVERPORT));
                mMasseur.setmConnected(true);
                Thread cThread = new Thread(new ClientThreadReceive(mMasseur,mAmn,mMa));
                cThread.start();
            } catch (UnknownHostException e) {
                Log.e("ClientActivity", "C: Error", e);
                mMasseur.setmConnected(false);         
                errMessage=e.getMessage();
            } catch (IOException e) {
                Log.e("ClientActivity", "C: Error", e);
                mMasseur.setmConnected(false);                    
                errMessage=e.getMessage();
            }
           	mStick2.release();               
        }
    }
    public class ClientThreadReceive implements Runnable {
    	ItemMasseur mMasseur;
    	String line;
    	ApplicationMassageNearby mAmn;
    	MainActivity mMa;
    	public ClientThreadReceive(ItemMasseur masseur,ApplicationMassageNearby amn, MainActivity ma) {
    		mMasseur=masseur;
    		mAmn=amn;
    		mMa=ma;
    	}
		@Override
		public void run() {
			while(true) {
				try {
                BufferedReader in = new BufferedReader(new InputStreamReader(mMasseur.getmSocket().getInputStream()));
                line = null;
                while ((line = in.readLine()) != null) {
                    Log.d("ServerActivity", line);
                    mAmn.handler.post(new Runnable() {
                        @Override
                        public void run() {
                        	if(!TextUtils.isEmpty(line)) {
	                        	String[] sa=line.split("\\~", -1);
	                        	String name=sa[0];
	                        	String userId=sa[1];
	                        	String command=sa[2];
	                        	String msg=sa[3];
                    			if(command.equals(GlobalStaticValues.COMMAND_HERES_MY_CHAT_MSG)) {
		                			ContentValues values = new ContentValues(2);
		                			values.put(DataProvider.COL_MSG, msg);
		                			values.put(DataProvider.COL_FROM,  /*bbhbb 51*/ String.valueOf(userId));
		                			values.put(DataProvider.COL_TO, /*bbhbb  52*/ String.valueOf(mAmn.mItemMasseur.getmUserId()));
		                			mMa.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
                        		}
                        	}
                        }
                    });
                }

				} catch (Exception e) {
					break;
				}
			}
		}
    	
    }

}
