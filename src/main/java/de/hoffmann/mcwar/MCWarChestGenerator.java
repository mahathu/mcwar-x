package de.hoffmann.mcwar;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class MCWarChestGenerator implements Runnable {

	private int mincenterPosX, mincenterPosZ, centerPosX, centerPosZ, centerPosY;
	private Location chestPosition, platformCenterPosition;
	private MCWar plugin;
	private List<MCWarChestContent> chestContent;
	private Material[] chestTrash = { Material.DIRT, Material.WEB, Material.GRAVEL, Material.SAND, Material.CLAY };
	private Material platformMaterial = Material.STONE;
	private Material cornerMaterial = platformMaterial;

	public MCWarChestGenerator(MCWar plugin, List<MCWarChestContent> chestContentList) {
		this.mincenterPosX = (int) plugin.getMapCenter().getX() - plugin.getMapSize();
		this.mincenterPosZ = (int) plugin.getMapCenter().getZ() - plugin.getMapSize();
		this.plugin = plugin;
		this.chestContent = chestContentList;
	}

	@Override
	public void run() {
		if (plugin.isGameActive()) {
			centerPosX = mincenterPosX + (int) (Math.random() * plugin.getMapSize() * 2);
			centerPosZ = mincenterPosZ + (int) (Math.random() * plugin.getMapSize() * 2);
			centerPosY = Bukkit.getServer().getWorld(plugin.getWorldName()).getHighestBlockYAt(centerPosX, centerPosZ)
					+ 3;
			platformCenterPosition = new Location(Bukkit.getServer().getWorld(plugin.getWorldName()), centerPosX,
					centerPosY, centerPosZ);
			chestPosition = platformCenterPosition.clone().add(0, 1, 0); // on
																			// top
																			// of
																			// the
																			// platform

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
					blockPos.getBlock().setType(platformMaterial);
				}
			}
			for (int i = -1; i < 2; i++) {
				for (int j = -3; j < 4; j++) {
					Location blockPos = platformCenterPosition.clone().add(j, 0, i);
					blockPos.getBlock().setType(platformMaterial);
				}
			}

			int[] corners = { -2, 2 };
			for (int x = 0; x < 2; x++) {
				for (int z = 0; z < 2; z++) {
					platformCenterPosition.clone().add(corners[x], 0, corners[z]).getBlock().setType(cornerMaterial);
				}
			}
			platformCenterPosition.clone().add(-2, 0, 2).getBlock().setType(cornerMaterial);
			platformCenterPosition.clone().add(-2, 0, -2).getBlock().setType(platformMaterial);
			platformCenterPosition.clone().add(2, 0, 2).getBlock().setType(platformMaterial);
			platformCenterPosition.clone().add(2, 0, -2).getBlock().setType(platformMaterial);

			chestPosition.getBlock().setType(Material.CHEST);
			Chest chest = (Chest) chestPosition.getBlock().getState();
			Random r = new Random();
			for (int i = 0; i < 27; i++) { // fill the chest with random trash
				if (Math.random() < 0.5) {
					// picks a random item out of the trash list
					ItemStack trashItem = new ItemStack(chestTrash[r.nextInt(chestTrash.length)], r.nextInt(4) + 1);
					chest.getInventory().setItem(i, trashItem);
				}
			}

			for (MCWarChestContent item : chestContent) { // adds every item in
															// the loot list
															// with a certain
															// probability.
															// items can
															// overwrite each
															// other as they get
															// added at a random
															// position

				if (Math.random() < item.getChance()) {
					String name = item.getName();
					int amount = item.getAmount();
					ItemStack loot = new ItemStack(Material.getMaterial(name), amount);
					chest.getInventory().setItem(r.nextInt(27), loot);
				}
			}

			Bukkit.broadcastMessage(
					ChatColor.GOLD + "A new chest has been generated at (" + centerPosX + "x|" + centerPosZ + "z)");

		}
	}
}
