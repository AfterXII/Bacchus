package com.projects.bacchus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class DrinkScheduler extends Service implements Runnable {

	private Timer _timer;
	private Messenger _messenger;
	private int _delay;
	
	@Override
    public void onCreate() {
		// get perpetuated data
		// calculate remaining time
		// restart new timer
	}
	
	public DrinkScheduler(Messenger messenger) {
		_timer = new Timer();
		_messenger = messenger;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("DrinkScheduler", "Drink scheduler service started");
		
        _timer.schedule(new TimerTask() {
        	@Override
			public void run() {
        		Log.v("DrinkScheduler", "Timer started");
        	}
        }, _delay);
        
		return START_STICKY;
    }
	
	@Override
    public void onDestroy() {
		Log.v("DrinkScheduler", "Drink scheduler being destroyed");
		
		// get timestamp
		// get remaining time
		// perpetuate
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void run() {
		
	}

}
