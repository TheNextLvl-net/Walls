package net.nonswag.fvr.walls.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackTools {
    public static ItemStack enchantItem(ItemStack item, Enchantment enchantment, int level) {
        final ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack changeItemName(ItemStack item, String name) {
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
