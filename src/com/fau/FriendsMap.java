package com.fau;

import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class FriendsMap extends MapActivity implements OnClickListener {
	
	private MapView map=null;
	private double lat,lon;
	private Drawable my_marker,friend_marker;
	private TextView userinfo;
	private String mobile_no;
	private Button call,sms,chat;
	private String username;
	private int count;
	private ArrayList<String> friends_name,friends_lat,friends_lon,friends_distance,friends_mobile;
	private FriendsOverlay myoverlay,f_overlay;
	 
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.friendsmap);
	    userinfo=(TextView) findViewById(R.id.txtuserinfog);
	    friends_name=new ArrayList<String>();
		friends_lat=new ArrayList<String>();
		friends_lon=new ArrayList<String>();
		friends_distance=new ArrayList<String>();
		friends_mobile=new ArrayList<String>();
		
		
	    username=getIntent().getStringExtra("username");
	    lat=getIntent().getDoubleExtra("lat", 0.0);
	    lon=getIntent().getDoubleExtra("lon", 0.0);
	    friends_name=getIntent().getStringArrayListExtra("friends_name");
	    friends_lat=getIntent().getStringArrayListExtra("friends_lat");
	    friends_lon=getIntent().getStringArrayListExtra("friends_lon");
	    friends_distance=getIntent().getStringArrayListExtra("friends_distance");
	    friends_mobile=getIntent().getStringArrayListExtra("friends_mobile");
	    count=friends_name.size();
	    
	   // Log.d("MAP","***"+friends_distance.toString()+"***");
	    call=(Button) findViewById(R.id.btncall);
	    sms=(Button) findViewById(R.id.btnsms);
	    chat=(Button) findViewById(R.id.btnchat);
	    call.setOnClickListener(this);
	    sms.setOnClickListener(this);
	    chat.setOnClickListener(this);
	    
	    map=(MapView)findViewById(R.id.map);
	    map.getController().setCenter(getPoint(lat,lon));
	    map.getController().setZoom(10);
	    map.setBuiltInZoomControls(true); 
	    my_marker=getResources().getDrawable(R.drawable.green_full_marker);
	    friend_marker=getResources().getDrawable(R.drawable.blue_full_marker);
	    
	    myoverlay=new FriendsOverlay(my_marker);
	    myoverlay.addOverlay(new FriendItem(getPoint(lat, lon), username, username, "7276342274", "0.0"));
	    map.getOverlays().add(myoverlay);
	    
	    f_overlay=new FriendsOverlay(friend_marker);
	    for(int i=0;i<count;i++)
	    {
	    	f_overlay.addOverlay(new FriendItem(getPoint(Double.parseDouble(friends_lat.get(i)), Double.parseDouble(friends_lon.get(i))), friends_name.get(i), "doing tp", friends_mobile.get(i), friends_distance.get(i)));
	    	map.getOverlays().add(f_overlay);
	    }
	    map.invalidate();
	  }
	
	  
	  @Override
	  public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		this.finish();
	  }
	  
	  @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		f_overlay.clear();
		myoverlay.clear();
		map.getOverlays().clear();
		Log.d("MAP", "destroyed");
	}
	  
	  @Override
	  protected boolean isRouteDisplayed() {
	    return(false);
	  }

	  private GeoPoint getPoint(double lat, double lon) {
	    return(new GeoPoint((int)(lat*1000000.0),(int)(lon*1000000.0)));
	  }
	  
	  private class FriendsOverlay extends ItemizedOverlay<FriendItem>
	  {
		  private ArrayList<FriendItem> friends=new ArrayList<FriendItem>();
		  public FriendsOverlay(Drawable marker) 
		  {
			  super(boundCenterBottom(marker));
		  }
		
		  public void addOverlay(FriendItem friend) {
			  friends.add(friend);
			  populate();
		  }
		  
		  /*public void removeOverlay(FriendItem overlay) {
			  friends.remove(overlay);
		       populate();
		    }*/

		    public void clear() {
		    	friends.clear();
		        populate();
		    }

		  
		  @Override
		  protected FriendItem createItem(int i) {
			  // TODO Auto-generated method stub
			  return friends.get(i);
		  }

		  @Override
		  public int size() {
			  // TODO Auto-generated method stub
			  return friends.size();
		  }
		  
		  @Override
		protected boolean onTap(int index) {
			// TODO Auto-generated method stub
			  FriendItem fr=friends.get(index);
			  if(fr.getTitle().equals(username)){
				  userinfo.setText("you are here");
				  mobile_no=null;
			  }
			  else{
				  userinfo.setText(fr.getTitle()+" is "+fr.getDistance()+" k.m. away from you");
				  mobile_no=fr.getMobile();  
			  }
			  return true;
		}
	  }
	  //FriendsOverlay Class end
	  
	  class FriendItem extends OverlayItem {
		private String mobile,distance;
	 
	    FriendItem(GeoPoint pt, String username, String statusmsg,String mobileno,String distance) {
	      super(pt, username, statusmsg);
	      mobile=mobileno;
	      this.distance=distance;
	    }
	    
	    String getMobile()
	    {
	    	return mobile;
	    }
	    
	    String getDistance()
	    {
	    	return distance;
	    }
	  }
	  
	  @Override
	  public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.btncall)
		{
			if( mobile_no!=null){
				Toast.makeText(this,"call clicked", 0).show();
				startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+mobile_no)));
			}
		}
		
		else if(v.getId()==R.id.btnsms)
		{
			if( mobile_no!=null){
				Toast.makeText(this,"sms clicked", 0).show();
				startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("sms: "+mobile_no)));
			}
		}
		else if(v.getId()==R.id.btnchat)
		{
			Uri gtlak=new Uri.Builder().scheme("im").authority("gtalk").build();
			Intent ichat=new Intent(Intent.ACTION_SENDTO, gtlak);
			ichat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try{
				startActivity(ichat);
			}
			catch (ActivityNotFoundException e) {
				// TODO: handle exception
				Toast.makeText(this,"chat application not found", 0).show();
			}
			catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(this,"Chat Error", 0).show();
			}	
		}	
	  }
}
