package com.fau;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import com.fau.pref.Instruction;

public class PrefSynch extends Activity implements OnClickListener {
	Button done;
	SharedPreferences sp_synchpref;
	RadioButton synch,dontsynch;
	SharedPreferences.Editor spedit;
	String username;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synchpref);
		done=(Button) findViewById(R.id.btndone);
		synch=(RadioButton) findViewById(R.id.radio1);
		dontsynch=(RadioButton) findViewById(R.id.radio2);
		sp_synchpref=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		spedit=sp_synchpref.edit();
		username=sp_synchpref.getString("username", "");
		done.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.btndone)
		{
			if(synch.isChecked()==true)
			{
				//start synchronization service
				usercommit();
				Intent intent=new Intent(PrefSynch.this,ContactSynch.class);
				intent.putExtra("username",username);
				startActivity(intent);
			}
			else
			{
				usercommit();
				startActivity(new Intent(PrefSynch.this,Instruction.class));
			}
		}
	}

	private void usercommit() {
		// TODO Auto-generated method stub
		spedit.putString("synchusername",username);
		spedit.commit();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
	}

}
