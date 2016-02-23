package com.fau;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.fau.pref.Instruction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	public static boolean connectivity;
	 EditText un,pass;
	 Button log,reg;
	 SharedPreferences loginpref;
	 SharedPreferences.Editor edit;
	 String csync_username=null;
	 
    @Override  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivity=haveNetworkConnection();
        if(!connectivity)
		{
			Toast.makeText(this, "No internet connectivty found, please check the netwotk setting",Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			startActivity(intent);
			return;
		}
        
        loginpref=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(loginpref.getString("username", "").length()!=0)
        {
        	Toast.makeText(Login.this, "already login", Toast.LENGTH_SHORT).show();
        	startActivity(new Intent(Login.this,Main.class));
        }
        else
        {
        	setContentView(R.layout.login);
        	un=(EditText)findViewById(R.id.txt_username);
        	pass=(EditText)findViewById(R.id.txt_password);
        	log=(Button)findViewById(R.id.btn_login);
        	reg=(Button)findViewById(R.id.btn_register);
        	log.setOnClickListener(this);
        	reg.setOnClickListener(this);
        }
    }

	public void onClick(View v) 
	{
		if(v.getId()==R.id.btn_register)
		{
			Log.d("button","rgistration is clicked");
			startActivity(new Intent(Login.this, Reg.class));
		}
		else
		{
			if(un.getText().toString().trim().length()==0)
			{
				Toast.makeText(Login.this, "please enter the user name", Toast.LENGTH_LONG).show();
			}
			else if(pass.getText().toString().trim().length()==0)
			{
				Toast.makeText(getApplicationContext(), "please enter the password", Toast.LENGTH_LONG).show();
			}
			else
			{
				try {
		        // Add your data
					
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(ServiceCollection.url+"login.php");
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("username",un.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("password",pass.getText().toString()));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					// Execute HTTP Post Request
					HttpResponse response = httpclient.execute(httppost);
					String result = ServiceCollection.parseStr(response);
					
					if(result.equalsIgnoreCase("001"))
					{
						//Log.d("synchname begin",loginpref.getString("synchuser",""));
						edit=loginpref.edit();
						edit.putString("username", un.getText().toString());
						edit.putString("loginstatus", "online");
						edit.commit();

						csync_username=loginpref.getString("synchusername", "n/a");
						//Log.d("synchusername",loginpref.getString("synchusername", "n/a"));
						if(csync_username.equalsIgnoreCase(loginpref.getString("username","")))
						{
							startActivity(new Intent(Login.this,Instruction.class));
							Toast.makeText(this, "Login success", Toast.LENGTH_LONG).show();	
						}
						else
						{
							//start activity for location synch preferences
							startActivity(new Intent(Login.this,PrefSynch.class));
							Log.d("sync","************synch************");
						}
					}
					else if(result.equalsIgnoreCase("002")){
						Toast.makeText(this, "Invalid username and password", Toast.LENGTH_LONG).show();
					}
					else if(result.equalsIgnoreCase("003")){
						Toast.makeText(this, "server query failed", Toast.LENGTH_LONG).show();
					}
					else if(result.equalsIgnoreCase("004")){
						Toast.makeText(this, "Please check mail, verify your user name and password", Toast.LENGTH_LONG).show();
					}
					else{
						//Toast.makeText(this,result,Toast.LENGTH_LONG).show();
						Toast.makeText(this,"server error",Toast.LENGTH_LONG).show();
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
				catch (NullPointerException e) {
				// TODO: handle exception
			    e.printStackTrace();
				}
				catch (Exception e) {
			    e.printStackTrace();
				}
			}//else 2 end
		}//else 1 end
	}//function onClick	

	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}

}//class