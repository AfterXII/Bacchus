package activities;

import java.util.ArrayList;
import java.util.Map;
import modules.TaxiContainer;
import modules.TaxiLocator;
import com.projects.bacchus.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import services.TaxiLocatorService;
import android.content.Intent;
import android.os.Messenger;

/*
 * Class: TaxiServiceActivity
 * 
 * View results generated by the TaxiLocatorService background process
 */
public class TaxiServiceActivity extends ListActivity {
	public static final String NEW_TAXI = "NewTaxi";
	public static final String NEW_TAXI_HANDLER = "NewTaxiHandler";
	
	private ArrayList<String> _items;
	private ArrayAdapter<String> _adapter;
	
	/*
	 * Method: addItem
	 * Parameters
	 * 		String newItem: a string representing the item to be added to the ListView
	 * 
	 * Adds a new item to the ListView, and refreshes the adapter so the item is visible
	 */
	public void addItem(String newItem) {
		_items.add(newItem);
		_adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Let the activity know that _items holds the data for our ListView
		_items = new ArrayList<String>();
		_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, _items);
		
		// This handler allows TaxiLocatorService to pass messages back to our UI thread
		Handler addListItemHandler = new Handler() {
			/*
			 * Method: handleMessage
			 * Parameters:
			 * 		Message msg: contains a Bundle which holds a TaxiContainer full of nearby taxi services
			 * 
			 * Unpacks the Bundle into a new TaxiContainer and adds each item to the list
			 */
			@Override
			public void handleMessage(Message msg) {
				TaxiContainer result = msg.getData().getParcelable(TaxiLocator.JSON_STREAM);
				
				if(result.size() > 0) {
					for(Map.Entry<String, String> m : result.entrySet()) {
						String newItem = m.getKey() + ": " + m.getValue();
						addItem(newItem);
					}
		 		} else {
					addItem("No nearby taxis found.");
				}
			}
		};
		
		// Start the TaxiLocatorService in the background, and give it the handler
		Intent findTaxis = new Intent(this, TaxiLocatorService.class);
		findTaxis.putExtra(TaxiServiceActivity.NEW_TAXI_HANDLER, new Messenger(addListItemHandler));
		startService(findTaxis);
		
		// This is a ListActivity, so set our list adapter to reflect changes to _items
		setListAdapter(_adapter);
		
		setContentView(R.layout.taxiservices);
	}
}
