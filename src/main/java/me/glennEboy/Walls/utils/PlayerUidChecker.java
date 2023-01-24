package me.glennEboy.Walls.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class PlayerUidChecker {


//    https://api.mojang.com/users/profiles/minecraft/Bee_I_Gee?at=1422887879
//    {"id":"6835fa3bcd654652b72e0ef3eb5c6b3a","name":"FlowerPower"}
//    {"id":"838eace7c28e4458b93095a5e4ae2780","name":"glennEboy"}
    
    private static String mojangUidURL = "https://api.mojang.com/users/profiles/minecraft/";
    private static String dateBeforeIGNChanges = "?at=1422887879";
    
//    private static String mojangUidURL = "https://api.mojang.com/users/profiles/minecraft/";

    public static String getUIDString(String playerName) throws Exception {

        String uidOfPlayer = null;

        URL versionDoc = new URL(PlayerUidChecker.mojangUidURL + playerName + PlayerUidChecker.dateBeforeIGNChanges);

        URLConnection myConnection = versionDoc.openConnection();

        myConnection.setConnectTimeout(1000);
        myConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        BufferedReader statsStreamIn = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));

        String throwAwayString, gotIt = "";
        while ((throwAwayString = statsStreamIn.readLine()) != null) {
            gotIt = throwAwayString;
        }

        final JSONObject dataJSON = (JSONObject) new JSONParser().parse(gotIt);
        uidOfPlayer = (String) dataJSON.get("id");
//                final String name = (String) dataJSON.get("name");

//                System.out.println("ID - " + id);
//                System.out.println("Name - " + name);
        // close the connection
        statsStreamIn.close();


        return uidOfPlayer;
    }
}
