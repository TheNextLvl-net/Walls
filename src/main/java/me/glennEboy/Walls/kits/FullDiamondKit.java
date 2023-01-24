package me.glennEboy.Walls.kits;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.utils.GameNotifications;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FullDiamondKit {

    
    
    private TheWalls myWalls;
    
    public FullDiamondKit(TheWalls wallsPlugin){
        
        myWalls = wallsPlugin;
        
    }
    
    
    
    public void givePlayerKit(Player p) {
        
        switch (myWalls.getGameState()){
        case PREGAME:

            GameNotifications.sendPlayerCommandError(p, "Kits available once you spawn into the game :)");
            
            break;
        case PEACETIME:
        case FIGHTING:            
            p.setLevel(64);
            p.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE, 1));
            p.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE, 1));
            p.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
            p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
            p.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 64));
            p.getInventory().addItem(new ItemStack(Material.BOOKSHELF, 64));
            p.getInventory().addItem(new ItemStack(Material.ANVIL, 1));
            p.getInventory().addItem(new ItemStack(Material.BUCKET, 2));
            p.getInventory().addItem(new ItemStack(Material.ENCHANTMENT_TABLE, 1));
            p.getInventory().addItem(new ItemStack(Material.BOW, 1));
            p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 5));
            p.getInventory().addItem(new ItemStack(Material.QUARTZ_BLOCK, 64, (short)1));
            p.getInventory().addItem(new ItemStack(Material.QUARTZ_BLOCK, 64, (short)1));
            p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 10));
            p.getInventory().addItem(new ItemStack(Material.ARROW, 64));
    		p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
			p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
			p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
			p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
			break;
			
		case FINISHED:
			
			GameNotifications.sendPlayerCommandError(p, "Too late :(. Kits only available during peace time.");
			
			break;
		default:
			break;
		}
		
	}
	

}
