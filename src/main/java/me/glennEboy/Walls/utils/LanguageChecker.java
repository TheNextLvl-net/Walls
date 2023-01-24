package me.glennEboy.Walls.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class LanguageChecker {


	public static String curseString = "fuck|bitch|stfu|fuk|fcuk|fuuck|fuuuck|shit |penis|dick|asshole|fag|piss |omfg|nigger|nigga|cunt |vittu|vitsi|mierda|joder|gilipollas|cabron|bastard|biatch|biach|twat |fag ";
	private static TheWalls myWalls;
	
	
	public static void enableChecker(TheWalls aWalls){
		myWalls = aWalls;
		LanguageChecker.getCurseWords();
	}
	
	public static void checkLanguageForCursing(AsyncPlayerChatEvent event){

		if (LanguageChecker.curseFinder(event.getMessage())){
    		
    		LanguageChecker.notifyPeopleOfInfraction(event.getPlayer(), event.getMessage());
    		LanguageChecker.checkActionRequired(event.getPlayer().getUniqueId());
    		event.setCancelled(true);
    		return;
    	}

	}

	
	
	private static void notifyPeopleOfInfraction(Player p, String message){
	
		GameNotifications.sendPlayerCommandError(p, "Cursing in chat will earn a ban.");
		GameNotifications.staffNotification(myWalls, p.getName()+" warned for cursing.");
		InterWallsClient.sendMessage(TheWalls.scCode+p.getName()+" warned for cursing on w"+TheWalls.serverNumber);

		GameNotifications.opBroadcast(p.getName()+": "+message);
		Bukkit.getServer().getLogger().info(TheWalls.chatPrefix+p.getName()+" WARNED FOR CURSING {"+message+"}");
	
	}

	
	private static void checkActionRequired(UUID pUID){
	
		WallsPlayer twp = myWalls.getWallsPlayer(pUID);
		int numberOfCurses = ++twp.curseCount;
		switch(numberOfCurses){
		case 2:
			myWalls.kickOffPlayerKicker(pUID,TheWalls.chatPrefix+"Cursing in chat will earn you a ban.");
			break;
		case 3:
			myWalls.kickOffPlayerKicker(pUID,TheWalls.chatPrefix+"Cursing in chat will earn you a ban.");
			break;
		case 4:
			try {
				myWalls.myDB.banPlayer(Bukkit.getPlayer(pUID).getName(), "Plugin banned excessive cursing in chat.", "[W"+TheWalls.serverNumber+"]");
				InterWallsClient.sendMessage(TheWalls.scCode+Bukkit.getPlayer(pUID).getName()+" warned for cursing on w"+TheWalls.serverNumber);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myWalls.kickOffPlayerKicker(pUID,TheWalls.chatPrefix+"Plugin banned excessive cursing in chat. [W"+TheWalls.serverNumber+"]");
			break;
			
		}
		
		
	}
	
	
	public static boolean curseFinder(String chatText){
		Pattern p = Pattern.compile("^*" + curseString + "*");
		Matcher m = p.matcher(chatText.toLowerCase());
		return m.find();
	}
	
	public static void getCurseWords() {
		try {

			URL versionDoc = new URL("https://dl.dropboxusercontent.com/u/58929303/MCBawz/MCBawz_curse_words.txt");

			URLConnection myConnection = versionDoc.openConnection();

			myConnection.setConnectTimeout(1000);
	        myConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

			BufferedReader statsStreamIn = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));

			String throwAwayString = "";
			while ((throwAwayString = statsStreamIn.readLine()) != null) {
				LanguageChecker.curseString = throwAwayString;

			}

			if (LanguageChecker.curseString == null) {
				LanguageChecker.curseString = "fuck|bitch|fuk|fuuck|fuuuck|gay|shit|penis|dick|asshole|piss|nigger|nigga|cunt|vittu|vitsi|mierda|joder|gilipollas|cabron|bastard|biatch|biach";
			} else {
				Bukkit.getServer().getLogger().log(Level.INFO, "TheWalls: got Curse words OK from server.");
			}
			// close the connection
			statsStreamIn.close();

		} catch (IOException ioe) {

			LanguageChecker.curseString = "fuck|bitch|fuk|fuuck|fuuuck|gay|shit|penis|dick|asshole|piss|nigger|nigga|cunt|vittu|vitsi|mierda|joder|gilipollas|cabron|bastard|biatch|biach";
			System.err.println("Caught IOException: " + ioe.getMessage());
		}

	}

	
}
