package fr.utbm.geochat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.utbm.geochat.activity.ChannelAdapter;
import fr.utbm.geochat.host.Channel;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

public abstract class ApplicationManager {

	protected String username;
	protected String currentChannel = "Main";
	protected Location myLocation;
	protected Intent myIntentService;
	protected Map<String, LinkedList<Message>> channelsJoined;
	protected Set<Channel> listChannels;
	protected ChannelAdapter channelAdapter;
	protected Map<String, Location> listLocations;

	public abstract void whisp(String message, String receiver);

	public abstract void sendServer(String message);

	public abstract void createChannel(String name);

	public abstract void createPrivateChannel(String name, String password);

	public abstract void quitChannel();

	public abstract void runServiceGPS();

	public abstract void stopServiceGPS();

	public abstract void acceptFile();

	public abstract void refuseFile();

	public abstract void exit() throws IOException;

	public Location getMyLocation() {
		return myLocation;
	}

	public Set<Channel> getListChannels() {
		return listChannels;
	}

	public void setMyLocation(Location myLocation) {
		this.myLocation = myLocation;
	}

	protected boolean isServiceActif(String nomService) {
		final ActivityManager activityManager = (ActivityManager) GeoChat
				.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		int i = 0;
		while (i < services.size()
				&& !nomService.equals(services.get(i).service.getClassName())) {
			i++;
		}

		return i < services.size();
	}

	protected void addNewChannelToList(String channelName) {
		channelsJoined.put(channelName, new LinkedList<Message>());
	}

	protected void addMessagesToChannel(String channelName, Message message) {
		channelsJoined.get(channelName).add(message);
	}

	protected boolean isOnChannel(String channelName) {
		if (channelsJoined.containsKey(channelName)) {
			return true;
		}
		return false;
	}

	public List<Message> getMessages(String idChannel) {
		return channelsJoined.get(idChannel);
	}

	protected void quitChannel(String channelName) {
		if (channelsJoined.containsKey(channelName)) {
			channelsJoined.remove(channelName);
		}
	}

	public ChannelAdapter getAdapter() {
		return channelAdapter;
	}

	public Map<String, Location> getLocations() {
		return listLocations;
	}

	public Map<String, LinkedList<Message>> getChannelsJoined() {
		return channelsJoined;
	}

	public void setChannelsJoined(
			Map<String, LinkedList<Message>> channelsJoined) {
		this.channelsJoined = channelsJoined;
	}

}
