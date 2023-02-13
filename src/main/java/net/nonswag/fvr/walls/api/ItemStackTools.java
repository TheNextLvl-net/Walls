package net.nonswag.fvr.walls.api;

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
}
