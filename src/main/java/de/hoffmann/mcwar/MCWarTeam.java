package de.hoffmann.mcwar;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class MCWarTeam {
	//TODO: eventually remove plugin if I dont need metadata
	private MCWar plugin;
	private String teamName, joinKey, creatorName;
	private List<String> players = new ArrayList<String>();

	public MCWarTeam(MCWar plugin, String teamName, String joinKey, String creatorName) {
		this.teamName = teamName;
		this.joinKey = joinKey;
		this.plugin = plugin;
		this.creatorName = creatorName;
	}
	
	public String getCreator(){
		return creatorName;
	}

	public void addPlayer(Player player) {
		String playerName = player.getName();
		players.add(playerName);
		player.setMetadata("team", new FixedMetadataValue(plugin, teamName));
	}
	
	public String getJoinKey(){
		return joinKey;
	}

	public boolean isPlayerInTeam(String playerName){
		return players.contains(playerName);
	}
	
	public void removePlayer(Player player) {
		players.remove(player.getName());
		if( player.hasMetadata("team") ){
			player.removeMetadata("team", plugin);
		}
	}

	public int getPlayerCount() {
		return players.size();
	}
	
	public List<String> getPlayers() {
		return players;
	}

	public void lose() {
		for(String playerName : players){
			Bukkit.getPlayer(playerName).setGameMode(GameMode.SPECTATOR);
		}
	}
	
}
