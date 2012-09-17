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
import android.content.Intent;
import android.location.Location;
/**
 * 
 * @author Bfrost
 * La classe AapplicationManager est la classe abstraite qui permettra d'implémenter
 * la partie client et la partie serveur
 */
public abstract class ApplicationManager {

	protected String username;
	protected String currentChannel = "Main";
	protected Location myLocation;
	protected Intent myIntentService;
	protected Map<String, LinkedList<Message>> channelsJoined;

	protected Set<Channel> listChannels;
	protected ChannelAdapter channelAdapter;
	protected Map<String, Location> listLocations;

	/**
	 * Envoyer un message à un utilisateur
	 * @param message est le message envoyé
	 * @param receiver la personne cible du message
	 */
	public abstract void whisp(String message, String receiver);
	/**
	 * 
	 * @param message message envoyé au serveur
	 */
	public abstract void sendServer(String message);

	/**
	 * Créer un channel
	 * @param name nom du channel
	 */
	public abstract void createChannel(String name);
	/**
	 * Créer un channel privée
	 * @param name nom du channel 
	 * @param password mot de passe du channel
	 */
	public abstract void createPrivateChannel(String name, String password);
	/**
	 * Quitter un channel
	 */
	public abstract void quitChannel();
	
	/**
	 * Lancer le service de geolocalisation
	 */
	public abstract void runServiceGPS();
	/**
	 * Stoper le service de geolocalisation
	 */
	public abstract void stopServiceGPS();
	
	/**
	 * Accepter la reception d'un fichier
	 */
	public abstract void acceptFile();
	
	/**
	 * Refuser la reception d'un fichier
	 */
	public abstract void refuseFile();
	
	/**
	 * Quitter
	 * @throws IOException
	 */
	public abstract void exit() throws IOException;

	/**
	 * Récuper sa localisation
	 * @return
	 */
	public Location getMyLocation() {
		return myLocation;
	}

	/**
	 * Liste des channels
	 * @return
	 */
	public Set<Channel> getListChannels() {
		return listChannels;
	}
	
	/**
	 * Setter sa localisation
	 * @param myLocation sa localisation
	 */
	public void setMyLocation(Location myLocation) {
		this.myLocation = myLocation;
	}

	/**
	 * Vérifier si le service de géolocalisation est actif
	 * @param nomService service ciblé
	 * @return
	 */
	protected boolean isServiceActif(String nomService) {
		final ActivityManager activityManager = (ActivityManager) GeoChat.getInstance().getSystemService(GeoChat.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		int i = 0;
		while (i < services.size()
				&& !nomService.equals(services.get(i).service.getClassName())) {
			i++;
		}

		return i < services.size();
	}
	
	/**
	 * Ajouter un nouveau channel
	 * @param channelName nom du channel
	 */
	protected void addNewChannelToList(String channelName) {
		channelsJoined.put(channelName, new LinkedList<Message>());
	}
	
	/**
	 * Ajouter un message au channel
	 * @param channelName channel ciblée
	 * @param message message à diffuser
	 */
	protected void addMessagesToChannel(String channelName, Message message) {
		channelsJoined.get(channelName).add(message);
	}
	
	/**
	 * Vérifier si l'utilisateur est sur le channel
	 * @param channelName nom du channel cible
	 * @return vrai si l'utilisateur à été trouvé
	 */
	protected boolean isOnChannel(String channelName) {
		if(channelsJoined.containsKey(channelName)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Récuperer la liste des messages du channel cible
	 * @param idChannel channel cible
	 * @return
	 */
	public List<Message> getMessages(String idChannel) {
		return channelsJoined.get(idChannel);
	}
	
	/**
	 * Quitter un channel
	 * @param channelName channel cible
	 */
	protected void quitChannel(String channelName) {
		if(channelsJoined.containsKey(channelName)) {
			channelsJoined.remove(channelName);
		}
	}
	
	/**
	 * Récuperer l'adapter des channel
	 * @return
	 */
	public ChannelAdapter getAdapter() {
		return channelAdapter;
	}
	
	/**
	 * Récuperer la liste des localisations des utilisateurs
	 * @return liste des geolocalisations
	 */
	public Map<String, Location> getLocations() {
		return listLocations;
	}
	
	/**
	 * Channels rejoint
	 * @return
	 */
	public Map<String, LinkedList<Message>> getChannelsJoined() {
		return channelsJoined;
	}

	/**
	 * Setter les channels rejoint
	 * @param channelsJoined
	 */
	public void setChannelsJoined(Map<String, LinkedList<Message>> channelsJoined) {
		this.channelsJoined = channelsJoined;
	}
	
}
