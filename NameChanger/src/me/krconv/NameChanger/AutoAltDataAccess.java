package me.krconv.NameChanger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class AutoAltDataAccess {

	private static AutoAltDataAccess instance = new AutoAltDataAccess();
	private NameChanger plugin;
	private File altList;
	
	// Use 2 custom case-insensitive hash maps, 1 indexed by player and 1 indexed by alt
	// Allows case-insensitive searching by either player or alt, while preserving original cases
	private CaseInsensitiveHashMap mPlayerAltList;
	private CaseInsensitiveHashMap mAltPlayerList;

	private AutoAltDataAccess() {
		// Private constructor to enforce singleton
	}

	public boolean initialize(NameChanger plugin) {
		this.plugin = plugin;
		// altList = new File(plugin.getDataFolder().getPath() + "AutomaticAlts.txt"); // Use this when running on minecraft server
		altList = new File("plugins/NameChanger/AutomaticAlts.txt"); // Use this when testing outside of minecraft
		if (!altList.exists()) {
			try {
				// Create empty automatic alts file
				altList.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(altList, true));
				writer.write("# Storage of Player Names and automatic Alt Names separated by | character");
				writer.newLine();
				writer.write("# notch|fakeGuy   <-- Example - Renames notch to fakeGuy");
				writer.newLine();
				writer.close();
			} catch (IOException e) {
				//plugin.logger.log(Level.SEVERE, "NameChanger: Could not write to AutomaticAlts.txt"); // Enable once plugin is live
				e.printStackTrace();
				return false;
			}
		} 
		else {
			loadAutoAltList(); // Load list into hash map
		}
		return true;
	}

	public static AutoAltDataAccess getInstance() {
		// Return instance of AutoAltDataAccess
		return instance;
	}
	
	public boolean loadAutoAltList() {
		// Loads data from auto alt file into hashmaps to live in memory while plugin is running
		mPlayerAltList = new CaseInsensitiveHashMap();
		mAltPlayerList = new CaseInsensitiveHashMap();
		try {
			BufferedReader br = new BufferedReader(new FileReader(altList));
			String line = null;

			// Iterate through current file
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) { // Ignore commented lines
					String[] data = line.split("\\|");
					if (data.length == 2) {
						if (!mPlayerAltList.containsKey(data[0].toLowerCase()) && !mAltPlayerList.containsKey(data[1].toLowerCase())) {
							// Only add as long as player or alt have not already been added (should be 0 duplicates)
							mPlayerAltList.put(data[0], data[1]);
							mAltPlayerList.put(data[1], data[0]);
						}
						else {
							// Log scenarios where duplicates exist
							//plugin.logger.log(Level.WARNING, "NameChanger: Ignoring duplicate player or alt in AutomaticAlts.txt. Entry: " + line); // Enable once plugin is live							
							System.out.println("NameChanger: Ignoring duplicate player or alt in AutomaticAlts.txt. Entry: " + line); // Enable once plugin is live
						}
						
					}
				}
			}
			br.close();
		} catch (IOException e) {
			//plugin.logger.log(Level.SEVERE, "NameChanger: Could not read from AutomaticAlts.txt"); // Enable once plugin is live
			e.printStackTrace();
		}
		return true;
	}
	
	public void testWriteHashMap() {
		Iterator<Entry<String, String>> iter = mPlayerAltList.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry mAlt = (Map.Entry) iter.next();
			System.out.println(mAlt.getKey() + " : " + mAlt.getValue());
		}
	}
	
	public void testWriteHashMap2() {
		Iterator<Entry<String, String>> iter = mAltPlayerList.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry mAlt = (Map.Entry) iter.next();
			System.out.println(mAlt.getKey() + " : " + mAlt.getValue());
		}
	}

	public boolean doesPlayerHaveAlt(String PlayerName) {
		// Returns true if player has alt
		return mPlayerAltList.containsKey(PlayerName);
	}

	public String getAltNameForPlayer(String PlayerName) {
		// Return alt name associated with player name
		return mPlayerAltList.get(PlayerName);
	}

	public String getPlayerNameForAlt(String AltName) {
		// Return alt name associated with player name
		return mAltPlayerList.get(AltName);
	}

	public boolean setAltNameForPlayer(String PlayerName, String AltName) {
		// Stores new alt name for a player name
		// First remove any instances of name in existing file, then append new record to end of file
		removeAltNameForPlayer(PlayerName);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(altList,
					true));
			writer.write(PlayerName + "|" + AltName);
			writer.newLine();
			writer.close();
			
			loadAutoAltList(); // Reload new list from updated file
			
			return true;
			
		} catch (IOException e) {
			//plugin.logger.log(Level.SEVERE, "NameChanger: Could not write to AutomaticAlts.txt"); // Enable once plugin is live
			e.printStackTrace();
		}
		return false; // If we get here, it didn't work
	}

	public boolean removeAltNameForPlayer(String PlayerName) {
		// Iterate through current file and identify matching records
		// Write non-matching records to temp file, ignore matching records
		// Delete current file and rename temp file to current file name
		try {
			File tempFile = new File(altList.getAbsolutePath() + ".tmp");
			boolean hasBeenMatched = false; // At least one record has been
											// matched (for return value)

			BufferedReader br = new BufferedReader(new FileReader(altList));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Iterate through current file

			while ((line = br.readLine()) != null) {
				boolean match = false; // Current record is a match
				if (!line.startsWith("#")) { // Do not check comments
					String[] data = line.split("\\|");
					if (data.length > 1) {
						match = data[0].toLowerCase().equals(
								PlayerName.toLowerCase());
					}
				}
				if (!match) {
					// Write non-matching lines to temp file
					pw.println(line);
					pw.flush();
				} else {
					hasBeenMatched = true; // At least one match has been found, set this for return value
				}
			}
			pw.close();
			br.close();

			// Delete old file and replace it with new updated temporary file
			altList.delete();
			tempFile.renameTo(altList);
			
			loadAutoAltList(); // Reload new list from updated file

			return hasBeenMatched;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public class CaseInsensitiveHashMap extends HashMap<String, String> {

		private static final long serialVersionUID = 1L;

		String get(String key) {
	    	return super.get(key.toLowerCase());
		}
		
		public String put(String key, String value) {
			return super.put(key.toLowerCase(), value);
	    }
	}
	
}
