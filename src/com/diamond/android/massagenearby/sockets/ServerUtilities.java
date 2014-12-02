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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.diamond.android.massagenearby.ApplicationMassageNearby;

import java.util.Map.Entry;
import java.util.Random;

import android.util.Log;



/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {
	
	private static final String TAG = "ServerUtilities";

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Send a message.
     */
    public static String send(String msg, String to, ApplicationMassageNearby amn) throws IOException {
        //Log.i(TAG, "sending message (msg = " + msg + ")");
        Map<String, String> params = new HashMap<String, String>();
        params.put(ApplicationMassageNearby.MSG, msg);
        params.put(ApplicationMassageNearby.FROM, amn.getSettingsManager().getChatId());
        params.put(ApplicationMassageNearby.TO, to);        
        
        return post(null, params, MAX_ATTEMPTS);
    }
    
    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     * @return response
     * @throws IOException propagated from POST.
     */
    private static String executePost(String endpoint, Map<String, String> params) throws IOException {
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
    
    /** Issue a POST with exponential backoff */
    private static String post(String endpoint, Map<String, String> params, int maxAttempts) throws IOException {
    	long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
    	for (int i = 1; i <= maxAttempts; i++) {
    		Log.d(TAG, "Attempt #" + i + " to connect");
    		try {
    			return executePost(endpoint, params);
    		} catch (IOException e) {
    			Log.e(TAG, "Failed on attempt " + i + ":" + e);
    			if (i == maxAttempts) {
    				throw e;
                }
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                backoff *= 2;    			
    		} catch (IllegalArgumentException e) {
    			throw new IOException(e.getMessage(), e);
    		}
    	}
    	return null;
    }
}
