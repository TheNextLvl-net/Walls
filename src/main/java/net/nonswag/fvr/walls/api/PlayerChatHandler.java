package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;


public class PlayerChatHandler {

    public static void playerChat(AsyncPlayerChatEvent event, Walls walls) {
        if (Walls.shhhhh && (!event.getPlayer().isOp() && !walls.getPlayer(event.getPlayer().getUniqueId()).getRank().mgm()) && !Walls.teamCaptains.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
        if (event.isCancelled()) return;
        WallsPlayer twp = walls.getPlayer(event.getPlayer().getUniqueId());
        String rank = twp.getRank().display();
        String clan = "";
        if (twp.getClan() != null) {
            clan = ChatColor.DARK_RED + "" + ChatColor.BOLD + ChatColor.translateAlternateColorCodes('&', twp.getClan()) + ChatColor.WHITE + "◊";
        }
        switch (walls.getGameState()) {
            case PREGAME:
                event.setFormat(rank + clan + ChatColor.LIGHT_PURPLE + "[PRE-GAME]" + ChatColor.WHITE + "%s: " + ChatColor.GRAY + "%s");
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                event.setFormat(rank + clan + Walls.teamChatColors[twp.getPlayerState().ordinal()] + "%s: " + ChatColor.GRAY + "%s");
                if (!walls.staffListSnooper.contains(event.getPlayer().getUniqueId())) {
                    event.getRecipients().clear();
                    for (UUID uuid : walls.getTeamList(event.getPlayer().getUniqueId())) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) event.getRecipients().add(player);
                    }
                    for (UUID uuid : walls.staffListSnooper) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) event.getRecipients().add(player);
                    }
                }
                break;
            default:
                break;
        }
    }
}
     