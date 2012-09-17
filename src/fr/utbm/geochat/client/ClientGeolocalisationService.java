package fr.utbm.geochat.client;

import java.io.Serializable;

import fr.utbm.geochat.Connection;
import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.LocationGeochat;
import fr.utbm.geochat.TCPCommandType;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
 
/**
 * Service de géolocalisation lié aux client
 * 
 *
 */
public class ClientGeolocalisationService extends Service {
	
	private LocationManager locationManager = null;
	private float distance = 0;
	private Long time = (long) 60000;
	private String bestProvider;
	private Connection clientConnection;
	
	private LocationListener onLocationChange = new LocationListener() {
		
		 public void onStatusChanged(String provider, int status, Bundle extras)
		 {
		 }
		 
		 public void onProviderEnabled(String provider)
		 {
		 }
		 
		 public void onProviderDisabled(String provider)
		 {
		 }
		 
		 public void onLocationChanged(Location location)
		 {
			 sendData(location);
		 }
		 
		
	};
 
	private void sendData(Location location) {
		GeoChat.getInstance().getClient().setMyLocation(location);
		LocationGeochat l = new LocationGeochat(location.getLatitude(), location.getLongitude(), location.getProvider());
		clientConnection.write(GeoChat.getInstance().getClient().getUsername(),TCPCommandType.SEND_LOCATION, (Serializable) l);
	}
	
	public IBinder onBind(Intent arg0) {
		return null;
	}
	 
	public void onCreate() {
		super.onCreate();
		 	
	}
 
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			time = new Long(GeoChat.getInstance().getTimeGps())*1000;
			clientConnection = GeoChat.getInstance().getClient().getConnection();
			launchMyService();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	 
	public void onDestroy() {
		super.onDestroy();
		try{
			locationManager.removeUpdates(onLocationChange);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void launchMyService() {

		try {
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_COARSE);
			c.setAltitudeRequired(false);
			c.setBearingRequired(false);
			c.setSpeedRequired(false);
			c.setCostAllowed(true);
			c.setPowerRequirement(Criteria.POWER_HIGH);

			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			bestProvider = locationManager.getBestProvider(c, true);
			
			if(bestProvider.equals(LocationManager.NETWORK_PROVIDER) || bestProvider.equals(LocationManager.GPS_PROVIDER))
			{
				try {
					Location location = locationManager.getLastKnownLocation(bestProvider);
					if(location != null) {
						sendData(location);
					}
					locationManager.requestLocationUpdates(bestProvider, time, distance, onLocationChange);
				}
				catch (IllegalArgumentException e) {
					Context context = getApplicationContext();
					CharSequence text = "No providers are available";
					int duration = Toast.LENGTH_SHORT;
	
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
			else {
				Context context = getApplicationContext();
				CharSequence text = "No providers are available";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		}
		catch (Exception e) {
		}
	}

}