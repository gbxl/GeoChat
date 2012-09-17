package fr.utbm.geochat;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Classe permettant de créer des packet pouvant etre envoyé par la connexion
 *
 *
 */
public class Packet implements Serializable { 

	private static final long serialVersionUID = 6408909738971878661L;
	
	protected TCPCommandType commandType;
	private Serializable obj = null;
	private byte[] file = null;
	protected String sender = "";
	protected String addressSource ="";	
	private String idChannel = "";
	private String receiver = "";
	private String filename = "";
	private List<String> receivers = new ArrayList<String>();
	
	/**
	 * Constructeur de packet
	 * @param commandType
	 * @param sender
	 * @param objet
	 */
	public Packet(TCPCommandType commandType, String sender, Serializable objet) {
		this.commandType = commandType;
		this.sender = sender;
		this.obj = objet;
	}
	
	/**
	 * Constructeur de packet
	 * @param commandType
	 * @param idChannel
	 * @param sender
	 * @param filename
	 * @param file
	 */
	public Packet(TCPCommandType commandType, String idChannel, String sender, String filename, byte[] file) {
		this.commandType = commandType;
		this.sender = sender;
		this.filename = filename;
		this.file = file;
		this.idChannel = idChannel;
	}
	
	/**
	 * Constructeur de packet
	 * @param commandType
	 * @param idChannel
	 * @param sender
	 * @param filename
	 * @param receivers
	 * @param file
	 */
	public Packet(TCPCommandType commandType, String idChannel, String sender, String filename, List<String> receivers,  byte[] file) {
		this.commandType = commandType;
		this.sender = sender;
		this.filename = filename;
		this.file = file;
		this.idChannel = idChannel;
		this.receivers = new ArrayList<String>(receivers);
	}
	
	/**
	 * Constructeur de packet
	 * @param commandType
	 * @param idChannel
	 * @param sender
	 * @param receiver
	 * @param obj
	 */
	public Packet(TCPCommandType commandType, String idChannel, String sender, String receiver,  Serializable obj) {
		this.commandType = commandType;
		this.sender = sender;
		this.obj = obj;
		this.idChannel = idChannel;
		this.receiver = receiver;
	}

	public String getIdChannel() {
		return idChannel;
	}

	public String getReceiver() {
		return receiver;
	}
	
	public List<String> getReceivers() {
		return receivers;
	}
	
	public TCPCommandType getCommandType() {
		return commandType;
	}
	
	public byte[] getFile() {
		return file;
	}
	
	public Serializable getObject() {
		return obj;
	}
	
	public String getSender() {
		return sender;
	}
	
	public void setAddressSource(String addr) {
		this.addressSource = addr;
	}
	
	public String getAddressSource() {
		return this.addressSource;
	}

	public String getFilename() {
		return filename;
	}

}
