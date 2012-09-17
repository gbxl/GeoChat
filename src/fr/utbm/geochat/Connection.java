package fr.utbm.geochat;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;

import android.os.Handler;
import android.util.Log;

/**
 * Classe de connection afin d'envoyer des message entre utilisateur
 * @author Bfrost
 *
 */
public class Connection implements Runnable, Serializable {

	private static final long serialVersionUID = 239551610525606241L;
	
	private Socket socket;
	private Handler handler;
	protected InputStream inStream;
    protected OutputStream outStream;
    protected boolean isRunning;
    
    /**
     * Constructeur d'une connection
     * @param handler
     */
    public Connection(Handler handler) {
    	this.isRunning = true;
        this.inStream = null;
        this.outStream = null;
    	this.handler = handler;
    }
    
    /**
     * 
     * @param handler
     * @param socket
     */
    public Connection(Handler handler, Socket socket) {
    	this.socket = socket;
    	this.isRunning = true;
    	this.handler = handler;
    	try {
            this.inStream = socket.getInputStream();
            this.outStream = socket.getOutputStream();
        } catch (IOException e) {
        	GeoChat.appendLog(Log.ERROR, "Connection", e.getMessage());
        	e.printStackTrace();
        }
	}
	
    /**
     * Renvoie vrai si fonctionne
     * @return
     */
	protected synchronized boolean isRunning() {
		return isRunning;
	}
    
	/**
	 * Methode pour ecrire et envoyer un message
	 * @param commandType type de commande a envoyer
	 * @param idChannel channel cible
	 * @param sender utilisateur qui envoie le message
	 * @param receiver utilisateur cible du message
	 * @param data données à envoyer
	 * @throws IOException
	 */
    public void write(TCPCommandType commandType, String idChannel, String sender, String receiver, Serializable data) throws IOException {
    	this.write(new Packet(commandType,idChannel,sender,receiver,data));
    }
    
    /**
     * Méthode pour écrir et envoyer un message
     * @param username Utilisateur qui envoie le message
     * @param commandType commande de l'utilisateur
     */
    public void write(String username, TCPCommandType commandType) {
    	try {
    		this.write(new Packet(commandType, username, ""));
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Méthode pour écrir et envoyer un fichier
     * @param commandType commande à réaliser
     * @param idChannel channel cible
     * @param sender utilisateur qui envoie
     * @param filename nom du fichier envoyé
     * @param receivers noms des personnes cibles
     * @param file fichier
     */
    public void write(TCPCommandType commandType, String idChannel, String sender, String filename, List<String> receivers, byte[] file) {
    	try {
    		this.write(new Packet(commandType, idChannel, sender, filename, receivers, file));
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    /**
     * Méthode pour écrir et envoyer un fichier
     * @param commandType commande à réaliser
     * @param idChannel channel cible
     * @param sender utilisateur qui envoie
     * @param filename nom du fichier envoyé
     * @param file fichier
     */
	public void write(TCPCommandType commandType, String idChannel, String sender, String filename, byte[] file) {
		try {
    		this.write(new Packet(commandType, idChannel, sender, filename, file));
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}

	/**
	 * Méthode pour créer un channel
	 * @param creator créateur
	 * @param commandType commande
	 * @param idChannel identifiant du channel
	 * @param listChannels liste des channels
	 */
	public void write(String creator, TCPCommandType commandType,	String idChannel, Serializable listChannels) {
		try {
			this.write(new Packet(commandType,idChannel,creator,"all",listChannels));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	/**
	 * Envoie de données
	 * @param username nom de l'utilisateur
	 * @param commandType commande
	 * @param data données
	 */
    public void write(String username, TCPCommandType commandType, Serializable data) {
    	try {
			this.write(new Packet(commandType, username, data));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Envoie de données
     * @param packet pacquet envoyé
     * @throws IOException
     */
	private void write(Packet packet) throws IOException {
		ObjectOutputStream os = new ObjectOutputStream(this.outStream);
		os.writeObject(packet);
	}
	
	/**
	 * Adresse source
	 * @return
	 */
	public String getAddressSource() {
		return socket.getRemoteSocketAddress().toString();
	}
	
	@Override
	public void run() {
		while (isRunning()) {
			try {
				// Read from the InputStream
				ObjectInputStream in = new ObjectInputStream(inStream);
				while(in.read() != -1) {
				}
				Packet p = (Packet)in.readObject();
				p.setAddressSource(getAddressSource());
				handler.obtainMessage(p.getCommandType().ordinal(), p).sendToTarget();
				GeoChat.appendLog(Log.INFO, "Run() Connection", "running");

            } catch (IOException e) {
            	GeoChat.appendLog(Log.ERROR, "Connection", "io exception");
            	e.printStackTrace();
            	handler.obtainMessage(TCPCommandType.CONNECTION_EXIT.ordinal(), new Packet(TCPCommandType.CONNECTION_EXIT, "", this)).sendToTarget();
            	this.disconnect();
            } catch (ClassNotFoundException ce) {
            	GeoChat.appendLog(Log.ERROR, "Connection", ce.getMessage());
            }
        }
	}

	/**
	 * Se déconnecter
	 */
    public synchronized void disconnect() {
    	isRunning = false;
        try {
			this.socket.close();
		} catch (IOException e) {
			GeoChat.appendLog(Log.ERROR, "Connection", e.getMessage());
		}
	}
	
}
