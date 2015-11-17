package de.hoffmann.mcwar;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class MCWarChestGenerator extends BukkitRunnable {

	private int maxcenterPosX, mincenterPosX, maxcenterPosZ, mincenterPosZ, centerPosX, centerPosZ, centerPosY;
	private Location chestPosition, platformCenterPosition;
	private MCWar plugin;
	private List<MCWarChestContent> chestContent;

	public MCWarChestGenerator(MCWar plugin, List<MCWarChestContent> chestContentList) {
		this.maxcenterPosX = (int) plugin.getMapCenter().getX() + plugin.getMapSize();
		this.mincenterPosX = (int) plugin.getMapCenter().getX() - plugin.getMapSize();
		this.maxcenterPosZ = (int) plugin.getMapCenter().getZ() + plugin.getMapSize();
		this.mincenterPosZ = (int) plugin.getMapCenter().getZ() - plugin.getMapSize();
		this.plugin = plugin;
		this.chestContent = chestContentList;
	}

	@Override
	public void run() {
		if (plugin.isGameActive()) {
			centerPosX = mincenterPosX + (int) (Math.random() * plugin.getMapSize() * 2);
			centerPosZ = mincenterPosZ + (int) (Math.random() * plugin.getMapSize() * 2);
			centerPosY = Bukkit.getServer().getWorld(plugin.getWorldName()).getHighestBlockYAt(centerPosX, centerPosZ) + 3;
			platformCenterPosition = new Location(Bukkit.getServer().getWorld(plugin.getWorldName()), centerPosX, centerPosY, centerPosZ);
			chestPosition = platformCenterPosition.clone().add(0, 1, 0); // on top of the platform

			// generate the platform
			// . . . . . . . . .
			// . . . P P P . . .
			// . . P P P P P . .
			// . P P P P P P P .
			// . P P P C P P P .
			// . P P P P P P P .
			// . . P P P P P . .
			// . . . P P P . . .
			// . . . . . . . . .
			for (int i = -1; i < 2; i++) {
				for (int j = -3; j < 4; j++) {
					Location blockPos = platformCenterPosition.clone().add(i, 0, j);
					blockPos.getBlock().setType(Material.STONE);
				}
			}
			for (int i = -1; i < 2; i++) {
				for (int j = -3; j < 4; j++) {
					Location blockPos = platformCenterPosition.clone().add(j, 0, i);
					blockPos.getBlock().setType(Material.STONE);
				}
			}

			platformCenterPosition.clone().add(-2, 0, 2).getBlock().setType(Material.STONE);
			platformCenterPosition.clone().add(-2, 0, -2).getBlock().setType(Material.STONE);
			platformCenterPosition.clone().add(2, 0, 2).getBlock().setType(Material.STONE);
			platformCenterPosition.clone().add(2, 0, -2).getBlock().setType(Material.STONE);

			Material[] chestTrash = { Material.DIRT, Material.WEB, Material.GRAVEL, Material.SAND, Material.CLAY };
			chestPosition.getBlock().setType(Material.CHEST);

			Chest chest = (Chest) chestPosition.getBlock().getState();
			Random r = new Random();
			for (int i = 0; i < 27; i++) {
				if (Math.random() < 0.5) {
					ItemStack trashItem = new ItemStack(chestTrash[r.nextInt(chestTrash.length)], r.nextInt(4) + 1); // picks a random piece of trash (1-4) to
																														// put it in the chest
					chest.getInventory().setItem(i, trashItem);
				}
			}

			for (MCWarChestContent item : chestContent) {
				String name = item.getName();
				float chance = item.getChance();
				int amount = item.getAmount();

				if (Math.random() < chance) {
					Bukkit.getLogger().info(name);
					ItemStack loot = new ItemStack(Material.getMaterial(name), amount);
					chest.getInventory().setItem(r.nextInt(27), loot);
				}
			}

			Bukkit.broadcastMessage(ChatColor.GOLD + "A new chest has been generated at (" + centerPosX + "x|" + centerPosZ + "z)");

		} else {
			this.cancel();
		}
	}
}
