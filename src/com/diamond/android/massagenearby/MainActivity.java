package com.diamond.android.massagenearby;


import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.Semaphore;

import com.diamond.android.massagenearby.sockets.ServerUtilities;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,  
        	WaitingForDataAcquiredAsynchronously,DataGetter,
    		MessagesFragment.OnFragmentInteractionListener
        	{
	
	ProgressDialog mProgressDialog;
	String profileChatId;



    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    

    @Override
	protected void onDestroy() {
 	   new AcquireDataRemotelyAsynchronously("byebye~", this, this);
 	   super.onDestroy();
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
        mTitle = getTitle();

 	   new AcquireDataRemotelyAsynchronously("all~", this, this);

	    android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
	    android.app.Fragment prev = getFragmentManager().findFragmentByTag("login");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    Login selectLogin=new Login();
		selectLogin.show(ft,"login");
        
        
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
    	try {
    		profileChatId=String.valueOf(position+1);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1,((ApplicationMassageNearby)getApplication()).getSettingsManager()))
                .commit();
    	} catch (Exception e) {}
    }

    public void onSectionAttached(int number) {
    	ArrayList allMs=((ApplicationMassageNearby)getApplication()).mAllMasseurs;
    	if(allMs!=null) {
    		mTitle = ((ItemMasseur)allMs.get(number-1)).getmName();
    	} else {
    		mTitle="";
    	}
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment  {
    	private EditText msgEdit;
    	private Button btnSend;
    	String profileChatId;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, SettingsManager sm) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
        	fragment.profileChatId=String.valueOf(sectionNumber);
        	sm.setChatId(fragment.profileChatId);
            return fragment;
        }
        ServerUtilities su=new ServerUtilities();
        Semaphore stick = new Semaphore(0);
    	private void send(final String txt) {
    		final String txt2=txt;
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    try {
                    	msg = su.send(txt2, profileChatId,((ApplicationMassageNearby)getActivity().getApplication()),stick,(MainActivity)getActivity());
                    	try {
                    		stick.acquire();
                    	} catch (Exception ee) {
                    		return null;
                    	}
            			ContentValues values = new ContentValues(2);
            			values.put(DataProvider.COL_MSG, txt);
            			values.put(DataProvider.COL_TO, profileChatId);
            			getActivity().getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
            			
                    } catch (IOException ex) {
                        msg = ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                	if (!TextUtils.isEmpty(msg)) {
                		Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                	}
                }
            }.execute(null, null, null);		
    	}	
        
        
        public PlaceholderFragment() {
        }
       
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container2, MessagesFragment.newInstance(Integer.valueOf(profileChatId)))
                    .commit();

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            msgEdit = (EditText)rootView.findViewById(R.id.msg_edit);
            btnSend = (Button) rootView.findViewById(R.id.send_btn);
            btnSend.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
	    			String msg = msgEdit.getText().toString();
	    			if (!TextUtils.isEmpty(msg)) {
	    				send(msg);
	    				msgEdit.setText(null);
	    			}
				}
			});
            return rootView;
        }
        
        @Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
		}

		@Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

	@Override
	public String getProfileChatId() {
		return profileChatId;
	}

    public class Login extends DialogFragment {
    	View mView;
    	EditText mEditText;
    	

		@Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
		        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		        // Get the layout inflater
		        LayoutInflater inflater = getActivity().getLayoutInflater();
		        mView = inflater.inflate(R.layout.login, null);
		        mEditText=(EditText)mView.findViewById(R.id.edittextloginid);
		        mEditText.setText(((ApplicationMassageNearby)getApplication()).getSettingsManager().getCurrentUserName());
		        mEditText.selectAll();
		        builder.setView(mView);
	
		        // Inflate and set the layout for the dialog
		        // Pass null as the parent view because its going in the dialog layout
//		        builder.setView(inflater.inflate(R.layout.find_home, null));
		        builder.setTitle("Login")
		        	   .setMessage("Key in your name")
		               .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                	   final EditText mEditText=(EditText)Login.this.getDialog().findViewById(R.id.edittextloginid);
		                	   MainActivity mMainActivity=(MainActivity)getActivity();
		                	   new AcquireDataRemotelyAsynchronously("moi~"+ mEditText.getText().toString(), mMainActivity, mMainActivity);
		           			   mMainActivity.mProgressDialog = ProgressDialog.show(getActivity(),"Searching ...","Logging in "+mEditText.getText().toString(),true,false,null);
		                	   
		                  }
		               })
		               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                	   finish();
		                   }
		               });
		        // Create the AlertDialog object and return it
		        return builder.create();
        }		
    }
	@Override
	public ArrayList<Object> getRemoteData(String keynname) {
		try {
			String[] array = keynname.split("\\~", -1);
			String key=array[0];
			String name=array[1];
			// 10.0.0.253 when wifi on my computer
			String url=
					key.equals("moi")?"http://listplus.no-ip.org/MassageNearby/Masseur.aspx"+"?Name="+URLEncoder.encode(name)+"&URL="+URLEncoder.encode(getLocalIpAddress()): (
					key.equals("byebye")?"http://listplus.no-ip.org/MassageNearby/Masseur.aspx"+"?MasseurId="+ 
						((ApplicationMassageNearby)getApplication()).mItemMasseur.mMasserId
							: (
							"http://listplus.no-ip.org/MassageNearby/Masseur.aspx"
							));
			// Add your data

			ArrayList<Object> data = new JsonReaderFromRemotelyAcquiredJson(
				new ParsesJsonMasseur(name), 
				url
				).parse();
			return data;
		} catch (Exception e) {
			int bkhere1=3;
			int bkhere2=bkhere1;
		} finally {
		}			
	return null;
	}
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                    	String hostAddress=inetAddress.getHostAddress().toString();
                    	return hostAddress; 
                    	}
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

	@Override
	public void gotMyData(String keynname, ArrayList<Object> data) {
		String[] array = keynname.split("\\~", -1);
		String key=array[0];
		String name=array[1];
		if(key.equals("moi")) {
			this.mProgressDialog.dismiss();
		}
		if(data!=null && data.size()>0) {
				if(key.equals("moi")) {
					((ApplicationMassageNearby)getApplication()).mItemMasseur=(ItemMasseur)data.get(0);
					getSettingsManager().setCurrentUserName(((ApplicationMassageNearby)getApplication()).mItemMasseur.getmName());
				} else {
					if(key.equals("byebye")) {
						int bkyere=3;
						int bb=bkyere;
					} else {
					((ApplicationMassageNearby)getApplication()).mAllMasseurs=data;
				}
			}
		}
	}
	private SettingsManager getSettingsManager() {
		return ((ApplicationMassageNearby)getApplication()).getSettingsManager();
	}

}
