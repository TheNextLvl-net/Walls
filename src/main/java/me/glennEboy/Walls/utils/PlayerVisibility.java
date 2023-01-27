package me.glennEboy.Walls.utils;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;


public class PlayerVisibility {


    public static void makeSpecInvis(TheWalls myWalls, Player spec) {
        for (int i = 1; i < 5; i++) {
            PlayerState ps = PlayerState.values()[i];
            for (UUID fighter : myWalls.getTeamList(ps)) {
                Player player = Bukkit.getPlayer(fighter);
                if (player != null) player.hidePlayer(spec);
            }
        }
    }

    public static void makeSpecVisToSpecs(TheWalls myWalls, Player spec) {
        for (UUID specTeam : myWalls.getTeamList(PlayerState.SPECTATORS)) {
            Player player = Bukkit.getPlayer(specTeam);
            if (player == null) continue;
            player.showPlayer(spec);
            spec.showPlayer(player);
        }
    }

    public static void hideAllSpecs(TheWalls myWalls, Player player) {
        for (UUID specTeam : myWalls.getTeamList(PlayerState.SPECTATORS)) {
            Player all = Bukkit.getPlayer(specTeam);
            if (all != null) player.hidePlayer(all);
        }
    }

    public static void makeInVisPlayerNowVisible(TheWalls myWalls, Player wasInvisible) {
        for (Player allPlayers : myWalls.getServer().getOnlinePlayers()) {
            allPlayers.showPlayer(wasInvisible);
        }
    }
}
