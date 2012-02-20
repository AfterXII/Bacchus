package activities;

import com.projects.bacchus.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/*
 * Class: BacchusActivity
 * 
 * This is the entry point for Bacchus - the main UI.
 */
public class BacchusActivity extends Activity {

	public static final String TAXI_HANDLER = "NewTaxiHandler";

	/*
	 * Method: findTaxis
	 * Parameters
	 * 		View v: the current view - in this case, the main UI.
	 * 
	 * Starts the taxi location activity to view nearby taxi services.
	 */
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