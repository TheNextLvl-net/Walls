package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.GameState;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.utils.GameNotifications;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CornerCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public CornerCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


        if (myWalls.getGameState()!=GameState.PEACETIME){
            GameNotifications.sendPlayerCommandError((Player) sender, "Sorry /corner is only available during peace time.");
            return true;
        }
        final Player player = (Player) sender;

        if (!myWalls.isVIP(player.getUniqueId()) && !myWalls.isStaff(player.getUniqueId()) && !sender.isOp()){
            GameNotifications.sendPlayerCommandError(player,"You need a rank to be able to /corner! Get "+ChatColor.BLUE+"PRO"+ChatColor.RED+" / "+ChatColor.GREEN+"VIP"+ChatColor.RED+" at mySite.com");
            return true;            
        }

        WallsPlayer twp = myWalls.getWallsPlayer(player.getUniqueId());
        Location corner = null;

        corner = TheWalls.corners.get(twp.playerState.ordinal());

        final Location loc = new Location(
                Bukkit.getServer().getWorld("world"), corner.getBlockX(), 
                Bukkit.getServer().getWorld("world").getHighestBlockYAt(corner.getBlockX(), corner.getBlockZ()), 
                corner    .getBlockZ());
        if (loc.getY() > TheWalls.buildHeight) {
            player.sendMessage(ChatColor.RED + "Surface is too high! Can't teleport here :(");
            return true;
        }
        player.teleport(loc);
        GameNotifications.sendPlayerCommandSuccess(player, "Teleported to the corner!");

        
        return true;
    }
}
