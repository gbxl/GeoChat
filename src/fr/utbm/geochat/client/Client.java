package fr.utbm.geochat.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.utbm.geochat.ApplicationManager;
import fr.utbm.geochat.Connection;
import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.LocationGeochat;
import fr.utbm.geochat.Message;
import fr.utbm.geochat.Packet;
import fr.utbm.geochat.TCPCommandType;
import fr.utbm.geochat.activity.ChannelAdapter;
import fr.utbm.geochat.activity.ProfileChooser;
import fr.utbm.geochat.host.Channel;

import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;

/**
 * Classe qui implémente une application client et ses fonctionnalités propres
 * 
 *
 */
public class Client extends ApplicationManager {

	private Connection connection;
	private Handler redrawHandler;
	private String fileName;
	
	public Client(Handler mRedrawHandler, String username) {
		this.username = username;
		this.redrawHandler = mRedrawHandler;
		this.channelsJoined = new HashMap<String, LinkedList<Message>>();
		this.listChannels = new HashSet<Channel>();
		this.listLocations = new HashMap<String, Location>();
		listChannels.add(new Channel("Main", username, username));
		LinkedList<Message> lm = new LinkedList<Message>();
		lm.add(new Message("#SYSTEM#", new Date(), "Welcome in Main"));
		channelsJoined.put("Main",lm);
		this.currentChannel = "Main";
	}
	
	public Client(String username, Connection connect) {
		this.username = username;
		this.connection = connect;
		this.currentChannel = "Main";
	}

	public void setRedrawHandler(Handler redrawHandler) {
		this.redrawHandler = redrawHandler;
	}
	
	public void sendFile(byte[] file, String filename, List<String> receivers) {
		connection.write(TCPCommandType.SEND_FILE, currentChannel, username, filename, receivers, file);
	}
	
	public void sendServer(String message) {
		try {
			//channelsJoined.get(currentChannel).add(new Message(username,new Date(),message));
			this.channelAdapter.getMessAdapter().updateMessages(channelsJoined.get(currentChannel));
			connection.write(TCPCommandType.SEND_MESSAGE_TO_SERVER, currentChannel, username, "", message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void whisp(String message, String receiver) {
		try {
			connection.write(TCPCommandType.WHISP, currentChannel, username, receiver, message);
			channelsJoined.get(currentChannel).add(new Message(username,new Date(),message, true));
			this.channelAdapter.getMessAdapter().updateMessages(channelsJoined.get(currentChannel));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createChannel(String name) {
		connection.write(username, TCPCommandType.CLIENT_CHANNEL_CREATION, name);
	}
	
	@Override
	public void createPrivateChannel(String name, String password) {
		try {
			connection.write(TCPCommandType.CLIENT_PRIVATE_CHANNEL_CREATION, name, username, "", password);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getCurrentChannel() {
		return currentChannel;
	}

	public void setCurrentChannel(String currentChannel) {
		this.currentChannel = currentChannel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	
	protected final Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(android.os.Message msg) {
			Packet p = (Packet)msg.obj;

			try {
				switch (p.getCommandType()) {
				case AUTH_FAIL:
					onAuthFail();
					break;
				case AUTH_SUCCESS:
					onAuthSuccess((String)p.getObject());
					break;
				case CONNECTION_EXIT:
					onConnectionExit();
					connection.disconnect();
					Intent intent = new Intent(GeoChat.getInstance().getApplicationContext(), ProfileChooser.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					GeoChat.getInstance().getApplicationContext().startActivity(intent);
					break;
				case CLIENT_REFUSED:
					onClientRefused((String)p.getObject());
					break;
				case CLIENT_ACCEPTED:
					onClientAccepted((Set<Channel>)p.getObject());
					break;
				case NEW_CHANNEL_CREATED:
					onNewChannel(p.getSender(), p.getIdChannel(), (Set<Channel>)p.getObject());
					break;
				case SEND_LOCATIONS:
					onSendLocations((Map<String, LocationGeochat>)p.getObject());
					break;
				case MESSAGE_RECEIVED_FROM_SERVER:
					onMessageReceivedFromServer(p.getIdChannel(),p.getSender(),(String)p.getObject());
					break;
				case WHISP_RECEIVED_FROM_SERVER:
					onWhispReceivedFromServer(p.getIdChannel(), p.getSender(), (String)p.getObject());
				case FILE_FAIL:
					onFileSendFail(p.getSender());
					break;
				case FILE_REQUEST:
					onFileRequest(p.getSender());
					break;
				case FILE_RECEIVED:
					onFileReceived(p.getSender(), p.getFilename() ,p.getFile());
					break;
				case DELETE_CHANNEL:
					onDeleteChannel((String)p.getObject());
					break;
				case UPDATE_CHANNEL:
					onUpdateChannel((Set<Channel>)p.getObject());
					break;
				case REQUEST_PASSWORD:
					onRequestPassword();
					break;
				case WIZZ_RECEIVED:
					onWizz();
					break;
				default:
				}
			} catch (Exception e) {
				GeoChat.appendLog("ERRORCLIENT", "ERRORCLIENT connexion problem " + e.getMessage());
				e.printStackTrace();
			}
		}
	};
	
	private void onNewChannel(String creator, String idChannel ,Set<Channel> listChannels) {
		this.listChannels = new HashSet<Channel>(listChannels);
		//this.currentChannel = idChannel;
		channelAdapter.updateChannels(this.listChannels);
	}
	
	protected void onFileReceived(String sender, String filename ,byte[] file) {
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/download/" + filename;
		try
		{
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(file);
			fos.close();
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("FileNotFoundException : " + ex);
		}
		catch(IOException ioe)
		{
			System.out.println("IOException : " + ioe);
		}
		
	}

	protected void onFileRequest(String fileName) {
		this.fileName = fileName;
		android.os.Message msg = android.os.Message.obtain();
		msg.what = GeoChat.FILE_REQUEST_HANDLE;
		msg.obj = fileName;
		redrawHandler.sendMessage(msg);
		
	}

	protected void onFileSendFail(String message) {
		android.os.Message msg = android.os.Message.obtain();
		msg.what = GeoChat.FILE_FAIL_HANDLE;
		msg.obj = message;
		redrawHandler.sendMessage(msg);
	}
	
	public void refuseFile() {
		try {
			this.connection.write(TCPCommandType.FILE_REFUSED, currentChannel, username, "", fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void acceptFile() {		
		try {
			this.connection.write(TCPCommandType.FILE_ACCEPTED, currentChannel, username, "", fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void onWhispReceivedFromServer(String channel, String sender, String message) {
		channelsJoined.get(channel).add(new Message(sender,new Date(),message, true));
		this.channelAdapter.getMessAdapter().updateMessages(channelsJoined.get(channel));
	}

	private void onDeleteChannel(String idChannel) {
		if(currentChannel.equals(idChannel)) {
			currentChannel="Main";
		}
		channelsJoined.remove(idChannel);
		for(Channel chan:listChannels) {
			if(chan.getIdChannel().equals(idChannel)) {
				listChannels.remove(chan);
				break;
			}
		}
		channelAdapter.updateChannels(listChannels);
	}
	
	private void onUpdateChannel(Set<Channel> listChannels) {
		this.listChannels = new HashSet<Channel>(listChannels);
		channelAdapter.updateChannels(this.listChannels);
	}
	
	private void onMessageReceivedFromServer(String idChannel, String sender, String message) {
		if(channelsJoined.containsKey(idChannel)) {
			channelsJoined.get(idChannel).add(new Message(sender, new Date(), message));
			if (this.currentChannel.equals(idChannel)) {
				channelAdapter.getMessAdapter().updateMessages(channelsJoined.get(idChannel));
			}
		}
	}
	
	private void onAuthFail() {
		redrawHandler.sendEmptyMessage(GeoChat.LOGIN_CHANNEL_FAIL_HANDLE);
	}
	
	private void onAuthSuccess(String idChannel) {
		currentChannel = idChannel;
		redrawHandler.sendEmptyMessage(GeoChat.LOGIN_CHANNEL_SUCCES_HANDLE);
	}
	
	private void onRequestPassword() {
		redrawHandler.sendEmptyMessage(GeoChat.POPUP_LOGIN_CHANNEL_HANDLE);
	}
	
	private void onConnectionExit() {
		this.channelsJoined = new HashMap<String, LinkedList<Message>>();
		this.listChannels = new HashSet<Channel>();
	}
	
	private void onClientRefused(String mess) {
		android.os.Message msg = android.os.Message.obtain();
		msg.what = GeoChat.CONNECTION_REFUSED;
		msg.obj = mess;
		redrawHandler.sendMessage(msg);
	}
	
	private void onSendLocations (Map<String, LocationGeochat> listLocations) {
		
		this.listLocations = new HashMap<String, Location>();
		
		Iterator<LocationGeochat> itv = listLocations.values().iterator();
		Iterator<String> itk = listLocations.keySet().iterator();
		
		while(itv.hasNext() && itk.hasNext()) {
			LocationGeochat lg = itv.next();
			String key = itk.next();
			
			Location l = new Location(lg.getProvider());
			l.setLatitude(lg.getLat());
			l.setLongitude(lg.getLon());
			
			this.listLocations.put(key, l);
		}
		
		redrawHandler.sendEmptyMessage(GeoChat.LAUNCH_MAP_HANDLE);
	}
	
	private void onClientAccepted(Set<Channel> listChannels) {
		this.listChannels = new HashSet<Channel>(listChannels);
		channelAdapter = new ChannelAdapter(GeoChat.getInstance().getApplicationContext(), listChannels);
		channelAdapter.notifyDataSetChanged();
		//Ici on lance le handler vers l'activity
		redrawHandler.sendEmptyMessage(GeoChat.CONNECTION_ACCEPTED);
	}
	
	
	public void connect(String ipAddress, int port) throws UnknownHostException, IOException { 
		this.connection = new Connection(this.handler, new Socket(ipAddress, port));
        new Thread(this.connection).start();
        this.authenticate();
	}

	public void authenticate() throws IOException {
		this.connection.write(username, TCPCommandType.SEND_USERNAME);
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String usrn) {
		this.username = usrn;
	}
	
	public void runServiceGPS()
    {
    	try {
    		 
    		myIntentService = new Intent(GeoChat.getInstance().getApplicationContext(), ClientGeolocalisationService.class);
			
    		myIntentService.putExtra("TIME",GeoChat.getInstance().getTimeGps());
    		myIntentService.putExtra("CONNECTION", connection);
			
			GeoChat.getInstance().startService(myIntentService);
		}
		catch (Exception e) {
		}
		
	}
	
	public void stopServiceGPS()
	{
		if(myIntentService!=null)
			GeoChat.getInstance().stopService(myIntentService);
	}
	
	public void requestLocations() {
		connection.write(username, TCPCommandType.REQUEST_LOCATION);
	}
	
	public void requestJoinChannel(String idChannel) {
		if(!channelsJoined.containsKey(idChannel)) {
			LinkedList<Message> lm = new LinkedList<Message>();
			lm.add(new Message("#SYSTEM#", new Date(), "Welcome in " + idChannel));
			channelsJoined.put(idChannel, lm);
		}
		connection.write(this.username, TCPCommandType.CLIENT_JOIN_CHANNEL, idChannel);
	}
	
	public void requestJoinPrivateChannel(String idChannel, String password) {
		connection.write(this.username, TCPCommandType.CLIENT_JOIN_PRIVATE_CHANNEL, idChannel, password);
	}
	
	public void wizz(String receiver) {
		connection.write(this.username, TCPCommandType.WIZZ_CLIENT, receiver);
	}
	
	public void onWizz() {
		redrawHandler.sendEmptyMessage(GeoChat.VIBRATION_ON);
	}
	
	public List<String> getClients() {
		List<String> users = new ArrayList<String>();
		for(Channel chan : listChannels) {
			if(chan.getIdChannel().equals(currentChannel)) {
				users = new ArrayList<String>(chan.getUsers());
				users.remove(username);
				break;
			}
		}
		return users;
	}
	
	public void quitChannel() {
		if(!currentChannel.equals("Main")) {
			connection.write(this.username,TCPCommandType.CLIENT_LEAVE_CHANNEL, this.currentChannel);
			channelsJoined.remove(currentChannel);
			currentChannel = "Main";
		}
		channelAdapter.updateChannels(listChannels);
	}

	public void exit() {
		connection.disconnect();
		onConnectionExit();
	}
}
