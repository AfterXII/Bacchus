package com.projects.bacchus;

import modules.TaxiContainer;
import modules.TaxiLocator;
import services.TaxiLocatorService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.os.Messenger;

public class BacchusActivity extends Activity {

	public static final String TAXI_HANDLER = "NewTaxiHandler";

	private Handler _getTaxiServicesHandler;

	public void startAddDrink(View v) {
		Log.v("BacchusActivity", "Starting add drink activity...");

		Intent intent = new Intent(this, AddDrinkActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		_getTaxiServicesHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				TaxiContainer<String, String> result = new TaxiContainer<String, String>();
				result = msg.getData().getParcelable(TaxiLocator.JSON_STREAM);
				Log.v("BacchusActivity", "Taxi results received: " + result.entrySet().toString());
			}
		};

		Intent locateIntent = new Intent(this, TaxiLocatorService.class);
		locateIntent.putExtra(this.TAXI_HANDLER, new Messenger(_getTaxiServicesHandler));
		startService(locateIntent);
		Log.v("BacchusActivity", "Sent intent to taxi locator");

	}
}