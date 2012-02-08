package com.projects.bacchus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class AddDrinkActivity extends Activity {
	
	public void goHome(View v) {
		Log.v("BacchusActivity", "Going home");
		
		Intent intent = new Intent(this, BacchusActivity.class);
		startActivity(intent);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adddrink);
    }
}
