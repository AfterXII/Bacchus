package activities;

import java.util.ArrayList;

import com.projects.bacchus.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import handlers.AddListItemHandler;
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
		
		Handler addListItemHandler = new AddListItemHandler(this);
		Intent findTaxis = new Intent(this, TaxiLocatorService.class);
		findTaxis.putExtra(this.NEW_TAXI_HANDLER, new Messenger(addListItemHandler));
		startService(findTaxis);
		
		setListAdapter(_adapter);
		
		setContentView(R.layout.taxiservices);
	}
}
