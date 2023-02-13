package net.nonswag.fvr.walls.listeners;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import net.nonswag.fvr.walls.api.PlayerVisibility;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

@RequiredArgsConstructor
public class RespawnListener implements Listener {
    private final Walls walls;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(walls.getGameSpawn());
        if (walls.getPlayer(event.getPlayer().getUniqueId()).getRank().vip()) {
            walls.getSpectatorKit().givePlayerKit(event.getPlayer());
            PlayerVisibility.makeSpecInvisible(walls, event.getPlayer());
            PlayerVisibility.makeSpecVisToSpecs(walls, event.getPlayer());
        } else {
            Notifier.notify(event.getPlayer(), "You Died :( RIP. Want to fly spectate /surface /spawn ? Get Walls §aVIP§f at " + Walls.DISCORD);
        }
    }
}
