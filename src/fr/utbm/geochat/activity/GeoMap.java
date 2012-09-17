package fr.utbm.geochat.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

/**
 * Classe permettant d'afficher une carte qui contiendra l'ensemble des geolocalisations disponible.
 * Il sera possible d'interagir avec les utilisateurs directement à partir de leur point de localisation.
 * 
 *
 */
public class GeoMap extends MapActivity {

	private MapView mapView;
	private MapController mapController;
	private Map<String, Location> listLocations;
	String user;
	//private Map<String,GeoPoint> listGeoPoints;
	//private List<OverlayItem> listOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_map);
		
		addAllUsers();
		
		mapView = (MapView) this.findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		
		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		LisItimizedOverlay itemizedoverlay = new LisItimizedOverlay(drawable,this);
		
		GeoPoint geoPoint, mainGeoPoint = null;
		Set<String> listUsers =  new HashSet<String>(listLocations.keySet());
		List<Location> listLocs = new ArrayList<Location>(listLocations.values());
		Iterator<Location> it = listLocs.iterator();
		Iterator<String> it2 = listUsers.iterator();
		
		
		while(it.hasNext() && it2.hasNext()) {
			Location l = it.next();
			String s = it2.next();
			geoPoint = new GeoPoint((int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6));
			//listGeoPoints.put(s,geoPoint);
			OverlayItem overlayitem = new OverlayItem(geoPoint, "", s);
			itemizedoverlay.addOverlayItem(overlayitem);
			
			if(s.equals(user)) {
				mainGeoPoint = geoPoint;
				Drawable icon = getResources().getDrawable(R.drawable.gmap_marker);
				icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
				overlayitem.setMarker(icon);
			}
			else
			{
				Drawable icon = getResources().getDrawable(R.drawable.gmap_marker_all);
				icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
				overlayitem.setMarker(icon);
			}
		}
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.add(itemizedoverlay);
		
		try {
			mapController = mapView.getController();
			mapController.setCenter(mainGeoPoint);
			mapController.setZoom(10);
		}catch (Exception e) {
		}
	}

	public void addAllUsers() {
		if(GeoChat.getInstance().getClient()!=null) {
			user = GeoChat.getInstance().getClient().getUsername();
			listLocations = new HashMap<String,Location>(GeoChat.getInstance().getClient().getLocations());
			Location l = GeoChat.getInstance().getClient().getMyLocation();
			if(l!=null) {
				listLocations.put(user, l);
			}
		}
		else if(GeoChat.getInstance().getHost()!=null) {
			user = GeoChat.getInstance().getHost().getUsername();
			listLocations = new HashMap<String,Location>(GeoChat.getInstance().getHost().getLocations());
			Location l = GeoChat.getInstance().getHost().getMyLocation();
			if(l!=null) {
				listLocations.put(user, l);
			}
		}
	}
	
	protected boolean isRouteDisplayed() 
	{
		return false;
	}
}
