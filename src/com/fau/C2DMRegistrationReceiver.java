package com.fau;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;


public class C2DMRegistrationReceiver extends BroadcastReceiver  {
	private String username;
	SharedPreferences regsp;
	
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.w("C2DM", "Registration Receiver called");
		if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
			Log.w("C2DM", "Received registration ID");
			//get shared preferences to get user name
			regsp=PreferenceManager.getDefaultSharedPreferences(context);
			username=regsp.getString("username","");
			Log.d("username c2dmreg",username);
			String registrationId = intent.getStringExtra("registration_id");
			String error = intent.getStringExtra("error");
			String unregister = intent.getStringExtra("unregistered");
			if (error != null) {
	            Log.e("C2DM", "Registration error " + error);
	            if ("SERVICE_NOT_AVAILABLE".equals(error)) {
	                Log.d("c2DM", "Scheduling registration retry, backoff");
	                Intent retryIntent = new Intent("com.google.android.c2dm.intent.RETRY");
	                PendingIntent retryPIntent = PendingIntent.getBroadcast(context, 0 /*requestCode*/, retryIntent, 0 /*flags*/);
	                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	                am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), retryPIntent);
	            }
			}
			else if(unregister!=null)
			{
				Log.w("C2DM", "Unregistration Receiver called");
				processRegistrationId(context, registrationId,"unregister");
			}
			else if(registrationId!=null)
			{
				Log.d("C2DM", "registrationId = " + registrationId+ ", error = " + error);
				String deviceId = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
				Log.d("deviceid",deviceId);
				//createNotification(context, registrationId);
				sendRegistrationIdToServer(deviceId, registrationId);
				// Also save it in the preference to be able to show it later
				processRegistrationId(context, registrationId,"register");
			}
		}
	}

	private void processRegistrationId(Context context, String registrationId, String action) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		if(action.equals("unregister")){
			edit.remove("registrationId");
		}
		else if(action.equals("register")){
			edit.putString("registrationId", registrationId);
		}
		edit.commit();
	}
	// Incorrect usage as the receiver may be canceled at any time
	// do this in an service and in an own thread
	
	public void sendRegistrationIdToServer(String deviceId,String registrationId) {
		Log.d("C2DM", "Sending registration ID to my application server");
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(ServiceCollection.url+"regfile.php");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			// Get the deviceID
			nameValuePairs.add(new BasicNameValuePair("deviceid", deviceId));
			nameValuePairs.add(new BasicNameValuePair("registrationid",registrationId));
			nameValuePairs.add(new BasicNameValuePair("username", username));

			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				Log.e("HttpResponse", line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
