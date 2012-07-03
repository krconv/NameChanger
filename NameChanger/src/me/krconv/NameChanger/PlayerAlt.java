package me.krconv.NameChanger;

public class PlayerAlt {

	private String playerName;
	private String altName;
	
	public PlayerAlt(String PlayerName) {
		// Constructor with only player name (alt to be populated later)
		this.playerName = PlayerName;
	}
	
	public PlayerAlt(String PlayerName, String AltName) {
		// Constructor with player name and alt name
		this.playerName = PlayerName;
		this.altName = AltName;
	}
	
	public String getName() {
		// Get player name
		return playerName;
	}
	
	public String getAlt() {
		// Get alt name
		return altName;
	}
	
	public void setName(String PlayerName) {
		// Set player name
		this.playerName = PlayerName;
	}
	
	public void setAlt(String AltName) {
		// Set alt name
		this.altName = AltName;
	}
	
	// Implement methods that will compare the contents of PlayerAlt instances

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerAlt other = (PlayerAlt) obj;
		if (altName == null) {
			if (other.altName != null)
				return false;
		} else if (!altName.equals(other.altName))
			return false;
		if (playerName == null) {
			if (other.playerName != null)
				return false;
		} else if (!playerName.equals(other.playerName))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((altName == null) ? 0 : altName.hashCode());
		result = prime * result
				+ ((playerName == null) ? 0 : playerName.hashCode());
		return result;
	}

}
