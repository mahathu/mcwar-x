package de.hoffmann.mcwar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

public class MCWarRunMatch implements Runnable {
	private MCWar plugin;
	private Team team;
	private int timer;
	private ArrayList<String> activeTeams;
	private ArrayList<Player> activePlayers;
	private MCWarTools t;

	public MCWarRunMatch(MCWar plugin, ArrayList<String> activeTeams) {
		this.plugin = plugin;
		//this.timer = 30;
		this.timer = 0;
		this.activeTeams = activeTeams;
		this.activePlayers = new ArrayList<Player>();
		this.t = new MCWarTools(plugin);
	}

	@Override
	public void run() {
		plugin.setTeamTickets(new HashMap<String, Integer>()); // stores tickets for each team

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setGameMode(GameMode.SPECTATOR); // active players will become survival later
		}

		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "time set day");
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "weather clear");

		plugin.getObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
		plugin.getObjective().setDisplayName("Tickets");

		float distanceFromCenter = 0.8F;
		double radius = plugin.getConfig().getInt("preferences.mapSize") * distanceFromCenter;
		float angleDelta = 360 / activeTeams.size(); // sets up spawn points in a polygon
		for (int i = 0; i < activeTeams.size(); i++) { // iterates through all active teams

			String teamName = activeTeams.get(i);

			if (plugin.getBoard().getTeam(teamName) == null) {
				team = plugin.getBoard().registerNewTeam(teamName);
			}

			team.setDisplayName(teamName);
			team.setAllowFriendlyFire(false);

			plugin.getTeamTickets().put(teamName.toLowerCase(), plugin.getConfig().getInt("preferences.tickets")); // adds the team to the teamlist with the
																													// default
			// amount of
			// tickets

			double angle = angleDelta * (float) i + 0.5;

			int spawnX = (int) ((Math.cos(Math.toRadians(angle)) * radius) + plugin.getMapCenter().getBlockX());
			int spawnZ = (int) ((Math.sin(Math.toRadians(angle)) * radius) + plugin.getMapCenter().getBlockZ());
			int spawnY = Bukkit.getServer().getWorld(plugin.getWorldName()).getHighestBlockYAt((int) spawnX, (int) spawnZ) + 1; // finds a suitable place to
																																// spawn at

			Location spawnPoint = new Location(Bukkit.getWorld(plugin.getWorldName()), spawnX, spawnY, spawnZ);

			for (String playerName : plugin.getTeamList().get(teamName).getPlayers()) { // iterates through players in this team
				Player player = plugin.getServer().getPlayer(playerName);

				player.setGameMode(GameMode.SURVIVAL); // every player in any team will be in gamemode SURVIVAL

				player.getInventory().clear();
				if (plugin.getConfig().getBoolean("preferences.kitsEnable") && player.hasMetadata("kit")) {

					MCWarKit kit = plugin.getKitList().get(player.getMetadata("kit").get(0).asString());

					HashMap<String, Integer> items = kit.getStartingItems();

					for (Map.Entry<String, Integer> item : items.entrySet()) { // every Player gets his items from his kit
						String itemName = item.getKey();
						int itemAmount = item.getValue();
						ItemStack itemstack = new ItemStack(Material.getMaterial(itemName), itemAmount);

						player.getInventory().addItem(itemstack);
					}
				}

				team.addPlayer(player);

				activePlayers.add(player);

				
				t.resetPlayer(player);

				player.setMetadata("moveable", new FixedMetadataValue(plugin, false));
				player.teleport(spawnPoint);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawnpoint " + playerName + " " + spawnX + " " + spawnY + " " + spawnZ);
			}

			plugin.getObjective().getScore(Bukkit.getOfflinePlayer(teamName)).setScore(plugin.getConfig().getInt("preferences.tickets"));
		}

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.setScoreboard(plugin.getBoard());
		}

		plugin.setGameActive(true);

		plugin.getWb().setCenter(plugin.getMapCenter());
		plugin.getWb().setSize(plugin.getConfig().getInt("preferences.mapSize") * 2);

		Bukkit.broadcastMessage(ChatColor.BOLD + "The match will start in " + timer + " seconds!");

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() { // once per second
					public void run() {
						if (((timer <= 15 && timer % 5 == 0) || timer <= 5) && timer > 0) {
							Bukkit.broadcastMessage(ChatColor.BOLD + "The match will start in " + timer + " seconds!");
						}
						timer--;
					}
				}, 0, 20L);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.BOLD + "The match has been started!");
				for (Player player : activePlayers) {
					player.setFoodLevel(20);
					player.setExhaustion(0);
					player.setMetadata("moveable", new FixedMetadataValue(plugin, true));
				}
			}
		}, timer * 20L);


		List<MCWarChestContent> chestContentList = plugin.getChestContentList();
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new MCWarChestGenerator(plugin, chestContentList), timer + 15 * 60 * 20L, 10 * 60 * 20L); // starts generating
																																			// chests every 10
																																			// minutes after an
																																			// initial delay of
																																			// 15 minutes.
	}

}
