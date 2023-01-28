package net.nonswag.fvr.walls.kits;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FullChainKit {

    private final Walls walls;

    public FullChainKit(Walls walls) {
        this.walls = walls;
    }

    public void givePlayerKit(Player player) {
        switch (walls.getGameState()) {
            case PREGAME:
                Notifier.error(player, "Kits available once you spawn into the game :)");
                break;
            case PEACETIME:
            case FIGHTING:
                player.setLevel(100);
                player.getInventory().addItem(new ItemStack(Material.STONE_AXE, 1));
                player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE, 1));
                player.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1));
                player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
                player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 64));
                player.getInventory().addItem(new ItemStack(Material.BOOKSHELF, 24));
                player.getInventory().addItem(new ItemStack(Material.ANVIL, 1));
                player.getInventory().addItem(new ItemStack(Material.BUCKET, 2));
                player.getInventory().addItem(new ItemStack(Material.ENCHANTMENT_TABLE, 1));
                player.getInventory().addItem(new ItemStack(Material.BOW, 1));
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 5));
                player.getInventory().addItem(new ItemStack(Material.QUARTZ_BLOCK, 64, (short) 1));
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
                player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
                player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET, 1));
                player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
                break;
            case FINISHED:
                Notifier.error(player, "Too late :(. Kits only available during game time.");
                break;
            default:
                break;
        }
    }
}
