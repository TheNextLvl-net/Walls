package me.glennEboy.Walls.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;


public class PlayerChatHandler {

	
	public static void playerChat(AsyncPlayerChatEvent event, TheWalls myWalls){

    	if (TheWalls.shhhhh && (!event.getPlayer().isOp() && !myWalls.isMGM(event.getPlayer().getUniqueId())) && !TheWalls.teamCaptains.contains(event.getPlayer().getName())){
    		event.setCancelled(true);
    	}

    	LanguageChecker.checkLanguageForCursing(event);
    	if (event.isCancelled()) return;
		
		WallsPlayer twp = myWalls.getWallsPlayer(event.getPlayer().getUniqueId());
		
		String rank = "";
		String mvp = "";
		String dmvp = "";
		String clan = "";
		
		if (twp.owner){
			rank = ChatColor.BLACK+"["+ChatColor.RED + "OWNER" + ChatColor.BLACK+"]";
		}else if (twp.admin){
			rank = ChatColor.RED + "[ADMIN]";
		}else if (twp.youtuber){
			rank = ChatColor.DARK_GRAY + "["+ChatColor.GRAY +"You"+ChatColor.RED +"Tube"+ChatColor.DARK_GRAY +"]";
		}else if (twp.mgm){
			rank = ChatColor.GOLD + "[MGM]";
		}else if (twp.gm){
			rank = ChatColor.AQUA + "[GM]";
		}else if (twp.legendary){
			rank = ChatColor.BLACK+"["+ChatColor.GOLD + "LEGENDARY" + ChatColor.BLACK+"]";
		}else if (twp.pro){
			rank = ChatColor.BLUE + "[PRO]";
		}else if (twp.vip){
			rank = ChatColor.GREEN + "[VIP]";
		}

		if (twp.nMVP) mvp = ChatColor.YELLOW + "[MVP]";

		if (twp.dMVP) dmvp = ChatColor.DARK_AQUA + "[MVP]";

		if (twp.clan != null && twp.clan.equalsIgnoreCase("chessclub")){
			clan = ChatColor.DARK_RED + ""+ChatColor.BOLD+ChatColor.GRAY+"C"+ChatColor.WHITE+"h"+ChatColor.GRAY+"e"+ChatColor.WHITE
					+"s"+ChatColor.GRAY+"s"+ChatColor.WHITE+"C"+ChatColor.GRAY+"l"+ChatColor.WHITE+"u"+ChatColor.GRAY+"b"+ChatColor.WHITE+"◊";
		}else if (twp.clan!=null){
			clan = ChatColor.DARK_RED + ""+ChatColor.BOLD+ChatColor.translateAlternateColorCodes('&', twp.clan)+ChatColor.WHITE+"◊";
		}
		
		switch (myWalls.getGameState()){
		case PREGAME:
            event.setFormat(rank + mvp + dmvp + clan + ChatColor.LIGHT_PURPLE + "[PRE-GAME]" + ChatColor.WHITE + "%s: " + ChatColor.GRAY + "%s");
			break;
		case PEACETIME:
		case FIGHTING:
		case FINISHED:
			event.setFormat(rank + mvp + dmvp + clan + TheWalls.teamChatColors[twp.playerState.ordinal()] +  "%s: " + ChatColor.GRAY + "%s");
			
			if (!myWalls.staffListSnooper.contains(event.getPlayer().getUniqueId())){				
				event.getRecipients().clear();
				if (TheWalls.debugMode){				
					myWalls.getLogger().info("----DEBUG - CLEARED RECIPIENTS");
				}
				for (UUID u : myWalls.getTeamList(event.getPlayer().getUniqueId())){
					if (Bukkit.getPlayer(u)!=null){					
						event.getRecipients().add(Bukkit.getPlayer(u));
						if (TheWalls.debugMode){
							myWalls.getLogger().info("----DEBUG - ADDED "+u.toString() +" from TEAMMATES");
						}
					}
				}
				for (UUID u : myWalls.staffListSnooper){
					if (Bukkit.getPlayer(u)!=null){					
						event.getRecipients().add(Bukkit.getPlayer(u));
						if (TheWalls.debugMode){						
							myWalls.getLogger().info("----DEBUG - ADDED "+u.toString() +" from STAFF");
						}
					}
				}
			}
			
			break;
		default:
			break;
		}
	}

	
	@SuppressWarnings("deprecation")
	public static void fakePlayerChat(TheWalls myWalls, UUID playerUID, String message){

		String fakeMessage = "";
		
		WallsPlayer twp = myWalls.getWallsPlayer(playerUID);
		
		String rank = "";
		String mvp = "";
		String dmvp = "";
		String clan = "";
		
		if (twp.admin){
			rank = ChatColor.RED + "[ADMIN]";
		}else if (twp.mgm){
			rank = ChatColor.GOLD + "[MGM]";
		}else if (twp.gm){
			rank = ChatColor.AQUA + "[GM]";
		}else if (twp.pro){
			rank = ChatColor.BLUE + "[PRO]";
		}else if (twp.vip){
			rank = ChatColor.GREEN + "[VIP]";
		}

		if (twp.nMVP) mvp = ChatColor.YELLOW + "[MVP]";

		if (twp.dMVP) dmvp = ChatColor.DARK_AQUA + "[MVP]";

		if (twp.clan!=null) clan = ChatColor.DARK_RED + ""+ChatColor.BOLD+ChatColor.translateAlternateColorCodes('&', twp.clan)+ChatColor.WHITE+"◊";
		
		switch (myWalls.getGameState()){
		case PREGAME:
            fakeMessage = rank + mvp + dmvp + clan + ChatColor.LIGHT_PURPLE + "[PRE-GAME]" + ChatColor.WHITE + twp.username+": " + ChatColor.GRAY + message;

            for (Player p : Bukkit.getOnlinePlayers()){
            	p.sendMessage(fakeMessage);
            }
            myWalls.getLogger().info(fakeMessage);
            
            break;
		case PEACETIME:
		case FIGHTING:
		case FINISHED:
            fakeMessage = rank + mvp + dmvp + clan + TheWalls.teamChatColors[twp.playerState.ordinal()] + twp.username+": " + ChatColor.GRAY + message;
			

			for (UUID u : myWalls.getTeamList(playerUID)){
				if (Bukkit.getPlayer(u)!=null){					
					Bukkit.getPlayer(u).sendMessage(fakeMessage);
					if (TheWalls.debugMode){
						myWalls.getLogger().info("----DEBUG - ADDED "+u.toString() +" from TEAMMATES");
					}
				}
			}
			for (UUID u : myWalls.staffListSnooper){
				if (Bukkit.getPlayer(u)!=null){					
					Bukkit.getPlayer(u).sendMessage(fakeMessage);
					if (TheWalls.debugMode){						
						myWalls.getLogger().info("----DEBUG - ADDED "+u.toString() +" from STAFF");
					}
				}
			}
            myWalls.getLogger().info("<><><>"+fakeMessage);

			
			break;
		default:
			break;
		}
	}
	
	
	
}
	 