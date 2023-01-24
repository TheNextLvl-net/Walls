package me.glennEboy.Walls.kits;


import java.util.ArrayList;
import java.util.List;

import me.glennEboy.Walls.TheWalls;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class ProStartPlayerKitPerks {
    
    private final List<ItemStack> items = new ArrayList<>();

    
    public ProStartPlayerKitPerks(){

        this.items.add(new ItemStack(Material.PORK, 5));
        this.items.add(new ItemStack(Material.BOW, 1));
        this.items.add(new ItemStack(Material.ARROW, 8));
        this.items.add(new ItemStack(Material.IRON_CHESTPLATE, 1));
        this.items.add(new ItemStack(Material.IRON_HELMET, 1));
        this.items.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
        this.items.add(new ItemStack(Material.BONE, 8));
        this.items.add(new ItemStack(Material.COAL, 16));
        this.items.add(new ItemStack(Material.TORCH, 16));
        this.items.add(new ItemStack(Material.LOG, 16));
        this.items.add(new ItemStack(Material.COOKED_FISH, 8));
        this.items.add(new ItemStack(Material.TNT, 1));
        this.items.add(new ItemStack(Material.EMERALD, 3));
        this.items.add(new ItemStack(Material.GOLD_SWORD, 1));
        this.items.add(new ItemStack(Material.SNOW_BALL, 1));
        this.items.add(new ItemStack(Material.MONSTER_EGG, 1, (short) 120));
        this.items.add(new ItemStack(Material.MONSTER_EGG, 1, (short) 90));
        this.items.add(new ItemStack(Material.EXP_BOTTLE, 1));
        
    }
    

    public void givePlayerKit(Player p) {

        p.setLevel(5);
        ItemStack item;
        item = this.items.get(TheWalls.random.nextInt(this.items.size()));
        p.getInventory().addItem(item);
        item = this.items.get(TheWalls.random.nextInt(this.items.size()));
        p.getInventory().addItem(item);
        item = this.items.get(TheWalls.random.nextInt(this.items.size()));
        p.getInventory().addItem(item);

        
    }

}
