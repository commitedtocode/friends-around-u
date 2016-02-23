package com.fau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.fau.locpoll.LocationPoller;
import com.fau.locpoll.LocationPollerParameter;;

public class Main extends MapActivity implements OnCheckedChangeListener {

	private MapView map;
	private MyLocationOverlay overlay;
	private MapController mapController;
	private ToggleButton tb;
	private TextView cuser;
	private Intent i;
	private SharedPreferences login;
	private SharedPreferences.Editor edit;
	private static final String C2DM_UNREG = "com.google.android.c2dm.intent.UNREGISTER";
	private static final int PERIOD=30000; 	// 30 sec
	private PendingIntent pi=null;
	private AlarmManager mgr=null;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadmain);
        login=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit=login.edit();
        cuser=(TextView) findViewById(R.id.cname);
        tb=(ToggleButton)findViewById(R.id.maintooglebutton);
        try {
            Class.forName("android.os.AsyncTask");
        } 
        catch (ClassNotFoundException e) {
        }
        	
        map=(MapView) findViewById(R.id.mapview);
        cuser.append(login.getString("username", ""));
        i=new Intent(Main.this, LocationSynch.class);
        i.putExtra("username", login.getString("username", ""));
        map.setBuiltInZoomControls(true);
        mapController=map.getController();
        tb.setOnCheckedChangeListener(this);
        myOverlay();
        //new thread created for starting c2dm registration service
        // TODO Auto-generated method stub
        if(login.getString("registrationId", "").length()==0)
        {
        	new Thread(new Runnable() {
        		@Override
        		public void run() {
        			// TODO Auto-generated method stub
					Log.d("C2DM", "start registration process");
					Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
					intent.putExtra("app",PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(), 0));
					// Sender currently not used
					intent.putExtra("sender", "hrushi.4ever@gmail.com");
					startService(intent);
        		}
			}).start();
        }
        
        /*fragment which start location polling service in background*/
        
        mgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
    	Intent i=new Intent(this, LocationPoller.class);
    	Bundle bundle = new Bundle();
    	LocationPollerParameter parameter = new LocationPollerParameter(bundle);
    	parameter.setIntentToBroadcastOnCompletion(new Intent(this, LocationReceiver.class));
    	// First try for GPS and fall back to NETWORK_PROVIDER
    	parameter.setProviders(new String[] {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER});
    	parameter.setTimeout(60000);
    	i.putExtras(bundle);
   		pi=PendingIntent.getBroadcast(this, 0, i, 0);
   		if(login.getString("loginstatus","").equals("online")){
        	tb.setChecked(true);
   		}
        else if(login.getString("loginstatus","").equals("offline")){
        	tb.setChecked(false);
        }
	}//on create ends here
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		overlay.disableCompass();
		overlay.disableMyLocation();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		overlay.enableCompass();
		overlay.enableMyLocation();
	}
	private void myOverlay() {
		// TODO Auto-generated method stub
		overlay=new MyLocationOverlay(this, map);
		overlay.enableMyLocation();
		overlay.runOnFirstFix(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mapController.setZoom(15);
				mapController.animateTo(overlay.getMyLocation());
			}
		});
		map.getOverlays().add(overlay);
	}
//------------------------------------------------option menu code------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.faumenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId()==R.id.menu_logout)
		{
			ServiceCollection.FAUPost(ServiceCollection.url+"logout.php",login.getString("username",""));
			edit.remove("registrationId");
			edit.remove("username");
			edit.remove("loginstatus");
			edit.commit();
			Intent unregIntent = new Intent(C2DM_UNREG);
			unregIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
			startService(unregIntent);
			if(mgr!=null)
			{
				mgr.cancel(pi);
			}
			startActivity(new Intent(Main.this,Login.class));
			Log.d("logout", "logout successfully");
		}
		else if(item.getItemId()==R.id.menu_contactsynch)
		{
			startActivity(new Intent(Main.this,PrefSynch.class));
		}
		return true;
	}
//----------------------------------*option menu end here*-----------------------------------------------------------	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
//----------------------------------toggle button------------------------------------------------------------------
	@Override
	public void onCheckedChanged(CompoundButton b, boolean s) {
		try{
		// TODO Auto-generated method stub
		if(s==true)
		{
			if(pi!=null && mgr!=null)
			{
				PostToggle online=new PostToggle();
				online.execute("1");
				mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime(),PERIOD,pi);
				edit.putString("loginstatus", "online");
				edit.commit();
				Log.d("loginstatus",login.getString("loginstatus", ""));
				Log.d("service start","start");
			}
		}
		else
		{
			PostToggle online=new PostToggle();
			online.execute("0");
			mgr.cancel(pi);
			edit.putString("loginstatus", "offline");
			edit.commit();
			Log.d("loginstatus",login.getString("loginstatus", ""));
			Log.d("service stoped","stop");
			//send off line flag
		}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}//end checked change
//-------------------------------------toggle button code ends here----------------------------------------------
	 private class PostToggle extends AsyncTask<String, Void, String>
	 {
		@Override
		protected String doInBackground(String... arg0) {
			Log.d("doinbg","started");
			return postmsg(arg0[0]);
		}
			@Override
		protected void onPostExecute(String result) {
			Log.d("postexe","started "+result);
				if(result.equals("001"))
				{
					Log.d("status","status changed");
				}
				else if(result.equals("002"))
				{
					Log.d("status","status error");
				}
				else
				{
					Log.d("status","server status error");
				}
			
		}
	 }
	 
	 public String postmsg(String op) {
	    	StringBuilder stringBuilder = new StringBuilder();
	    	HttpClient client = new DefaultHttpClient();
	    	HttpPost httppost = new HttpPost(ServiceCollection.url+"statustoggle.php");
	    	List<NameValuePair> parameter = new ArrayList<NameValuePair>(2);
	    	parameter.add(new BasicNameValuePair("username",login.getString("username", "")));
	    	parameter.add(new BasicNameValuePair("opcode",op));
	    	try {
	    		httppost.setEntity(new UrlEncodedFormEntity(parameter));
	    		HttpResponse response = client.execute(httppost);
	    		StatusLine statusLine = response.getStatusLine();
	    		int statusCode = statusLine.getStatusCode();
	    		if (statusCode == 200) {
	    			HttpEntity entity = response.getEntity();
	    			InputStream content = entity.getContent();
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	    			String line;
	    			while ((line = reader.readLine()) != null) {
	    					stringBuilder.append(line);
	    			}
	    		} 
	    		else {
	    			Log.e("postmsg", "Failed to download file");
	    		}
	    	}
	    	catch (HttpHostConnectException e) {
	    	e.printStackTrace();
			}
	    	catch (ConnectException e) {
	    	e.printStackTrace();
			}
	    	catch (ClientProtocolException e) {
	    	e.printStackTrace();
	    	} catch (IOException e) {
	    	e.printStackTrace();
	    	}
	    	catch (Exception e) {
	    	e.printStackTrace();
			}
	    return stringBuilder.toString();
	    }
}


