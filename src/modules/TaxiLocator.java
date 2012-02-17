package modules;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import caching.AbstractCacher;
import caching.ObjectCacher;
import caching.StringCacher;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import modules.WebUtils;

public class TaxiLocator implements Runnable {

	public static final String JSON_STREAM = "NewJsonStream";

	private final String API_KEY = "AIzaSyBdXAPJl6qkgF1BMAL9NPOszpG16P1E8vQ";

	private Messenger _messenger;
	private Location _location;
	
	private StringCacher _locationCache;
	private ObjectCacher<TaxiContainer<String,String>> _taxiCache;

	private final double LOC_TOLERANCE = 5;

	private boolean _forceRefresh;

	public TaxiLocator(Messenger messenger, Location location) {
		this(messenger, location, false);
	}

	public TaxiLocator(Messenger messenger, Location location, boolean forceRefresh) {
		_messenger = messenger;
		_location = location;
		_forceRefresh = forceRefresh;
		
		_locationCache = new StringCacher(AbstractCacher.LOCATION);
		_taxiCache = new ObjectCacher<TaxiContainer<String,String>>(AbstractCacher.TAXI_SERVICES);
	}
	
	@Override
	public void run() {
		try {
			TaxiContainer<String, String> sourceData = this.handleCachedData();
			_messenger.send(this.createMessage(sourceData));
		} catch (RemoteException e) {
			Log.e("TaxiLocator", "RemoteException: " + e);
		}

	}

	public TaxiContainer<String, String> handleCachedData() {
		TaxiContainer<String, String> data = null;

		double diff = 0;

		String cachedLocationString = _locationCache.readData();

		if(!_forceRefresh) {
			if(cachedLocationString != null) {
				Log.v("TaxiLocator", "Persisted location discovered...");

				Location oldLocation = new Location("gps");
				String[] values = cachedLocationString.split(",");
				oldLocation.setLatitude(Double.valueOf(values[0]));
				oldLocation.setLongitude(Double.valueOf(values[1]));

				Log.v("TaxiLocator", "Current latitude: " + _location.getLatitude());
				Log.v("TaxiLocator", "Current longitude: " + _location.getLongitude());
				Log.v("TaxiLocator", "Cached latitude: " + oldLocation.getLatitude());
				Log.v("TaxiLocator", "Cached longitude: " + oldLocation.getLongitude());

				// Check if we've moved far enough away to merit more HTTP requests
				diff = 
						( Math.abs(_location.getLatitude()) - Math.abs(oldLocation.getLatitude()) ) +
						( Math.abs(_location.getLongitude()) - Math.abs(oldLocation.getLongitude()) );
			}
		}

		Log.v("TaxiLocator", "Diff is " + diff);

		if(_forceRefresh || (diff > this.LOC_TOLERANCE || cachedLocationString == null)) {
			Log.v("TaxiLocator", "Retrieving and caching local taxi services...");
			String json = this.getJson();
			data = this.getPlaceResults(json);
			_taxiCache.doPersist(data);
		}
		else if(diff < this.LOC_TOLERANCE && cachedLocationString != null) {
			Log.v("TaxiLocator", "Retrieving cached taxi services...");
			data = (TaxiContainer<String, String>) _taxiCache.readData();
		}

		String saveLocation = _location.getLatitude() + "," + _location.getLongitude();
		_locationCache.doPersist(saveLocation);

		return data;
	}

	private Message createMessage(Object o) {
		Message msg = new Message();
		Bundle bundle = new Bundle();

		if(o instanceof TaxiContainer) {
			bundle.putParcelable(this.JSON_STREAM, (TaxiContainer<?, ?>) o);
		} else {
			Log.e("TaxiLocator", "TaxiLocator cannot bundle type " + o.getClass());
		}

		msg.setData(bundle);

		return msg;
	}

	private TaxiContainer<String, String> getPlaceResults(String jsonData) {
		TaxiContainer<String, String> result = null;
		JSONObject json;

		try {
			result = new TaxiContainer<String, String>();
			json = new JSONObject(jsonData);
			JSONArray array = json.getJSONArray("results");

			for(int i = 0; i < array.length(); i++) {
				JSONObject o = (JSONObject) array.get(i);

				String name = o.getString("name");
				String ref = o.getString("reference");

				// Will hold the attributes - address & phone #
				String values = "";

				String baseURL = "https://maps.googleapis.com/maps/api/place/details/json?";
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("reference", ref);
				attributes.put("sensor", "true");
				attributes.put("key", this.API_KEY);
				String url = WebUtils.buildURL(baseURL, attributes);

				JSONObject phoneJson = new JSONObject(WebUtils.getHttpStream(url));
				Log.v("TaxiLocator", phoneJson.toString());
				if(phoneJson.has("result")) {
					JSONObject phoneObj = phoneJson.getJSONObject("result");

					String formattedPhone = phoneObj.getString("formatted_phone_number");
					String formattedAddress = phoneObj.getString("formatted_address");

					// Don't add in the area code... these are all local					
					values = formattedAddress + " (" + formattedPhone.substring(6) + ")";
				}

				result.put(name, values);
			}
		} catch (JSONException e) {
			Log.e("TaxiLocator", "JSONException: " + e);
		}

		return result;
	}

	private String getJson() {
		double lat, longitude;
		lat = _location.getLatitude();
		longitude = _location.getLongitude();

		String baseURL = "https://maps.googleapis.com/maps/api/place/search/json?";
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("location", lat + "," + longitude);
		attributes.put("radius", "16000");
		attributes.put("keyword", "taxi");
		attributes.put("sensor", "false");
		attributes.put("key", this.API_KEY);
		String url = WebUtils.buildURL(baseURL, attributes);

		String json = WebUtils.getHttpStream(url);

		Log.v("TaxiLocator", "Got JSON: " + json);

		return json;
	}

}
