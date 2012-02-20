package modules;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import constants.Constants;

import structures.TaxiContainer;
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

	private Messenger _messenger;
	private Location _location;
	private boolean _forceRefresh;
	
	private StringCacher _locationCache;
	private ObjectCacher<TaxiContainer> _taxiCache;

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

		// If we're not forcing HTTP requests, and there is a cached location present,
		// find out how far the user has moved since the last cache.
		if(!_forceRefresh) {
			if(cachedLocationString != null) {
				Log.v("TaxiLocator", "Persisted location discovered...");

				Location oldLocation = new Location("gps");
				String[] values = cachedLocationString.split(",");
				oldLocation.setLatitude(Double.valueOf(values[0]));
				oldLocation.setLongitude(Double.valueOf(values[1]));

				// Check if we've moved far enough away to merit more HTTP requests
				diff = 
						( Math.abs(_location.getLatitude()) - Math.abs(oldLocation.getLatitude()) ) +
						( Math.abs(_location.getLongitude()) - Math.abs(oldLocation.getLongitude()) );
			}
		}

		Log.v("TaxiLocator", "Diff is " + diff);

		// If we're forcing HTTP requests, or have exceeded the movement tolerance, or simply have not
		// cached any locations, run the HTTP requests to get local taxi services
		if(_forceRefresh || diff > Constants.Numbers.LOCATION_TOLERANCE || cachedLocationString == null) {
			Log.v("TaxiLocator", "Retrieving and caching local taxi services...");
			data = this.getPlaceResults();
			_taxiCache.doPersist(data);
		}
		
		// If we're within tolerance, and there is a cached location, load the cached TaxiContainer
		else if(diff < Constants.Numbers.LOCATION_TOLERANCE && cachedLocationString != null) {
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
			bundle.putParcelable(Constants.Handlers.JSON_STREAM, (TaxiContainer) o);
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
	 * 		A new TaxiContainer containing name=>refid pairs, where refid is the reference number 
	 * 		used to get details from the places API
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
		searchAttributes.put("key", Constants.API.PLACES_API_KEY);
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

				result.put(name, ref);
			}
		} catch (JSONException e) {
			Log.e("TaxiLocator", "JSONException: " + e);
		}

		return result;
	}

}
