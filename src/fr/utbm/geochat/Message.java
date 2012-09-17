package fr.utbm.geochat;

import java.util.Date;

/**
 * Classe de données utilisée pour les messages
 *
 */
public class Message {

	private String sender;
	private Date date;
	private String content;
	private Boolean whisp = false;
	
	public Message(String sender, Date date, String content) {
		this.sender = sender;
		this.date = date;
		this.content = content;
	}
	public Message(String sender, Date date, String content, Boolean isWhisp) {
		this.sender = sender;
		this.date = date;
		this.content = content;
		this.whisp = isWhisp;
	}
	
	public String getSender() {
		return sender;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getContent() {
		return content;
	}

	public Boolean isWhsip() {
		return whisp;
	}
	
}
