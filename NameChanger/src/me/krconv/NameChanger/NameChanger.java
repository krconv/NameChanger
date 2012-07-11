package me.krconv.NameChanger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.krconv.NameChanger.NameChanger;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.WorldServer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class NameChanger extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static NameChanger plugin;
	// public Map<Player, String> RealToAltList = new HashMap<Player, String>();
	public AutoAltManager autoAltManager = new AutoAltManager();
	public File NameChangerLog;

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(
				new MyPlayerListener(this), this);
		PluginDescriptionFile pdfFile = this.getDescription();
		FileConfiguration config = getConfig();
		config.options().copyDefaults(true);
		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
			saveConfig();
		}
		autoAltManager.initialize(this);
		this.initialize(this);
		this.logger.info(pdfFile.getName() + " v" + pdfFile.getVersion()
				+ " has been enabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		PluginDescriptionFile pdfFile = this.getDescription();
		if (commandLabel.equalsIgnoreCase("name")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("namechanger.basic")) {
					if (args.length == 0) {
						player.sendMessage(ChatColor.DARK_RED + "-----"
								+ ChatColor.GOLD + "NameChanger v"
								+ pdfFile.getVersion() + ChatColor.DARK_RED
								+ "-----");
						player.sendMessage(ChatColor.GOLD
								+ "by krconv and EliteSandwich");
						player.sendMessage(ChatColor.GOLD + "Use "
								+ ChatColor.GRAY + "/name help "
								+ ChatColor.GOLD + "for help");
					} else if (args.length == 1) {
						if (args[0].equalsIgnoreCase("help")) {
							player.sendMessage(ChatColor.DARK_RED + "-----"
									+ ChatColor.GOLD + "NameChanger Help"
									+ ChatColor.DARK_RED + "-----");
							player.sendMessage(ChatColor.GRAY + "/name "
									+ ChatColor.DARK_RED + "- "
									+ ChatColor.GOLD
									+ "Displays NameChanger version");
							player.sendMessage(ChatColor.GRAY + "/name help "
									+ ChatColor.DARK_RED + "- "
									+ ChatColor.GOLD + "Displays this page");
							player.sendMessage(ChatColor.GRAY + "/name reload "
									+ ChatColor.DARK_RED + "- "
									+ ChatColor.GOLD + "Reloads the config");
							player.sendMessage(ChatColor.GRAY + "/name <Name> "
									+ ChatColor.DARK_RED + "- "
									+ ChatColor.GOLD + "Changes your name");
							player.sendMessage(ChatColor.GRAY
									+ "/name <Target> <Name> "
									+ ChatColor.DARK_RED + "- "
									+ ChatColor.GOLD
									+ "Changes a target player's name");
							player.sendMessage(ChatColor.GRAY
									+ "/name auto <Player> <Name> "
									+ ChatColor.DARK_RED
									+ "- "
									+ ChatColor.GOLD
									+ "Sets a player to automatically logon as another name");
							player.sendMessage(ChatColor.GRAY
									+ "/name remove <Player> "
									+ ChatColor.DARK_RED
									+ "- "
									+ ChatColor.GOLD
									+ "Removes a player from the automatic alt list");
							player.sendMessage(ChatColor.GRAY
									+ "/name check <Player> "
									+ ChatColor.DARK_RED
									+ "- "
									+ ChatColor.GOLD
									+ "Checks to see if a player is set to logon as another name");
						} else if (args[0].equalsIgnoreCase("reload")) {
							if (player.hasPermission("namechanger.reload")) {
								reloadConfig();
								player.sendMessage(ChatColor.GOLD
										+ "NameChanger config reloaded!");
								System.out
										.print("[NameChanger] Config reloaded!");
							} else {
								player.sendMessage(ChatColor.RED
										+ "You do not have permission to do this.");

							}
						} else {
							if (player.hasPermission("namechanger.name."
									+ args[0])) {
								if (player.getServer().getPlayer(args[0]) == null) {
									EntityPlayer playerSelf = (EntityPlayer) ((CraftPlayer) sender)
											.getHandle();
									player.setGameMode(player.getServer()
											.getDefaultGameMode());
									String log = new String();
									log = player.getName()
											+ " changed their name to "
											+ args[0] + "!";
									this.logger.info("[NameChanger] " + log);
									WriteNameChangerLog(log);
									if (getConfig().getBoolean(
											"broadcastQuitMessage") == true) {
										getServer()
												.broadcastMessage(
														ChatColor.YELLOW
																+ player.getName()
																+ " has left the game.");
									}
									player.saveData();
									player.setPlayerListName(args[0]);
									player.setDisplayName(args[0]);
									setPlayerNameSelf(playerSelf, args[0]);
									player.loadData();
									player.teleport(player.getLocation());
									player.sendMessage(ChatColor.GREEN
											+ "Your name is now "
											+ player.getName() + ".");
									if (getConfig().getBoolean(
											"broadcastJoinMessage") == true) {
										getServer()
												.broadcastMessage(
														ChatColor.YELLOW
																+ player.getName()
																+ " has joined the game.");
										getServer().dispatchCommand(
												getServer().getConsoleSender(),
												"pex reload");
									}
								} else {
									player.sendMessage(ChatColor.RED + args[0]
											+ " is already logged on!");
								}
							} else {
								player.sendMessage(ChatColor.RED
										+ "You do not have permission for this name.");

							}
						}
					} else if (args.length == 2) {
						if (args[0].equalsIgnoreCase("remove")) {
							if (player.hasPermission("namechanger.auto."
									+ args[1])) {
								if (!isValidUserName(args[1])) {
									// Validate target player name
									player.sendMessage(ChatColor.RED
											+ args[1]
											+ " is not a possible Minecraft name.");
									return false;
								}

								// Make sure alt name not already in use for
								// another
								// player
								// Player target =
								// getServer().getPlayer(args[1]);
								// // Changed this so that it works for offline
								// players, I think
								boolean setResult;
								setResult = autoAltManager
										.removeAltNameForPlayer(args[1]);

								if (setResult == true) {
									// Successfully set automatic alt name for
									// target player
									String log = new String();
									log = player.getName() + " has removed "
											+ args[1]
											+ " from the automatic alt list";
									this.logger.info("[NameChanger] " + log);
									WriteNameChangerLog(log);
									player.sendMessage(ChatColor.GREEN
											+ "You have removed " + args[1]
											+ " from the automatic alt list.");
								} else if (setResult == false) {
									// Cannot have more than 1 player assigned
									// to an
									// alt
									player.sendMessage(ChatColor.RED
											+ args[1]
											+ " is not on the automatic alt list!");
								}
							}
						} else if (args[0].equalsIgnoreCase("check")) {
							if (!isValidUserName(args[1])) {
								// Validate target player name
								player.sendMessage(ChatColor.RED + args[1]
										+ " is not a possible Minecraft name.");
								return false;
							}
							if (autoAltManager.doesPlayerHaveAlt(args[1]) == true) {
								String altName = autoAltManager
										.getAltNameForPlayer(args[1]);
								player.sendMessage(ChatColor.GOLD + args[1]
										+ " is set to automatically log on as "
										+ altName);
							} else {
								player.sendMessage(ChatColor.GOLD
										+ args[1]
										+ " is not set to log on as another name.");
							}
						} else {
							Player target = getServer().getPlayer(args[0]);
							if (player.hasPermission("namechanger.other."
									+ args[1])) {
								if (target.getServer().getPlayer(args[0]) != null) {
									if (target.getServer().getPlayer(args[1]) == null) {
										EntityPlayer playerOther = (EntityPlayer) ((CraftPlayer) target)
												.getHandle();
										player.setGameMode(player.getServer()
												.getDefaultGameMode());
										String log = new String();
										log = player.getName() + " changed "
												+ target.getName()
												+ "'s name to " + args[1] + "!";
										this.logger
												.info("[NameChanger] " + log);
										WriteNameChangerLog(log);
										if (getConfig().getBoolean(
												"broadcastQuitMessage") == true) {
											getServer()
													.broadcastMessage(
															ChatColor.YELLOW
																	+ target.getName()
																	+ " has left the game.");
										}
										target.saveData();
										target.setPlayerListName(args[1]);
										target.setDisplayName(args[1]);
										setPlayerNameOther(playerOther, args[1]);
										target.loadData();
										target.teleport(target.getLocation());
										target.sendMessage(ChatColor.GREEN
												+ "Your name has been changed to "
												+ args[1] + ".");
										if (getConfig().getBoolean(
												"broadcastJoinMessage") == true) {
											getServer()
													.broadcastMessage(
															ChatColor.YELLOW
																	+ target.getName()
																	+ " has joined the game.");
										}
										getServer().dispatchCommand(
												getServer().getConsoleSender(),
												"pex reload");
									} else {
										player.sendMessage(ChatColor.RED
												+ args[0]
												+ " is already logged on!");
									}
								} else {
									player.sendMessage(ChatColor.RED + args[0]
											+ " is not logged on!");
								}
							} else {
								player.sendMessage(ChatColor.RED
										+ "You do not have permission to do this.");
							}
						}
					} else if (args.length == 3) {
						if (args[0].equalsIgnoreCase("auto")) {
							if (player.hasPermission("namechanger.auto."
									+ args[1] + "." + args[2])) {
								if (!isValidUserName(args[1])) {
									// Validate target player name
									player.sendMessage(ChatColor.RED
											+ args[1]
											+ " is not a possible Minecraft name.");
									return false;
								}
								if (!isValidUserName(args[2])) {
									// Validate alt name
									player.sendMessage(ChatColor.RED
											+ args[1]
											+ " is not a possible Minecraft name.");
									return false;
								}
								// Make sure alt name not already in use for
								// another
								// player
								String conflictingPlayer = autoAltManager
										.getPlayerNameForAlt(args[1]);
								if (conflictingPlayer != null) {
									// Conflict, cannot proceed
									player.sendMessage(ChatColor.RED
											+ conflictingPlayer
											+ " already uses that automatic alt. Remove this entry first.");
									return false;
								}
								// Player target =
								// getServer().getPlayer(args[1]);
								// // Changed this so that it works for offline
								// players, I think
								int setResult;
								setResult = autoAltManager.setAltNameForPlayer(
										args[1], args[2]);

								if (setResult == 0) {
									// Successfully set automatic alt name for
									// target player
									String log = new String();
									log = player.getName()
											+ " has set "
											+ args[1]
											+ "'s name automatically change to "
											+ args[2] + " on login!";
									this.logger.info("[NameChanger] " + log);
									WriteNameChangerLog(log);
									player.sendMessage(ChatColor.GREEN
											+ "You have set " + args[1]
											+ " to automatically log in as "
											+ args[2] + ".");
								} else if (setResult == 1) {
									// Cannot have more than 1 player assigned
									// to an
									// alt
									player.sendMessage(ChatColor.RED
											+ args[1]
											+ " is already an automatic alt for another player.");
								} else if (setResult == 2) {
									// Either player or alt is an impossible
									// minecraft name, but we already check that
									// above so no action needed here
								} else if (setResult == 3) {
									// Unknown error - passed verification but
									// somehow didn't complete
									player.sendMessage(ChatColor.RED
											+ "Unknown error when setting automatic alt for that player.");
								}

							}
						} else {
							player.sendMessage(ChatColor.RED
									+ "Too many arguments! Use /name help for help.");
						}
					} else {
						player.sendMessage(ChatColor.RED
								+ "Too many arguments! Use /name help for help.");

					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "You do not have permission to do this.");

				}
			} else {
				if (args.length == 0) {
					sender.sendMessage("-----NameChanger v-----");
					sender.sendMessage("by krconv and EliteSandwich");
					sender.sendMessage("Use /name help for help");
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("help")) {
						sender.sendMessage("-----NameChanger Help-----");
						sender.sendMessage("/name - Displays NameChanger version");
						sender.sendMessage("/name help - Displays this page");
						sender.sendMessage("/name reload - Reloads the config");
						sender.sendMessage("/name <Target> <Name> - Changes a target sender's name");
						sender.sendMessage("/name auto <Player> <Name> - Sets a player to automatically login as another name");
						sender.sendMessage("/name remove <Player> - Removes a player from the automatic alt list");
						sender.sendMessage("/name check <Player> - Checks to see if a player is set to logon as another name");
					} else if (args[0].equalsIgnoreCase("reload")) {
						reloadConfig();
						sender.sendMessage("NameChanger config reloaded!");

					} else {
						sender.sendMessage("You can't change your name silly!");

					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("remove")) {
						if (!isValidUserName(args[1])) {
							// Validate target player name
							sender.sendMessage(args[1]
									+ " is not a possible Minecraft name.");
							return false;
						}

						// Make sure alt name not already in use for
						// another
						// player
						// Player target =
						// getServer().getPlayer(args[1]);
						// // Changed this so that it works for offline
						// players, I think
						boolean setResult;
						setResult = autoAltManager
								.removeAltNameForPlayer(args[1]);

						if (setResult == true) {
							// Successfully set automatic alt name for
							// target player
							sender.sendMessage("You have removed " + args[1]
									+ " from the automatic alt list.");
							WriteNameChangerLog("Console removed " + args[0]
									+ "from the automatic alt list!");
						} else if (setResult == false) {
							// Cannot have more than 1 player assigned
							// to an
							// alt
							sender.sendMessage(args[1]
									+ " is not on the automatic alt list!");
						}
					} else if (args[0].equalsIgnoreCase("check")) {
						if (!isValidUserName(args[1])) {
							// Validate target player name
							sender.sendMessage(args[1]
									+ " is not a possible Minecraft name.");
							return false;
						}
						if (autoAltManager.doesPlayerHaveAlt(args[1]) == true) {
							String altName = autoAltManager
									.getAltNameForPlayer(args[1]);
							sender.sendMessage(args[1]
									+ " is set to automatically log on as "
									+ altName);
						} else {
							sender.sendMessage(args[1]
									+ " is not set to log on as another name.");
						}
					} else {

						Player target = getServer().getPlayer(args[0]);
						if (getServer().getPlayer(args[0]) != null) {
							if (getServer().getPlayer(args[1]) == null) {
								EntityPlayer PlayerOther = (EntityPlayer) ((CraftPlayer) target)
										.getHandle();
								target.setGameMode(getServer()
										.getDefaultGameMode());
								WriteNameChangerLog("Console changed "
										+ target.getName() + "'s name to "
										+ args[1] + "!");
								if (getConfig().getBoolean(
										"broadcastQuitMessage") == true) {
									getServer().broadcastMessage(
											ChatColor.YELLOW + target.getName()
													+ " has left the game.");
								}
								target.saveData();
								target.setPlayerListName(args[1]);
								target.setDisplayName(args[1]);
								setPlayerNameOther(PlayerOther, args[1]);
								target.loadData();
								target.teleport(target.getLocation());
								target.sendMessage(ChatColor.GREEN
										+ "Your name has been changed to "
										+ args[1] + ".");
								if (getConfig().getBoolean(
										"broadcastJoinMessage") == true) {
									getServer().broadcastMessage(
											ChatColor.YELLOW + target.getName()
													+ " has joined the game.");
								}
								getServer().dispatchCommand(
										getServer().getConsoleSender(),
										"pex reload");
							} else {
								sender.sendMessage(args[0]
										+ " is already logged on!");
							}
						} else {
							sender.sendMessage(args[0] + " is not logged on!");
						}
					}
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("auto")) {
						if (sender.hasPermission("namechanger.auto." + args[1]
								+ "." + args[2])) {
							if (!isValidUserName(args[1])) {
								// Validate target sender name
								sender.sendMessage(args[1]
										+ " is not a possible Minecraft name!");
								return false;
							}
							if (!isValidUserName(args[2])) {
								// Validate alt name
								sender.sendMessage(args[1]
										+ "' is not a possible Minecraft name!");
								return false;
							}
							// Make sure alt name not already in use for
							// another
							// sender
							String conflictingplayer = autoAltManager
									.getPlayerNameForAlt(args[1]);
							if (conflictingplayer != null) {
								// Conflict, cannot proceed
								sender.sendMessage(conflictingplayer
										+ " already uses that automatic alt. Remove this entry first!");
								return false;
							}
							// sender target =
							// getServer().getsender(args[1]);
							// // Changed this so that it works for offline
							// senders, I think
							int setResult;
							setResult = autoAltManager.setAltNameForPlayer(
									args[1], args[2]);

							if (setResult == 0) {
								// Successfully set automatic alt name for
								// target sender
								sender.sendMessage("You have set " + args[1]
										+ " to automatically log in as "
										+ args[2] + ".");
								WriteNameChangerLog("Console set " + args[1]
										+ " to automatically logon as "
										+ args[2] + "!");
							} else if (setResult == 1) {
								// Cannot have more than 1 sender assigned
								// to an
								// alt
								sender.sendMessage(args[1]
										+ " is already an automatic alt for another player.");
							} else if (setResult == 2) {
								// Either sender or alt is an impossible
								// minecraft name, but we already check that
								// above so no action needed here
							} else if (setResult == 3) {
								// Unknown error - passed verification but
								// somehow didn't complete
								sender.sendMessage("Unknown error when setting automatic alt for that player.");
							}
						}
					} else {
						sender.sendMessage("Too many arguments! Use /name help for help.");
					}
				} else {
					sender.sendMessage("Too many arguments! Use /name help for help.");
				}
			}
		}
		return false;
	}

	public void setPlayerNameSelf(EntityPlayer playerSelf, String newName) {
		WorldServer world = (WorldServer) playerSelf.world;
		EntityTracker tracker = world.tracker;
		tracker.untrackEntity(playerSelf);
		playerSelf.name = newName;
		tracker.track(playerSelf);

	}

	public void setPlayerNameOther(EntityPlayer playerOther, String newName) {
		WorldServer world = (WorldServer) playerOther.world;
		EntityTracker tracker = world.tracker;
		tracker.untrackEntity(playerOther);
		playerOther.name = newName;
		tracker.track(playerOther);
	}

	public boolean isValidUserName(String toTest) {
		// Ensures user name contains only valid characters and between 2-16
		// characters long
		Pattern p = Pattern.compile("[A-Za-z0-9_]{2,16}");
		Matcher m = p.matcher(toTest);
		return m.matches();
	}

	public boolean initialize(NameChanger plugin) {
		plugin = this;
		NameChangerLog = new File(getDataFolder(), "NameChangerLog.txt");
		if (!NameChangerLog.exists()) {
			try {
				NameChangerLog.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						NameChangerLog, true));
				writer.write("# Log of name changes");
				writer.newLine();
				writer.close();
			} catch (IOException e) {
				plugin.logger
						.severe("[NameChanger] Could not create NameChangerLog.txt!");
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	public boolean WriteNameChangerLog(String stringLog) {
		String stringToWrite = new String(); // Formatted string to write
		Date timeStamp = new Date(); // TimeStamp of event (timeStamp = now)
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd"); // Date
																		// formatter
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss:SSS"); // Time
																			// formatter
		stringToWrite = "[" + sdfDate.format(timeStamp) + "] ["
				+ sdfTime.format(timeStamp) + "] - ";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					NameChangerLog, true));
			writer.write(stringToWrite + stringLog);
			writer.newLine();
			writer.close();

			return true;

		} catch (IOException e) {
			this.logger
					.severe("[GMChecker] Could not write to NameChangerLog.txt!");
			e.printStackTrace();
		}
		return false; // If we get here, it didn't work
	}
}
