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
	
	private static final String API_KEY = "AIzaSyBdXAPJl6qkgF1BMAL9NPOszpG16P1E8vQ";
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
		String ret = baseURL;
		
		for(Map.Entry<String, String> e : attributes.entrySet()) {
			ret += "&" + e.getKey() + "=" + e.getValue();
		}
		
		return ret;
	}
	
	private TaxiContainer<String, String> getPlaceResults(String jsonData, ArrayList<String> innerAttributeNames) {
		TaxiContainer<String, String> ret = null;
		
		JSONObject json;
		try {
			ret = new TaxiContainer<String, String>();
			json = new JSONObject(jsonData);
			JSONArray array = json.getJSONArray("results");
			for(int i = 0; i < array.length(); i++) {
				for(String a : innerAttributeNames) {
					String s = array.getJSONObject(i).getString(a);
					if(s != null) {
						ret.put(a, s);
					}
				}
			}
		} catch (JSONException e) {
			Log.e("TaxiLocator", "JSONException: " + e);
		}
		
		return ret;
	}
	
	private String getJson() {
		String ret = "";
		
		String baseURL = "https://maps.googleapis.com/maps/api/place/search/json?";
		
		double lat, longitude;
		lat = _location.getLatitude();
		longitude = _location.getLongitude();
		
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("location", lat + "," + longitude);
		attributes.put("radius", "50000");
		attributes.put("keyword", "taxi");
		attributes.put("sensor", "false");
		attributes.put("key", this.API_KEY);
		
		String url = this.buildURL(baseURL, attributes);
		
		Log.v("TaxiLocator", "Built URL: " + url);
		
		try {
			URL google = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) google.openConnection();
			connection.setDoOutput(true);
			DataInputStream in = new DataInputStream(connection.getInputStream());
			String add = "";
			while(add != null) {
				add = in.readLine();
				ret += add;
			}
		} catch(MalformedURLException e) {
			Log.e("TaxiLocator", "MalformedURLException: " + e);
		} catch (IOException e) {
			Log.e("TaxiLocator", "IOException: " + e);
		}
		
		Log.v("TaxiLocator", "Retrieved JSON stream: " + ret);
		return ret;
	}

}
