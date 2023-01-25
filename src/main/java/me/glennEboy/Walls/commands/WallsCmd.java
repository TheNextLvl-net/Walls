package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.GameState;
import me.glennEboy.Walls.TheWalls.PlayerJoinType;
import me.glennEboy.Walls.TheWalls.PlayerState;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;


public class WallsCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public WallsCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        // usage: /walls start|stop|dropwalls|vip|unvip|reconnect|pro|unpro|addkit|removekit

        if (args.length < 1){
            if (sender instanceof Player ){                
                GameNotifications.sendPlayerCommandError((Player) sender, "usage: /walls stats | chat | whois | nostaffchat | start | stop | dropwalls | vip | unvip | addplayer | pro | unpro |");
                if (sender.isOp()){
                    GameNotifications.sendPlayerCommandError((Player) sender, "usage: dropwalls | players | vip | pro| prouid | gm| mgm | admin | updateplayer | autostartplayers | peacetimemins | clanbattle | captain");
                    GameNotifications.sendPlayerCommandError((Player) sender, "usage: restricted | diamondonly | cya | whitelist | migratevips | getvips | levels | info | test | fakemessage | fakeshout | lobbytrail");
                }
                
            }else{
                sender.sendMessage("usage: /walls start|stop|dropwalls|vip|unvip|addplayer|pro|unpro|");                
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")){
        myStats(sender, cmd, commandLabel, args);


        }else if (args[0].equalsIgnoreCase("chat")){
        chatListener(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("whois")){
        whoIs(sender, cmd, commandLabel, args);
        
        }else if (args[0].equalsIgnoreCase("nostaffchat")){
        noStaffChat(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("start")){

            startWalls(sender, cmd, commandLabel, args);
        
        }else if (args[0].equalsIgnoreCase("addplayer")){
        
            addPlayer(sender, cmd, commandLabel, args);

        
        }else if (args[0].equalsIgnoreCase("silence")){
            silenceComand(sender, cmd, commandLabel, args);



        }else if (args[0].equalsIgnoreCase("debug")){
            switchDebug(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("clan")){
            setClanName(sender, cmd, commandLabel, args);
        
        }else if (args[0].equalsIgnoreCase("dropwalls")){
            
            dropWalls(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("players")){
            showPlayers(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("vip")){
            toggleVIPStatus(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("pro")){
            togglePROStatus(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("prouid")){
            togglePROUIDStatus(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("gm")){
            toggleGMStatus(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("mgm")){
            toggleMGMStatus(sender, cmd, commandLabel, args);
        
        }else if (args[0].equalsIgnoreCase("admin")){
            toggleAdminStatus(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("updateplayer")){
            forceTagUpdate(sender, cmd, commandLabel, args);
            
        }else if (args[0].equalsIgnoreCase("autostartplayers")){
            setAutoStartPlayers(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("peacetimemins")){
            setPeaceTimeMins(sender, cmd, commandLabel, args);
        
        }else if (args[0].equalsIgnoreCase("clanbattle")){
            toggleClanBattle(sender, cmd, commandLabel, args);
            
        }else if (args[0].equalsIgnoreCase("restricted")){
            setPlayerJoinRestriction(sender, cmd, commandLabel, args);
            
        }else if (args[0].equalsIgnoreCase("diamondonly")){
            
            if (sender.isOp()) TheWalls.diamondONLY = !TheWalls.diamondONLY;
            sender.sendMessage("Set to ["+TheWalls.diamondONLY+"]");

        }else if (args[0].equalsIgnoreCase("levels")){
            this.addLevels(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("fakemessage")){
            this.fakeMessage(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("fakeshout")){
            this.fakeShout(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("lobbytrail")){
            this.lobbyTrail(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("cpstrigger")){
            this.cpsTrigger(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("cpsrepeat")){
            this.cpsRepeat(sender, cmd, commandLabel, args);
        
        }else if (args[0].equalsIgnoreCase("showcps")){
            this.showCPS(sender, cmd, commandLabel, args);
            
        }else if (args[0].equalsIgnoreCase("maxcps")){
            this.maxCPS(sender, cmd, commandLabel, args);

        }else if (args[0].equalsIgnoreCase("captain")){
            this.addCaptain(sender, cmd, commandLabel, args);
            
        }else if (args[0].equalsIgnoreCase("logPlayer")){
            this.logPlayer(sender, cmd, commandLabel, args);

        }
        return true;
    }

    private void myStats(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        WallsPlayer twp = myWalls.getWallsPlayer(((Player)sender).getUniqueId());
        try{            
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            
            float kd = (float)twp.statsKills/(float)twp.statsDeaths;

            GameNotifications.sendPlayerCommandSuccess((Player)sender, "Total - Kills: "+twp.statsKills
                    +".  Deaths: "+twp.statsDeaths
                    +".  KD: "+(df.format(kd))
                    +".  Wins: "+twp.statsWins
                    +".  Coins: "+twp.coins);
        }catch(Exception e){
            GameNotifications.sendPlayerCommandError((Player)sender, "Sorry - stats not available for you just now :(");
        }
    }

    
    private void chatListener(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (myWalls.isStaff(((Player)sender).getUniqueId()) || sender.isOp() ){
            if (myWalls.staffListSnooper.contains(((Player)sender).getUniqueId())){
                myWalls.staffListSnooper.remove((((Player)sender).getUniqueId()));
                if (!sender.isOp()){                    
                    GameNotifications.staffMessage(myWalls, myWalls.getStaffList(), sender.getName() + " is NO LONGER listening to ALL chat.");                
                }else{
                    GameNotifications.sendPlayerCommandSuccess((Player)sender, sender.getName() + " is NO LONGER listening to ALL chat.");
                }
            }else{                
                myWalls.staffListSnooper.add(((Player)sender).getUniqueId());
                if (!sender.isOp() ){                                        
                    GameNotifications.staffMessage(myWalls, myWalls.getStaffList(), sender.getName() + " is now listening to ALL chat.");
                }else{
                    GameNotifications.sendPlayerCommandSuccess((Player)sender, sender.getName() + " is now listening to ALL chat.");
                }
            }
                    
        }
    }

    private void noStaffChat(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (myWalls.isStaff(((Player)sender).getUniqueId()) || sender.isOp()){
            if (myWalls.noStaffChat.contains(((Player)sender).getUniqueId())){
                myWalls.noStaffChat.remove(((Player)sender).getUniqueId());
                GameNotifications.staffNotification(myWalls, sender.getName()+" is now getting staff chat messages again!");
            }else{
                GameNotifications.staffNotification(myWalls, sender.getName()+" will no longer get staff chat messages.");
                myWalls.noStaffChat.add(((Player)sender).getUniqueId());                
            }
        }
    }

    private void whoIs(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (myWalls.isStaff(((Player)sender).getUniqueId()) || sender.isOp()){


            if (sender instanceof Player){
                
                GameNotifications.sendPlayerCommandSuccess((Player)sender,  this.getPlayerIGNInfo(args[1]));
                
            }else{
                
                sender.sendMessage(this.getPlayerIGNInfo(args[1]));
                
            }
            

                    
        }
    }
    
    
    private String getPlayerIGNInfo(String userName) {
        try {

            Player p = Bukkit.getPlayer(userName);
            

            String mojangURL = "https://api.mojang.com/user/profiles/";
            mojangURL = mojangURL + p.getUniqueId().toString().replace("-", "") + "/" + "names";
            
            System.out.println(userName+" - "+p.getUniqueId().toString());
            System.out.println(mojangURL);
            
            URL mojangAPI = new URL(mojangURL);

            URLConnection myConnection = mojangAPI.openConnection();

            myConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            myConnection.setConnectTimeout(1000);

            BufferedReader statsStreamIn = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));

            String throwAwayString = "";
            throwAwayString = statsStreamIn.readLine();
            System.out.println("throwAwayString");

            this.myWalls.getLogger().log(Level.INFO, TheWalls.chatPrefix+" got Details for player {" + userName + "}");
            
            // close the connection
            statsStreamIn.close();
            
            return throwAwayString;

        } catch (IOException ioe) {

            System.err.println("Caught IOException: " + ioe.getMessage());
        }
        return "something didn't work";

    }

    
    
    
    /**
     * MGM / Admin + 
     */
    
    private void startWalls(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp() 
                || (sender instanceof Player 
                        && (myWalls.isMGM(((Player)sender).getUniqueId())) 
                        || TheWalls.specialGMs.contains(sender.getName())) 
                        && !myWalls.starting){
            
                GameNotifications.broadcastMessage("Game starts in " + ChatColor.LIGHT_PURPLE + "30"+ChatColor.WHITE + " seconds!!");

                myWalls.clock.setClock(30, new Runnable() {
                    @Override
                    public void run() {
                        GameStarter.startGame(myWalls.getAllPlayers(), myWalls);
                    }

                });
                myWalls.starting = true;

        }else{
            if (sender instanceof Player){
                GameNotifications.sendPlayerCommandError((Player)sender, "Patience OB1 its already starting!");
            }else{
                sender.sendMessage(TheWalls.chatPrefix + ChatColor.LIGHT_PURPLE + "Patience OB1 its already starting!");
            }
        }
    }
    
    private void addPlayer(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp() 
                || (sender instanceof Player && myWalls.isMGM(((Player)sender).getUniqueId()))){
            if (args.length < 3){
                GameNotifications.sendPlayerCommandError((Player)sender, "Command is /walls addplayer <IGN> <teamNumber>");                
                return;
            }
            
            if (Bukkit.getServer().getPlayer(args[1]) != null) {
                final Player player = Bukkit.getServer().getPlayer(args[1]);
                if (Bukkit.getServer().getPlayer(args[1]).isOnline()) {
                    
                    int teamNumber = -1;
                    try {
                        teamNumber = Integer.parseInt(args[2]);
                    } catch (final NumberFormatException e) {
                        GameNotifications.sendPlayerCommandError((Player)sender, "Try /walls addplayer <IGN> # {1, 2, 3 or 4}");
                        return;
                    }
                    if (teamNumber < 1 || teamNumber > 4) {
                        GameNotifications.sendPlayerCommandError((Player)sender, "Invalid team, please use a number between 1 and 4.");
                        return;
                    }

                    WallsPlayer twp = myWalls.getWallsPlayer(player.getUniqueId());
                    
                    
                    twp.playerState = PlayerState.values()[teamNumber];
                    myWalls.getAllPlayers().put(player.getUniqueId(), twp);
                    player.setAllowFlight(false);
                    player.getInventory().clear();
                    player.teleport(TheWalls.spawns.get(twp.playerState.ordinal()));
                    myWalls.playerScoreBoard.addPlayerToTeam(player.getUniqueId(), twp.playerState);
//                    player.setDisplayName(TheWalls.teamChatColors[teamNumber]+player.getName());
                    PlayerVisibility.hideAllSpecs(myWalls,player);
                    PlayerVisibility.makeInVisPlayerNowVisible(myWalls, player);
                    player.setHealth(20);
                    player.setFoodLevel(20);
                    
                    GameNotifications.sendPlayerCommandSuccess(player, "Gratz! You've been added to "+TheWalls.teamsNames[twp.playerState.ordinal()]);
                    GameNotifications.sendPlayerCommandSuccess((Player)sender, "Success! "+player.getName()+" been added to "+TheWalls.teamsNames[twp.playerState.ordinal()]);
                }
            }

        }
    }

    private void setClanName(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
//                || (sender instanceof Player && myWalls.isMGM(((Player)sender).getUniqueId()))){
            try{
                String uidOfPlayer = PlayerUidChecker.getUIDString(args[1]);
                
                if (args.length == 3){
                    try{        
                        if (myWalls.myDB.setUsersClan(uidOfPlayer, args[2])){
                            
                            Player player = Bukkit.getPlayerExact(args[1]);
                            if (player!=null){
                                UUID pUID = player.getUniqueId();
                                WallsPlayer twp = myWalls.getWallsPlayer(pUID);
                                
                                twp.clan = ChatColor.translateAlternateColorCodes('&', args[2]);
                            }
                            sender.sendMessage(ChatColor.GREEN + args[1] + " is now part of ["+ChatColor.translateAlternateColorCodes('&', args[2])+"] clan!");
                        }else{
                            GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
                        }
                            
                    }catch(Exception E){
                        GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
                    }
                }else if (args.length == 2){
                    try{        
                        if (myWalls.myDB.setUsersClan(args[1], null)){            
                            Player player = Bukkit.getPlayerExact(args[1]);
                            if (player!=null){
                                UUID pUID = player.getUniqueId();
                                WallsPlayer twp = myWalls.getWallsPlayer(pUID);                            
                                twp.clan = null;
                            }
                            sender.sendMessage(ChatColor.GREEN + args[1] + " had their clan removed!");
                        }else{
                            GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
                        }
                            
                    }catch(Exception E){
                        GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
                    }                
                }else{
                    sender.sendMessage(ChatColor.RED+ "clan <ign> <clanName>");
                }
                
            }catch (Exception e){
                sender.sendMessage(ChatColor.RED+ "Nope. That didnt work: and threw an exception :(");
                e.printStackTrace(System.out);
            }
            
        }
    }

    private void silenceComand(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        
        if (sender.isOp() 
                || (sender instanceof Player && myWalls.isMGM(((Player)sender).getUniqueId()))){
            TheWalls.shhhhh = !TheWalls.shhhhh;
            if (TheWalls.shhhhh){                
                GameNotifications.broadcastMessage(ChatColor.RED+"EVERYONE JUST GOT SHHHHH'D!!");
            }else{
                GameNotifications.broadcastMessage(ChatColor.GREEN+"You are Free. To speak. (ish)");
            }
        }
        
    }

    
    /**
     * OP 
     */
    
    private void switchDebug(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp()){
            TheWalls.debugMode = !TheWalls.debugMode;
            sender.sendMessage("Done. debug set to "+TheWalls.debugMode);
        }
    }

    private void addLevels(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp() ){
            if (args.length == 3){
                int levelsToAdd = 0;
                try {
                    levelsToAdd = Integer.parseInt(args[2]);
                } catch (final NumberFormatException e) {
                    GameNotifications.sendPlayerCommandError((Player)sender, "Try /walls levels <IGN> # ");
                    return;
                }

                try{                    
                    Bukkit.getPlayer(args[1]).setLevel(Bukkit.getPlayer(args[1]).getLevel()+levelsToAdd);
                    GameNotifications.sendPlayerCommandError((Player)sender, "Done.");
                }catch (Exception e){
                    GameNotifications.sendPlayerCommandError((Player)sender, "Nope.. ");
                    return;                    
                }
                
            }else{
                sender.sendMessage(ChatColor.RED+ "walls levels <ign> <levels>");
            }
        }
    }

    
    @SuppressWarnings("deprecation")
    private void getPlayerInfo(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp() || myWalls.isMGM(((Player)sender).getUniqueId()) ){
            if (args.length == 2){

                try{                    
                    WallsPlayer twp = myWalls.getWallsPlayer(Bukkit.getPlayer(args[1]).getUniqueId());
                    sender.sendMessage("Player: "+twp.username);
                    sender.sendMessage("Coins: "+twp.coins);
                    sender.sendMessage("Curse(s): "+twp.curseCount);
                    sender.sendMessage("IGN History: " + getPlayerIGNInfo(args[1]));
                    sender.sendMessage("Max CPS: " + myWalls.playerMaxCPSRate.get(Bukkit.getPlayer(args[1]).getUniqueId()));
                    sender.sendMessage("Last CPS: " + myWalls.playerLastCPSRate.get(Bukkit.getPlayer(args[1]).getUniqueId()));
                    
                }catch (Exception e){
                    sender.sendMessage("Nope.. ");
                    return;                    
                }
                
            }else{
                sender.sendMessage(ChatColor.RED+ "walls info <ign>");
            }
        }
    }


    
    @SuppressWarnings("deprecation")
    private void fakeMessage(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            if (args.length > 2){

                Player p = Bukkit.getServer().getPlayer(args[1]);

                if (p != null) {
                    
                    final StringBuilder fakeMessage = new StringBuilder();
                    
                    for (int x = 2; x < args.length; x++) {
                        
                        fakeMessage.append(args[x]).append(" ");
                    }
                    
                    fakeMessage.setLength(fakeMessage.length() - 1);

                    PlayerChatHandler.fakePlayerChat(myWalls, p.getUniqueId(), fakeMessage.toString());

                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found online :(");
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private void fakeShout(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            if (args.length > 2){

                Player p = Bukkit.getServer().getPlayer(args[1]);

                if (p != null) {
                    
                    final StringBuilder fakeMessage = new StringBuilder();
                    
                    for (int x = 2; x < args.length; x++) {
                        
                        fakeMessage.append(args[x]).append(" ");
                    }
                    
                    fakeMessage.setLength(fakeMessage.length() - 1);

                    ShoutCmd.fakeShout(this.myWalls,p.getUniqueId(), fakeMessage.toString());

                } else {
                    sender.sendMessage(ChatColor.RED + "Player not found online :(");
                }
            }
        }
    }


    private void lobbyTrail(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            String options = "flowerpower / diamond / richboi / bodyguard / ez / hungry / blockbawz / random / flashy / sweets / dangerous";
            if (args.length == 2){

                if (options.indexOf(args[1].toLowerCase())>-1){
                    TheWalls.lobbyTrail = args[1];
                    
                }else{
                    TheWalls.lobbyTrail = null;
                    sender.sendMessage(ChatColor.RED + "try /walls lobbytrail "+options);
                }
            }else{
                sender.sendMessage(ChatColor.RED + "try /walls lobbytrail "+options);
            }
        }
    }

    private void dropWalls(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp() && myWalls.getGameState()==GameState.PEACETIME){
            myWalls.dropWalls();
        }
    }

    
    private void setPlayerJoinRestriction(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp()){            

            if (args.length<2){
                sender.sendMessage("/walls restricted ANYONE, VIP, PRO, LEGENDARY, STAFF, YOUTUBER ");
                return;
                
            }
            try{
                TheWalls.playerJoinRestriction = PlayerJoinType.valueOf(args[1]);
            }
            catch(Exception e){
                sender.sendMessage("Nope");                
            }
            sender.sendMessage("Server restricted to :"+TheWalls.playerJoinRestriction.toString());
        }
        
    }
    
    
    private void addCaptain(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp()){            
            Player p = Bukkit.getPlayer(args[1]);


            TheWalls.teamCaptains.add(args[1]);
            sender.sendMessage(args[1]+" added to team captain list");

        }
        
    }


    private void showPlayers(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        Set<UUID> myUIDS = myWalls.getAllPlayers().keySet();
        sender.sendMessage(TheWalls.chatPrefix+"Getting all the players in game!");
        for (UUID pUID : myUIDS){
            
            sender.sendMessage(Bukkit.getOfflinePlayer(pUID).getName()+" "+myWalls.getWallsPlayer(pUID).playerState.name());
        }
        sender.sendMessage(TheWalls.chatPrefix+"---------DONE---------");
    }

    private void toggleVIPStatus(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            try{                
                UUID pUID = Bukkit.getPlayer(args[1]).getUniqueId();
                WallsPlayer twp = myWalls.getWallsPlayer(pUID);
                twp.vip = !twp.vip;
                myWalls.getAllPlayers().put(pUID, twp);
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Success. "+args[1]+"'s vip status changed to "+twp.vip);
            }catch(Exception E){
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
            }
        }
    }

    private void togglePROStatus(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            try{                
                UUID pUID = Bukkit.getPlayer(args[1]).getUniqueId();
                WallsPlayer twp = myWalls.getWallsPlayer(pUID);
                   twp.pro = !twp.pro;
   
//                   myWalls.myDB.setSCBPro(args[1], (twp.pro) ? 1 : 0);
                   myWalls.myDB.setPro(args[1], (twp.pro) ? 1 : 0);
                
                   myWalls.getAllPlayers().put(pUID, twp);
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Success. "+args[1]+"'s "+ChatColor.BLUE+"PRO"+ChatColor.WHITE+" status changed to "+twp.pro);
            }catch(Exception E){
                if (sender instanceof Player){                    
                    GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
                }else{
                    sender.sendMessage("Nope. Something went wrong there :(");
                }
            }
        }
    }

    private void togglePROUIDStatus(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            try{                

   
                   myWalls.myDB.setUIDPro(args[1].replace("-", ""));
                
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Success. "+args[1]+"'s "+ChatColor.BLUE+"PRO"+ChatColor.WHITE+" status changed to true");
            }catch(Exception E){
                if (sender instanceof Player){                    
                    GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
                }else{
                    sender.sendMessage("Nope. Something went wrong there :(");
                }
            }
        }
    }

    private void toggleGMStatus(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            try{                
                UUID pUID = Bukkit.getPlayer(args[1]).getUniqueId();
                WallsPlayer twp = myWalls.getWallsPlayer(pUID);
                twp.gm = !twp.gm;
                myWalls.getAllPlayers().put(pUID, twp);
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Success. "+args[1]+"'s GM status changed to "+twp.gm);
            }catch(Exception E){
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
            }
        }
    }

    private void toggleMGMStatus(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            try{                
                UUID pUID = Bukkit.getPlayer(args[1]).getUniqueId();
                WallsPlayer twp = myWalls.getWallsPlayer(pUID);
                twp.mgm = !twp.mgm;
                myWalls.getAllPlayers().put(pUID, twp);
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Success. "+args[1]+"'s MGM status changed to "+twp.mgm);
            }catch(Exception E){
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
            }
        }
    }
    
    
    private void toggleAdminStatus(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        if (sender.isOp()){
            try{                
                UUID pUID = Bukkit.getPlayer(args[1]).getUniqueId();
                WallsPlayer twp = myWalls.getWallsPlayer(pUID);
                twp.admin = !twp.admin;
                myWalls.getAllPlayers().put(pUID, twp);
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Success. "+args[1]+"'s ADMIN status changed to "+twp.admin);
            }catch(Exception E){
                GameNotifications.sendPlayerCommandSuccess((Player)sender, "Nope. Something went wrong there :(");
            }
        }
    }

    
    private void setAutoStartPlayers(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        //walls autostartplayers ##
        if (sender.isOp()){
            if (args.length == 2){
                try{        
                    TheWalls.preGameAutoStartPlayers = Integer.parseInt(args[1]);
                    GameNotifications.sendPlayerCommandSuccess((Player)sender, "Yup. Auto Start Set to "+myWalls.preGameAutoStartPlayers);
                }catch(Exception E){
                    GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Something went wrong there :(");
                }
            }else{
                sender.sendMessage(ChatColor.RED+ "autostartplayers <number of players> ");
            }
        }
    }
    
    private void setPeaceTimeMins(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        //walls peactimemins ##
        if (sender.isOp()){
            if (args.length == 2){
                try{        
                    TheWalls.peaceTimeMins = Integer.parseInt(args[1]);
                    GameNotifications.sendPlayerCommandSuccess((Player)sender, "Yup. Peace Time Minutes Set to "+myWalls.peaceTimeMins);
                }catch(Exception E){
                    GameNotifications.sendPlayerCommandError((Player)sender, "Nope. Something went wrong there :(");
                }
            }else{
                sender.sendMessage(ChatColor.RED+ "peacetimemins <mins>");
            }
        }
    }
    
    private void toggleClanBattle(CommandSender sender , Command cmd, String commandLabel, String[] args) {

        //walls peactimemins ##
        if (sender.isOp()){

            TheWalls.clanBattle = !TheWalls.clanBattle;
            GameNotifications.sendPlayerCommandSuccess((Player)sender, "Yup. ClanBattle set to "+myWalls.clanBattle);
        }
    }


    private void forceTagUpdate(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp()){

            Player playerToForce = Bukkit.getPlayer(args[1]);
            if (playerToForce != null){                
                myWalls.myDB.forceLoadPlayer(args[1], playerToForce.getUniqueId());
            }

        }
    }
    
    private void cpsTrigger(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp() && args.length==2) {
            myWalls.cpsTrigger=Integer.parseInt(args[1]);
            GameNotifications.sendPlayerCommandSuccess((Player)sender, "Yup. cpsTrigger set to "+args[1]);
        }else{
            GameNotifications.sendPlayerCommandError((Player)sender, "Nope. try /walls cpstrigger #");
        }
    }

    
    private void cpsRepeat(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp() && args.length==2){
            myWalls.cpsRepeatCancelTrigger=Integer.parseInt(args[1]);
            GameNotifications.sendPlayerCommandSuccess((Player)sender, "Yup. cpsRepeatCancel set to "+args[1]);
        }else{
            GameNotifications.sendPlayerCommandError((Player)sender, "Nope. try /walls cpsrepeat #");
        }
    }
    
    
    private void showCPS(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp()){
            TheWalls.showCPS = !TheWalls.showCPS;
            GameNotifications.sendPlayerCommandSuccess(((Player)sender), "Show CPS warnings set to "+TheWalls.showCPS);
        }else{
            GameNotifications.sendPlayerCommandError((Player)sender, "Nope.");
        }
    }
    
    
    private void maxCPS(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp() && args.length==2){
            
            TheWalls.MaxAllowedCPS = Integer.parseInt(args[1]);
            
            GameNotifications.sendPlayerCommandSuccess(((Player)sender), "Max CPS set to "+TheWalls.MaxAllowedCPS);
        }else{
            GameNotifications.sendPlayerCommandError((Player)sender, "Nope.");
        }
    }

    private void logPlayer(CommandSender sender , Command cmd, String commandLabel, String[] args) {
        if (sender.isOp() && args.length==2){
            
            TheWalls.logPlayer = args[1];
            
            GameNotifications.sendPlayerCommandSuccess(((Player)sender), "Now Logging data for: "+TheWalls.logPlayer);
        }else{
            GameNotifications.sendPlayerCommandError((Player)sender, "Nope.");
        }
    }

    
}
