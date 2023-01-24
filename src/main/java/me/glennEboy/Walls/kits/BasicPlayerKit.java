package me.glennEboy.Walls.kits;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class BasicPlayerKit {
	
    private final ItemMeta compass;

    
    public BasicPlayerKit(){

    	this.compass = Bukkit.getServer().getItemFactory().getItemMeta(Material.COMPASS);
    	this.compass.setDisplayName("Enemy Finder 3000");
		
	}
	

	public void givePlayerKit(Player p) {

		p.closeInventory();
		p.getInventory().clear();

        final ItemStack compass = new ItemStack(Material.COMPASS, 1);
        compass.setItemMeta(this.compass);
        p.getInventory().addItem(compass);

		
	}

}
