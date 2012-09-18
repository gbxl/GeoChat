package fr.utbm.geochat;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Packet implements Serializable {

	private static final long serialVersionUID = 6408909738971878661L;

	protected TCPCommandType commandType;
	private Serializable obj = null;
	private byte[] file = null;
	protected String sender = "";
	protected String addressSource = "";
	private String idChannel = "";
	private String receiver = "";
	private String filename = "";
	private List<String> receivers = new ArrayList<String>();

	public Packet(TCPCommandType commandType, String sender, Serializable objet) {
		this.commandType = commandType;
		this.sender = sender;
		this.obj = objet;
	}

	public Packet(TCPCommandType commandType, String idChannel, String sender,
			String filename, byte[] file) {
		this.commandType = commandType;
		this.sender = sender;
		this.filename = filename;
		this.file = file;
		this.idChannel = idChannel;
	}

	public Packet(TCPCommandType commandType, String idChannel, String sender,
			String filename, List<String> receivers, byte[] file) {
		this.commandType = commandType;
		this.sender = sender;
		this.filename = filename;
		this.file = file;
		this.idChannel = idChannel;
		this.receivers = new ArrayList<String>(receivers);
	}

	public Packet(TCPCommandType commandType, String idChannel, String sender,
			String receiver, Serializable obj) {
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
