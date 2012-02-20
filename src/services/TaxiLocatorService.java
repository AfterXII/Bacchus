package services;

import modules.TaxiLocator;
import activities.TaxiServiceActivity;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

/*
 * Class: TaxiLocatorService
 * 
 * A non-sticky background service that runs a TaxiLocator on a background thread
 */
public class TaxiLocatorService extends Service {

	private Thread _t;
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("TaxiLocatorService", "Received intent to start");
		
		// This is the real location code, to be used for testing on devices (not emulators)        
		/*LocationManager locationManager = 
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		Location location = m.getLastKnownLocation(m.getBestProvider(crit, true));*/
		
		// Add mock location (near UB north campus)		
		Location location = new Location("gps");
		location.setLatitude(43.0);
		location.setLongitude(-78.7);
		
		if(intent != null) {
			// Unpack the TaxiServiceActivity handler
			Messenger messenger = (Messenger) intent.getExtras().get(TaxiServiceActivity.NEW_TAXI_HANDLER);
			
			// Only start a new thread if one isn't still running
			if(_t == null || !_t.isAlive()) {
				_t = new Thread(new TaxiLocator(messenger, location));
				_t.start();
			}
		}
		
		return START_NOT_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
