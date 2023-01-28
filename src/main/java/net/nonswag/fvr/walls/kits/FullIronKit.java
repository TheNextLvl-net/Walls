package net.nonswag.fvr.walls.kits;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FullIronKit {

    private final Walls walls;

    public FullIronKit(Walls walls) {
        this.walls = walls;
    }

    public void givePlayerKit(Player p) {
        switch (walls.getGameState()) {
            case PREGAME:
                Notifier.error(p, "Kits available once you spawn into the game :)");
                break;
            case PEACETIME:
            case FIGHTING:
                p.setLevel(6);
                p.getInventory().addItem(new ItemStack(Material.IRON_AXE, 1));
                p.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE, 1));
                p.getInventory().addItem(new ItemStack(Material.TNT, 16));
                p.getInventory().addItem(new ItemStack(Material.IRON_SWORD, 1));
                p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 8));
                p.getInventory().addItem(new ItemStack(Material.BUCKET, 2));
                p.getInventory().addItem(new ItemStack(Material.BOW, 1));
                p.getInventory().addItem(new ItemStack(Material.LEAVES, 64, (short) 1));
                p.getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
                p.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS, 1));
                p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
                p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET, 1));
                p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
                break;
            case FINISHED:
                Notifier.error(p, "Too late :(. Kits only available during game time.");
                break;
            default:
                break;
        }
    }
}