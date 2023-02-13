package net.nonswag.fvr.walls.kits;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class FullDiamondKit {
    private final Walls walls;

    public void givePlayerKit(Player player) {
        switch (walls.getGameState()) {
            case PREGAME:
                Notifier.error(player, "Kits available once you spawn into the game :)");
                break;
            case PEACETIME:
            case FIGHTING:
                player.setLevel(64);
                player.getInventory().clear();
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE, 1));
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE, 1));
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
                player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
                player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 64));
                player.getInventory().addItem(new ItemStack(Material.BOOKSHELF, 64));
                player.getInventory().addItem(new ItemStack(Material.ANVIL, 1));
                player.getInventory().addItem(new ItemStack(Material.BUCKET, 2));
                player.getInventory().addItem(new ItemStack(Material.ENCHANTMENT_TABLE, 1));
                player.getInventory().addItem(new ItemStack(Material.BOW, 1));
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 5));
                player.getInventory().addItem(new ItemStack(Material.QUARTZ_BLOCK, 64, (short) 1));
                player.getInventory().addItem(new ItemStack(Material.QUARTZ_BLOCK, 64, (short) 1));
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 10));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                player.getInventory().addItem(new ItemStack(Material.INK_SACK, 64, (short) 4));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
                break;
            case FINISHED:
                Notifier.error(player, "Too late :(. Kits only available during peace time.");
                break;
            default:
                break;
        }
    }
}
