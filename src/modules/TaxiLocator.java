package modules;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.ArrayList;
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

	public TaxiLocator(Messenger messenger, Location location) {
		_messenger = messenger;
		_location = location;
	}

	@Override
	public void run() {
		if(_messenger != null) {
			String json = this.getJson();
			Log.v("TaxiLocator", "Attaching results to messenger: " + json);
			Bundle bundle = new Bundle();
			ArrayList<String> attributeNames = new ArrayList<String>();
			attributeNames.add("vicinity");
			TaxiContainer<String, String> result = this.getPlaceResults(json, attributeNames);
			Log.v("TaxiLocator", "Results size: " + result.size());
			bundle.putParcelable(this.JSON_STREAM, result);
			try {
				Message msg = new Message();
				msg.setData(bundle);
				_messenger.send(msg);
			} catch (RemoteException e) {
				Log.e("TaxiLocator", "RemoteException: " + e);
			}
		}
	}

	private String buildURL(String baseURL, Map<String, String> attributes) {
		String url = baseURL;

		for(Map.Entry<String, String> e : attributes.entrySet()) {
			url += "&" + e.getKey() + "=" + e.getValue();
		}

		return url;
	}

	private TaxiContainer<String, String> getPlaceResults(String jsonData, ArrayList<String> innerAttributeNames) {
		TaxiContainer<String, String> result = null;
		JSONObject json;

		try {
			result = new TaxiContainer<String, String>();
			json = new JSONObject(jsonData);
			JSONArray array = json.getJSONArray(this.RESULTS);

			Log.v("TaxiLocator", "Array: " + array.toString());

			for(int i = 0; i < array.length(); i++) {
				JSONObject o = (JSONObject) array.get(i);

				Log.v("TaxiLocator", "Inner contents: " + o.toString());

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

				Log.v("TaxiLocator", "Build URL to grab phone #: " + url);

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

		return stream;
	}

	private String getJson() {
		String json = "";

		String baseURL = "https://maps.googleapis.com/maps/api/place/search/json?";

		double lat, longitude;
		lat = _location.getLatitude();
		longitude = _location.getLongitude();

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("location", lat + "," + longitude);
		attributes.put("radius", "16000");
		attributes.put("keyword", "taxi");
		attributes.put("sensor", "false");
		attributes.put("key", this.API_KEY);
		String url = this.buildURL(baseURL, attributes);

		Log.v("TaxiLocator", "Built URL: " + url);

		json = this.getHttpStream(url);

		Log.v("TaxiLocator", "Retrieved JSON stream: " + json);

		return json;
	}

}
