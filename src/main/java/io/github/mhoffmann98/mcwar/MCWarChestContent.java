package io.github.mhoffmann98.mcwar;

public class MCWarChestContent {

	private String itemName;
	private float itemChance;
	private int itemAmount;
	
	public MCWarChestContent(String itemName, float itemChance, int itemAmount) {
		this.itemName = itemName;
		this.itemChance = itemChance;
		this.itemAmount = itemAmount;
	}

	public String getName(){
		return this.itemName;
	}
	public float getChance(){
		return this.itemChance;
	}
	public int getAmount(){
		return this.itemAmount;
	}

}
