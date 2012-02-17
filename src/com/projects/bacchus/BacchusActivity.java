package com.projects.bacchus;

import activities.TaxiServiceActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BacchusActivity extends Activity {

	public static final String TAXI_HANDLER = "NewTaxiHandler";
	
	public void findTaxis(View v) {
		Log.v("BacchusActivity", "Starting taxi finder activity...");
		
		Intent intent = new Intent(this, TaxiServiceActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
}