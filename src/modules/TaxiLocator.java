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

/*
 * Class: TaxiLocator
 * 
 * Finds nearby taxi services and sends them back to TaxiLocatorService in a TaxiContainer (this happens in "run")
 */
public class TaxiLocator implements Runnable {

	public static final String JSON_STREAM = "NewJsonStream";

	// Google Places API key
	public final static String API_KEY = "AIzaSyBdXAPJl6qkgF1BMAL9NPOszpG16P1E8vQ";

	private Messenger _messenger;
	private Location _location;
	
	private StringCacher _locationCache;
	private ObjectCacher<TaxiContainer> _taxiCache;

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
		_taxiCache = new ObjectCacher<TaxiContainer>(AbstractCacher.TAXI_SERVICES);
	}
	
	@Override
	public void run() {
		try {
			TaxiContainer sourceData = this.handleCachedData();
			_messenger.send(this.createMessage(sourceData));
		} catch (RemoteException e) {
			Log.e("TaxiLocator", "RemoteException: " + e);
		}

	}

	/*
	 * Method: handleCachedData
	 * 
	 * Check if we have cached location data. If we do, decide if we've gotten far enough away from the
	 * cached location to warrant additional HTTP requests and time. If so, generate new results. If not,
	 * re-inflate the TaxiContainer structure from local memory and return it.
	 * 
	 * Returns:
	 * 		A TaxiContainer with new results, if there is no cache or the user has moved past our tolerance.
	 * 		A cached TaxiContainer with existing results, otherwise.
	 */
	public TaxiContainer handleCachedData() {
		TaxiContainer data = null;

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

		if(_forceRefresh || diff > this.LOC_TOLERANCE || cachedLocationString == null) {
			Log.v("TaxiLocator", "Retrieving and caching local taxi services...");
			data = this.getPlaceResults();
			_taxiCache.doPersist(data);
		}
		else if(diff < this.LOC_TOLERANCE && cachedLocationString != null) {
			Log.v("TaxiLocator", "Retrieving cached taxi services...");
			data = (TaxiContainer) _taxiCache.readData();
		}

		String saveLocation = _location.getLatitude() + "," + _location.getLongitude();
		_locationCache.doPersist(saveLocation);

		return data;
	}

	/*
	 * Method: createMessage
	 * Parameters:
	 * 		Object o: an object to be bundled with the message
	 * 
	 * Returns: a message that can be passed across threads, bundled with o if it was a TaxiContainer
	 */
	private Message createMessage(Object o) {
		Message msg = new Message();
		Bundle bundle = new Bundle();

		if(o instanceof TaxiContainer) {
			bundle.putParcelable(TaxiLocator.JSON_STREAM, (TaxiContainer) o);
		} else {
			Log.e("TaxiLocator", "TaxiLocator cannot bundle type " + o.getClass());
		}

		msg.setData(bundle);

		return msg;
	}

	/*
	 * Method: getPlaceResults
	 * Parameters:
	 * 		String jsonData: the JSON returned by the Google Places API
	 * 
	 * Takes the JSON returned by the Google Places API, and requests additional information (formatted
	 * address and phone number) from the API using the returned "reference ID."
	 * 
	 * Returns:
	 * 		A new TaxiContainer containing name=>data pairs, where data is an address and phone number
	 * 
	 * NOTE: radius is in meters - 16,000 ~= 10mi
	 */
	private TaxiContainer getPlaceResults() {
		TaxiContainer result = null;
		JSONObject json;
		
		double latitude, longitude;
		latitude = _location.getLatitude();
		longitude = _location.getLongitude();

		String baseURL = "https://maps.googleapis.com/maps/api/place/search/json?";
		Map<String, String> searchAttributes = new HashMap<String, String>();
		searchAttributes.put("location", latitude + "," + longitude);
		searchAttributes.put("radius", "16000");
		searchAttributes.put("keyword", "taxi");
		searchAttributes.put("sensor", "false");
		searchAttributes.put("key", this.API_KEY);
		String searchURL = WebUtils.buildURL(baseURL, searchAttributes);

		String jsonData = WebUtils.getHttpStream(searchURL);

		try {
			result = new TaxiContainer();
			json = new JSONObject(jsonData);
			JSONArray array = json.getJSONArray("results");

			for(int i = 0; i < array.length(); i++) {
				JSONObject o = (JSONObject) array.get(i);

				String name = o.getString("name");
				String ref = o.getString("reference");

				// Will hold the attributes - address & phone #
				String values = "";

				/*String detailBaseURL = "https://maps.googleapis.com/maps/api/place/details/json?";
				Map<String, String> detailAttributes = new HashMap<String, String>();
				detailAttributes.put("reference", ref);
				detailAttributes.put("sensor", "true");
				detailAttributes.put("key", this.API_KEY);
				String detailURL = WebUtils.buildURL(detailBaseURL, detailAttributes);

				JSONObject phoneJson = new JSONObject(WebUtils.getHttpStream(detailURL));
				Log.v("TaxiLocator", phoneJson.toString());
				if(phoneJson.has("result")) {
					JSONObject phoneObj = phoneJson.getJSONObject("result");

					String formattedPhone = phoneObj.getString("formatted_phone_number");
					String formattedAddress = phoneObj.getString("formatted_address");

					// Don't add in the area code... these are all local					
					values = formattedAddress + " (" + formattedPhone.substring(6) + ")";
				}*/

//				result.put(name, values);
				result.put(name, ref);
			}
		} catch (JSONException e) {
			Log.e("TaxiLocator", "JSONException: " + e);
		}

		return result;
	}

}
