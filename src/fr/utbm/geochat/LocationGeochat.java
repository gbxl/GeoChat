package fr.utbm.geochat;

import java.io.Serializable;

/**
 * Classe de Donnée utilisé pour la geolocalisation
 * 
 *
 */
public class LocationGeochat implements Serializable{
	
	private static final long serialVersionUID = -2063581148183391537L;
	private double lat;
	private double lon;
	private String provider;
	
	public LocationGeochat(double lat, double lon, String provider) {
		this.lat=lat;
		this.lon=lon;
	}
	
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

}
