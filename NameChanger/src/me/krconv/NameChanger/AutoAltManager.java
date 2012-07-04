package me.krconv.NameChanger;

public class AutoAltManager {

	NameChanger plugin;
	AutoAltDataAccess da;

	public boolean initialize(NameChanger plugin) {
		this.plugin = plugin;
		da = AutoAltDataAccess.getInstance();
		return da.initialize(plugin);
	}
	
	public boolean doesPlayerHaveAlt(String PlayerName) {
		// Returns true if player has alt
		return da.doesPlayerHaveAlt(PlayerName);
	}
	
	public String getAltNameForPlayer(String PlayerName) {
		// Return alt name associated with player name
		return da.getAltNameForPlayer(PlayerName);
	}

	public String getPlayerNameForAlt(String AltName) {
		// Return player name associated with alt name
		return da.getPlayerNameForAlt(AltName);
	}

	public boolean setAltNameForPlayer(String PlayerName, String AltName) {
		// Stores new alt name for a player name
		// Confirms valid PlayerName and AltName before saving (should already be validated, just making sure)
		if (plugin.isValidUserName(PlayerName) && plugin.isValidUserName(AltName)) {
			// Player and alt name are valid
			return da.setAltNameForPlayer(PlayerName, AltName);
		} else {
			// At least one of the names is invalid, return false
			return false;
		}
	}
	
	public boolean removeAltNameForPlayer(String PlayerName) {
		// Removes stored alt name for a player name
		return da.removeAltNameForPlayer(PlayerName);
	}
	
}
