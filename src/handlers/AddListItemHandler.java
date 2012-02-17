package handlers;

import java.util.Map;

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
		
		if(result.size() > 0) {
			for(Map.Entry<String, String> m : result.entrySet()) {
				String newItem = m.getKey() + ": " + m.getValue();
				_tsa.addItem(newItem);
			}
 		} else {
			_tsa.addItem("No nearby taxis found.");
		}
	}
	
}
