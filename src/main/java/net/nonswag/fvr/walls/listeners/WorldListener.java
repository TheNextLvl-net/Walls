package net.nonswag.fvr.walls.listeners;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import net.nonswag.fvr.walls.api.ProtectedContainer;
import net.nonswag.fvr.walls.api.Selection;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class WorldListener implements Listener {
    private final Walls walls;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        switch (walls.getGameState()) {
            case PREGAME:
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
                break;
            case PEACETIME:
                Block block = event.getBlock();
                for (Selection selection : walls.getSelections()) {
                    if (selection.contains(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), true)) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "You are not allowed to break blocks here.");
                        return;
                    }
                }

                if (walls.isInASpawn(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You are not allowed to break blocks here.");
                    return;
                }
                Material clickedMaterial = event.getBlock().getType();
                if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)
                        || (clickedMaterial == Material.ENCHANTMENT_TABLE) || (clickedMaterial == Material.WORKBENCH)) {
                    for (ProtectedContainer container : walls.getProtectedContainers()) {
                        final boolean owned = container.getOwner().equals(event.getPlayer().getName());
                        if (owned) {
                            walls.getProtectedContainers().remove(container);
                            return;
                        }
                    }
                }
                if ((block.getType() == Material.IRON_ORE) || (block.getType() == Material.GOLD_ORE)
                        || (block.getType() == Material.DIAMOND_ORE) || (block.getType() == Material.EMERALD_ORE)) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.2D) {
                        if (walls.leprechaunOwners.containsKey(event.getPlayer().getUniqueId())) {
                            checkLeprechaunDrop(block);
                        }
                    }
                }
            case FIGHTING:
                if (walls.isInASpawn(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You are not allowed to break blocks here.");
                    return;
                }
                block = event.getBlock();
                if ((block.getType() == Material.IRON_ORE) || (block.getType() == Material.GOLD_ORE)
                        || (block.getType() == Material.DIAMOND_ORE) || (block.getType() == Material.EMERALD_ORE)) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.2D) {
                        if (walls.leprechaunOwners.containsKey(event.getPlayer().getUniqueId())) {
                            checkLeprechaunDrop(block);
                        }
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (walls.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlock().getLocation().getBlockY() > Walls.buildHeight) {
            event.setCancelled(true);
            if (walls.getGameState().equals(Walls.GameState.PREGAME)) return;
            Notifier.error(event.getPlayer(), "You reached the build limit and cannot place blocks here!");
            return;
        }
        if ((event.getBlock().getX() < -142 || event.getBlock().getX() > 137) || (event.getBlock().getZ() < -10 || event.getBlock().getZ() > 269)) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlockPlaced().getType().equals(Material.CARROT)) {
            Notifier.error(event.getPlayer(), "You don't look like a rabbit.. why you need to farm carrots?");
            event.setCancelled(true);
            return;
        }
        switch (walls.getGameState()) {
            case PREGAME:
            case FINISHED:
                event.setCancelled(true);
                break;
            case PEACETIME:
                if (walls.isOnPaths(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You are not allowed to place blocks here.");
                    return;
                }
                final Material type = event.getBlock().getType();
                if (type == Material.TNT) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "Cannot place TNT while the walls are up!!");
                    return;
                }
                walls.checkForProtectedPlacement(event);
                break;
            default:
                break;
        }
        if (event.isCancelled()) return;
        ItemStack stack = event.getPlayer().getItemInHand();
        if (walls.loreMatch(stack, "BOOOM")) {
            walls.getBoom().put(event.getBlock().getLocation(), walls.getPlayer(event.getPlayer().getUniqueId()).getTeam());
        }
    }

    public void checkLeprechaunDrop(Block block) {
        Material drop = null;
        switch (block.getType()) {
            case IRON_ORE:
                drop = Material.IRON_INGOT;
                break;
            case GOLD_ORE:
                drop = Material.GOLD_INGOT;
                break;
            case DIAMOND_ORE:
                drop = Material.DIAMOND;
                break;
            case EMERALD_ORE:
                drop = Material.EMERALD;
                break;
            default:
                break;
        }
        if (drop != null) block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(drop, 1));
    }
}
