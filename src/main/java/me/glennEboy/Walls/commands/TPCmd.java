package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.utils.GameNotifications;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCmd implements CommandExecutor{
	
	TheWalls myWalls;
	
	public TPCmd(TheWalls tw){
		myWalls=tw;
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (args.length < 1){
			GameNotifications.sendPlayerCommandError((Player)sender, "That didn't work.. try /tp <IGN>");
			return true;
		}
		
		if (sender instanceof Player && !this.myWalls.isVIP(((Player)sender).getUniqueId())){
			GameNotifications.sendPlayerCommandError((Player)sender, "Sorry only VIP and above can use this command.");
			return true;
		}
		
		@SuppressWarnings("deprecation")
		Player friend = Bukkit.getServer().getPlayer(args[0]);

		if (friend == null){
			return true;
		}
    	switch (myWalls.getGameState()){
		case PREGAME:
			break;
		case PEACETIME:
			if (myWalls.isSpec(((Player)sender).getUniqueId())){
				((Player)sender).teleport(friend.getLocation().add(0, +5, 0));
				sender.sendMessage(ChatColor.GREEN + "You have been teleported to " + args[0]);
				break;
			}
			
			if (myWalls.sameTeam(((Player)sender).getUniqueId(), friend.getUniqueId())){
				((Player)sender).teleport(friend);
				sender.sendMessage(ChatColor.GREEN + "You have been teleported to " + args[0]);
				friend.sendMessage(sender.getName()+ChatColor.GREEN + " teleported to you.");
			}else{
				GameNotifications.sendPlayerCommandError((Player)sender, "Sorry you can only TP to people on your team.");
			}
			break;
		case FIGHTING:
		case FINISHED:
			if (myWalls.isSpec(((Player)sender).getUniqueId())){
				((Player)sender).teleport(friend.getLocation().add(0, +5, 0));
				sender.sendMessage(ChatColor.GREEN + "You have been teleported to " + args[0]);
				break;
			}
			break;
		default:
			break;
		}
    	return true;
    }
}
