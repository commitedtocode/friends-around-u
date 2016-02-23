package com.fau;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.fau.pref.Instruction;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

public class ContactSynch extends Activity {
	ContentResolver cr;
	Cursor cur;
	Cursor pCur;
	String username,synchresponse;
	ArrayList<String> num_list,name_list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactsynch);
		num_list=new ArrayList<String>();
	    name_list=new ArrayList<String>();
	    username=getIntent().getStringExtra("username");
	    new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Looper.prepare();
				try{
			    	cr = getContentResolver();
			    	cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			    	if (cur.getCount() > 0) 
			    	{
			    		while (cur.moveToNext()) 
			    		{
			    			String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
			    			String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			    			if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
			    			{
			    				pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{id}, null);
			    				while (pCur.moveToNext()) 
			    				{
			    					String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			    					name_list.add(name);
			    					num_list.add(phoneNo);
			    				}
			    			}
			    		}
			    		cur.close();
			    		pCur.close(); 
			    		send();
			    		Log.d("contact Name",name_list.toString());
			    		Log.d("contact number",num_list.toString());
			    	}
			    }//try
			    catch(Exception e){
			    	Log.d("exception occured",e.toString());
			    }
			}
		}).start();
	    startActivity(new Intent(ContactSynch.this,Instruction.class));
	}
	
	private void send() 
	{
		 try {
			 // Add your data
			 HttpClient httpclient = new DefaultHttpClient();
			 HttpPost httppost = new HttpPost(ServiceCollection.url+"contact.php");
			 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			 nameValuePairs.add(new BasicNameValuePair("username",username));
			 nameValuePairs.add(new BasicNameValuePair("contact",num_list.toString()));
			 httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			 
			 // Execute HTTP Post Request
			 HttpResponse response=httpclient.execute(httppost);
			 synchresponse=ServiceCollection.parseStr(response);
			 Log.d("contact result",synchresponse);
			 //String result = ServiceCollection.parseStr(response);
		 }
		 catch (ClientProtocolException e) {
			 // TODO Auto-generated catch block
		    	
		 } 
		 catch (IOException e) {
			 Log.d("contact error","contact io erroe");
			 // TODO Auto-generated catch block
		 }
		 catch(Exception e){
				Toast.makeText(this, "some error ocuured", Toast.LENGTH_LONG).show();
		 }
	}//end send

}
