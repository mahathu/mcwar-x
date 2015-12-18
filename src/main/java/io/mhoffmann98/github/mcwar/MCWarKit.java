package io.mhoffmann98.github.mcwar;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class MCWarKit {
	
	private String kitName, description;
	private FileConfiguration cfg;
	private HashMap<String, Integer> startingItems = new HashMap<>();
	
	public MCWarKit(String kitName, String description, MCWar plugin){
		this.cfg = plugin.getConfig();
		this.kitName = kitName;
		this.description = description;
	}
	
	public void addStartingItem(String materialName, int amount){
		startingItems.put(materialName, amount);
	}
	
	public HashMap<String, Integer> getStartingItems(){
		return startingItems;
	}
	
	public String getKitName(){
		return kitName;
	}
	
	public String getDescription(){
		return description;
	}

}
