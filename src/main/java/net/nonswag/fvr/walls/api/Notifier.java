package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Notifier {

    private static final String PREFIX = "§6§lWalls §r";

    public static void error(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + message);
    }

    public static void success(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + message);
    }

    public static void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage("§d§lTheWalls §r" + message));
    }

    public static void notify(CommandSender sender, String message) {
        if (sender != null) sender.sendMessage(PREFIX + message);
    }

    public static void team(Walls walls, Team state, String message) {
        for (UUID uuid : walls.getTeamList(state)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) notify(player, message);
        }
    }

    public static void staff(Walls walls, String message) {
        for (UUID uuid : walls.getStaffList()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || walls.noStaffChat.contains(uuid)) continue;
            player.sendMessage(Walls.STAFFCHATT_PREFIX + "§f: " + message);
        }
    }
}
