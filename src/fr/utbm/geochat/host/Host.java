package fr.utbm.geochat.host;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;

import fr.utbm.geochat.ApplicationManager;
import fr.utbm.geochat.Connection;
import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.LocationGeochat;
import fr.utbm.geochat.Message;
import fr.utbm.geochat.Packet;
import fr.utbm.geochat.TCPCommandType;
import fr.utbm.geochat.activity.ChannelAdapter;
import fr.utbm.geochat.client.Client;
import fr.utbm.geochat.client.ClientGeolocalisationService;

public class Host extends ApplicationManager implements Runnable {

	private boolean isRunning;
	private ServerSocket serverSocket;
	private int limitClients;
	private int limitChannelsCreatedPerClient;
	private int fileMaxSize;
	private Handler redrawHandler;
	private Set<Client> clients;
	private Set<Connection> awaitingConnections;
	private Map<String, Pair<String, byte[]>> files = new HashMap<String, Pair<String, byte[]>>();
	private Map<String, List<String>> waitingFiles = new HashMap<String, List<String>>();
	private List<String> fileReceivers;
	private String fileName;

	public Host(String username, int nbMaxClients, int nbMaxChannelPerClient,
			int maxSizeFile, int port) {

		this.isRunning = true;
		this.username = username;
		this.limitClients = nbMaxClients;
		this.limitChannelsCreatedPerClient = nbMaxChannelPerClient;
		this.fileMaxSize = maxSizeFile;
		this.listChannels = new HashSet<Channel>();
		this.awaitingConnections = new HashSet<Connection>();
		this.clients = new HashSet<Client>();
		this.listLocations = new HashMap<String, Location>();

		this.channelsJoined = new HashMap<String, LinkedList<Message>>();
		listChannels.add(new Channel("Main", username, username));
		LinkedList<Message> lm = new LinkedList<Message>();
		lm.add(new Message("#SYSTEM#", new Date(), "Welcome in Main"));
		channelsJoined.put("Main", lm);

		currentChannel = "Main";

		this.channelAdapter = new ChannelAdapter(GeoChat.getInstance()
				.getApplicationContext(), listChannels);
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			GeoChat.appendLog(Log.ERROR, "Host creation",
					"Host creation failed");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Thread t = null;
		Connection con = null;
		try {
			while (isRunning()) {
				Socket clientSocket = serverSocket.accept();
				if (clientSocket != null) {
					GeoChat.appendLog(Log.DEBUG, "Host", "New client connected");
					con = new Connection(handler, clientSocket);
					this.awaitingConnections.add(con);
					t = new Thread(con);
					t.start();
				}
			}
		} catch (IOException e) {
			GeoChat.appendLog(Log.ERROR, "Host", e.getMessage());
			this.awaitingConnections.remove(con);
			for (Client cli : clients) {
				if (cli.getConnection().equals(con)) {
					this.clients.remove(cli);
				}
			}
			con.disconnect();
			t.interrupt();
		}
	}

	@Override
	public void createChannel(String name) {
		onChannelCreation(username, name);
	}

	@Override
	public void createPrivateChannel(String name, String password) {
		onPrivateChannelCreation(username, name, password);
	}

	public void setRedrawHandler(Handler redrawHandler) {
		this.redrawHandler = redrawHandler;
	}

	private boolean acceptNewClient() {
		return this.clients.size() < this.limitClients;
	}

	private boolean isUsernameFree(String username) {
		for (Client cli : clients) {
			if (cli.getUsername().equals(username)) {
				return false;
			}
		}
		return true;
	}

	protected final Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			Packet p = (Packet) msg.obj;

			try {
				switch (p.getCommandType()) {
				case SEND_USERNAME:
					onNewClientConnected(p.getSender(), p.getAddressSource());
					break;
				case SEND_MESSAGE_TO_SERVER:
					onSendAll(p.getIdChannel(), p.getSender(),
							(String) p.getObject());
					break;
				case WHISP:
					onServerWhisp(p.getIdChannel(), p.getSender(),
							p.getReceiver(), (String) p.getObject());
					break;
				case SEND_FILE:
					onFileSent(p.getSender(), p.getFilename(),
							p.getReceivers(), p.getFile());
					break;
				case FILE_REFUSED:
					onFileRefused(p.getSender(), (String) p.getObject());
					break;
				case CLIENT_CHANNEL_CREATION:
					onChannelCreation(p.getSender(), (String) p.getObject());
					break;
				case CLIENT_PRIVATE_CHANNEL_CREATION:
					onPrivateChannelCreation(p.getSender(), p.getIdChannel(),
							(String) p.getObject());
					break;
				case SEND_LOCATION:
					onSendLocation(p.getSender(),
							(LocationGeochat) p.getObject());
					break;
				case REQUEST_LOCATION:
					onResquestLocation(p.getSender());
					break;
				case CLIENT_JOIN_CHANNEL:
					onClientJoinChannel((String) p.getObject(), p.getSender());
					break;
				case CLIENT_JOIN_PRIVATE_CHANNEL:
					onClientJoinPrivateChannel(p.getIdChannel(), p.getSender(),
							(String) p.getObject());
					break;
				case CLIENT_LEAVE_CHANNEL:
					onClientLeaveChannel((String) p.getObject(), p.getSender());
					break;
				case CONNECTION_EXIT:
					onClientDisconnect((Connection) p.getObject());
					break;
				case FILE_ACCEPTED:
					onFileAccepted(p.getFilename(), p.getSender());
					break;
				case WIZZ_CLIENT:
					onWizz((String) p.getObject());
					break;
				default:
				}
			} catch (Exception e) {
				GeoChat.appendLog("ERRORSERVER", "ERRORSERVER server problem "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	};

	private void onClientLeaveChannel(String idChannel, String username) {
		if (!idChannel.equals("Main")) {
			if (username.equals(this.username)) {
				channelsJoined.remove(idChannel);
				for (Channel chan : listChannels) {
					if (chan.getIdChannel().equals(idChannel)) {
						chan.getUsers().remove(username);
						if (chan.getUsers().size() == 0) {
							listChannels.remove(chan);
							for (Client cli : clients) {
								cli.getConnection().write(username,
										TCPCommandType.DELETE_CHANNEL,
										idChannel);
							}
						}
						break;
					}
				}
				currentChannel = "Main";
			} else {
				for (Client cli : clients) {
					if (cli.getUsername().equals(username)) {
						cli.setCurrentChannel("Main");
						break;
					}
				}
				for (Channel chan : listChannels) {
					if (chan.getIdChannel().equals(idChannel)) {
						chan.getUsers().remove(username);
						if (chan.getUsers().size() == 0) {
							listChannels.remove(chan);
							for (Client cli : clients) {
								cli.getConnection().write(username,
										TCPCommandType.DELETE_CHANNEL,
										idChannel);
							}
						}
						break;
					}
				}
			}
			for (Client cli : clients) {
				cli.getConnection().write(username,
						TCPCommandType.UPDATE_CHANNEL,
						(Serializable) listChannels);
			}
		}
		channelAdapter.updateChannels(listChannels);
	}

	public void wizz(String receiver) {
		onWizz(receiver);
	}

	private void onWizz(String receiver) {
		if (username.equals(receiver)) {
			redrawHandler.sendEmptyMessage(GeoChat.VIBRATION_ON);
		} else {
			for (Client cli : clients) {
				if (cli.getUsername().equals(receiver)) {
					cli.getConnection().write(username,
							TCPCommandType.WIZZ_RECEIVED, "");
					break;
				}
			}
		}
	}

	@Override
	public void refuseFile() {
		onFileRefused(username, fileName);
	}

	@Override
	public void acceptFile() {
		saveFile(fileName, files.get(fileName).second);
	}

	private void saveFile(String filename, byte[] file) {
		String filePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/download/" + filename;
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(file);
			fos.close();
		} catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException : " + ex);
		} catch (IOException ioe) {
			System.out.println("IOException : " + ioe);
		}
		android.os.Message msg = android.os.Message.obtain();
		msg.obj = filename;
		msg.what = GeoChat.FILE_DOWNLOADED_HANDLE;
		redrawHandler.sendMessage(msg);
	}

	private void onFileRefused(String client, String fileName) {
		waitingFiles.get(fileName).remove(client);
		if (waitingFiles.get(fileName).size() == 0) {
			waitingFiles.remove(fileName);
			files.remove(fileName);
		}
	}

	protected void onFileAccepted(String fileName, String client) {
		for (Client cli : clients) {
			if (cli.getUsername().equals(client)) {
				cli.getConnection().write(TCPCommandType.SEND_FILE,
						currentChannel, files.get(fileName).first, fileName,
						files.get(fileName).second);
			}
			break;
		}
	}

	public void sendFile(byte[] file, String fileName, List<String> receivers) {
		onFileSent(username, fileName, receivers, file);
	}

	protected void onFileSent(String sender, String fileName,
			List<String> receivers, byte[] file) {
		if ((file.length / 1024) >= fileMaxSize) {
			for (Client cli : clients) {
				if (cli.getUsername().equals(sender)) {
					cli.getConnection().write("File too big",
							TCPCommandType.FILE_FAIL);
				}
				break;
			}
		} else {
			files.put(fileName, new Pair<String, byte[]>(sender, file));
			fileReceivers = new ArrayList<String>(receivers);
			waitingFiles.put(fileName, fileReceivers);
			if (fileReceivers.contains(username)) {
				onFileRequest(fileName);
			}
			for (Client cli : clients) {
				if (fileReceivers.contains(cli.getUsername())
						&& !cli.getUsername().equals(sender)) {
					cli.getConnection().write(fileName,
							TCPCommandType.FILE_REQUEST);
				}
			}
		}
	}

	protected void onFileRequest(String fileName) {
		this.fileName = fileName;
		android.os.Message msg = android.os.Message.obtain();
		msg.what = GeoChat.FILE_REQUEST_HANDLE;
		msg.obj = this.fileName;
		redrawHandler.sendMessage(msg);

	}

	protected void onClientDisconnect(Connection con) {
		for (Client cli : clients) {
			if (cli.getConnection().equals(con)) {
				clients.remove(cli);
				for (Channel chan : listChannels) {
					chan.getUsers().remove(cli.getUsername());
				}
				break;
			}
		}
		for (Client cli : clients) {
			if (!cli.getConnection().equals(con)) {
				cli.getConnection().write(username,
						TCPCommandType.UPDATE_CHANNEL,
						(Serializable) listChannels);
			}
		}
	}

	private void onClientJoinPrivateChannel(String idChannel, String username,
			String password) {

		for (Channel chan : listChannels) {
			if (chan.getIdChannel().equals(idChannel)) {
				if (!((PrivateChannel) chan).getPassword().equals(password)) {
					if (username.equals(this.username)) {
						redrawHandler
								.sendEmptyMessage(GeoChat.LOGIN_CHANNEL_FAIL_HANDLE);
					} else {
						for (Client cli : clients) {
							if (cli.getUsername().equals(username)) {
								cli.getConnection().write(username,
										TCPCommandType.AUTH_FAIL);
								return;
							}
						}
					}
				} else {
					if (username.equals(this.username)) {
						redrawHandler
								.sendEmptyMessage(GeoChat.LOGIN_CHANNEL_SUCCES_HANDLE);
					} else {
						for (Client cli : clients) {
							if (cli.getUsername().equals(username)) {
								cli.getConnection().write(username,
										TCPCommandType.AUTH_SUCCESS, idChannel);
								break;
							}
						}
					}
				}
				break;
			}
		}

		if (username.equals(this.username)) {
			LinkedList<Message> lm = new LinkedList<Message>();
			lm.add(new Message("#SYSTEM#", new Date(), "Welcome in "
					+ idChannel));
			currentChannel = idChannel;
			channelsJoined.put(idChannel, lm);
			for (Channel chan : listChannels) {
				if (chan.getIdChannel().equals(idChannel)) {
					chan.getUsers().add(username);
					for (Client cli : clients) {
						cli.getConnection().write(username,
								TCPCommandType.UPDATE_CHANNEL,
								(Serializable) listChannels);
					}
					break;
				}
			}
		} else {
			for (Client cli : clients) {
				if (cli.getUsername().equals(username)) {
					cli.setCurrentChannel(idChannel);
					break;
				}
			}
			for (Channel chan : listChannels) {
				if (chan.getIdChannel().equals(idChannel)) {
					chan.getUsers().add(username);
					listChannels.add(chan);
					for (Client cli : clients) {
						cli.getConnection().write(username,
								TCPCommandType.UPDATE_CHANNEL,
								(Serializable) listChannels);
					}
					break;
				}
			}
		}
		channelAdapter.updateChannels(listChannels);
	}

	private void onClientJoinChannel(String idChannel, String username) {

		for (Channel chan : listChannels) {
			if (chan.getIdChannel().equals(idChannel)) {
				try {
					if (((PrivateChannel) chan).getPassword() != "") {
						if (username.equals(this.username)) {
							onRequestPassword();
							return;
						}
						for (Client cli : clients) {
							if (cli.getUsername().equals(username)) {
								cli.getConnection().write(username,
										TCPCommandType.REQUEST_PASSWORD);
								return;
							}
						}
					}
				} catch (Exception e) {
					GeoChat.appendLog(Log.DEBUG, "Chanel type",
							"This channel is not a private one !");
					// e.printStackTrace();
				}
				break;
			}
		}

		if (username.equals(this.username)) {
			if (!channelsJoined.containsKey(idChannel)) {
				LinkedList<Message> lm = new LinkedList<Message>();
				lm.add(new Message("#SYSTEM#", new Date(), "Welcome in "
						+ idChannel));
				channelsJoined.put(idChannel, lm);
				currentChannel = idChannel;
			}
			redrawHandler.sendEmptyMessage(GeoChat.LOGIN_CHANNEL_SUCCES_HANDLE);
			for (Channel chan : listChannels) {
				if (chan.getIdChannel().equals(idChannel)) {
					chan.getUsers().add(username);
					for (Client cli : clients) {
						cli.getConnection().write(username,
								TCPCommandType.UPDATE_CHANNEL,
								(Serializable) listChannels);
					}
					break;
				}
			}
		} else {
			for (Channel chan : listChannels) {
				if (chan.getIdChannel().equals(idChannel)) {
					if (!chan.getUsers().contains(username)) {
						chan.getUsers().add(username);
						for (Client cli : clients) {
							if (cli.getUsername().equals(username)) {
								cli.getConnection().write(username,
										TCPCommandType.AUTH_SUCCESS, idChannel);
								cli.setCurrentChannel(idChannel);
							}
							cli.getConnection().write(username,
									TCPCommandType.UPDATE_CHANNEL,
									(Serializable) listChannels);
						}
						break;
					}
					for (Client cli : clients) {
						if (cli.getUsername().equals(username)) {
							cli.getConnection().write(username,
									TCPCommandType.AUTH_SUCCESS, idChannel);
							cli.setCurrentChannel(idChannel);
						}
					}
				}
			}
		}
		channelAdapter.updateChannels(listChannels);
	}

	private void onSendLocation(String username, LocationGeochat location) {

		Location l = new Location(location.getProvider());
		l.setLatitude(location.getLat());
		l.setLongitude(location.getLon());

		listLocations.put(username, l);
	}

	public void addMyLocationToList(Location location) {
		listLocations.put(this.username, location);
	}

	private void onNewClientConnected(String usnername, String address) {
		Connection c = null;
		for (Connection con : awaitingConnections) {
			if (con.getAddressSource().equals(address)) {
				c = con;
				break;
			}
		}
		if (!isUsernameFree(usnername) && c != null) {
			c.write(usnername, TCPCommandType.CLIENT_REFUSED,
					"Username already taken");
			return;
		}
		if (!acceptNewClient() && c != null) {
			c.write(usnername, TCPCommandType.CLIENT_REFUSED,
					"Limit nb clients reached");
			return;
		}
		onClientAccepted(usnername, address);
	}

	private void onClientAccepted(String id, String address) {
		for (Connection conn : awaitingConnections) {
			if (conn.getAddressSource().equals(address)) {
				this.clients.add(new Client(id, conn));
				for (Channel chan : listChannels) {
					if (chan.getIdChannel().equals("Main")) {
						chan.addUser(id);
					}
				}
				conn.write(username, TCPCommandType.CLIENT_ACCEPTED,
						(Serializable) listChannels);
			}
		}
		for (Client cli : clients) {
			cli.getConnection().write(username, TCPCommandType.UPDATE_CHANNEL,
					(Serializable) listChannels);
		}
		channelAdapter.updateChannels(listChannels);
	}

	private void onSendAll(String channel, String username, String message) {
		if (this.getChannelsJoined().containsKey(channel)) {
			channelsJoined.get(channel).add(
					new Message(username, new Date(), message));
			if (this.currentChannel.equals(channel)) {
				this.channelAdapter.getMessAdapter().updateMessages(
						channelsJoined.get(channel));
			}
		}
		for (Client cli : clients) {
			for (Channel chan : listChannels) {
				if (chan.getIdChannel().equals(cli.getCurrentChannel())) {
					try {
						cli.getConnection().write(
								TCPCommandType.MESSAGE_RECEIVED_FROM_SERVER,
								channel, username, cli.getUsername(), message);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void onServerWhisp(String channel, String username,
			String receiver, String message) {
		if (channelsJoined.containsKey(channel)) {
			if (this.username.equals(receiver)) {
				channelsJoined.get(channel).add(
						new Message(username, new Date(), message, true));
				this.channelAdapter.getMessAdapter().updateMessages(
						channelsJoined.get(channel));
			}
		}
		for (Client cli : clients) {
			if (cli.getUsername().equals(receiver)) {
				for (Channel chan : listChannels) {
					if (chan.getIdChannel().equals(cli.getCurrentChannel())) {
						try {
							cli.getConnection().write(
									TCPCommandType.WHISP_RECEIVED_FROM_SERVER,
									channel, username, cli.getUsername(),
									message);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void onChannelCreation(String creator, String name) {
		if (isMaxLimitChannelCreationReached(creator,
				limitChannelsCreatedPerClient)) {
			GeoChat.appendLog(Log.ERROR, "Max channel",
					"Limit of channel creations reached");
		} else {
			if (creator.equals(this.username)) {
				listChannels.add(new Channel(name, creator));
				requestJoinChannel(name);
				onNewChannel(name, creator);
			} else {
				listChannels.add(new Channel(name, creator));
				if (clients.size() != 0) {
					onNewChannel(name, creator);
				}
			}
			channelAdapter.updateChannels(listChannels);
		}
	}

	private void onPrivateChannelCreation(String creator, String name,
			String password) {
		if (isMaxLimitChannelCreationReached(creator,
				limitChannelsCreatedPerClient)) {
			GeoChat.appendLog(Log.ERROR, "Max channel",
					"Limit of channel creations reached");
		} else {
			listChannels.add(new PrivateChannel(name, creator, password));
			onNewChannel(name, creator);
			LinkedList<Message> lm = new LinkedList<Message>();
			lm.add(new Message("#SYSTEM#", new Date(), "Welcome in " + name));
			channelsJoined.put(name, lm);
			channelAdapter.updateChannels(listChannels);
		}
	}

	private void onNewChannel(String name, String creator) {
		if (creator.equals(username)) {
			this.currentChannel = name;
		}
		for (Channel chan : listChannels) {
			if (chan.getIdChannel().equals(name)) {
				chan.getUsers().add(creator);
			}
		}
		for (Client cli : clients) {
			if (cli.getUsername().equals(creator)) {
				cli.setCurrentChannel(name);
			}
			cli.getConnection().write(creator,
					TCPCommandType.NEW_CHANNEL_CREATED, name,
					(Serializable) listChannels);
		}

	}

	private boolean isMaxLimitChannelCreationReached(String creator, int limit) {
		int number = 0;
		for (Channel chan : listChannels) {
			if (chan.getCreator().equals(creator)) {
				number++;
			}
			if (number == limit) {
				return true;
			}
		}
		return false;
	}

	private synchronized boolean isRunning() {
		return isRunning;
	}

	@Override
	public void sendServer(String message) {
		onSendAll(currentChannel, username, message);
	}

	@Override
	public void whisp(String message, String receiver) {
		onServerWhisp(currentChannel, username, receiver, message);
		channelsJoined.get(currentChannel).add(
				new Message(username, new Date(), message, true));
		this.channelAdapter.getMessAdapter().updateMessages(
				channelsJoined.get(currentChannel));
	}

	public String getIPAddress() {
		return this.serverSocket.getInetAddress().toString();
	}

	@Override
	public void runServiceGPS() {
		try {

			myIntentService = new Intent(GeoChat.getInstance()
					.getApplicationContext(),
					ClientGeolocalisationService.class);

			myIntentService
					.putExtra("TIME", GeoChat.getInstance().getTimeGps());

			GeoChat.getInstance().startService(myIntentService);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopServiceGPS() {
		if (myIntentService != null)
			GeoChat.getInstance().stopService(myIntentService);
	}

	public void onResquestLocation(String receiver) {
		for (Client cli : clients) {
			if (cli.getUsername().equals(receiver)) {

				Map<String, LocationGeochat> listLoc = new HashMap<String, LocationGeochat>();

				Iterator<Location> itv = listLocations.values().iterator();
				Iterator<String> itk = listLocations.keySet().iterator();

				while (itv.hasNext() && itk.hasNext()) {
					Location lg = itv.next();
					String key = itk.next();

					LocationGeochat l = new LocationGeochat(lg.getLatitude(),
							lg.getLongitude());

					listLoc.put(key, l);
				}

				cli.getConnection().write(username,
						TCPCommandType.SEND_LOCATIONS, (Serializable) listLoc);
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public void requestJoinChannel(String idChannel) {
		onClientJoinChannel(idChannel, this.username);
	}

	public void requestJoinPrivateChannel(String idChannel, String password) {
		onClientJoinPrivateChannel(idChannel, this.username, password);
	}

	@Override
	public void quitChannel() {
		onClientLeaveChannel(this.currentChannel, this.username);
	}

	private void onRequestPassword() {
		redrawHandler.sendEmptyMessage(GeoChat.POPUP_LOGIN_CHANNEL_HANDLE);
	}

	@Override
	public synchronized void exit() throws IOException {
		this.isRunning = false;
		for (Client cli : clients) {
			cli.getConnection()
					.write(this.username, TCPCommandType.SERVER_EXIT);
			cli.getConnection().disconnect();
		}
	}

	public List<String> getClients() {
		List<String> receivers = new ArrayList<String>();
		for (Client cli : clients) {
			if (cli.getCurrentChannel().equals(currentChannel)) {
				receivers.add(cli.getUsername());
			}
		}
		return receivers;
	}

	public void launchMap() {
		redrawHandler.sendEmptyMessage(GeoChat.LAUNCH_MAP_HANDLE);
	}
}
