package me.krconv.NameChanger;

public class AutoAltManager {

	NameChanger plugin;
	private AutoAltDataAccess da;

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

	public int setAltNameForPlayer(String PlayerName, String AltName) {
		// Stores new alt name for a player name
		// Confirms valid PlayerName and AltName before saving (should already be validated, just making sure)
		// Returns 0 if successful, 1 if error because alt is already used, 2 if one of the names is invalid, 3 other error
		if (getPlayerNameForAlt(AltName)!=null) {
			// Alt name is already used, cannot have 2 players pointing to the same alt
			// Should also make this check when receiving the command to set the alt
			return 1;
		}
		if (plugin.isValidUserName(PlayerName) && plugin.isValidUserName(AltName)) {
			// Player and alt name are valid
			if (da.setAltNameForPlayer(PlayerName, AltName)) {
				return 0; // Success
			} else {
				return 3; // Unknown other error
			}
		} else {
			// At least one of the names is invalid, return 2
			return 2;
		}
	}
	
	public boolean removeAltNameForPlayer(String PlayerName) {
		// Removes stored alt name for a player name
		return da.removeAltNameForPlayer(PlayerName);
	}
	
}
