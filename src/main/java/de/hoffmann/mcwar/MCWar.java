package de.hoffmann.mcwar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class MCWar extends JavaPlugin {
	private Location mapCenter;

	private boolean gameActive;

	private Map<String, Integer> teamTickets;
	private ScoreboardManager manager;
	private Scoreboard board;
	private Objective objective;
	private WorldBorder wb;
	private String worldName;

	private final HashMap<String, MCWarTeam> teamList = new HashMap<>();
	private final HashMap<String, MCWarKit> kitList = new HashMap<>();
	private final List<MCWarChestContent> chestContentList = new ArrayList<>();

	private ArrayList<String> activeTeams = new ArrayList<>(); // teams with at least 1 player
	private ArrayList<Player> activePlayers;

	@Override
	public void onEnable() {
		worldName = this.getConfig().getString("preferences.worldName");

		setGameActive(false);
		setWb(Bukkit.getWorld(worldName).getWorldBorder());
		manager = Bukkit.getScoreboardManager();
		setBoard(manager.getNewScoreboard());
		setObjective(getBoard().registerNewObjective("tickets", "dummy"));

		getWb().setSize(60000000);

		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();

		this.t = new MCWarTools(this);

		int mapCenterX = this.getConfig().getInt("preferences.mapCenterX");
		int mapCenterZ = this.getConfig().getInt("preferences.mapCenterZ");

		setMapCenter(new Location(Bukkit.getWorld(worldName), mapCenterX, 0, mapCenterZ));

		new MCWarEventListener(this, getTeamList());

		String[] commands = { "createTeam", "deleteTeam", "joinTeam", "leaveTeam", "ready", "notready", "kit", "showkits", "kits", "randomChests", "mapSize",
				"nether", "setTimer", "timer", "setMaxKills", "maxKills", "setTickets", "tickets", "assignPlayer", "resetTeams", "setMapCenter", "startMatch",
				"stopMatch" };

		for (String commandName : commands) {
			this.getCommand(commandName).setExecutor(new MCWarCommandExecutor(this, getTeamList(), getKitList()));
		}

		for (String key : this.getConfig().getConfigurationSection("kits").getKeys(false)) {

			String kitPath = "kits." + key;
			String kitName = this.getConfig().getString(kitPath + ".name");

			String description = this.getConfig().getString(kitPath + ".description");

			MCWarKit newKit = new MCWarKit(kitName, description, this);

			for (String itemName : this.getConfig().getConfigurationSection(kitPath + ".items").getKeys(false)) {
				int itemAmount = Integer.parseInt(this.getConfig().getString(kitPath + ".items." + itemName + ".amount"));

				newKit.addStartingItem(itemName, itemAmount);
			}

			getKitList().put(kitName.toUpperCase(), newKit);
		}

		for (String key : this.getConfig().getConfigurationSection("chestcontent").getKeys(false)) {

			String itemPath = "chestcontent." + key;
			String itemMaterial = this.getConfig().getString(itemPath + ".material");
			float itemChance = Float.parseFloat(this.getConfig().getString(itemPath + ".chance"));
			int itemAmount = this.getConfig().getInt(itemPath + ".amount");
			MCWarChestContent newChestContent = new MCWarChestContent(itemMaterial, itemChance, itemAmount);
			getChestContentList().add(newChestContent);
		}
	}

	@Override
	public void onDisable() {

	}

	public boolean startMatch(int timer) {

		for (String teamName : this.getTeamList().keySet()) {
			if (teamList.get(teamName).getPlayerCount() > 0) {
				getActiveTeams().add(teamName);
			}
		}

		if (getActiveTeams().size() < 2) { // there need to be at least 2 active teams
			return false;
		}

		if (timer > 0) {
			Bukkit.broadcastMessage(ChatColor.BOLD + "The match will start in " + timer + " seconds!");
		} else {
			Bukkit.broadcastMessage(ChatColor.BOLD + "The match is about to start!");
		}
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new MCWarRunMatch(this, getActiveTeams()), timer * 20);

		return true;
	}

	public boolean getGameActive() {
		return isGameActive();
	}

	public void stopMatch() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {

			t.resetPlayer(p);

			p.setMetadata("moveable", new FixedMetadataValue(this, true));
			p.setGameMode(GameMode.SPECTATOR);

			getActiveTeams().clear();
			getActivePlayers().clear();
		}

		setGameActive(false);
	}

	public String getWorldName() {
		return worldName;
	}

	public Map<String, Integer> getTeamTickets() {
		return teamTickets;
	}

	public void setTeamTickets(Map<String, Integer> teamTickets) {
		this.teamTickets = teamTickets;
	}

	public Objective getObjective() {
		return objective;
	}

	public void setObjective(Objective objective) {
		this.objective = objective;
	}

	public Location getMapCenter() {
		return mapCenter;
	}

	public void setMapCenter(Location mapCenter) {
		this.mapCenter = mapCenter;
	}

	public HashMap<String, MCWarTeam> getTeamList() {
		return teamList;
	}

	public WorldBorder getWb() {
		return wb;
	}

	public void setWb(WorldBorder wb) {
		this.wb = wb;
	}

	public boolean isGameActive() {
		return gameActive;
	}

	public void setGameActive(boolean gameActive) {
		this.gameActive = gameActive;
	}

	public Scoreboard getBoard() {
		return board;
	}

	public void setBoard(Scoreboard board) {
		this.board = board;
	}

	public HashMap<String, MCWarKit> getKitList() {
		return kitList;
	}

	public ArrayList<Player> getActivePlayers() {
		return activePlayers;
	}

	public void setActivePlayers(ArrayList<Player> activePlayers) {
		this.activePlayers = activePlayers;
	}

	public ArrayList<String> getActiveTeams() {
		return activeTeams;
	}

	public void setActiveTeams(ArrayList<String> activeTeams) {
		this.activeTeams = activeTeams;
	}

	public int getMapSize() {
		return this.getConfig().getInt("preferences.mapSize");
	}

	public List<MCWarChestContent> getChestContentList() {
		return chestContentList;
	}
}