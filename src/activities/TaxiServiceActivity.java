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

public class TaxiServiceActivity extends ListActivity {
	public static final String NEW_TAXI = "NewTaxi";
	public static final String NEW_TAXI_HANDLER = "NewTaxiHandler";
	
	private ArrayList<String> _items;
	private ArrayAdapter<String> _adapter;
	
	public void addItem(String newItem) {
		_items.add(newItem);
		_adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		_items = new ArrayList<String>();
		
		_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, _items);
		
		Handler addListItemHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				TaxiContainer result = new TaxiContainer();
				result = msg.getData().getParcelable(TaxiLocator.JSON_STREAM);
				
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
		
		Intent findTaxis = new Intent(this, TaxiLocatorService.class);
		findTaxis.putExtra(TaxiServiceActivity.NEW_TAXI_HANDLER, new Messenger(addListItemHandler));
		startService(findTaxis);
		
		setListAdapter(_adapter);
		
		setContentView(R.layout.taxiservices);
	}
}
