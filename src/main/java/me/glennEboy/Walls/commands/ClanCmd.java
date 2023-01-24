package me.glennEboy.Walls.commands;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.utils.ClanUtils;
import me.glennEboy.Walls.utils.GameNotifications;
import me.glennEboy.Walls.utils.LanguageChecker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ClanCmd implements CommandExecutor{
	
	TheWalls myWalls;
	
	public ClanCmd(TheWalls tw){
		myWalls=tw;
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

    	// usage: /clan list | invite | accept | kick | leave
    	
    	String givenCommand = "/clan";
    	for (String arg : args){
    		givenCommand+=" "+arg;
    	}
    	Bukkit.getLogger().info(sender.getName()+" RAN COMMAND "+givenCommand);

    	if (args.length < 1){
    		if (sender instanceof Player ){    			
    			GameNotifications.sendPlayerCommandError((Player) sender, "usage: /clan list | invite | accept | kick | leave | rename | create | disband | leader");
    			if (sender.isOp()){

        			GameNotifications.sendPlayerCommandError((Player) sender, "usage: /clan list | invite | accept | kick | create ");
    				
    			}
    			
    		}else{
    			sender.sendMessage("usage: /clan invite | accept | kick | create ");    			
    		}
    		return true;
    	}
    	
    	/**
    	 *  Everyone
    	 *  
    	 */
    	if (args[0].equalsIgnoreCase("invite")){
    		invite(sender, cmd, commandLabel, args);

    	}else if (args[0].equalsIgnoreCase("kick")){
    		kick(sender, cmd, commandLabel, args);
    	
    	}else if (args[0].equalsIgnoreCase("accept")){
    		accept(sender, cmd, commandLabel, args);

    	}else if (args[0].equalsIgnoreCase("leave")){
    		leave(sender, cmd, commandLabel, args);

    	}else if (args[0].equalsIgnoreCase("list")){
    		list(sender, cmd, commandLabel, args);

    	}else if (args[0].equalsIgnoreCase("rename")){
    		rename(sender, cmd, commandLabel, args);

    	}else if (args[0].equalsIgnoreCase("disband")){
    		disband(sender, cmd, commandLabel, args);
    		
    	}else if (args[0].equalsIgnoreCase("leader")){
    		changeLeader(sender, cmd, commandLabel, args);
    		

    	/**
    	 * OP + 
    	 */

    	}else if (args[0].equalsIgnoreCase("create")){
    		create(sender, cmd, commandLabel, args);
    	
    	}
    	return true;
    }
    

    /**
     * ALL players
     */

    
    private void invite(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	
    	if (sender instanceof Player){

    		WallsPlayer twp = myWalls.getWallsPlayer(((Player)sender).getUniqueId());
    		if (twp.clanLeader){
    			Player invitee = Bukkit.getPlayer(args[1]);
    			if (invitee!=null){
    				GameNotifications.sendPlayerSimpleMessage(invitee, twp.username+" invited you to join their clan: "+twp.clan);
    				GameNotifications.sendPlayerSimpleMessage(invitee, "/clan accept to join ");    				
    				myWalls.clanInvites.put(invitee.getUniqueId(),twp.clan);
    				GameNotifications.sendPlayerSimpleMessage(((Player)sender), twp.username+": Invite sent to "+invitee.getName()+" to join "+twp.clan);
    			}else{
    				GameNotifications.sendPlayerCommandError(((Player)sender), "Player not found / online.. They need to be :-/");
    			}
    		}
    	}else{
    		sender.sendMessage("Need to be a player in game to make that work :(");
    	}
    	
    }
    
    private void list(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	
    	if (sender instanceof Player){

    		WallsPlayer twp = myWalls.getWallsPlayer(((Player)sender).getUniqueId());
    		if (twp.clan!=null){

    			// get all the players in the clan from the DB.
    			
    			List<String> clanMembers = myWalls.myDB.listClanMembers(twp.clan);
    			
    			GameNotifications.sendPlayerCommandSuccess(((Player)sender), "~~~~~~~~~~ "+ChatColor.translateAlternateColorCodes('&',twp.clan)+" ~~~~~~~~~~");
    			for (String member : clanMembers){
    				GameNotifications.sendPlayerCommandError(((Player)sender), "{ "+member+" }");
    			}
    			GameNotifications.sendPlayerCommandSuccess(((Player)sender), "~~~~~~~~~~ "+ChatColor.translateAlternateColorCodes('&',twp.clan)+" ~~~~~~~~~~");
    			
    			
    		}
    	}else{
    		sender.sendMessage("Need to be a player in game to make that work :(");
    	}
    	
    }
    
    

    private void rename(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	
    	if (sender instanceof Player){

    		WallsPlayer twp = myWalls.getWallsPlayer(((Player)sender).getUniqueId());
    		
    		Player player = ((Player)sender);
    		if (args.length==3 && (sender.isOp() || myWalls.getWallsPlayer(player.getUniqueId()).mgm || myWalls.getWallsPlayer(player.getUniqueId()).admin)){
				
    			String oldName = args[1];
    			String newName = args[2];
				
				oldName = stripSpecialClanCharacters(oldName);
								
				if (newName.length()>16){
					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name is too long :-/");
					return;
				}
				
				if (myWalls.myDB.staffRenameClan(oldName, newName)){
					
					Bukkit.getLogger().info("CLAN NAME CHANGE from "+ ChatColor.translateAlternateColorCodes('&',twp.clan) +" to "+ChatColor.translateAlternateColorCodes('&',newName) + " by player ("+twp.username+")");
					GameNotifications.sendPlayerCommandSuccess(((Player)sender), "Clan name will change to : "+ChatColor.translateAlternateColorCodes('&',newName)+" in the next game.");
					GameNotifications.sendPlayerCommandSuccess(((Player)sender), ChatColor.GRAY+"Inappropriate names will = ban :-/ choose carefully.");
					
//					ClanUtils.changeAllOnlineClanNamesByStaff(myWalls, oldName, newName);
					
				}else{
					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name NOT changed - already exists.. try another name :-/");
				}

    		}else if (args.length==2){
    			
    			if (twp.clanLeader){
    				
    				String newName = args[1];
    				
    				newName = stripSpecialClanCharacters(newName);
    				
    				if (LanguageChecker.curseFinder(stripAllClanCharacters(newName))){
    					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name not appropriate. You have been warned. :-/");
    					Bukkit.getLogger().info("INAPPROPRIATE CLAN NAME CHANGE from "+ChatColor.translateAlternateColorCodes('&',twp.clan) +" to "+ ChatColor.translateAlternateColorCodes('&',newName)+ " by player ("+twp.username+")");
    					return;
    				}
    				
    				if (TheWalls.bannedClanNames.contains(stripAllClanCharacters(newName).toLowerCase())){
    					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name not avilable.");
    					Bukkit.getLogger().info("CLAN NAME not avilable. "+ChatColor.translateAlternateColorCodes('&',twp.clan) +" to "+ ChatColor.translateAlternateColorCodes('&',newName)+ " by player ("+twp.username+")");
    					return;    					
    				}
    				
    				if (newName.length()>16){
    					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name is too long :-/");
    					return;
    				}
    				
    				if (myWalls.myDB.renameClan(twp.clan, newName, ClanCmd.stripAllClanCharacters(newName))){
    					Bukkit.getLogger().info("CLAN NAME CHANGE from "+ ChatColor.translateAlternateColorCodes('&',twp.clan) +" to "+ ChatColor.translateAlternateColorCodes('&',newName)+ " by player ("+twp.username+")");
    					GameNotifications.sendPlayerCommandSuccess(((Player)sender), "Clan name will change to : "+newName+" in the next game.");
    					GameNotifications.sendPlayerCommandSuccess(((Player)sender), ChatColor.GRAY+"Inappropriate names will = ban :-/ choose carefully.");

    					ClanUtils.changeAllOnlineClanNames(myWalls, twp.clan, newName);

    					this.setClanName(((Player)sender).getUniqueId(), newName);
    					
    				}else{
    					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name NOT changed - already exists.. try another name :-/");
    				}
    			}else{
    				GameNotifications.sendPlayerCommandError(((Player)sender), "Only the clan leader can rename the clan :)");
    			}
    		}else{
				GameNotifications.sendPlayerCommandError(((Player)sender), "try /clan rename NewName");
				return;
    		}
    		
    	}else{
    		sender.sendMessage("Need to be a player in game to make that work :(");
    	}
    }

    private String stripSpecialClanCharacters(String clanName){
    	String finalString="";
    	for (int i=0 ;i< clanName.length();i++){
    		int ASCIIValue = (int)clanName.charAt(i);
//    		Bukkit.getLogger().info("TESTTT: "+clanName.charAt(i)+" "+ASCIIValue);
//    		48 <-> 57 && 65 <-> 90 97 <-> 122 OR 38
    		if ((ASCIIValue > 47 && ASCIIValue < 58) || (ASCIIValue > 64 && ASCIIValue < 91) || (ASCIIValue > 96 && ASCIIValue < 123) || (ASCIIValue == 38)){
    			// THEN OK CHARACTER LIST.
    			finalString +=clanName.charAt(i);
    		}
    	}
		return finalString.replace("&k", "").replace("&l", "").replace("&m", "").replace("&n", "").replace("&o", "").replace("&r", "")
				.replace("&K", "").replace("&L", "").replace("&M", "").replace("&N", "").replace("&O", "").replace("&R", "");
    }

    public static String stripAllClanCharacters(String clanName){
    	
		return clanName.replace("&k", "").replace("&l", "").replace("&m", "").replace("&n", "").replace("&o", "").replace("&r", "")
				.replace("&1", "").replace("&2", "").replace("&3", "").replace("&4", "").replace("&5", "").replace("&6", "")
				.replace("&7", "").replace("&8", "").replace("&9", "").replace("&0", "")
				.replace("&a", "").replace("&b", "").replace("&c", "").replace("&d", "").replace("&e", "").replace("&f", "")
				.replace("&A", "").replace("&B", "").replace("&C", "").replace("&D", "").replace("&E", "").replace("&F", "")
				.replace("&K", "").replace("&L", "").replace("&M", "").replace("&N", "").replace("&O", "").replace("&R", "");
    }
    
    private void kick(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	if (sender instanceof Player){

    		WallsPlayer twp = myWalls.getWallsPlayer(((Player)sender).getUniqueId());
    		if (twp.clanLeader && args.length > 0){
    			String personToKick = args[1];
    			if (sender.getName().equals(personToKick)){
    				GameNotifications.sendPlayerCommandError(((Player)sender), "Can't kick yourself. Try /clan disband if you're leader. Or /clan leave.");
    				return;
    			}

    			if (myWalls.myDB.kickClanMember(personToKick, twp.clan)){
    				
    				Player player = Bukkit.getPlayer(personToKick);
    				if (player!=null){
						UUID pUID = player.getUniqueId();
						WallsPlayer wp = myWalls.getWallsPlayer(pUID);	    					
						wp.clan = null;
    				}
    						
    				
    				GameNotifications.sendPlayerCommandSuccess(((Player)sender), personToKick+" has been kicked from "+ChatColor.translateAlternateColorCodes('&', twp.clan));
    			}else{
    				GameNotifications.sendPlayerCommandError(((Player)sender), "Player was not in the clan / somethign went wrong :-/");
    			}
    		}else{
    			GameNotifications.sendPlayerCommandError(((Player)sender), "Only the clan leader can kick players :)");
    		}
    	}else{
    		sender.sendMessage("Need to be a player in game to make that work :(");
    	}
    }

    
    private void accept(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	if (myWalls.clanInvites.containsKey(((Player)sender).getUniqueId())){
    	
    		if (myWalls.getWallsPlayer(((Player)sender).getUniqueId()).clanLeader){
    			GameNotifications.sendPlayerCommandError((Player)sender, "Nope. A leader can't just join another clan.. think of YOUR clan members!! (or /clan disband)");
    			return;
    		}
    		String clanName = myWalls.clanInvites.get(((Player)sender).getUniqueId());
    		this.setClanName(((Player)sender).getUniqueId(), clanName);
    		
            myWalls.getLogger().log(Level.INFO, TheWalls.chatPrefix + "/CC: " + sender.getName() + " joined "+clanName);

            for (UUID u : myWalls.getAllPlayers().keySet()) {
            	if (Bukkit.getPlayer(u)!=null){        		
            		WallsPlayer anotherWP = myWalls.getWallsPlayer(u);
            		if ((anotherWP.clan!=null && anotherWP.clan.equals(clanName)) || (Bukkit.getPlayer(u).isOp() && myWalls.staffListSnooper.contains(u))){        			
            			Bukkit.getPlayer(u).sendMessage(TheWalls.CLANCHAT_PREFIX.replace("??", clanName) + sender.getName() + ChatColor.WHITE + " joined " + ChatColor.translateAlternateColorCodes('&', clanName));
            		}
            	}
            }

    		
    		
    	}else{
    		GameNotifications.sendPlayerCommandError(((Player)sender), "Aww man you don't have any invites to accept :(");
    	}
    }

    private void disband(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	
    	// clan disband <CLANNAME>
    	
    	if (sender instanceof Player){
        	String playerUID = ((Player)sender).getUniqueId().toString().replace("-", "");
        	try{    	
        		Player player = ((Player)sender);
        		
        		if (args.length==2 && (sender.isOp() || myWalls.getWallsPlayer(player.getUniqueId()).mgm || myWalls.getWallsPlayer(player.getUniqueId()).admin)){
    				if (myWalls.myDB.disbandClanByName(args[1])){
    					GameNotifications.sendPlayerCommandSuccess(player, args[1]+" blew up - all gone! :(");
    					GameNotifications.staffNotification(myWalls, args[1]+" was disbanded by "+player.getName());

    					// TODO: need to remove the clan from anyone on line .. 

    				}else{
    					GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Something went wrong there :(");
    				}
        			
        		}else if (args.length==1){        			
        			if (myWalls.getWallsPlayer(player.getUniqueId()).clanLeader){
        				
        				if (myWalls.myDB.disbandClan(myWalls.getWallsPlayer(player.getUniqueId()).clan)){
        					GameNotifications.sendPlayerCommandSuccess(player, "You're clan blew up - all gone! :(");
        					GameNotifications.staffNotification(myWalls, ChatColor.translateAlternateColorCodes('&',myWalls.getWallsPlayer(player.getUniqueId()).clan)+" was disbanded.");
        					
        					myWalls.getWallsPlayer(player.getUniqueId()).clan="";
        					myWalls.getWallsPlayer(player.getUniqueId()).clanLeader=false;
        				}else{
        					GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Something went wrong there :(");
        				}
        				
        			}else{
        				GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Only a leader can disband!! (you can /clan leave)");	    	
        			}
        		}
        		
        		
        	}catch(Exception E){
        		GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Something went wrong there :(");
        	}    			
    	}
    	

    }


    private void changeLeader(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	
//    	/clan leader <IGN of NewLeader>
    	
    	if (sender instanceof Player){
        	String playerUID = ((Player)sender).getUniqueId().toString().replace("-", "");
        	try{    	
        		Player player = ((Player)sender);
        		if (myWalls.getWallsPlayer(player.getUniqueId()).clanLeader){
        			
        			if (args.length==2){
        				
        				Player newLeader = Bukkit.getPlayer(args[1]);
        				
        				
        				if (newLeader==null){
        					GameNotifications.sendPlayerCommandError((Player)sender, "Nope. New leader must be online");
        					return;
        					
        				}

        				WallsPlayer twpOldLeader = myWalls.getWallsPlayer(player.getUniqueId());
        				WallsPlayer twpNewLeader = myWalls.getWallsPlayer(newLeader.getUniqueId());
        				
        				if (!twpOldLeader.clan.equalsIgnoreCase(twpNewLeader.clan)){

        					Bukkit.getLogger().info("Old Leader Clan - "+twpOldLeader.clan + " & new - "+twpNewLeader.clan );
        					GameNotifications.sendPlayerCommandError((Player)sender, "Nope. New leader must be in your clan");
        					
        					return;        					
        				}
        				
        				if (myWalls.myDB.setNewClanLeader(player.getUniqueId(), newLeader.getName(), newLeader.getUniqueId())){        				
        					GameNotifications.sendPlayerCommandSuccess(player, "You're no longer clan leader! :(");
        					GameNotifications.sendPlayerCommandSuccess(newLeader, "You're now clan leader! =)");
        					myWalls.getWallsPlayer(player.getUniqueId()).clanLeader=false;
        					myWalls.getWallsPlayer(newLeader.getUniqueId()).clanLeader=true;
        					
        				}else{
        					GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Couldn't find clan leader O_o. Let staff now pls :)");
        				}

        			}else{
            			GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Try /clan leader <IGN of NewLeader>  (New leader must be online)");	    	        				
        			}
        			
        			
        		}else{
        			GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Only a leader can disband!! (you can /clan leave)");	    	
        		}
        		
        	}catch(Exception E){
        		GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Something went wrong there :( ");
        		Bukkit.getLogger().info("Failed to set new clan leader: "+E.getStackTrace());
        	}    			
    	}
    	

    }

    
    
    private void leave(CommandSender sender , Command cmd, String commandLabel, String[] args) {
    	
    	if (sender instanceof Player){
        	String playerUID = ((Player)sender).getUniqueId().toString().replace("-", "");
        	try{    	
        		Player player = ((Player)sender);
        		if (myWalls.getWallsPlayer(player.getUniqueId()).clanLeader){
        			GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. A leader can't just leave.. think of the members!! (or /clan disband)");
        		}else if (myWalls.myDB.kickClanMember(sender.getName(), myWalls.getWallsPlayer(((Player)sender).getUniqueId()).clan)){	    	
        			if (player!=null){
        				UUID pUID = player.getUniqueId();
        				WallsPlayer twp = myWalls.getWallsPlayer(pUID);	    					
        				twp.clan = null;
        			}
        			GameNotifications.sendPlayerCommandSuccess(player, "You're clanless! :(");
        		}else{
        			GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
        		}
        		
        	}catch(Exception E){
        		GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
        	}    			
    	}
    	

    }

    private void create(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        
//    	if (sender.isOp() || myWalls.isMGM(((Player)sender).getUniqueId())){

    		
    		// check the clan name does not exist
    		
    		// create the clan in the .guilds table
    		
    		// set the name (of guild) 
			//    		Leader (name) 
			//    		uuid (of leader
    		
    		if (sender.isOp() && args.length == 3){
    			Player newLeader = Bukkit.getPlayer(args[2]);
    			
    			if (myWalls.getWallsPlayer(newLeader.getUniqueId()).clanLeader){
    				GameNotifications.sendPlayerCommandError(((Player)sender), "Player is a leader already.. they need to /clan disband first!");
    				
    			}else{
        			String newName = stripSpecialClanCharacters(args[1]);

    				
    				if (myWalls.myDB.createClan(newName, args[2], newLeader.getUniqueId().toString().replace("-", ""), ClanCmd.stripAllClanCharacters(newName))){
    					GameNotifications.sendPlayerCommandSuccess(((Player)sender), args[2]+" is now leader of "+newName);
    					GameNotifications.sendPlayerCommandSuccess(newLeader, "You are now leader of "+newName);
    					this.setClanName(newLeader.getUniqueId(),newName);
    					myWalls.getWallsPlayer(newLeader.getUniqueId()).clanLeader = true;
        			}else{
        				GameNotifications.sendPlayerCommandError(((Player)sender), "Clan NOT created - it already exists.. :-/");
        			}
    				
    			}
    		}else if (args.length == 2 && sender instanceof Player){

    			if (!this.myWalls.isVIP(((Player)sender).getUniqueId())){
    				GameNotifications.sendPlayerCommandError(((Player)sender), "You need to be VIP and above to create clans.");
    				return;
    			}
    			
    			Player newLeader = (Player)sender;
    			
    			if (myWalls.getWallsPlayer(newLeader.getUniqueId()).clanLeader){
    				GameNotifications.sendPlayerCommandError(((Player)sender), "Um... You're a leader already.. you need to /clan disband first!");
    				
    			}else{
        			String newName = stripSpecialClanCharacters(args[1]);

    				if (LanguageChecker.curseFinder(stripAllClanCharacters(newName))){
    					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name not appropriate. You have been warned. :-/");
    					Bukkit.getLogger().info("INAPPROPRIATE CLAN NAME CREATE "+ ChatColor.translateAlternateColorCodes('&',newName)+ " by player ("+newLeader.getName()+")");
    					return;
    				}
    				
    				if (TheWalls.bannedClanNames.contains(stripAllClanCharacters(newName).toLowerCase())){
    					GameNotifications.sendPlayerCommandError(((Player)sender), "Clan name not avilable.");
    					Bukkit.getLogger().info("INAPPROPRIATE CLAN NAME CREATE "+ ChatColor.translateAlternateColorCodes('&',newName)+ " by player ("+newLeader.getName()+")");
    					return;    					
    				}

    				if (myWalls.myDB.createClan(newName, newLeader.getName(), newLeader.getUniqueId().toString().replace("-", ""), ClanCmd.stripAllClanCharacters(newName))){
    					GameNotifications.staffNotification(this.myWalls, newLeader.getName()+" is now leader of "+newName);
    					GameNotifications.sendPlayerCommandSuccess(newLeader, "You are now leader of "+newName);
    					this.setClanName(newLeader.getUniqueId(),newName);
//    					myWalls.getWallsPlayer(newLeader.getUniqueId()).clan = newName;
    					myWalls.getWallsPlayer(newLeader.getUniqueId()).clanLeader = true;
        			}else{
        				GameNotifications.sendPlayerCommandError(((Player)sender), "Clan NOT created - it already exists.. :-/");
        			}
    				
    			}
    		}else{
    			GameNotifications.sendPlayerCommandError(((Player)sender), "/clan create <clanName>");
    		}
    		
//    	}
    	
    }

    
    private void setClanName(UUID uidOfPlayer, String clanName) {
    	
    	String playerUID = uidOfPlayer.toString().replace("-", "");
		
    	try{    	
    		Player player = Bukkit.getPlayer(uidOfPlayer);
			if (myWalls.myDB.setUsersClan(playerUID, clanName)){
				
				if (player!=null){
					UUID pUID = player.getUniqueId();
					WallsPlayer twp = myWalls.getWallsPlayer(pUID);
					
//					twp.clan = ChatColor.translateAlternateColorCodes('&', clanName);
					twp.clan = clanName;
				}
				GameNotifications.sendPlayerCommandSuccess(player, "You're now part of the clan ["+ChatColor.translateAlternateColorCodes('&', clanName)+"] !");
				
				myWalls.clanInvites.remove(uidOfPlayer);
				
			}else{
				GameNotifications.sendPlayerCommandSuccess(player, "Nope. Something went wrong there :(");
			}
				
		}catch(Exception E){
//			GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
		}
    }
	
}
