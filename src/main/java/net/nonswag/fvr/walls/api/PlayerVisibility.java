package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerVisibility {

    public static void makeSpecInvisible(Walls walls, Player spec) {
        for (Team team : Team.values()) {
            for (UUID fighter : walls.getTeamList(team)) {
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
        for (UUID spectator : walls.getTeamList(Team.SPECTATORS)) {
            Player all = Bukkit.getPlayer(spectator);
            if (all != null) player.hidePlayer(all);
        }
    }

    public static void makeInVisPlayerNowVisible(Player wasInvisible) {
        for (Player all : Bukkit.getOnlinePlayers()) all.showPlayer(wasInvisible);
    }
}
