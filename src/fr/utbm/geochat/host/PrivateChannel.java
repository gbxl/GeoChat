package fr.utbm.geochat.host;

/**
 * Classe d'implémentation d'un channel privée
 * 
 *
 */
public class PrivateChannel extends Channel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6125327229176719605L;
	private String password;
	
	public PrivateChannel(String idChannel, String creator, String password) {
		super(idChannel, creator);
		this.password = password;
	}
	
	public boolean checkPassword(String password) {
		if (this.password == password) {
			return true;
		}
		return false;
	}

	public String getPassword() {
		return password;
	}

}
