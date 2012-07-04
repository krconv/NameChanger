package me.krconv.NameChanger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
	//public Map<Player, String> RealToAltList = new HashMap<Player, String>();
	public AutoAltManager autoAltManager = new AutoAltManager();

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
		this.logger.info(pdfFile.getName() + " v" + pdfFile.getVersion()
				+ " has been enabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = (Player) sender;
		PluginDescriptionFile pdfFile = this.getDescription();
		if (commandLabel.equalsIgnoreCase("name")) {
			if (player.hasPermission("namechanger.basic")) {
				if (args.length == 0) {
					player.sendMessage(ChatColor.DARK_RED + "-----"
							+ ChatColor.GOLD + "NameChanger v"
							+ pdfFile.getVersion() + ChatColor.DARK_RED
							+ "-----");
					player.sendMessage(ChatColor.GOLD
							+ "by krconv and EliteSandwich");
					player.sendMessage(ChatColor.GOLD + "Use " + ChatColor.GRAY
							+ "/name help " + ChatColor.GOLD + "for help");
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("help")) {
						player.sendMessage(ChatColor.DARK_RED + "-----"
								+ ChatColor.GOLD + "NameChanger Help"
								+ ChatColor.DARK_RED + "-----");
						player.sendMessage(ChatColor.GRAY + "/name "
								+ ChatColor.DARK_RED + "- " + ChatColor.GOLD
								+ "Displays NameChanger version");
						player.sendMessage(ChatColor.GRAY + "/name help "
								+ ChatColor.DARK_RED + "- " + ChatColor.GOLD
								+ "Displays this page");
						player.sendMessage(ChatColor.GRAY + "/name <Name> "
								+ ChatColor.DARK_RED + "- " + ChatColor.GOLD
								+ "Changes your name");
						player.sendMessage(ChatColor.GRAY
								+ "/name <Target> <Name> " + ChatColor.DARK_RED
								+ "- " + ChatColor.GOLD
								+ "Changes a target player's name");
						player.sendMessage(ChatColor.GRAY + "/name reload "
								+ ChatColor.DARK_RED + "- " + ChatColor.GOLD
								+ "Reloads the config");
					} else if (args[0].equalsIgnoreCase("reload")) {
						if (player.hasPermission("namechanger.reload")) {
							reloadConfig();
							player.sendMessage(ChatColor.GOLD
									+ "NameChanger config reloaded!");
							System.out.print("[NameChanger] Config reloaded!");
						} else {
							player.sendMessage(ChatColor.RED
									+ "You do not have permission to do this.");

						}
					} else {
						if (player.hasPermission("namechanger.name." + args[0])) {
							if (player.getServer().getPlayer(args[0]) == null) {
								EntityPlayer playerSelf = (EntityPlayer) ((CraftPlayer) sender)
										.getHandle();
								System.out.print("[NameChanger] "
										+ player.getName()
										+ " changed their name to " + args[0]
										+ "!");
								if (getConfig().getBoolean(
										"broadcastQuitMessage") == true) {
									getServer().broadcastMessage(
											ChatColor.YELLOW + player.getName()
													+ " has left the game.");
								}
								player.saveData();
								player.setPlayerListName(args[0]);
								player.setDisplayName(args[0]);
								setPlayerNameSelf(playerSelf, args[0]);
								player.loadData();
								player.teleport(player.getLocation());
								player.setGameMode(getServer()
										.getDefaultGameMode());
								player.sendMessage(ChatColor.GREEN
										+ "Your name is now "
										+ player.getName() + ".");
								if (getConfig().getBoolean(
										"broadcastJoinMessage") == true) {
									getServer().broadcastMessage(
											ChatColor.YELLOW + player.getName()
													+ " has joined the game.");
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
					Player target = getServer().getPlayer(args[0]);
					if (player.hasPermission("namechanger.other." + args[1])) {
						if (target.getServer().getPlayer(args[0]) != null) {
							if (target.getServer().getPlayer(args[1]) == null) {
								EntityPlayer playerOther = (EntityPlayer) ((CraftPlayer) target)
										.getHandle();
								System.out.print("[NameChanger] "
										+ player.getName() + " changed "
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
								setPlayerNameOther(playerOther, args[1]);
								target.loadData();
								target.teleport(target.getLocation());
								target.setGameMode(getServer()
										.getDefaultGameMode());
								target.sendMessage(ChatColor.GREEN
										+ "Your name has been changed to "
										+ player.getName() + ".");
								if (getConfig().getBoolean(
										"broadcastJoinMessage") == true) {
									getServer().broadcastMessage(
											ChatColor.YELLOW + target.getName()
													+ " has joined the game.");
								}

							} else {
								player.sendMessage(ChatColor.RED + args[0]
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
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("auto")) {
						if (player.hasPermission("namechanger.auto." + args[1] + "." + args[2])) {
							if (!isValidUserName(args[1])) {
								// Validate target player name
								player.sendMessage(ChatColor.RED + "<Target> '"  + args[1] + "' is not a possible Minecraft name.");
								return false;
							}
							if (!isValidUserName(args[2])) {
								// Validate alt name
								player.sendMessage(ChatColor.RED + "Alt <Name> '"  + args[1] + "' is not a possible Minecraft name.");
								return false;
							}
							// Make sure alt name not already in use for another player
							String conflictingPlayer = autoAltManager.getPlayerNameForAlt(args[1]);
							if (conflictingPlayer != null) {
								// Conflict, cannot proceed
								player.sendMessage(ChatColor.RED + "Player '" + conflictingPlayer + "' already uses that automatic alt. Remove this entry first.");
								return false;
							}
							//Player target = getServer().getPlayer(args[1]); // Changed this so that it works for offline players, I think
							int setResult;
							setResult = autoAltManager.setAltNameForPlayer(args[1], args[2]);
							
							if (setResult == 0) {
								// Successfully set automatic alt name for target player
								System.out.print("[NameChanger] " + player.getName() + " has set "  + args[1] + "'s name automatically change to " + args[2] + " on login!");
								player.sendMessage(ChatColor.GREEN + "You have set " + args[1] + " to automatically log in as " + args[2] + ".");	
							} else if (setResult == 1) {
								// Cannot have more than 1 player assigned to an alt
								player.sendMessage(ChatColor.RED + "Alt <Name> '" + args[1] + "' is already an automatic alt for another player.");	
							} else if (setResult == 2) {
								// Either player or alt is an impossible minecraft name, but we already check that above so no action needed here	
							} else if (setResult == 3) {
								// Unknown error - passed verification but somehow didn't complete
								player.sendMessage(ChatColor.RED + "Unknown error when setting automatic alt for player.");	
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
		// Ensures user name contains only valid characters and between 2-16 characters long
		Pattern p = Pattern.compile("[A-Za-z0-9_]{2,16}");
		Matcher m = p.matcher(toTest);
		return m.matches();
	}
}
