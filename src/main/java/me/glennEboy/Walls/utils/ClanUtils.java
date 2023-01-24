package me.glennEboy.Walls.utils;

import java.util.UUID;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ClanUtils {

	
	public static void changeAllOnlineClanNamesByStaff(TheWalls myWalls, String oldClan, String newClan){		
		for (UUID u : myWalls.getAllPlayers().keySet()) {
			if (Bukkit.getPlayer(u)!=null){        		
				WallsPlayer anotherWP = myWalls.getWallsPlayer(u);
				if (anotherWP.clan!= null){
					if (ChatColor.translateAlternateColorCodes('&', anotherWP.clan).equals(oldClan)){        			
						anotherWP.clan = newClan;
					}
				}
			}
		}
	}

	public static void changeAllOnlineClanNames(TheWalls myWalls, String oldClan, String newClan){		
		for (UUID u : myWalls.getAllPlayers().keySet()) {
			if (Bukkit.getPlayer(u)!=null){        		
				WallsPlayer anotherWP = myWalls.getWallsPlayer(u);
				if (anotherWP.clan!= null){
					if (anotherWP.clan.equals(oldClan)){        			
						anotherWP.clan = newClan;
					}
				}
			}
		}
	}
	
}
