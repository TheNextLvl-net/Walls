package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;


public class PlayerVisibility {

    public static void makeSpecInvisible(Walls walls, Player spec) {
        for (int i = 1; i < 5; i++) {
            Team ps = Team.values()[i];
            for (UUID fighter : walls.getTeamList(ps)) {
                Player player = Bukkit.getPlayer(fighter);
                if (player != null) player.hidePlayer(spec);
            }
        }
    }

    public static void makeSpecVisToSpecs(Walls walls, Player spec) {
        for (UUID specTeam : walls.getTeamList(Team.SPECTATORS)) {
            Player player = Bukkit.getPlayer(specTeam);
            if (player == null) continue;
            player.showPlayer(spec);
            spec.showPlayer(player);
        }
    }

    public static void hideAllSpecs(Walls walls, Player player) {
        for (UUID specTeam : walls.getTeamList(Team.SPECTATORS)) {
            Player all = Bukkit.getPlayer(specTeam);
            if (all != null) player.hidePlayer(all);
        }
    }

    public static void makeInVisPlayerNowVisible(Player wasInvisible) {
        for (Player allPlayers : Bukkit.getOnlinePlayers()) {
            allPlayers.showPlayer(wasInvisible);
        }
    }
}
