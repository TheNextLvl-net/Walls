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

    private final Walls myWalls;

    public CornerCmd(Walls walls) {
        myWalls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (myWalls.getGameState() != GameState.PEACETIME) {
            Notifier.error(sender, "Sorry /corner is only available during peace time.");
            return true;
        }
        Player player = (Player) sender;
        if (!myWalls.isVIP(player.getUniqueId()) && !myWalls.isStaff(player.getUniqueId()) && !sender.isOp()) {
            Notifier.error(player, "You need a rank to be able to /corner! Get " + ChatColor.BLUE + "PRO" + ChatColor.RED + " / " + ChatColor.GREEN + "VIP" + ChatColor.RED + " at mySite.com");
            return true;
        }
        WallsPlayer twp = myWalls.getWallsPlayer(player.getUniqueId());
        Location corner;
        corner = Walls.corners.get(twp.playerState.ordinal());
        final Location loc = new Location(
                Bukkit.getServer().getWorld("world"), corner.getBlockX(),
                Bukkit.getServer().getWorld("world").getHighestBlockYAt(corner.getBlockX(), corner.getBlockZ()),
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
