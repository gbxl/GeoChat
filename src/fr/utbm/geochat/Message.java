package fr.utbm.geochat;

import java.util.Date;

public class Message {

	private String sender;
	private Date date;
	private String content;
	private boolean whisp = false;

	public Message(String sender, Date date, String content) {
		this.sender = sender;
		this.date = date;
		this.content = content;
	}

	public Message(String sender, Date date, String content, boolean isWhisp) {
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

	public boolean isWhsip() {
		return whisp;
	}

}
