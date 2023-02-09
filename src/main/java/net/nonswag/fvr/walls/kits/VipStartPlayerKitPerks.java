package net.nonswag.fvr.walls.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VipStartPlayerKitPerks {
    private final List<ItemStack> items = new ArrayList<>();

    public VipStartPlayerKitPerks() {
        items.add(new ItemStack(Material.PORK, 5));
        items.add(new ItemStack(Material.BOW, 1));
        items.add(new ItemStack(Material.ARROW, 8));
        items.add(new ItemStack(Material.IRON_CHESTPLATE, 1));
        items.add(new ItemStack(Material.IRON_HELMET, 1));
        items.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
        items.add(new ItemStack(Material.BONE, 8));
        items.add(new ItemStack(Material.COAL, 16));
        items.add(new ItemStack(Material.TORCH, 16));
        items.add(new ItemStack(Material.LOG, 16));
        items.add(new ItemStack(Material.COOKED_FISH, 8));
        items.add(new ItemStack(Material.TNT, 1));
        items.add(new ItemStack(Material.EMERALD, 3));
        items.add(new ItemStack(Material.GOLD_SWORD, 1));
        items.add(new ItemStack(Material.SNOW_BALL, 1));
        items.add(new ItemStack(Material.MONSTER_EGG, 1, (short) 120));
        items.add(new ItemStack(Material.MONSTER_EGG, 1, (short) 90));
        items.add(new ItemStack(Material.EXP_BOTTLE, 1));
    }

    public void givePlayerKit(Player player) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        player.getInventory().addItem(items.get(random.nextInt(items.size())));
        player.getInventory().addItem(items.get(random.nextInt(items.size())));
        player.setLevel(5);
    }
}
