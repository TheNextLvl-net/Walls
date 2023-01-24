package me.glennEboy.Walls.utils;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.commands.ClanCmd;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class Database extends Thread{

    private final TheWalls myWalls;
    
    private String wallDB = "";
    private String myDB = "";
    
    private String url = "";
    private String user  = "";
    private String password  = "";
    private String mUser  = "";
    private String mPassword  = "";

    private String wallDB2 = "/tableName";
    private String url2 = "jdbc:mysql://127.0.0.1:3306";
    private String user2  = "user1";
    private String password2  = "password1";

    private String wallDB3 = "/tableName";
    private String url3 = "jdbc:mysql://127.0.0.1:3306";
    private String user3  = "user2";
    private String password3  = "password2";
    
    private final Map<UUID, String> playersToLogin = new HashMap<UUID, String>();

    private Connection wallsConnection;
    private Connection myConnection; 
    private Connection wallsConnection2;
    private Connection wallsConnection3;

    
    public List<String> VIPS = new ArrayList<String>();
    

    public Database(TheWalls plugin) {
        this.myWalls = plugin;
        this.start();
    }

    
    /**
     * Called from onEnable in JavaPlugin
     */
    public void onEnable() {
//        final FileConfiguration config = this.myWalls.getConfig();
        this.url = "jdbc:mysql://127.0.0.1:3306";
        this.wallDB = "/tableName";
        this.myDB = "/tableName2";
        this.mUser = "usrename";
        this.mPassword = "passwor";
        
        this.user = "username2";
        this.password = "password";
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }
    

    @Override
    public void run() {
        int timer=100;
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(timer);
            } catch (final InterruptedException e) {
                break;
            }
            
            switch (myWalls.getGameState()){
            case PREGAME:
                timer = 100;
            case PEACETIME:
            case FIGHTING:
                timer = 600;

                if (this.playersToLogin.size()>0){
                    List<UUID> doneList = new ArrayList<UUID>();
                    synchronized(playersToLogin){                        
                        for (UUID playerUID : this.playersToLogin.keySet()){
                            if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(playerUID))){                    
                                loadStatsPlayer(this.playersToLogin.get(playerUID),playerUID);
                                loadStatsWalls(this.playersToLogin.get(playerUID),playerUID);
                                loadPaidKitsForUser(this.playersToLogin.get(playerUID), playerUID);
                                doneList.add(playerUID);
                            }
                        }
                    }
                    for (UUID a : doneList){
                        this.playersToLogin.remove(a);
                    }
                }
                break;
                default:
                break;
            }
            
        }
    }

    public void abort() {
    }
    

    public void forceLoadPlayer(String playerName, UUID aUID){
        if (!myWalls.getAllPlayers().containsKey(aUID)){            
            this.playersToLogin.put(aUID, playerName);
        }
    }

    public void loadPlayer(String playerName, UUID aUID){
        if (!myWalls.getAllPlayers().containsKey(aUID)){
            synchronized(playersToLogin){                
                this.playersToLogin.put(aUID, playerName);
            }
        }
    }
    
    public void loadPaidKitsForUser(String username, UUID playerUID) {
        WallsPlayer twp = myWalls.getWallsPlayer(playerUID);

        try (PreparedStatement statement = getWallsConnection().prepareStatement("SELECT `kitname` FROM `paidkits` WHERE `uuid` = ? AND `kitname`<> ?")) {
            statement.setString(1, playerUID.toString().replace("-", ""));
            statement.setString(2, "pro");
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                twp.paidKits = twp.paidKits + "," + result.getString("kitname");
            }
        } catch (final SQLException e) {
            myWalls.getLogger().warning("Failure to load paid kits: " + e.getMessage());
        }
        myWalls.getAllPlayers().put(playerUID, twp);
    }


    private void loadStatsPlayer(String playerName, UUID aUID) {
        
        String uuid = aUID.toString().replace("-", ""); 

        WallsPlayer wallsPlayer = null;
        
        if (this.myWalls.getAllPlayers().containsKey(aUID)){
            wallsPlayer = this.myWalls.getWallsPlayer(aUID);
        }else{
            wallsPlayer = new WallsPlayer();
        }
        

        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `accounts` WHERE unique_id = UNHEX(?)")) {

            statement.setString(1, uuid);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.first()) {
                if (resultSet.getInt("vip") == 1) {
                    wallsPlayer.vip = true;
                }
                if (resultSet.getInt("gm") == 1) {
                    wallsPlayer.gm = true;
                }
                if (resultSet.getInt("mgm") == 1) {
                    wallsPlayer.mgm = true;
                }
                if (resultSet.getInt("admin") == 1) {
                    wallsPlayer.admin = true;
                }
                if (resultSet.getInt("pro") == 1) {
                    wallsPlayer.pro = true;
                }
                if (resultSet.getInt("legendary") == 1) {
                    wallsPlayer.legendary = true;
                }
                if (resultSet.getInt("owner") == 1) {
                    wallsPlayer.owner = true;
                }
                if (resultSet.getInt("youtuber") == 1) {
                    wallsPlayer.youtuber = true;
                }
                
                wallsPlayer.clan = resultSet.getString("guild");
                
                wallsPlayer.coins = resultSet.getInt("butter_coins");
                
            } else {
                try (PreparedStatement statement2 = getMConnection().prepareStatement("INSERT INTO `accounts` (`username`) VALUES (?)")) {
                    statement2.setString(1, playerName);
                    statement2.executeUpdate();
                }
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to load stats: " + e.getMessage());
        }
        
        wallsPlayer.clanLeader = isClanOwner(uuid);
        
        myWalls.getAllPlayers().put(aUID, wallsPlayer);

    }

    
    private void loadStatsWalls(String playerName, UUID aUID) {
        String tempUID = aUID.toString().replace("-", "");

        WallsPlayer wallsPlayer = null;
        
        if (this.myWalls.getAllPlayers().containsKey(aUID)){
            wallsPlayer = this.myWalls.getWallsPlayer(aUID);
        }else{
            wallsPlayer = new WallsPlayer();
        }

        wallsPlayer.username = playerName;
        wallsPlayer.uid = tempUID;
        
        try (PreparedStatement statement = getWallsConnection().prepareStatement("SELECT * FROM `accounts` WHERE uuid = ?")) {

            statement.setString(1, tempUID);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.first()) {
                if (resultSet.getInt("mvplevel") == 1 || resultSet.getInt("mvplevel") == 3) {
                    wallsPlayer.nMVP = true;
                }
                if (resultSet.getInt("mvplevel") == 2 || resultSet.getInt("mvplevel") == 3) {
                    wallsPlayer.dMVP = true;
                }
                wallsPlayer.statsKills = resultSet.getInt("kills"); 
                wallsPlayer.statsDeaths = resultSet.getInt("deaths"); 
                wallsPlayer.statsWins = resultSet.getInt("wins"); 
//                wallsPlayer.clan = resultSet.getString("clan");
            }else{
                try (PreparedStatement statement2 = getWallsConnection().prepareStatement("INSERT INTO `accounts` (`username`, `uuid`) VALUES (?,?)")) {
                    statement2.setString(1, playerName);
                    statement2.setString(2, tempUID);
                    statement2.executeUpdate();
                }

            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to load stats: " + e.getMessage());
        }
        
        myWalls.getAllPlayers().put(aUID, wallsPlayer);

    }

    public void saveAllData() {
        // save all data in cache
        for (final WallsPlayer aWP : myWalls.getAllPlayers().values()) {
            this.saveWallsStats(aWP);
        }
    }

    
    private void saveWallsStats(final WallsPlayer wallsPlayer) {

        new BukkitRunnable() {
            @Override
            public void run() {

                
                try {
                    try (final PreparedStatement statement = getWallsConnection().prepareStatement("UPDATE `accounts` SET `kills` = `kills` + ?, `deaths` = `deaths` + ?, `playingtime` = `playingtime` + ?, `totalplays` = `totalplays` + ?, `wins` = wins + ? WHERE `uuid` = ?")) {
                        statement.setInt(1, wallsPlayer.kills);
                        statement.setInt(2, wallsPlayer.deaths);
                        statement.setInt(3, wallsPlayer.minutes);
                        statement.setInt(4, 1);
                        statement.setInt(5, wallsPlayer.wins);
                        statement.setString(6, wallsPlayer.uid);
                        statement.executeUpdate();

                    }

                    try (final PreparedStatement statement = getWallsConnection().prepareStatement("INSERT INTO `player_stats` (`kills`,`deaths`,`playingtime`, `win`, `uuid`, `username`) VALUES (?,?,?,?,?,?)")) {
                        statement.setInt(1, wallsPlayer.kills);
                        statement.setInt(2, wallsPlayer.deaths);
                        statement.setInt(3, wallsPlayer.minutes);
                        statement.setInt(4, wallsPlayer.wins);
                        statement.setString(5, wallsPlayer.uid);
                        statement.setString(6, wallsPlayer.username);
                        statement.executeUpdate();
                    }
                    
                    
                    try (final PreparedStatement statement = getMConnection().prepareStatement("UPDATE `accounts` SET `butter_coins` = ? WHERE `uuid` = ?")) {
                        statement.setInt(1, wallsPlayer.coins);
                        statement.setString(2, wallsPlayer.uid);
                        statement.executeUpdate();
                        if (TheWalls.debugMode){
                            System.out.println(TheWalls.chatPrefix+"Coins updated." + wallsPlayer.username);
                        }
                        
                    }

                } catch (final SQLException e) {
                    Database.this.myWalls.getLogger().warning("Failure to save stats: " + e.getMessage());
                }

//                addCoins(player, stat.coins);
            }
        }.runTaskAsynchronously(this.myWalls);
    }

    
    private Connection getWallsConnection() throws SQLException {
        if (this.wallsConnection != null) {
            try {
                if (this.wallsConnection.isValid(1)) {
                    return this.wallsConnection;
                }
            } catch (final SQLException e) {
                // This only throws an SQLException if the number input is less than 0
                myWalls.getLogger().log(Level.WARNING, String.format("Unexpected SQLException when testing connection: %s", e.getMessage()));
            }
        }

        try {
            this.wallsConnection = DriverManager.getConnection(this.url + this.wallDB, this.user, this.password);
            return this.wallsConnection;
        } catch (final SQLException e) {
            myWalls.getLogger().log(Level.SEVERE, String.format("Error while connecting to the database: %s", e.getMessage()));
            e.printStackTrace();
            throw e;
        }

    }

    private Connection getMConnection() {
        if (this.myConnection != null) {
            try {
                if (this.myConnection.isValid(1)) {
                    return this.myConnection;
                }
            } catch (final SQLException e) {
                // This only throws an SQLException if the number input is less than 0
                myWalls.getLogger().log(Level.WARNING, String.format("Unexpected SQLException when testing connection: %s", e.getMessage()));
            }
        }

        try {
            this.myConnection = DriverManager.getConnection(this.url + this.myDB, this.mUser, this.mPassword);
            return this.myConnection;
        } catch (final SQLException e) {
            myWalls.getLogger().log(Level.SEVERE, String.format("Error while connecting to the database: %s", e.getMessage()));
            e.printStackTrace();
            return null;
        }
    }
    

    private Connection getSCBConnection() throws SQLException {
        if (this.wallsConnection2 != null) {
            try {
                if (this.wallsConnection2.isValid(1)) {
                    return this.wallsConnection2;
                }
            } catch (final SQLException e) {
                // This only throws an SQLException if the number input is less than 0
                myWalls.getLogger().log(Level.WARNING, String.format("Unexpected SQLException when testing connection: %s", e.getMessage()));
            }
        }

        try {
            this.wallsConnection2 = DriverManager.getConnection(this.url2 + this.wallDB2, this.user2, this.password2);
            return this.wallsConnection2;
        } catch (final SQLException e) {
            myWalls.getLogger().log(Level.SEVERE, String.format("Error while connecting to the database: %s", e.getMessage()));
            e.printStackTrace();
            throw e;
        }

    }

    private Connection getVDEFConnection() throws SQLException {
        if (this.wallsConnection3 != null) {
            try {
                if (this.wallsConnection3.isValid(1)) {
                    return this.wallsConnection3;
                }
            } catch (final SQLException e) {
                // This only throws an SQLException if the number input is less than 0
                myWalls.getLogger().log(Level.WARNING, String.format("Unexpected SQLException when testing connection: %s", e.getMessage()));
            }
        }

        try {
            this.wallsConnection3 = DriverManager.getConnection(this.url3 + this.wallDB3, this.user3, this.password3);
            return this.wallsConnection3;
        } catch (final SQLException e) {
            myWalls.getLogger().log(Level.SEVERE, String.format("Error while connecting to the database: %s", e.getMessage()));
            e.printStackTrace();
            throw e;
        }

    }
    
    public boolean setUsersClan(final String playerUID, final String clan) {
        boolean result = false;
        PreparedStatement stmt = null;
        try {
            stmt = getMConnection().prepareStatement("SELECT * FROM `accounts` WHERE `uuid` = ?");
            stmt.setString(1, playerUID);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
                // in database
                PreparedStatement stmt2 = getMConnection().prepareStatement("UPDATE `accounts` SET `guild` = ? WHERE `uuid` = ?");
                stmt2.setString(1, clan);
                stmt2.setString(2, playerUID);
                stmt2.executeUpdate();
                stmt2.close();
                result = true;
                this.myWalls.getLogger().log(Level.INFO, "WALLS: user Clan set ! { " + playerUID + " }" + " to "+clan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    
    
    public void getVIPs(String vipsURL) {
        try {

            URL versionDoc = new URL(vipsURL);

            URLConnection myConnection = versionDoc.openConnection();

            myConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            myConnection.setConnectTimeout(1000);

            BufferedReader statsStreamIn = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));

            String throwAwayString = "";
            while ((throwAwayString = statsStreamIn.readLine()) != null) {
                VIPS.add(throwAwayString);
            }

            if (VIPS.size() > 0) {
                this.myWalls.getLogger().log(Level.INFO, "Walls: got Users from dropbox {" + VIPS.size() + "}");
                for (String s : VIPS){
                    this.myWalls.getLogger().log(Level.INFO, "Walls: got Users from dropbox { " + s + " }");
                }
            }
            // close the connection
            statsStreamIn.close();

        } catch (IOException ioe) {

            System.err.println("Caught IOException: " + ioe.getMessage());
        }

    }

    
    
    public void migrateUserToWalls(final String player) {
        PreparedStatement stmt = null;
        try {
            stmt = getWallsConnection().prepareStatement("SELECT * FROM `accounts` WHERE `username` = ?");
            stmt.setString(1, player);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
                // in database
                PreparedStatement stmt2 = getWallsConnection().prepareStatement("UPDATE `accounts` SET `pro` = 1 WHERE `username` = ?");
                stmt2.setString(1, player);
                stmt2.executeUpdate();
                stmt2.close();
                this.myWalls.getLogger().log(Level.INFO, "WALLS: user Migrated ! { " + player + " }");
            } else {
                PreparedStatement stmt2 = getWallsConnection().prepareStatement("INSERT INTO `accounts` (`username`, `pro`) VALUES (?,1)");
                stmt2.setString(1, player);
                stmt2.executeUpdate();
                this.myWalls.getLogger().log(Level.INFO, "WALLS: user ADDED ! { " + player + " }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    public void migrateUserToVD(final String player) {
        PreparedStatement stmt = null;
        try {
            stmt = getVDEFConnection().prepareStatement("SELECT * FROM `ranks` WHERE `username` = ?");
            stmt.setString(1, player);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
                // in database
                PreparedStatement stmt2 = getVDEFConnection().prepareStatement("UPDATE `ranks` SET `ranks` = 1 WHERE `username` = ?");
                stmt2.setString(1, player);
                stmt2.executeUpdate();
                stmt2.close();
                this.myWalls.getLogger().log(Level.INFO, "VDEF: user Migrated ! { " + player + " }");
            } else {
                PreparedStatement stmt2 = getVDEFConnection().prepareStatement("INSERT INTO `ranks` (`username`, `ranks`) VALUES (?,1)");
                stmt2.setString(1, player);
                stmt2.executeUpdate();
                this.myWalls.getLogger().log(Level.INFO, "VDEF: user ADDED ! { " + player + " }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void migrateUserToSCB(final String player) {
        PreparedStatement stmt = null;
        try {
            stmt = getSCBConnection().prepareStatement("SELECT * FROM `accounts` WHERE `username` = ?");
            stmt.setString(1, player);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
                // in database
                PreparedStatement stmt2 = getSCBConnection().prepareStatement("UPDATE `accounts` SET `pro` = 1 WHERE `username` = ?");
                stmt2.setString(1, player);
                stmt2.executeUpdate();
                stmt2.close();
                this.myWalls.getLogger().log(Level.INFO, "SCB: user Migrated ! { " + player + " }");
            } else {
                PreparedStatement stmt2 = getSCBConnection().prepareStatement("INSERT INTO `accounts` (`username`, `pro`) VALUES (?,1)");
                stmt2.setString(1, player);
                stmt2.executeUpdate();
                this.myWalls.getLogger().log(Level.INFO, "SCB: user ADDED ! { " + player + " }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

  
        
    
    public void setSCBPro(final String player, int onOrOff) {
        PreparedStatement stmt = null;
        try {
            stmt = getSCBConnection().prepareStatement("SELECT * FROM `accounts` WHERE `username` = ?");
            stmt.setString(1, player);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
                // in database
                PreparedStatement stmt2 = getSCBConnection().prepareStatement("UPDATE `accounts` SET `pro` = ? WHERE `username` = ?");
                stmt2.setInt(1, onOrOff);
                stmt2.setString(2, player);
                stmt2.executeUpdate();
                stmt2.close();
                this.myWalls.getLogger().log(Level.INFO, "SCB Pro set for user { " + player + " }");
            } else {
                PreparedStatement stmt2 = getSCBConnection().prepareStatement("INSERT INTO `accounts` (`username`, `pro`) VALUES (?,?)");
                stmt2.setString(1, player);
                stmt2.setInt(2, onOrOff);
                stmt2.executeUpdate();
                this.myWalls.getLogger().log(Level.INFO, "SCB Pro set for user" + player + " }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setPro(final String player, int onOrOff) {
        PreparedStatement stmt = null;
        try {
            stmt = getMConnection().prepareStatement("SELECT * FROM `accounts` WHERE `username` = ?");
            stmt.setString(1, player);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
                // in database
                PreparedStatement stmt2 = getMConnection().prepareStatement("UPDATE `accounts` SET `pro` = ? WHERE `username` = ?");
                stmt2.setInt(1, onOrOff);
                stmt2.setString(2, player);
                stmt2.executeUpdate();
                stmt2.close();
                this.myWalls.getLogger().log(Level.INFO, "Pro set for user { " + player + " }");
            } else {
                PreparedStatement stmt2 = getMConnection().prepareStatement("INSERT INTO `accounts` (`username`, `pro`) VALUES (?,?)");
                stmt2.setString(1, player);
                stmt2.setInt(2, onOrOff);
                stmt2.executeUpdate();
                this.myWalls.getLogger().log(Level.INFO, "Pro set for user { " + player + " }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

    public void setUIDPro(final String uuid) {
        PreparedStatement stmt = null;
        try {
            stmt = getMConnection().prepareStatement("SELECT * FROM `accounts` WHERE `uuid` = ?");
            stmt.setString(1, uuid);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
                // in database
                PreparedStatement stmt2 = getMConnection().prepareStatement("UPDATE `accounts` SET `pro` = ? WHERE `uuid` = ?");
                stmt2.setInt(1, 1);
                stmt2.setString(2, uuid);
                stmt2.executeUpdate();
                stmt2.close();
                this.myWalls.getLogger().log(Level.INFO, "Pro set for user { " + uuid + " }");
            } else {
                PreparedStatement stmt2 = getMConnection().prepareStatement("INSERT INTO `accounts` (`uuid`, `pro`) VALUES (?,?)");
                stmt2.setString(1, uuid);
                stmt2.setInt(2, 1);
                stmt2.executeUpdate();
                this.myWalls.getLogger().log(Level.INFO, "Pro set for user { " + uuid + " }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    public void updateServerState() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (final PreparedStatement statement = getWallsConnection().prepareStatement("UPDATE `servers` SET `state` = ?, `players` = ?, `motd` = ?, `max_players` = ?  WHERE `server` = ?")) {
                    switch (myWalls.getGameState()){
                    case PREGAME:
                        statement.setInt(1, 1);
                        break;
                    case PEACETIME:
                    case FIGHTING:
                        statement.setInt(1, 2);
                        break;
                    case FINISHED:
                        statement.setInt(1, 3);
                        break;
                    default:
                        break;
                    }
                    statement.setInt(2, Database.this.myWalls.getNumberOfPlayers());
                    statement.setString(3, "");
                    statement.setInt(4, TheWalls.preGameAutoStartPlayers+1);
                    statement.setInt(5, TheWalls.serverNumber);
                    statement.executeUpdate();
                } catch (final SQLException e) {
                    myWalls.getLogger().warning("Failed to update server state: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(myWalls);
    }

    public void banPlayer(String username, String reason, String bannedBy) throws SQLException {
        try (PreparedStatement stmt = getMConnection().prepareStatement("SELECT * FROM bans WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet set = stmt.executeQuery();
            if (set.first()) {
                try (PreparedStatement stmt2 = getMConnection().prepareStatement(
                        "UPDATE bans SET is_banned = ?, reason = ?, banned_date = ?, banned_by = ? WHERE username = ?")) {
                    stmt2.setBoolean(1, true);
                    stmt2.setString(2, reason);
                    stmt2.setDate(3, new Date(System.currentTimeMillis()));
                    stmt2.setString(4, bannedBy);
                    stmt2.setString(5, username);
                    stmt2.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt2 = getMConnection().prepareStatement(
                        "INSERT INTO bans (username, is_banned, reason, banned_date, banned_by) VALUES (?,?,?,?,?)")) {
                    stmt2.setString(1, username);
                    stmt2.setBoolean(2, true);
                    stmt2.setString(3, reason);
                    stmt2.setDate(4, new Date(System.currentTimeMillis()));
                    stmt2.setString(5, bannedBy);
                    stmt2.executeUpdate();
                }
            }
        }
    }
    
    
    
    public boolean disbandClanByName(String clan){
        
        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?")) {

            statement.setString(1, clan);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.first()) {
                
                String actualClanName = resultSet.getString("name");


                try (PreparedStatement statement2 = getMConnection().prepareStatement("UPDATE `accounts` SET `guild` = ? WHERE `guild` = ?")) {
                    
                    statement2.setString(1, null);
                    statement2.setString(2, actualClanName);
                    statement2.executeUpdate();
                }

                try (PreparedStatement statement3 = getMConnection().prepareStatement("DELETE FROM `guilds` WHERE name = ?")) {

                    statement3.setString(1, actualClanName);

                    statement3.executeUpdate();

                }

                return true;
                
            }else{
                return false;
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to disband clan by name : " + e.getMessage());
            return false;
        }
        
    }

    
    public boolean disbandClan(String clan){
        
        try (PreparedStatement statement = getMConnection().prepareStatement("UPDATE `accounts` SET `guild` = ? WHERE `guild` = ?")) {
            statement.setString(1, "");
            statement.setString(2, clan);
            
            Bukkit.getLogger().info(statement.toString());
            statement.executeUpdate();


            try (PreparedStatement statement2 = getMConnection().prepareStatement("DELETE FROM `guilds` WHERE name = ?")) {

                statement2.setString(1, clan);

                statement2.executeUpdate();

            }
            
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to disband clan: " + e.getMessage());
            return false;
        }
        
        return true;
    }

    
    public boolean kickClanMember(String personToKick, String clan){
        
        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `accounts` WHERE `username` = ? AND `guild` = ?")) {

            statement.setString(1, personToKick);
            statement.setString(2, clan);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.first()) {

                try (PreparedStatement statement2 = getMConnection().prepareStatement("UPDATE `accounts` SET `guild` = ? WHERE `username` = ?")) {
                    
                    statement2.setString(1, null);
                    statement2.setString(2, personToKick);
                    statement2.executeUpdate();
                }
                return true;
                
            }else{
                return false;
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to kick clan member: " + e.getMessage());
        }
        
        return true;
    }
 

    public boolean staffRenameClan(String oldClanName, String newClanName){

        
        try{

            String oldFancyName = null;

            // makes sure the clan name is not already taken.
            PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?");
            statement.setString(1, ClanCmd.stripAllClanCharacters(newClanName));
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.first() && !ClanCmd.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newClanName)) {
                Bukkit.getLogger().info("StaffRenameClan: found same name clan - Cannot override.");
                return false;
            }


            // gets the fancy old name (in case its color enabled) so can change later in accounts table.
            PreparedStatement statementOld = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?");
            statementOld.setString(1, ClanCmd.stripAllClanCharacters(oldClanName));
            final ResultSet resultSet2 = statementOld.executeQuery();

            if (!resultSet2.first()) {                    
                Bukkit.getLogger().info("StaffRenameClan: could not find this clan in the guilds. ");
                return false;
            }
            oldFancyName = resultSet2.getString("name");
                
            PreparedStatement statement2 = getMConnection().prepareStatement("UPDATE `guilds` SET `name` = ?, `plain` = ? where `plain` = ?");
    
            statement2.setString(1, newClanName);
            statement2.setString(2, ClanCmd.stripAllClanCharacters(newClanName));
            statement2.setString(3, ClanCmd.stripAllClanCharacters(oldClanName));
            
            statement2.executeUpdate();
                
            
            
    
            // change the name for all other players in the accounts table.
            
            if (TheWalls.debugMode){
                Bukkit.getLogger().info("Staff Rename Clan - old Name - "+oldFancyName);
            }
            if (oldFancyName!=null){                    
                try (PreparedStatement statement3 = getMConnection().prepareStatement("UPDATE `accounts` SET  `guild` = ? where `guild` = ?")) {
                    
                    statement3.setString(1, newClanName);
                    statement3.setString(2, oldFancyName);
                    
                    statement3.executeUpdate();
                }            
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to change clan name by staff: " + e.getMessage());
            return false;
        }
        return true;
    }

    
    public boolean renameClan(String oldClanName, String newClanName, String newPlainClanName){

        
        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?")) {

            statement.setString(1, newPlainClanName);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.first() && !ClanCmd.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newPlainClanName)) {
                return false;
            }else{

                try (PreparedStatement statement2 = getMConnection().prepareStatement("UPDATE `guilds` SET  `name` = ?, `plain` = ? where `name` = ?")) {
        
                    statement2.setString(1, newClanName);
                    statement2.setString(2, newPlainClanName);
                    statement2.setString(3, oldClanName);
                    
                    statement2.executeUpdate();
                    
        
                } catch (final SQLException e) {
                    this.myWalls.getLogger().warning("Failure to change clan name: " + e.getMessage());
                    return false;
                }
                
                try (PreparedStatement statement3 = getMConnection().prepareStatement("UPDATE `accounts` SET  `guild` = ? where `guild` = ?")) {
        
                    statement3.setString(1, newClanName);
                    statement3.setString(2, oldClanName);
                    
                    statement3.executeUpdate();
                }            
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to change clan name: " + e.getMessage());
            return false;
        }
        return true;
    }

    
    public boolean setNewClanLeader(UUID clanOwner, String newClanOwnerName, UUID newClanOwner){
        
        String oldOwnerUID = clanOwner.toString().replace("-", "");
        String newOwnerUID = newClanOwner.toString().replace("-", "");

        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `uuid` = ?")) {

            statement.setString(1, oldOwnerUID);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.first()) {
                
                try (PreparedStatement statement3 = getMConnection().prepareStatement("UPDATE `guilds` SET  `leader` = ?, `uuid` = ? where `uuid` = ?")) {
                    
                    statement3.setString(1, newClanOwnerName);
                    statement3.setString(2, newOwnerUID);
                    statement3.setString(3, oldOwnerUID);
                    statement3.executeUpdate();
                }
                
            }else{
                return false;
            }
            return true;
        } catch (final SQLException e) {
            this.myWalls.getLogger().info("Failure to set new Clan leader: " + e.getMessage());
            return false;
        }
    }

    
    public boolean createClan(String clanName, String clanOwner, String clanOwnerUID, String plainClanName){
        
        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?")) {

            statement.setString(1, plainClanName);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.first()) {
                return false;
            }else{

                try (PreparedStatement statement2 = getMConnection().prepareStatement("INSERT INTO `guilds` (`name`, `leader`, `uuid`, `plain`) VALUES (?,?,?,?)")) {
                    
                    statement2.setString(1, clanName);
                    statement2.setString(2, clanOwner);
                    statement2.setString(3, clanOwnerUID);
                    statement2.setString(4, plainClanName);
                    statement2.executeUpdate();
                }
                
                try (PreparedStatement statement3 = getMConnection().prepareStatement("UPDATE `accounts` SET  `guild` = ? where `uuid` = ?")) {
                    
                    statement3.setString(1, clanName);
                    statement3.setString(2, clanOwnerUID);
                    statement3.executeUpdate();
                }
                
            }
            return this.setUsersClan(clanOwnerUID, clanName);
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to create new clan: " + e.getMessage());
            return false;
        }
    }

    public List<String>  listClanMembers(String clanName){
        List<String> clanMembers = new ArrayList<String>();

        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `accounts` WHERE `guild` = ?")) {

            statement.setString(1, clanName);
            final ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                clanMembers.add(resultSet.getString("username"));
                
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to load clan memebers: " + e.getMessage());
        }
        
        return clanMembers;
    }

    
    public boolean isClanOwner(String aUID) {

        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `uuid` = ?")) {
            statement.setString(1, aUID);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (final SQLException e) {
            myWalls.getLogger().warning("Failure to find player in guilds table: " + e.getMessage());
        }
        return false;
    }


    
    

}