package net.nonswag.fvr.walls.utils;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;


public class PlayerChatHandler {

    public static void playerChat(AsyncPlayerChatEvent event, Walls myWalls) {
        if (Walls.shhhhh && (!event.getPlayer().isOp() && !myWalls.isMGM(event.getPlayer().getUniqueId())) && !Walls.teamCaptains.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
        if (event.isCancelled()) return;
        WallsPlayer twp = myWalls.getWallsPlayer(event.getPlayer().getUniqueId());
        String rank = "";
        String mvp = "";
        String dmvp = "";
        String clan = "";
        if (twp.owner) {
            rank = "§0[§cOWNER§0]";
        } else if (twp.admin) {
            rank = "§c[ADMIN]";
        } else if (twp.mgm) {
            rank = "§6[MGM]";
        } else if (twp.gm) {
            rank = "§b[GM]";
        } else if (twp.legendary) {
            rank = "§0[§6LEGENDARY§0]";
        } else if (twp.pro) {
            rank = "§9[PRO]";
        } else if (twp.vip) {
            rank = "§a[VIP]";
        }
        if (twp.nMVP) mvp = "§e[MVP]";
        if (twp.dMVP) dmvp = "§3[MVP]";
        if (twp.clan != null) {
            clan = ChatColor.DARK_RED + "" + ChatColor.BOLD + ChatColor.translateAlternateColorCodes('&', twp.clan) + ChatColor.WHITE + "◊";
        }
        switch (myWalls.getGameState()) {
            case PREGAME:
                event.setFormat(rank + mvp + dmvp + clan + ChatColor.LIGHT_PURPLE + "[PRE-GAME]" + ChatColor.WHITE + "%s: " + ChatColor.GRAY + "%s");
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                event.setFormat(rank + mvp + dmvp + clan + Walls.teamChatColors[twp.playerState.ordinal()] + "%s: " + ChatColor.GRAY + "%s");
                if (!myWalls.staffListSnooper.contains(event.getPlayer().getUniqueId())) {
                    event.getRecipients().clear();
                    if (Walls.debugMode) {
                        myWalls.getLogger().info("----DEBUG - CLEARED RECIPIENTS");
                    }
                    for (UUID u : myWalls.getTeamList(event.getPlayer().getUniqueId())) {
                        if (Bukkit.getPlayer(u) != null) {
                            event.getRecipients().add(Bukkit.getPlayer(u));
                            if (Walls.debugMode) {
                                myWalls.getLogger().info("----DEBUG - ADDED " + u.toString() + " from TEAMMATES");
                            }
                        }
                    }
                    for (UUID u : myWalls.staffListSnooper) {
                        if (Bukkit.getPlayer(u) != null) {
                            event.getRecipients().add(Bukkit.getPlayer(u));
                            if (Walls.debugMode) {
                                myWalls.getLogger().info("----DEBUG - ADDED " + u.toString() + " from STAFF");
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
     