package com.projects.bacchus;

import services.TaxiLocatorService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.os.Messenger;
import handlers.*;

public class BacchusActivity extends Activity {

	public static final String TAXI_HANDLER = "NewTaxiHandler";

	public void startAddDrink(View v) {
		Log.v("BacchusActivity", "Starting add drink activity...");

		Intent intent = new Intent(this, AddDrinkActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Handler getTaxiServiceHandler = new GetTaxiServiceHandler();

		Intent locateIntent = new Intent(this, TaxiLocatorService.class);
		locateIntent.putExtra(this.TAXI_HANDLER, new Messenger(getTaxiServiceHandler));
		startService(locateIntent);
		Log.v("BacchusActivity", "Sent intent to taxi locator");

	}
}