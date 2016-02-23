package com.fau;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

public class C2DMMessageReceiver extends BroadcastReceiver {
	private String username;
	private double lat,lon;
	private ArrayList<String> friends_name,friends_lat,friends_lon,friends_distance,friends_mobile;
	private JSONObject json;
	private Vibrator vib;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.w("C2DM", "Message Receiver called");
		if ("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
			Log.w("C2DM", "Received message");
			final String payload = intent.getStringExtra("payload");
			Log.d("C2DM", "Control: payload = " + payload);
			try
			{
				json=new JSONObject(payload);
				friends_name=new ArrayList<String>();
				friends_lat=new ArrayList<String>();
				friends_lon=new ArrayList<String>();
				friends_distance=new ArrayList<String>();
				friends_mobile=new ArrayList<String>();
				
				username=intent.getStringExtra("user");
				lat=Double.parseDouble(intent.getStringExtra("lat"));
				lon=Double.parseDouble(intent.getStringExtra("lon"));
				friends_name.add(json.getString("username"));
				friends_lat.add(String.valueOf(json.getDouble("lat")));
				friends_lon.add(String.valueOf(json.getDouble("lon")));
				friends_distance.add(String.valueOf(json.getDouble("distance")));
				friends_mobile.add(json.getString("mobile"));
				
				
			}
			catch (JSONException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			// TODO Send this to my application server to get the real data
			// Lets make something visible to show that we received the message
			createNotification(context);
		}
	}

	public void createNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		vib=(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		MediaPlayer sound_note=MediaPlayer.create(context, R.raw.faubell);
		Notification notification = new Notification(android.R.drawable.ic_menu_mylocation,"found new friend around you", System.currentTimeMillis());
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(context, FriendsMap.class);
		
		intent.putExtra("username", username);
		intent.putExtra("lat", lat);
		intent.putExtra("lon", lon);
		intent.putStringArrayListExtra("friends_name",friends_name);
		intent.putStringArrayListExtra("friends_distance",friends_distance);
		intent.putStringArrayListExtra("friends_lat",friends_lat);
		intent.putStringArrayListExtra("friends_lon",friends_lon);
		intent.putStringArrayListExtra("friends_mobile", friends_mobile);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, "friends around u","hey found new friend around you", pendingIntent);
		notificationManager.notify(0, notification);
		sound_note.start();
		vib.vibrate(2000);
	}
}