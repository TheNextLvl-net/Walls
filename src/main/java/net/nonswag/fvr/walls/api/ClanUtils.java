package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ClanUtils {

    public static void changeAllOnlineClanNames(Walls myWalls, String oldClan, String newClan) {
        for (UUID u : myWalls.getPlayers().keySet()) {
            if (Bukkit.getPlayer(u) != null) {
                WallsPlayer anotherWP = myWalls.getPlayer(u);
                if (anotherWP.clan != null) {
                    if (anotherWP.clan.equals(oldClan)) {
                        anotherWP.clan = newClan;
                    }
                }
            }
        }
    }
}
