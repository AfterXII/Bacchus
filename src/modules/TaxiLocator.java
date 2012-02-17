package modules;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class TaxiLocator implements Runnable {

	public static final String JSON_STREAM = "NewJsonStream";

	private final String API_KEY = "AIzaSyBdXAPJl6qkgF1BMAL9NPOszpG16P1E8vQ";
	private final String RESULTS = "results";

	private Messenger _messenger;
	private Location _location;
	
	private DataCacher _locationCache;
	private DataCacher _taxiCache;

	private final double LOC_TOLERANCE = 10.0;

	public TaxiLocator(Messenger messenger, Location location) {
		_messenger = messenger;
		
		_location = location;
		
		_locationCache = new DataCacher(DataCacher.LOCATION);
		_taxiCache = new DataCacher(DataCacher.TAXI_SERVICES);
	}

	@Override
	public void run() {
		TaxiContainer<String, String> sourceData = null;

		double diff = 0.0;

		String cachedLocationString = _locationCache.getLine();
		
		if(cachedLocationString != null) {
			Log.v("TaxiLocator", "Persisted location discovered...");
			
			Location oldLocation = new Location(cachedLocationString);
			
			// Check if we've moved far enough away to merit more HTTP requests
			diff = 
					( Math.abs(_location.getLatitude()) - Math.abs(oldLocation.getLatitude()) ) +
					( Math.abs(_location.getLongitude()) - Math.abs(oldLocation.getLongitude()) );
		}

		Log.v("TaxiLocator", "Diff is " + diff);

		if(diff > this.LOC_TOLERANCE || cachedLocationString == null) {
			Log.v("TaxiLocator", "Retrieving and caching local taxi services...");
			String json = this.getJson();
			sourceData = this.getPlaceResults(json);
			_taxiCache.doPersist(sourceData);
		}
		else if(diff < this.LOC_TOLERANCE && cachedLocationString != null) {
			Log.v("TaxiLocator", "Retrieving cached taxi services...");
			sourceData = (TaxiContainer<String, String>) _taxiCache.getObject();
		}

		try {
			_messenger.send(this.createMessage(sourceData));
		} catch (RemoteException e) {
			Log.e("TaxiLocator", "RemoteException: " + e);
		}
		
		String saveLocation = _location.getLatitude() + "," + _location.getLongitude();
		_locationCache.writeLine(saveLocation);

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
			JSONArray array = json.getJSONArray(this.RESULTS);

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
				String url = this.buildURL(baseURL, attributes);

				JSONObject phoneJson = new JSONObject(this.getHttpStream(url));
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

	private String buildURL(String baseURL, Map<String, String> attributes) {
		String url = baseURL;

		boolean appendAmp = false;

		for(Map.Entry<String, String> e : attributes.entrySet()) {
			if(appendAmp) {
				url += "&";
			}
			url += e.getKey() + "=" + e.getValue();

			appendAmp = true;
		}

		Log.v("TaxiLocator", "Built URL: " + url);

		return url;
	}

	private String getHttpStream(String url) {
		String stream = "";

		URL urlObj;
		try {
			urlObj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setDoOutput(true);
			DataInputStream in = new DataInputStream(connection.getInputStream());
			String add = "";
			while(add != null) {
				add = in.readLine();
				stream += add;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.v("TaxiLocator", "Got HTTP Stream: " + stream);

		return stream;
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
		String url = this.buildURL(baseURL, attributes);

		String json = getHttpStream(url);

		Log.v("TaxiLocator", "Got JSON: " + json);

		return json;
	}

}
