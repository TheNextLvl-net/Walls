package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.utils.Notifier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CornerCmd implements CommandExecutor {

    private final Walls walls;

    public CornerCmd(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (walls.getGameState() != GameState.PEACETIME) {
            Notifier.error(sender, "Sorry /corner is only available during peace time.");
            return true;
        }
        Player player = (Player) sender;
        if (!walls.getPlayer(player.getUniqueId()).rank.vip() && !sender.isOp()) {
            Notifier.error(player, "You need a rank to be able to /corner! Get " + ChatColor.BLUE + "PRO" + ChatColor.RED + " / " + ChatColor.GREEN + "VIP" + ChatColor.RED + " at " + Walls.DISCORD);
            return true;
        }
        WallsPlayer twp = walls.getPlayer(player.getUniqueId());
        Location corner;
        corner = Walls.corners.get(twp.playerState.ordinal());
        final Location loc = new Location(
                Bukkit.getWorld(Walls.levelName), corner.getBlockX(),
                Bukkit.getWorld(Walls.levelName).getHighestBlockYAt(corner.getBlockX(), corner.getBlockZ()),
                corner.getBlockZ());
        if (loc.getY() > Walls.buildHeight) {
            player.sendMessage(ChatColor.RED + "Surface is too high! Can't teleport here :(");
            return true;
        }
        player.teleport(loc);
        Notifier.success(player, "Teleported to the corner!");
        return true;
    }
}
