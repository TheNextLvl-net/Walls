package me.glennEboy.Walls.utils;

import java.util.List;
import java.util.UUID;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.PlayerState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GameNotifications {
	
	public static void sendPlayerCommandError(Player p, String text){
		p.sendMessage(TheWalls.chatPrefix + ChatColor.RED + " " +text);
	}
	
	public static void sendPlayerCommandSuccess(Player p, String text){
		p.sendMessage(TheWalls.chatPrefix + ChatColor.GREEN + " " +text);
	}
	
	public static void broadcastMessage(String text){
		Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "TheWalls: " + ChatColor.WHITE + text);
	}

	public static void sendPlayerSimpleMessage(Player p, String text){
		if (p!=null) p.sendMessage(TheWalls.chatPrefix + " " +text);
	}

	public static void teamMessage(TheWalls aTheWalls, PlayerState aPlayerState, String aMessage){
		List<UUID> team = aTheWalls.getTeamList(aPlayerState);
		for (UUID u : team){
			sendPlayerSimpleMessage(Bukkit.getPlayer(u),aMessage);
		}
	}

	public static void staffMessage(TheWalls myWalls, List<UUID> team, String aMessage){
		for (UUID u : team){
    		if (!myWalls.noStaffChat.contains(u)){        			    			
    			sendPlayerSimpleMessage(Bukkit.getPlayer(u),aMessage);
    		}
		}
	}

	public static void opBroadcast(String aMessage){
        for (Player p : Bukkit.getOnlinePlayers()) {
        	if (p.isOp()) p.sendMessage(TheWalls.OPCHAT_PREFIX + ChatColor.WHITE + ": " + aMessage);
        }
	}
	
	public static void staffNotification(TheWalls myWalls, String aMessage){
        for (UUID u : myWalls.getStaffList()) {
        	try{
        		if (!myWalls.noStaffChat.contains(u)){        			
        			Bukkit.getPlayer(u).sendMessage(TheWalls.STAFFCHATT_PREFIX + ChatColor.WHITE + ": " + aMessage);
        		}
        	}catch (Exception e){
        		Bukkit.getServer().getLogger().info("TheWalls: staffNotification Failed to send to player "+u.toString());
        	}
        }
	}
	

}
