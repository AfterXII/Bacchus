package handlers;

import modules.TaxiContainer;
import modules.TaxiLocator;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetTaxiServiceHandler extends Handler {
	@Override
	public void handleMessage(Message msg) {
		TaxiContainer<String, String> result = new TaxiContainer<String, String>();
		result = msg.getData().getParcelable(TaxiLocator.JSON_STREAM);
		Log.v("BacchusActivity", "Taxi results received: " + result.entrySet().toString());
	}
}
