package io.mhoffmann98.github.mcwar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class MCWarCommandExecutor implements CommandExecutor {
	private final MCWar plugin;
	private HashMap<String, MCWarTeam> teamList;
	private HashMap<String, MCWarKit> kitList;
	private String worldName;

	public MCWarCommandExecutor(MCWar plugin, HashMap<String, MCWarTeam> teamList, HashMap<String, MCWarKit> kitList) {
		this.plugin = plugin;
		this.teamList = teamList;
		this.kitList = kitList;
		this.worldName = plugin.getWorldName();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equalsIgnoreCase("createTeam")) {

			if (args.length < 1 || args.length > 2) {
				return false;
			}

			String teamName = args[0];
			String joinKey = "";
			if (args.length > 1) {
				joinKey = args[1];
			}

			MCWarTeam newTeam = new MCWarTeam(plugin, teamName, joinKey, sender.getName());

			if (!teamList.containsKey(teamName)) {
				teamList.put(teamName, newTeam);
				sender.sendMessage(
						"Created new team \"" + teamName + "\" with join key \"" + joinKey + "\" successfully!");
			} else {
				sender.sendMessage(ChatColor.RED + "Team already exists!");
			}
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("deleteTeam")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
				return true;
			}

			if (args.length != 1) {
				return false;
			}

			String teamName = args[0];

			if (!teamList.containsKey(teamName)) {
				sender.sendMessage(ChatColor.RED + "Team couldn't be found!");
				return true;
			}

			if (!teamList.get(teamName).getCreator().equalsIgnoreCase(sender.getName())) {
				sender.sendMessage(ChatColor.RED + "You are not the creator of this team!");
				return true;
			}

			ArrayList<String> removePlayerNames = new ArrayList<>(); // this
																		// step
																		// is
																		// required
																		// to
																		// avoid
																		// java.util.ConcurrentModificationException
			for (String teamPlayerName : teamList.get(teamName).getPlayers()) {
				removePlayerNames.add(teamPlayerName);
			}

			for (String teamPlayerName : removePlayerNames) {
				Player teamPlayer = plugin.getServer().getPlayer(teamPlayerName);
				teamList.get(teamName).removePlayer(teamPlayer);
				Bukkit.broadcastMessage("MCWar: " + teamPlayerName + " has left Team \"" + teamName + "\"!");
			}

			teamList.remove(teamName);
			Bukkit.broadcastMessage("MCWar: Team \"" + teamName + "\" was deleted by the creator!");

			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("joinTeam")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
				return true;
			}

			String playerName = sender.getName();

			if (args.length < 1 || args.length > 2) {
				return false;
			}

			String teamName = args[0];
			String joinKey = "";
			if (args.length > 1) {
				joinKey = args[1];
			}

			if (!teamList.containsKey(teamName)) {
				sender.sendMessage(ChatColor.RED + "Team doesn't exist!");
				return true;
			}

			String teamKey = teamList.get(teamName).getJoinKey();
			if (!joinKey.equals(teamKey)) {
				sender.sendMessage(ChatColor.RED + "Join key incorrect!");
				return true;
			}

			if (teamList.get(teamName).isPlayerInTeam(playerName)) {
				sender.sendMessage(ChatColor.RED + "You are already part of this team!");
				return true;
			}

			for (String team : teamList.keySet()) { // checks if the player is
													// in another team already
				if (teamList.get(team).isPlayerInTeam(playerName)) {
					sender.sendMessage(ChatColor.RED + "You are already part of: " + team);
					return true;
				}
			}

			teamList.get(teamName).addPlayer(sender.getServer().getPlayer(playerName));
			Bukkit.broadcastMessage("MCWar: " + playerName + " joined Team \"" + teamName + "\"! (Total members: "
					+ teamList.get(teamName).getPlayerCount() + ")");

			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("leaveTeam")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
				return true;
			}

			String playerName = sender.getName();

			if (args.length != 0) {
				return false;
			}

			Player player = (Player) sender;

			for (String team : teamList.keySet()) {
				if (teamList.get(team).isPlayerInTeam(playerName)) {
					teamList.get(team).removePlayer(player);
					Bukkit.broadcastMessage("MCWar: " + playerName + " has left Team \"" + team + "\"! (Total members: "
							+ teamList.get(team).getPlayerCount() + ")");
					return true;
				}
			}

			sender.sendMessage(ChatColor.RED + "You are in no team!");
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("ready")) {
			if (plugin.isGameActive()) {
				sender.sendMessage(ChatColor.RED + "You can't change your ready status while a game is active!");
				return true;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
				return true;
			}

			if (args.length != 0) {
				return false;
			}

			Player player = (Player) sender;

			String playerName = sender.getName();

			if (player.hasMetadata("ready") && player.getMetadata("ready").get(0).asBoolean()) {
				sender.sendMessage(ChatColor.RED + "You are ready already!");
				return true;
			}

			if (!player.hasMetadata("team")) {
				sender.sendMessage(ChatColor.RED + "You need to join a team first!");
				return true;
			}
			player.setMetadata("ready", new FixedMetadataValue(plugin, true));

			int readyPlayers = 0;
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer.hasMetadata("ready")) {
					readyPlayers++;
				}
			}

			Bukkit.broadcastMessage("MCWar: " + playerName + " is ready. (" + readyPlayers + "/"
					+ Bukkit.getOnlinePlayers().size() + ")");

			if (readyPlayers == Bukkit.getOnlinePlayers().size()) {
				Bukkit.broadcastMessage(ChatColor.BOLD + "MCWar: All players are ready!");
				plugin.startMatch(10);
			}

			return true;

		}

		else if (cmd.getName().equalsIgnoreCase("notready")) {
			String returnMessage = "";

			if (plugin.isGameActive()) {
				sender.sendMessage(ChatColor.RED + "You can't change your ready status while a game is active!");
				return true;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return true;
			}

			if (args.length != 0) {
				return false;
			}

			Player player = (Player) sender;
			String playerName = sender.getName();

			if (player.getMetadata("ready").get(0).asBoolean()) {
				player.setMetadata("ready", new FixedMetadataValue(plugin, false));

				int readyPlayers = 0;
				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					if (onlinePlayer.getMetadata("ready").get(0).asBoolean()) {
						readyPlayers++;
					}
				}

				Bukkit.broadcastMessage("MCWar: " + playerName + " is not ready. (" + readyPlayers + "/"
						+ Bukkit.getOnlinePlayers().size() + ")");

				return true;
			} else {
				returnMessage = ChatColor.RED + "You are not ready!";
			}
			sender.sendMessage(returnMessage);
			return true;
		}

		//
		// else if (cmd.getName().equalsIgnoreCase("surrender")) {
		//
		// }
		//
		// else if (cmd.getName().equalsIgnoreCase("spectate")) {
		//
		// }
		//
		// else if (cmd.getName().equalsIgnoreCase("setspawn")) {
		//
		// }
		//
		else if (cmd.getName().equalsIgnoreCase("kit")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
				return true;
			}

			if (args.length != 1) {
				return false;
			}

			String input = args[0].toUpperCase();

			if (!kitList.containsKey(input)) {
				sender.sendMessage(ChatColor.RED + "Kit not found.");
				return true;
			}

			MCWarKit kit = kitList.get(input);
			sender.sendMessage("Your kit: " + kit.getKitName());

			Player player = (Player) sender;
			player.setMetadata("kit", new FixedMetadataValue(plugin, input.toUpperCase()));

			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("showkits")) {
			for (Map.Entry<String, MCWarKit> kit : kitList.entrySet()) {
				String kitName = kit.getKey();
				String description = kit.getValue().getDescription();

				sender.sendMessage(ChatColor.BOLD + kitName);
				sender.sendMessage(description);

				sender.sendMessage("");
			}

			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("assignPlayer")) {
			if (args.length != 2) {
				return false;
			}

			String playerName = args[0];
			String teamName = args[1];
			Player player = Bukkit.getPlayer(playerName);
			if (!(player instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Player not found.");
				return true;
			}

			if (!teamList.containsKey(teamName)) {
				sender.sendMessage(ChatColor.RED + "Team not found.");
				return true;
			}

			for (String team : teamList.keySet()) {
				if (teamList.get(team).isPlayerInTeam(playerName)) {
					teamList.get(team).removePlayer(player);
					Bukkit.broadcastMessage("MCWar: " + playerName + " has left Team \"" + team + "\"! (Total members: "
							+ teamList.get(team).getPlayerCount() + ")");
				}
			}
			teamList.get(teamName).addPlayer(player);
			Bukkit.broadcastMessage("MCWar: " + playerName + " was assigned to Team \"" + teamName + "\"! (Total members: "
					+ teamList.get(teamName).getPlayerCount());

			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("resetTeams")) {
			ArrayList<String> removeTeamNames = new ArrayList<>(); // this step
																	// is
																	// required
																	// to avoid
																	// java.util.ConcurrentModificationException
			for (String teamName : teamList.keySet()) {
				removeTeamNames.add(teamName);
			}

			for (String team : removeTeamNames) {

				ArrayList<String> removePlayerNames = new ArrayList<>(); // this
																			// step
																			// is
																			// required
																			// to
																			// avoid
																			// java.util.ConcurrentModificationException
				for (String teamPlayerName : teamList.get(team).getPlayers()) {
					removePlayerNames.add(teamPlayerName);
				}

				for (String teamPlayerName : removePlayerNames) {
					Player teamPlayer = plugin.getServer().getPlayer(teamPlayerName);
					teamList.get(team).removePlayer(teamPlayer);
					Bukkit.broadcastMessage("MCWar: " + teamPlayerName + " has left Team \"" + team + "\"!");
				}

				teamList.remove(team);
				Bukkit.broadcastMessage("MCWar: Team \"" + team + "\" was deleted by an operator!");

			}

			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("kits")) {
			if (args.length != 1) {
				return false;
			}

			if (args[0].equals("enable")) {
				plugin.getConfig().set("preferences.kitsEnable", true);
				Bukkit.broadcastMessage("MCWar: Kits are now enabled.");
				return true;
			}

			if (args[0].equals("disable")) {
				plugin.getConfig().set("preferences.kitsEnable", false);
				Bukkit.broadcastMessage("MCWar: Kits are now disabled.");
				return true;
			}

			return false;
		}

		else if (cmd.getName().equalsIgnoreCase("randomChests")) {
			if (args.length != 1) {
				return false;
			}

			if (args[0].equals("enable")) {
				plugin.getConfig().set("preferences.randomChestsEnable", true);
				Bukkit.broadcastMessage("MCWar: Random chests are now enabled.");
				return true;
			}

			if (args[0].equals("disable")) {
				plugin.getConfig().set("preferences.randomChestsEnable", false);
				Bukkit.broadcastMessage("MCWar: Random chests are now disabled.");
				return true;
			}

			return false;
		}

		else if (cmd.getName().equalsIgnoreCase("mapSize")) {
			if (args.length != 1) {
				return false;
			}

			plugin.getConfig().set("preferences.mapSize", Integer.parseInt(args[0]));
			Bukkit.broadcastMessage("MCWar: Map size set to " + plugin.getConfig().getInt("preferences.mapSize"));
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("nether")) {
			if (args.length != 1) {
				return false;
			}

			if (args[0].equals("enable")) {
				plugin.getConfig().set("preferences.netherEnable", true);
				Bukkit.broadcastMessage("MCWar: Nether is now enabled.");
				return true;
			}

			if (args[0].equals("disable")) {
				plugin.getConfig().set("preferences.netherEnable", false);
				Bukkit.broadcastMessage("MCWar: Nether is now disabled.");
				return true;
			}

			return false;
		}

		else if (cmd.getName().equalsIgnoreCase("setTimer")) {
			if (args.length != 1) {
				return false;
			}

			plugin.getConfig().set("preferences.maxTime", Integer.parseInt(args[0]));
			Bukkit.broadcastMessage("MCWar: Timer size set to " + args[0]);
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("timer")) {
			if (args.length != 1) {
				return false;
			}

			if (args[0].equals("enable")) {
				plugin.getConfig().set("preferences.timerEnable", true);
				Bukkit.broadcastMessage(
						"MCWar: The timer is now enabled. (" + plugin.getConfig().getInt("preferences.maxTime") + ")");
				return true;
			}

			if (args[0].equals("disable")) {
				plugin.getConfig().set("preferences.timerEnable", false);
				Bukkit.broadcastMessage("MCWar: Nether is now disabled.");
				return true;
			}

			return false;
		}

		else if (cmd.getName().equalsIgnoreCase("setMaxKills")) {
			if (args.length != 1) {
				return false;
			}

			plugin.getConfig().set("preferences.maxKills", Integer.parseInt(args[0]));
			Bukkit.broadcastMessage("MCWar: Max kills set to " + args[0]);
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("maxKills")) {
			if (args.length != 1) {
				return false;
			}

			if (args[0].equals("enable")) {

				if (plugin.getConfig().getBoolean("preferences.ticketsEnable")) {
					plugin.getConfig().set("preferences.ticketsEnable", false);
					Bukkit.broadcastMessage("MCWar: Ticket limit is now disabled.");
				}

				plugin.getConfig().set("preferences.maxKillsEnable", true);
				Bukkit.broadcastMessage("MCWar: Kill limit is now enabled. ("
						+ plugin.getConfig().getInt("preferences.maxKills") + ")");
				return true;
			}

			if (args[0].equals("disable")) {
				plugin.getConfig().set("preferences.maxKillsEnable", false);
				Bukkit.broadcastMessage("MCWar: Kill limit is now disabled.");
				return true;
			}

			return false;
		}

		else if (cmd.getName().equalsIgnoreCase("setTickets")) {
			if (args.length != 1) {
				return false;
			}

			plugin.getConfig().set("preferences.tickets", Integer.parseInt(args[0]));
			Bukkit.broadcastMessage("MCWar: Ticket limit set to " + args[0]);
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("tickets")) {
			if (args.length != 1) {
				return false;
			}

			if (args[0].equals("enable")) {

				if (plugin.getConfig().getBoolean("preferences.maxKillsEnable")) {
					plugin.getConfig().set("preferences.maxKillsEnable", false);
					Bukkit.broadcastMessage("MCWar: Kill limit is now disabled.");
				}

				plugin.getConfig().set("preferences.ticketsEnable", true);
				Bukkit.broadcastMessage("MCWar: Ticket limit is now enabled. ("
						+ plugin.getConfig().getInt("preferences.maxTime") + ")");
				return true;
			}

			if (args[0].equals("disable")) {
				plugin.getConfig().set("preferences.ticketsEnable", false);
				Bukkit.broadcastMessage("MCWar: Ticket limit is now disabled.");
				return true;
			}

			return false;
		}

		else if (cmd.getName().equalsIgnoreCase("setMapCenter")) {
			int posX, posZ;

			if (!(sender instanceof Player)) {
				if (args.length != 2) {
					return false;
				}
				posX = Integer.parseInt(args[0]);
				posZ = Integer.parseInt(args[1]);
			}

			else {
				Player player = (Player) sender;
				if (args.length == 2) {
					posX = Integer.parseInt(args[0]);
					posZ = Integer.parseInt(args[1]);
				}

				else if (args.length == 0) {
					posX = player.getLocation().getBlockX();
					posZ = player.getLocation().getBlockZ();
				}

				else
					return false;
			}
			plugin.setMapCenter(new Location(Bukkit.getWorld(worldName), posX, 0, posZ));

			Bukkit.broadcastMessage("New Map center set to: (" + posX + "|" + posZ + ")");
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("startMatch")) {
			if (args.length > 1) {
				return false;
			}

			int timer = 10;
			if (args.length > 0) {
				timer = Integer.parseInt(args[0]);
			}

			if (plugin.isGameActive()) {
				sender.sendMessage("A game is already active");
				return true;
			}

			if (!plugin.startMatch(timer)) {
				sender.sendMessage("There need to be at least 2 active teams!");
			}
			return true;
		}
		//
		// else if (cmd.getName().equalsIgnoreCase("endMatch")) {
		//
		// }
		//
		else if (cmd.getName().equalsIgnoreCase("stopMatch")) {
			plugin.stopMatch();
			return true;
		}
		return false;
	}
}
