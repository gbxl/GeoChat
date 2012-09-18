package fr.utbm.geochat.host;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Channel implements Comparable<Channel>, Serializable {

	private static final long serialVersionUID = 7674037596700993545L;
	private String idChannel;
	private String creator;

	private Set<String> users;

	public Channel(String idChannel, String creator) {
		this.idChannel = idChannel;
		this.creator = creator;
		this.users = new HashSet<String>();
	}

	public Channel(String idChannel, String creator, String member) {
		this.idChannel = idChannel;
		this.creator = creator;
		this.users = new HashSet<String>();
		this.users.add(member);
	}

	public Channel(String idChannel, String creator, Set<String> member) {
		this.idChannel = idChannel;
		this.creator = creator;
		this.users = member;

	}

	public String getIdChannel() {
		return idChannel;
	}

	public String getCreator() {
		return creator;
	}

	public Set<String> getUsers() {
		return users;
	}

	public int getSize() {
		return users.size();
	}

	public void addUser(String userName) {
		users.add(userName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((idChannel == null) ? 0 : idChannel.hashCode());
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
		Channel other = (Channel) obj;
		if (idChannel == null) {
			if (other.idChannel != null)
				return false;
		} else if (!idChannel.equals(other.idChannel))
			return false;
		return true;
	}

	@Override
	public int compareTo(Channel another) {
		return another.idChannel.compareTo(idChannel);
	}

}
