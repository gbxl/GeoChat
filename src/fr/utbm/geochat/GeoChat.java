package fr.utbm.geochat;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import fr.utbm.geochat.client.Client;
import fr.utbm.geochat.host.Host;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Classe de l'application
 * @author Bfrost
 *
 */
public class GeoChat extends Application {

	private static GeoChat instance;
	private SharedPreferences prefs;
	private Client client = null;
	private Host host =  null;
	public enum ProfileType { Client, Host };

	public final static int LOGIN_CHANNEL_SUCCES_HANDLE = 1;
	public final static int LOGIN_CHANNEL_FAIL_HANDLE = 2;
	public final static int POPUP_LOGIN_CHANNEL_HANDLE = 3;
	
	public final static int FILE_FAIL_HANDLE = 4;
	public final static int FILE_REQUEST_HANDLE = 5;
	public final static int FILE_DOWNLOADED_HANDLE = 6;
	
	public final static int LAUNCH_MAP_HANDLE = 7;
	
	public final static int CONNECTION_REFUSED = 8;
	public final static int CONNECTION_ACCEPTED = 9;
	
	public final static int VIBRATION_ON = 10;
	
	/**
	 * Singleton de notre classe
	 * @return
	 */
	public static GeoChat getInstance() {
		return instance;
	}

	/**
	 * Récuperer le client de l'application
	 * @return
	 */
	public Client getClient() {
		if (client == null) {
			GeoChat.appendLog("MissCall", "The application doesn't run as a Client");
		}
		return client;
	}

	/**
	 * Récuperer le serveur de l'application
	 * @return
	 */
	public Host getHost() {
		if (host == null) {
			GeoChat.appendLog("MissCall", "The application doesn't run as a Host");
		}
		return host;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		instance = this;
	}
	
	public String getIpAddress() {
		return prefs.getString("ip", "");
	}

	private void setIpAddress(String ipAddress) {
		Editor ed = prefs.edit();
		ed.putString("ip", ipAddress);
		ed.commit();
		appendLog("IP", "IP adress set to " + ipAddress);
	}
	
	public int getTimeGps() {
		return prefs.getInt("gpsTime", 60);
	}

	public void setTimeGps(int time) {
		Editor ed = prefs.edit();
		ed.putInt("gpsTime", time);
		ed.commit();
		appendLog("GPSTIME", "gpsTime is set to " + time);
	}
	
	/**
	 * Test si la puce gps est activée
	 * @return
	 */
	public boolean isGeolocalisationActivate() {
		return prefs.getBoolean("isGpsActivate", false);
	}
	
	public void setGeolocalisationActivate(boolean activation) {
		Editor ed = prefs.edit();
		ed.putBoolean("isGpsActivate", activation);
		ed.commit();
		appendLog("ISGPSACTIVATE", "ISGPSACTIVATE is set to " + activation);
	}
	
	public int getPort() {
		return prefs.getInt("port", 9000);
	}

	private void setPort(int port) {
		Editor ed = prefs.edit();
		ed.putInt("port", port);
		ed.commit();
		appendLog("PORT", "PORT server set to " + port);
	}
	
	public String getUsername() {
		return prefs.getString("username", android.os.Build.MODEL);
	}

	private void setUsername(String username) {
		Editor ed = prefs.edit();
		ed.putString("username", username);
		ed.commit();
		appendLog("USERNAME", "USERNAME is set to " + username);
	}
	
	public int getNbMaxClients() {
		return prefs.getInt("nbMaxClients", 10);
	}

	private void setNbMaxClients(int nbMaxClients) {
		Editor ed = prefs.edit();
		ed.putInt("nbMaxClients", nbMaxClients);
		ed.commit();
		appendLog("NBMAXCLIENTS", "NBMAXCLIENTS is set to " + nbMaxClients);
	}
	
	public int getNbMaxChannelPerClient() {
		return prefs.getInt("NbMaxChannelPerClient", 5);
	}

	private void setNbMaxChannelPerClient(int nbMaxChannelPerClient) {
		Editor ed = prefs.edit();
		ed.putInt("NbMaxChannelPerClient", nbMaxChannelPerClient);
		ed.commit();
		appendLog("NBMAXCHANNELPERCLIENT", "NBMAXCHANNELPERCLIENT is set to " + nbMaxChannelPerClient);
	}
	
	public int getMaxSizeFile() {
		return prefs.getInt("nbMaxClients", 10);
	}

	private void setMaxSizeFile(int maxSizeFile) {
		Editor ed = prefs.edit();
		ed.putInt("maxSizeFile", maxSizeFile);
		ed.commit();
		appendLog("MAXSIZEFILE", "MAXSIZEFILE is set to " + maxSizeFile);
	}
	
	public void createClient(Handler mRedrawHandler, String username, String ipAddress, int port) throws Exception {
		final String IPADDRESS_PATTERN = 
				"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
						"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
						"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
						"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		final Pattern ipAdd= Pattern.compile(IPADDRESS_PATTERN);
		if (!ipAdd.matcher(ipAddress).matches()) {
			throw new Exception("The IP address is not valid");
		}

		setIpAddress(ipAddress);
		setUsername(username);
		setPort(port);

		client = new Client(mRedrawHandler, username);
		try {
			((Client)client).connect(ipAddress, port);
		} catch (Exception e) {
			throw new Exception("Failure to connect");
			//TODO : user warning for the fail
		}
	}
	
	public void createHost(String username, int port, int nbMaxClients, int nbMaxChannelPerClient, int maxSizeFile) throws Exception {
		try {
			host = new Host(username, nbMaxClients, nbMaxChannelPerClient, maxSizeFile, port);
			setUsername(username);
			setNbMaxClients(nbMaxClients);
			setNbMaxChannelPerClient(nbMaxChannelPerClient);
			setMaxSizeFile(maxSizeFile);
			setPort(port);
		} catch (Exception e) {
			throw new Exception("Failure to launch the host");
		}
		new Thread(host).start();
	}
	
	public void exitClient() throws Exception {
		client.exit();
		client = null;
	}
	
	public void exitServer() throws IOException, Exception {
		host.exit();
		host = null;
	}
	
	public static void appendLog(String tag, String text)
	{   
		appendLog(Log.DEBUG, tag, text);
	}
	
	public static void appendLog(int tag, String fullTag, String text)
	{   
		switch (tag) {
		case Log.DEBUG :
			Log.d(fullTag, text);
			break;
		case Log.ERROR :
			Log.e(fullTag, text);
			break;
		case Log.INFO :
			Log.i(fullTag, text);
			break;
		case Log.VERBOSE :
			Log.v(fullTag, text);
			break;
		default :
			Log.d(fullTag, text);
		}

		File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/GeoChat.log");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
			buf.append("" + DateFormat.getDateTimeInstance().format(new Date()) + "\n" + " : " + text + "\n");
			buf.newLine();
			buf.flush();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
