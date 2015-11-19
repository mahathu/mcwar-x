package de.hoffmann.mcwar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class MCWarTools {
	private MCWar plugin;

	public MCWarTools(MCWar plugin) {
		this.plugin = plugin;
	}

	public boolean resetPlayer(Player player) {

		if (!Bukkit.getOnlinePlayers().contains(player)) { //Player not online
			return false;
		}

		player.setHealth(20);
		player.setFoodLevel(20);
		player.setExhaustion(0);
		player.setExp(0);
		player.setLevel(0);

		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);

		player.setMetadata("ready", new FixedMetadataValue(plugin, false));
		return true;
	}
}
