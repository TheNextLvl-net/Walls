package me.glennEboy.Walls.utils;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.commands.ClanCmd;
import net.nonswag.core.api.sql.Database;
import net.nonswag.core.api.sql.SQLConnection;
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

public class DatabaseUtil extends Thread {

    private final TheWalls myWalls;

    private final Map<UUID, String> playersToLogin = new HashMap<>();

    public DatabaseUtil(TheWalls plugin) {
        this.myWalls = plugin;
        this.start();
    }

    @Override
    public void run() {
        int timer = 100;
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(timer);
            } catch (final InterruptedException e) {
                break;
            }

            switch (myWalls.getGameState()) {
                case PREGAME:
                    timer = 100;
                case PEACETIME:
                case FIGHTING:
                    timer = 600;
                    if (this.playersToLogin.size() > 0) {
                        List<UUID> doneList = new ArrayList<>();
                        synchronized (playersToLogin) {
                            for (UUID playerUID : this.playersToLogin.keySet()) {
                                if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(playerUID))) {
                                    loadStatsPlayer(this.playersToLogin.get(playerUID), playerUID);
                                    loadStatsWalls(this.playersToLogin.get(playerUID), playerUID);
                                    loadPaidKitsForUser(this.playersToLogin.get(playerUID), playerUID);
                                    doneList.add(playerUID);
                                }
                            }
                        }
                        for (UUID a : doneList) {
                            this.playersToLogin.remove(a);
                        }
                    }
                    break;
                default:
                    break;
            }

        }
    }

    public void forceLoadPlayer(String playerName, UUID aUID) {
        if (!myWalls.getAllPlayers().containsKey(aUID)) {
            this.playersToLogin.put(aUID, playerName);
        }
    }

    public void loadPlayer(String playerName, UUID aUID) {
        if (!myWalls.getAllPlayers().containsKey(aUID)) {
            synchronized (playersToLogin) {
                this.playersToLogin.put(aUID, playerName);
            }
        }
    }

    public void loadPaidKitsForUser(String username, UUID playerUID) {
        WallsPlayer twp = myWalls.getWallsPlayer(playerUID);
        try (ResultSet result = Database.getConnection().executeQuery("SELECT `kitname` FROM `paidkits` WHERE `uuid` = ? AND `kitname`<> ?", playerUID.toString().replace("-", ""), "pro")) {
            if (result == null) return;
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
        WallsPlayer wallsPlayer = this.myWalls.getAllPlayers().getOrDefault(aUID, new WallsPlayer());
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE unique_id = UNHEX(?)", uuid.toString())) {
            if (resultSet == null) return;
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
                Database.getConnection().executeUpdate("INSERT INTO `accounts` (`username`) VALUES (?)", playerName);
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to load stats: " + e.getMessage());
        }
        wallsPlayer.clanLeader = isClanOwner(uuid);
        myWalls.getAllPlayers().put(aUID, wallsPlayer);
    }

    private void loadStatsWalls(String playerName, UUID aUID) {
        String tempUID = aUID.toString().replace("-", "");
        WallsPlayer wallsPlayer = this.myWalls.getAllPlayers().getOrDefault(aUID, new WallsPlayer());
        wallsPlayer.username = playerName;
        wallsPlayer.uid = tempUID;
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE uuid = ?", tempUID)) {
            if (resultSet == null) return;
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
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `accounts` (`username`, `uuid`) VALUES (?,?)", playerName, tempUID);
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to load stats: " + e.getMessage());
        }
        myWalls.getAllPlayers().put(aUID, wallsPlayer);
    }

    public void saveAllData() {
        for (final WallsPlayer aWP : myWalls.getAllPlayers().values()) {
            this.saveWallsStats(aWP);
        }
    }

    private void saveWallsStats(final WallsPlayer wallsPlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Database.getConnection().executeUpdate("UPDATE `accounts` SET `kills` = `kills` + ?, `deaths` = `deaths` + ?, `playingtime` = `playingtime` + ?, `totalplays` = `totalplays` + ?, `wins` = wins + ? WHERE `uuid` = ?",
                            wallsPlayer.kills, wallsPlayer.deaths, wallsPlayer.minutes, 1, wallsPlayer.wins, wallsPlayer.uid);
                    Database.getConnection().executeUpdate("INSERT INTO `player_stats` (`kills`,`deaths`,`playingtime`, `win`, `uuid`, `username`) VALUES (?,?,?,?,?,?)",
                            wallsPlayer.kills, wallsPlayer.deaths, wallsPlayer.minutes, wallsPlayer.wins, wallsPlayer.uid, wallsPlayer.username);
                    Database.getConnection().executeUpdate("UPDATE `accounts` SET `butter_coins` = ? WHERE `uuid` = ?", wallsPlayer.coins, wallsPlayer.uid);
                    if (TheWalls.debugMode) {
                        System.out.println(TheWalls.chatPrefix + "Coins updated." + wallsPlayer.username);
                    }
                } catch (final SQLException e) {
                    DatabaseUtil.this.myWalls.getLogger().warning("Failure to save stats: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this.myWalls);
    }

    public boolean setUsersClan(final String playerUID, final String clan) {
        boolean result = false;
        PreparedStatement stmt = null;
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `uuid` = ?", playerUID)) {
            if (set == null) return false;
            if (set.next()) {
                Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `uuid` = ?", clan, playerUID);
                result = true;
                this.myWalls.getLogger().log(Level.INFO, "WALLS: user Clan set ! { " + playerUID + " }" + " to " + clan);
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

    public void setPro(final String player, int onOrOff) {
        PreparedStatement stmt = null;
        try {
            stmt = getMConnection().prepareStatement("SELECT * FROM `accounts` WHERE `username` = ?");
            stmt.setString(1, player);
            ResultSet set = stmt.executeQuery();
            if (set.next()) {
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

    public boolean disbandClanByName(String clan) {
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
            } else {
                return false;
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to disband clan by name : " + e.getMessage());
            return false;
        }
    }

    public boolean disbandClan(String clan) {
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

    public boolean kickClanMember(String personToKick, String clan) {
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
            } else {
                return false;
            }
        } catch (final SQLException e) {
            this.myWalls.getLogger().warning("Failure to kick clan member: " + e.getMessage());
        }
        return true;
    }

    public boolean staffRenameClan(String oldClanName, String newClanName) {
        try {
            PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?");
            statement.setString(1, ClanCmd.stripAllClanCharacters(newClanName));
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.first() && !ClanCmd.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newClanName)) {
                Bukkit.getLogger().info("StaffRenameClan: found same name clan - Cannot override.");
                return false;
            }
            PreparedStatement statementOld = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?");
            statementOld.setString(1, ClanCmd.stripAllClanCharacters(oldClanName));
            final ResultSet resultSet2 = statementOld.executeQuery();
            if (!resultSet2.first()) {
                Bukkit.getLogger().info("StaffRenameClan: could not find this clan in the guilds. ");
                return false;
            }
            String oldFancyName = resultSet2.getString("name");
            PreparedStatement statement2 = getMConnection().prepareStatement("UPDATE `guilds` SET `name` = ?, `plain` = ? where `plain` = ?");
            statement2.setString(1, newClanName);
            statement2.setString(2, ClanCmd.stripAllClanCharacters(newClanName));
            statement2.setString(3, ClanCmd.stripAllClanCharacters(oldClanName));
            statement2.executeUpdate();
            if (TheWalls.debugMode) {
                Bukkit.getLogger().info("Staff Rename Clan - old Name - " + oldFancyName);
            }
            if (oldFancyName != null) {
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

    public boolean renameClan(String oldClanName, String newClanName, String newPlainClanName) {
        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?")) {
            statement.setString(1, newPlainClanName);
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.first() && !ClanCmd.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newPlainClanName)) {
                return false;
            } else {
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

    public boolean setNewClanLeader(UUID clanOwner, String newClanOwnerName, UUID newClanOwner) {
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
            } else {
                return false;
            }
            return true;
        } catch (final SQLException e) {
            this.myWalls.getLogger().info("Failure to set new Clan leader: " + e.getMessage());
            return false;
        }
    }

    public boolean createClan(String clanName, String clanOwner, String clanOwnerUID, String plainClanName) {
        try (PreparedStatement statement = getMConnection().prepareStatement("SELECT * FROM `guilds` WHERE `plain` = ?")) {
            statement.setString(1, plainClanName);
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.first()) {
                return false;
            } else {
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

    public List<String> listClanMembers(String clanName) {
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
