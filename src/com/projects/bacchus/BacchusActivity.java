package com.projects.bacchus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BacchusActivity extends Activity {
	
	public void startAddDrink(View v) {
		Log.v("BacchusActivity", "Starting add drink activity...");
	
		Intent intent = new Intent(this, AddDrinkActivity.class);
		startActivity(intent);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}