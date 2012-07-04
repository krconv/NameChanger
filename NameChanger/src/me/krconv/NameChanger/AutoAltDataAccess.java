package me.krconv.NameChanger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;

public final class AutoAltDataAccess {

	private static AutoAltDataAccess instance = new AutoAltDataAccess();
	private ArrayList<PlayerAlt> alts;

	private File altList;
	private NameChanger plugin;

	private AutoAltDataAccess() {
		// Private constructor to enforce singleton
	}

	public boolean initialize(NameChanger plugin) {
		this.plugin = plugin;
		// altList = new File(plugin.getDataFolder().getPath() + "AutomaticAlts.txt"); // Use this when running on minecraft server
		altList = new File("plugins/NameChanger/AutomaticAlts.txt"); // Use this when testing outside of minecraft
		if (!altList.exists()) {
			try {
				altList.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(altList, true));
				writer.write("# Storage of Player Names and automatic Alt Names separated by | character");
				writer.newLine();
				writer.write("# notch|fakeGuy   <-- Example - Renames notch to fakeGuy");
				writer.newLine();
				writer.close();
			} catch (IOException e) {
				// plugin.logger.log(Level.SEVERE, "NameChanger: Could not write to AutomaticAlts.txt"); // Enable once plugin is live
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static AutoAltDataAccess getInstance() {
		// Return instance of AutoAltDataAccess
		return instance;
	}

	public boolean doesPlayerHaveAlt(String PlayerName) {
		// Returns true if player has alt
		try {
			BufferedReader br = new BufferedReader(new FileReader(altList));
			String line = null;

			// Iterate through current file

			while ((line = br.readLine()) != null) {
				boolean match = false; // Current record is a match
				if (!line.startsWith("#")) { // Do not check comments
					String[] data = line.split("\\|");
					if (data.length == 2) {
						if (data[0].toLowerCase().equals(PlayerName.toLowerCase())) {
							br.close();
							return true;
						}
					}
				}
			}
			br.close();
		} catch (IOException e) {
			// plugin.logger.log(Level.SEVERE, "NameChanger: Could not read from AutomaticAlts.txt"); // Enable once plugin is live
			e.printStackTrace();
		}
		return false;
	}

	public String getAltNameForPlayer(String PlayerName) {
		// Return alt name associated with player name
		try {
			BufferedReader br = new BufferedReader(new FileReader(altList));
			String line = null;

			// Iterate through current file

			while ((line = br.readLine()) != null) {
				boolean match = false; // Current record is a match
				if (!line.startsWith("#")) { // Do not check comments
					String[] data = line.split("\\|");
					if (data.length == 2) {
						if (data[0].toLowerCase().equals(PlayerName.toLowerCase())) {
							br.close();
							return data[1];
						}
					}
				}
			}
			br.close();
		} catch (IOException e) {
			// plugin.logger.log(Level.SEVERE, "NameChanger: Could not read from AutomaticAlts.txt"); // Enable once plugin is live
			e.printStackTrace();
		}
		return null;
	}

	public String getPlayerNameForAlt(String AltName) {
		// Return player name associated with alt name (loops through file til finds first instance)
		try {
			BufferedReader br = new BufferedReader(new FileReader(altList));
			String line = null;

			// Iterate through current file

			while ((line = br.readLine()) != null) {
				boolean match = false; // Current record is a match
				if (!line.startsWith("#")) { // Do not check comments
					String[] data = line.split("\\|");
					if (data.length == 2) {
						if (data[1].toLowerCase().equals(AltName.toLowerCase())) {
							br.close();
							return data[0];
						}
					}
				}
			}
			br.close();
		} catch (IOException e) {
			// plugin.logger.log(Level.SEVERE, "NameChanger: Could not read from AutomaticAlts.txt"); // Enable once plugin is live
			e.printStackTrace();
		}
		return null;
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
			return true;
		} catch (IOException e) {
			// plugin.logger.log(Level.SEVERE, "NameChanger: Could not write to AutomaticAlts.txt"); // Enable once plugin is live
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
					hasBeenMatched = true; // At least one match has been found,
											// set this for return value
				}
			}
			pw.close();
			br.close();

			// Delete old file and replace it with new updated temporary file
			altList.delete();
			tempFile.renameTo(altList);

			return hasBeenMatched;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

}
