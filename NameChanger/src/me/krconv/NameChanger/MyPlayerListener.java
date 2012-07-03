package me.krconv.NameChanger;

import java.util.Map;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.WorldServer;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MyPlayerListener implements Listener {


	NameChanger plugin;

	MyPlayerListener(NameChanger instance) {
		plugin = instance;
	}
	
	
	
	

	@EventHandler
	public void PlayerJoinAltChanger(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.sendMessage("hi");
		if (plugin.RealToAltList.containsKey(player)) {
			String target = plugin.RealToAltList.get(player);
			player.sendMessage("hi");
			if (player.getServer().getPlayer(target) == null) {
				player.sendMessage("hi");
				event.setJoinMessage(ChatColor.YELLOW
						+ plugin.RealToAltList.get(player) + " has joined the game.");
				System.out.print("[NameChanger] " + player.getName()
						+ " automatically logged in as "
						+ plugin.RealToAltList.get(player) + "!");

				player.saveData();
				player.setPlayerListName(target);
				player.setDisplayName(target);
				EntityPlayer playerSelf = ((CraftPlayer) player).getHandle();
				setPlayerNameSelf(playerSelf, target);
				player.loadData();
				player.teleport(player.getLocation());
				player.setGameMode(player.getServer().getDefaultGameMode());
				player.sendMessage(ChatColor.GREEN + "Your name is now "
						+ player.getName() + ".");
				player.sendMessage("hi");
			}
		}
	}

	public void setPlayerNameSelf(EntityPlayer playerSelf, String newName) {
		WorldServer world = (WorldServer) playerSelf.world;
		EntityTracker tracker = world.tracker;
		tracker.untrackEntity(playerSelf);
		playerSelf.name = newName;
		tracker.track(playerSelf);

	}
}
