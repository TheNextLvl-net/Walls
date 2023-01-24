package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.GameState;
import me.glennEboy.Walls.kits.FullDiamondKit;
import me.glennEboy.Walls.utils.GameNotifications;
import me.glennEboy.Walls.utils.GameStarter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SurfaceCmd implements CommandExecutor{
	
	TheWalls myWalls;
	
	public SurfaceCmd(TheWalls tw){
		myWalls=tw;
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


    	if (myWalls.getGameState()!=GameState.PEACETIME){
    		GameNotifications.sendPlayerCommandError((Player) sender, "Sorry /surface is only available during peace time.");
    		return true;
    	}
        final Player player = (Player) sender;
        
        if (myWalls.isVIP(player.getUniqueId()) || player.isOp()) {
            final Location loc = new Location(Bukkit.getServer().getWorld("world"), player.getLocation().getBlockX(), 
            		Bukkit.getServer().getWorld("world").getHighestBlockYAt(player.getLocation().getBlockX(), 
            				player.getLocation().getBlockZ()), player.getLocation().getBlockZ());
            if (loc.getY() > TheWalls.buildHeight) {
                player.sendMessage(ChatColor.RED + "Surface is too high! Can't teleport here :(");
                return true;
            }
            if (loc.getBlock().getType()==Material.LAVA || loc.getBlock().getType()==Material.STATIONARY_LAVA){
                player.sendMessage(ChatColor.RED + "Its a little hot up there.. I don't think you can swim in lava :-/ try surfacing somewhere else!");
                return true;            	
            }
            
            player.teleport(loc);
            player.sendMessage(ChatColor.GREEN + "Teleported to the surface!");
        }

    	return true;
    }
}
