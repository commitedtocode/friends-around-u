package com.fau.pref;

import com.fau.Main;
import com.fau.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Instruction extends Activity implements OnClickListener {
	Button location,network,account,tour;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instruction);
		location=(Button) findViewById(R.id.instlocationbtn);
		network=(Button) findViewById(R.id.instrnetwrkbtn);
		account=(Button) findViewById(R.id.instraccountbtn);
		tour=(Button) findViewById(R.id.instrtourbtn);
		
		location.setOnClickListener(this);
		network.setOnClickListener(this);
		account.setOnClickListener(this);
		tour.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.instlocationbtn:{
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
				break;
			}
			case R.id.instrnetwrkbtn:{
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				startActivity(intent);
				break;
			}
			case R.id.instraccountbtn:{
				Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
				startActivity(intent);
				break;
			
			}
			case R.id.instrtourbtn:{
				Intent intent = new Intent(Instruction.this,Main.class);
				startActivity(intent);
				break;
			
			}
		}
	}
}