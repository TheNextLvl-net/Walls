package net.nonswag.fvr.walls.listeners;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@RequiredArgsConstructor
public class MoveListener implements Listener {
    private final Walls walls;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        switch (walls.getGameState()) {
            case PREGAME:
                int lobbyHeight = walls.getGameSpawn().getBlockY();
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
                if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
                if (event.getTo().getBlockY() < lobbyHeight - 6) event.getPlayer().teleport(walls.getGameSpawn());
                break;
            case PEACETIME:
                if (walls.isSpectator(event.getPlayer())) return;
                if (walls.isOnPaths(event.getTo()) || event.getTo().getBlockY() > walls.getGameSpawn().getBlockY() - 4) {
                    Notifier.error(event.getPlayer(), "Trying to escape :( try /spawn");
                    event.setCancelled(true);
                }
                break;
            case FIGHTING:
            case FINISHED:
                if (walls.isSpectator(event.getPlayer())) return;
                if (event.getTo().getBlockY() > Walls.buildHeight) {
                    Notifier.error(event.getPlayer(), "Trying to escape :( try /spawn");
                    event.setCancelled(true);
                } else if (walls.isInASpawn(event.getTo()) && event.getTo().getBlockY() > 82) {
                    Notifier.error(event.getPlayer(), "Aww man its way too dangerous to climb up there.. :(");
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }
}
