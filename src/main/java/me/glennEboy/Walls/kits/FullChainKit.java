package me.glennEboy.Walls.kits;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.utils.GameNotifications;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FullChainKit {

	
	
	private TheWalls myWalls;
	
	public FullChainKit(TheWalls wallsPlugin){
		
		myWalls = wallsPlugin;
		
	}
	
	
	
	public void givePlayerKit(Player p) {
		
		switch (myWalls.getGameState()){
		case PREGAME:

			GameNotifications.sendPlayerCommandError(p, "Kits available once you spawn into the game :)");
			
			break;
		case PEACETIME:
		case FIGHTING:			
			p.setLevel(100);
			p.getInventory().addItem(new ItemStack(Material.STONE_AXE, 1));
			p.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE, 1));
			p.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1));
			p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
			p.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 64));
			p.getInventory().addItem(new ItemStack(Material.BOOKSHELF, 24));
			p.getInventory().addItem(new ItemStack(Material.ANVIL, 1));
			p.getInventory().addItem(new ItemStack(Material.BUCKET, 2));
			p.getInventory().addItem(new ItemStack(Material.ENCHANTMENT_TABLE, 1));
			p.getInventory().addItem(new ItemStack(Material.BOW, 1));
			p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 5));
			p.getInventory().addItem(new ItemStack(Material.QUARTZ_BLOCK, 64, (short)1));
			p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1, (short)1));
			p.getInventory().addItem(new ItemStack(Material.ARROW, 64));
			p.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
			p.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
			p.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET, 1));
			p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
			break;
			
		case FINISHED:
			
			GameNotifications.sendPlayerCommandError(p, "Too late :(. Kits only available during game time.");
			
			break;
		default:
			break;
		}
		
	}
	

}
