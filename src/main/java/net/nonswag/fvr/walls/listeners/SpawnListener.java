package net.nonswag.fvr.walls.listeners;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.BUILD_WITHER;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

@RequiredArgsConstructor
public class SpawnListener implements Listener {
    private final Walls walls;

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(BUILD_WITHER)) event.setCancelled(true);
        else if (event.getSpawnReason().equals(NATURAL) && !walls.getAllowedMobs().contains(event.getEntityType())) {
            event.setCancelled(true);
        }
    }
}
