package fr.utbm.geochat.host;

import fr.utbm.geochat.GeoChat;
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
 * Service de géolocalisation lié au serveur
 * 
 *
 */
public class HostGeolocalisationService  extends Service {
	
	private LocationManager locationManager = null;
	private float distance = 0;
	private Long time = (long) 60000;
	private String bestProvider;
	
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
		try {
			GeoChat.getInstance().getHost().setMyLocation(location);
			GeoChat.getInstance().getHost().addMyLocationToList(location);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			launchMyService();
		}
		catch (Exception e) {
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
