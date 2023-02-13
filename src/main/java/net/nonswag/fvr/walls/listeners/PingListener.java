package net.nonswag.fvr.walls.listeners;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

@RequiredArgsConstructor
public class PingListener implements Listener {
    private final Walls walls;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerListPing(ServerListPingEvent event) {
        switch (walls.getGameState()) {
            case PREGAME:
                event.setMotd("§aWaiting for more players to start\n§6Click to join");
                break;
            case PEACETIME:
                event.setMotd("§9The players are preparing to fight\n§bConnect to spectate");
                break;
            case FIGHTING:
                event.setMotd("§cThe walls have fallen\n§4Everyone is fighting");
                break;
            case FINISHED:
                event.setMotd("§3The game has ended\n§bRestarting...");
                break;
        }
    }
}
