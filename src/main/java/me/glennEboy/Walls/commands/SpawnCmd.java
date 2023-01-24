package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.GameState;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.utils.GameNotifications;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCmd implements CommandExecutor{
	
	TheWalls myWalls;
	
	public SpawnCmd(TheWalls tw){
		myWalls=tw;
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


    	if (myWalls.getGameState()!=GameState.PEACETIME){
    		GameNotifications.sendPlayerCommandError((Player) sender, "Sorry /spawn is only available during peace time.");
    		return true;
    	}
        final Player player = (Player) sender;
        
        if (!myWalls.isVIP(player.getUniqueId()) && !myWalls.isStaff(player.getUniqueId()) && !sender.isOp()){
        	GameNotifications.sendPlayerCommandError(player,"You need a rank to be able to /spawn! Get "+ChatColor.BLUE+"PRO"+ChatColor.RED+" / "+ChatColor.GREEN+"VIP"+ChatColor.RED+" at Mysite.com");
        	return true;        	
        }

        
        WallsPlayer twp = myWalls.getWallsPlayer(player.getUniqueId());

        switch (twp.playerState){
        case TEAM1:
        	player.teleport(TheWalls.team1Spawn);
        	break;
        case TEAM2:
        	player.teleport(TheWalls.team2Spawn);
        	break;
        case TEAM3:
        	player.teleport(TheWalls.team3Spawn);
        	break;
        case TEAM4:
        	player.teleport(TheWalls.team4Spawn);        	
        	break;
        case SPEC:
        	return true;
        }
        
        GameNotifications.sendPlayerCommandSuccess(player, "Teleported to spawn!");

        
    	return true;
    }
}
