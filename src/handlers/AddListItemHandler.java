package handlers;

import android.os.Handler;
import android.os.Message;
import activities.TaxiServiceActivity;
import modules.TaxiContainer;
import modules.TaxiLocator;

public class AddListItemHandler extends Handler {

	private TaxiServiceActivity _tsa;
	
	public AddListItemHandler(TaxiServiceActivity tsa) {
		_tsa = tsa;
	}
	
	@Override
	public void handleMessage(Message msg) {
		TaxiContainer<String, String> result = new TaxiContainer<String, String>();
		result = msg.getData().getParcelable(TaxiLocator.JSON_STREAM);
		
		String newItem = result.entrySet().toString();
		
		_tsa.addItem(newItem);
	}
	
}
