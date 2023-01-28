package net.nonswag.fvr.walls.utils;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ClanUtils {

    public static void changeAllOnlineClanNames(Walls myWalls, String oldClan, String newClan) {
        for (UUID u : myWalls.getAllPlayers().keySet()) {
            if (Bukkit.getPlayer(u) != null) {
                WallsPlayer anotherWP = myWalls.getWallsPlayer(u);
                if (anotherWP.clan != null) {
                    if (anotherWP.clan.equals(oldClan)) {
                        anotherWP.clan = newClan;
                    }
                }
            }
        }
    }
}