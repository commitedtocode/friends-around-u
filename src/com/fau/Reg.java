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
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Reg extends Activity implements OnClickListener {
	EditText name,un,email,pass,mobile;
	Button reg;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        un=(EditText) findViewById(R.id.run);
        name=(EditText) findViewById(R.id.rname);
        email=(EditText) findViewById(R.id.remail);
        pass=(EditText) findViewById(R.id.rpass);
        mobile=(EditText) findViewById(R.id.rmob);
        reg=(Button) findViewById(R.id.regbtn);
        reg.setOnClickListener(this);	
    }//end function       
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(un.getText().toString().trim().length()==0)
		{
			Toast.makeText(Reg.this, "please enter the user name", Toast.LENGTH_LONG).show();
		}
		else if(name.getText().toString().trim().length()==0)
		{
			Toast.makeText(Reg.this, "please enter the name", Toast.LENGTH_LONG).show();
		}
		else if(email.getText().toString().trim().length()==0)
		{
			Toast.makeText(Reg.this, "please enter the email id", Toast.LENGTH_LONG).show();
		}
		else if(pass.getText().toString().trim().length()==0)
		{
			Toast.makeText(Reg.this, "please enter the password", Toast.LENGTH_LONG).show();
		}
		else if(mobile.getText().toString().trim().length()==0)
		{
			Toast.makeText(Reg.this, "please enter the mobile umber", Toast.LENGTH_LONG).show();
		}
		else
		{
			try {
		        // Add your data
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(ServiceCollection.url+"register.php");//10.202.56.40
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("username",un.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("name", name.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("email",email.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("password", pass.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("mobile", mobile.getText().toString()));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				String result = ServiceCollection.parseStr(response);
				
				if(result.trim().equalsIgnoreCase("001")){
					Toast.makeText(this, "username is not available", Toast.LENGTH_LONG).show();
				}
				else if(result.trim().equalsIgnoreCase("002")){
					Toast.makeText(this, "email id is not available", Toast.LENGTH_LONG).show();
				}
				else if(result.trim().equalsIgnoreCase("003")){
					Toast.makeText(this, "mobile number is not available", Toast.LENGTH_LONG).show();
				}
				else if(result.trim().equalsIgnoreCase("004")){
					Toast.makeText(this, "Registration completed successfully", Toast.LENGTH_LONG).show();
					startActivity(new Intent(Reg.this, Login.class));
				}
				else{
					Toast.makeText(this,result.trim(),Toast.LENGTH_LONG).show();
					Log.d("serve error", result);
					Toast.makeText(this,"server error",Toast.LENGTH_LONG).show();
				} 
			}
			catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
		    	e.printStackTrace();
			} catch (IOException e) {
		        // TODO Auto-generated catch block
				e.printStackTrace();
			}	
			catch(Exception e){
				Toast.makeText(this, "some error ocuured", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}//end onclick methos


	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		startActivity(new Intent(this,Login.class));
	}
	
}//calls end
