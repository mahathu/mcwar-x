package io.github.mhoffmann98.mcwar;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class MCWarEventListener implements Listener {
	private final HashMap<String, MCWarTeam> teamList;
	private MCWar plugin;
	private MCWarTools t;

	public MCWarEventListener(MCWar plugin, HashMap<String, MCWarTeam> teamList) {
		this.plugin = plugin;
		this.teamList = teamList;
		this.t = new MCWarTools(plugin);

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!plugin.isGameActive()) {
			player.setMetadata("ready", new FixedMetadataValue(plugin, false));
			player.setMetadata("moveable", new FixedMetadataValue(plugin, true));
			player.setGameMode(GameMode.SPECTATOR);
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}

		Player player = event.getPlayer();
		String teamPrefix = "No Team";

		event.setFormat("[" + teamPrefix + "] %s : %s");

		if (player.hasMetadata("team")) {
			event.setFormat("[" + player.getMetadata("team").get(0).asString() + "] %s: %s");
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		double pitch = player.getLocation().getPitch();
		double yaw = player.getLocation().getYaw();
		if (player.hasMetadata("moveable") && !player.getMetadata("moveable").get(0).asBoolean()
				&& yaw == player.getLocation().getYaw() && pitch == player.getLocation().getPitch()) { // player
																										// moving
																										// (not
																										// changing
																										// view
																										// angle)
			player.teleport(player);
			player.sendMessage("You can't move!");
		}
	}

	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent event) {
		Player sender = event.getPlayer();

		if (sender.getItemInHand().getType() == Material.COMPASS) {
			if (!sender.hasMetadata("team")) {
				return;
			}

			String playerTeam = sender.getMetadata("team").get(0).asString();

			double distance = 0.0;
			double closestDistance = 600000.0;
			Player closestPlayer = null;

			for (Player player : plugin.getActivePlayers()) {
				if (player == sender) {
					continue;
				}
				if (!(playerTeam.equals(player.getMetadata("team").get(0).asString()))) {
					//player from the loop is in a different team
					distance = player.getLocation().distance(sender.getLocation());

					if (distance < closestDistance) {
						closestDistance = distance;
						closestPlayer = player;
					}
				}

			}

			if (closestPlayer == null) {
				sender.sendMessage("No player was found!");
			}

			String output = "Pointing compass towards " + closestPlayer.getName();

			if (sender.hasMetadata("kit") && sender.getMetadata("kit").get(0).asString().equalsIgnoreCase("spy")) {
				//the player is a spy!
				output += " (y: " + closestPlayer.getLocation().getY() + ")";
			}

			sender.sendMessage(output);
			sender.setCompassTarget(closestPlayer.getLocation());
			return;
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();

		if (!player.hasMetadata("team")) {
			return;
		}

		String teamName = player.getMetadata("team").get(0).asString();

		if (plugin.getConfig().getBoolean("preferences.ticketsEnable")) {
			int newTickets = plugin.getTeamTickets().get(teamName.toLowerCase()) - 1;

			plugin.getTeamTickets().put(teamName.toLowerCase(), newTickets);
			plugin.getObjective().getScore(Bukkit.getOfflinePlayer(teamName)).setScore(newTickets);

			if (newTickets <= 0) {
				Bukkit.broadcastMessage(teamName + " is out of the game!");
				plugin.getTeamList().get(teamName).lose();
			}
		}
	}
}
