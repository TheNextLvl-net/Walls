package net.nonswag.fvr.walls.kits;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class FullIronKit extends BasicPlayerKit {
    private final Walls walls;

    public void givePlayerKit(Player player) {
        switch (walls.getGameState()) {
            case PREGAME:
                Notifier.error(player, "Kits available once you spawn into the game :)");
                break;
            case PEACETIME:
            case FIGHTING:
                player.setLevel(6);
                player.getInventory().clear();
                super.givePlayerKit(player);
                player.getInventory().addItem(new ItemStack(Material.IRON_AXE, 1));
                player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE, 1));
                player.getInventory().addItem(new ItemStack(Material.TNT, 16));
                player.getInventory().addItem(new ItemStack(Material.IRON_SWORD, 1));
                player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 8));
                player.getInventory().addItem(new ItemStack(Material.BUCKET, 2));
                player.getInventory().addItem(new ItemStack(Material.BOW, 1));
                player.getInventory().addItem(new ItemStack(Material.LEAVES, 64, (short) 1));
                player.getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                player.getInventory().addItem(new ItemStack(Material.INK_SACK, 8, (short) 4));
                player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS, 1));
                player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
                player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET, 1));
                player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
                break;
            case FINISHED:
                Notifier.error(player, "Too late :(. Kits only available during game time.");
                break;
            default:
                break;
        }
    }
}
