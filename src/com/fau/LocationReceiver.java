package com.fau;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.fau.locpoll.LocationPollerResult;

public class LocationReceiver extends BroadcastReceiver {
	private String username=null;
	private SharedPreferences sp;
	
	@Override
	public void onReceive(Context context, Intent intent){
	  Log.d("reciver fired","reciver fired");
	  sp=PreferenceManager.getDefaultSharedPreferences(context);
	  if(username==null)
	  {
		  username=sp.getString("username", "n/a");  
	  }
      
      try {
    	  Bundle b=intent.getExtras();
    	  LocationPollerResult locationResult = new LocationPollerResult(b);
    	  Location loc=locationResult.getLocation();
    	  String msg;

    	  if (loc==null) {
    		  loc=locationResult.getLastKnownLocation();

    		  if (loc==null) {
    			  msg=locationResult.getError();
    		  }
    		  else 
    		  {
    			  msg="TIMEOUT, lastKnown="+loc.toString();
    		  }
    	  }
    	  else{
    		  msg=loc.toString();
    		  Log.d("location Recived",msg);
    		  Log.d("send username",username);
    		  Log.d("latitude",String.valueOf(loc.getLatitude()));
    		  Log.d("longitude",String.valueOf(loc.getLongitude()));
    		  Toast.makeText(context,username+ "|"+loc.getLatitude()+"|"+loc.getLongitude(), 1).show();
    		  Intent intentsynch=new Intent(context,LocationSynch.class);
    		  intentsynch.putExtra("username", username);
    		  intentsynch.putExtra("lat", loc.getLatitude());
    		  intentsynch.putExtra("lon", loc.getLongitude());
    		  context.startService(intentsynch);
    	  }
    	  
    	  if (msg==null) {
    		  msg="Invalid broadcast received!";
    	  }
      }
      catch (NullPointerException e) {
		// TODO: handle exception
    	Toast.makeText(context, "location error", 1).show();
      }
      catch (Exception e) {
		// TODO: handle exception
    	  Toast.makeText(context, "exception error", 1).show();
	}
  	}
}