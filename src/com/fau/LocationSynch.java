package com.fau;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

public class LocationSynch extends IntentService {
	private String username,address,server_response;
	private double lat,lon;
	private Geocoder gc;
	private List<Address> addresses;
	private ArrayList<String> friends_name,friends_lat,friends_lon,friends_distance,friends_mobile;
	private Notification notification;
	private NotificationManager nm;
	private Vibrator vib;
	private JSONObject friendsjson;

	public LocationSynch() {
		super("Location synchronization");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		/*all initialization work here*/
		username=intent.getStringExtra("username");
		lat=intent.getDoubleExtra("lat",0.0);
		lon=intent.getDoubleExtra("lon",0.0);
		gc=new Geocoder(this, Locale.getDefault());
		//get location details
		getLocationLable();
		//send location details to server and store result in veriable
		server_response=sendlocations();
		//create notification
		if(server_response.equals("001"))
			Log.d("friends","no friends");
		else
			parseData();
	}
	
	private void getLocationLable()
	{
		try
		{
			addresses=gc.getFromLocation(lat, lon, 2);
			//Log.d("addresses",addresses.toString());
			address=addresses.get(1).getAddressLine(0);
		}
		catch (Exception e) {
			// TODO: handle exception
			address="No information about location";
		}
		Log.d("address",address.toString());
	}
	
	private void parseData() {
		// TODO Auto-generated method stub
		int friends_lenghth;
		nm=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		vib=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		try {
			friendsjson=new JSONObject(server_response);
			friends_name=new ArrayList<String>();
			friends_lat=new ArrayList<String>();
			friends_lon=new ArrayList<String>();
			friends_distance=new ArrayList<String>();
			friends_mobile=new ArrayList<String>();
			
			Log.d("friends", friendsjson.toString());
			friends_lenghth=friendsjson.length();
			
			for (int i = 0; i < friends_lenghth; i++) {
				JSONObject json=friendsjson.getJSONObject(String.valueOf(i));
				friends_name.add(json.getString("username"));
				friends_lat.add(String.valueOf(json.getDouble("lat")));
				friends_lon.add(String.valueOf(json.getDouble("lon")));
				friends_distance.add(String.valueOf(json.getDouble("distance")));
				friends_mobile.add(json.getString("mobile"));
				createNotification(friends_lenghth);
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void createNotification(int i){
		
		notification = new Notification(android.R.drawable.ic_dialog_info, "found "+i+"friends around you", System.currentTimeMillis());
		MediaPlayer sound_note=MediaPlayer.create(getApplicationContext(), R.raw.faubell);
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(this, FriendsMap.class);
		
		intent.putExtra("username", username);
		intent.putExtra("lat", lat);
		intent.putExtra("lon", lon);
		intent.putStringArrayListExtra("friends_name",friends_name);
		intent.putStringArrayListExtra("friends_distance",friends_distance);
		intent.putStringArrayListExtra("friends_lat",friends_lat);
		intent.putStringArrayListExtra("friends_lon",friends_lon);
		intent.putStringArrayListExtra("friends_mobile", friends_mobile);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this,"friends around you" ,"found "+i+" friends around you", pendingIntent);
		nm.notify(i, notification);
		sound_note.start();
		vib.vibrate(2000);
	}
	
	private String sendlocations() {
		// TODO Auto-generated method stub
		String result=null;
		HttpClient client=new DefaultHttpClient();
		HttpPost post=new HttpPost(ServiceCollection.url+"location.php");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("username",username));
		nameValuePairs.add(new BasicNameValuePair("lat",String.valueOf(lat)));
		nameValuePairs.add(new BasicNameValuePair("lon",String.valueOf(lon)));
		nameValuePairs.add(new BasicNameValuePair("address",address));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response=client.execute(post);
			result = ServiceCollection.parseStr(response);
			Log.d("friends",result);
		} 
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}
	
	
}

