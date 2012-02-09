package services;

import modules.TaxiLocator;

import com.projects.bacchus.BacchusActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class TaxiLocatorService extends Service {

	private Thread _t;
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("TaxiLocatorService", "Received intent to start");
        
		LocationManager m = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// This is the real location code, to be used for testing on devices (not emulators)
//		Criteria crit = new Criteria();
//		crit.setAccuracy(Criteria.ACCURACY_FINE);
//		Location location = m.getLastKnownLocation(m.getBestProvider(crit, true));
		
		// Add mock location (near UB north campus)		
		Location loc = new Location("gps");
		loc.setLatitude(43.0);
		loc.setLongitude(-78.7);
		
		if(intent != null) {
			Messenger messenger = (Messenger) intent.getExtras().get(BacchusActivity.TAXI_HANDLER);
			
			if(_t == null || !_t.isAlive()) {
				_t = new Thread(new TaxiLocator(messenger, loc));
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
