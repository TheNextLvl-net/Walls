package net.nonswag.fvr.walls.utils;

import net.nonswag.core.api.sql.Database;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.commands.ClanCmd;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class DatabaseUtil {
    private final Map<UUID, String> queue = new HashMap<>();
    private final Walls walls;

    public DatabaseUtil(Walls plugin) {
        this.walls = plugin;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {

            try {
                Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `paidkits` (uuid varchar(255), kitname varchar(255))");
                Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `accounts` (username varchar(255), uuid varchar(255), rank int, kills int, deaths int, wins int, guild varchar(255))");
                Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `guilds` (plain varchar(255), name varchar(255), uuid varchar(255))");
                Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `player_stats` (kills int, deaths int, win varchar(255), uuid varchar(255), username varchar(255))");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (walls.getGameState().equals(Walls.GameState.FINISHED)) return;
            synchronized (queue) {
                for (UUID playerUID : this.queue.keySet()) {
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(playerUID))) {
                        loadStatsPlayer(this.queue.get(playerUID), playerUID);
                        loadStatsWalls(this.queue.get(playerUID), playerUID);
                        loadPaidKitsForUser(playerUID);
                        queue.remove(playerUID);
                    }
                }
            }
        }, 0, 12);
    }

    public void loadPlayer(String playerName, UUID aUID) {
        if (!walls.getAllPlayers().containsKey(aUID)) {
            synchronized (queue) {
                this.queue.put(aUID, playerName);
            }
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    public void loadPaidKitsForUser(UUID playerUID) {
        Walls.WallsPlayer twp = walls.getWallsPlayer(playerUID);
        try (ResultSet result = Database.getConnection().executeQuery("SELECT `kitname` FROM `paidkits` WHERE `uuid` = ? AND `kitname`<> ?", playerUID.toString(), "pro")) {
            if (result == null) return;
            while (result.next()) {
                twp.paidKits += "," + result.getString("kitname");
            }
        } catch (final SQLException e) {
            walls.getLogger().warning("Failure to load paid kits: " + e.getMessage());
            e.printStackTrace();
        }
        walls.getAllPlayers().put(playerUID, twp);
    }

    private void loadStatsPlayer(String playerName, UUID aUID) {
        String uuid = aUID.toString();
        Walls.WallsPlayer wallsPlayer = this.walls.getAllPlayers().getOrDefault(aUID, new Walls.WallsPlayer());
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE uuid = ?", uuid)) {
            if (resultSet == null) return;
            if (resultSet.first()) {
                wallsPlayer.rank = Walls.Rank.values()[resultSet.getInt("rank")];
                wallsPlayer.clan = resultSet.getString("guild");
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `accounts` (`username`) VALUES (?)", playerName);
            }
        } catch (final SQLException e) {
            this.walls.getLogger().warning("Failure to load stats: " + e.getMessage());
            e.printStackTrace();
        }
        wallsPlayer.clanLeader = isClanOwner(uuid);
        walls.getAllPlayers().put(aUID, wallsPlayer);
    }

    private void loadStatsWalls(String playerName, UUID aUID) {
        String tempUID = aUID.toString();
        Walls.WallsPlayer wallsPlayer = this.walls.getAllPlayers().getOrDefault(aUID, new Walls.WallsPlayer());
        wallsPlayer.username = playerName;
        wallsPlayer.uid = tempUID;
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE uuid = ?", tempUID)) {
            if (resultSet == null) return;
            if (resultSet.first()) {
                wallsPlayer.statsKills = resultSet.getInt("kills");
                wallsPlayer.statsDeaths = resultSet.getInt("deaths");
                wallsPlayer.statsWins = resultSet.getInt("wins");
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `accounts` (`username`, `uuid`) VALUES (?,?)", playerName, tempUID);
            }
        } catch (final SQLException e) {
            this.walls.getLogger().warning("Failure to load stats: " + e.getMessage());
            e.printStackTrace();
        }
        walls.getAllPlayers().put(aUID, wallsPlayer);
    }

    public void saveAllData() {
        for (final Walls.WallsPlayer aWP : walls.getAllPlayers().values()) {
            this.saveWallsStats(aWP);
        }
    }

    private void saveWallsStats(final Walls.WallsPlayer wallsPlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Database.getConnection().executeUpdate("UPDATE `accounts` SET `kills` = `kills` + ?, `deaths` = `deaths` + ?, `wins` = wins + ? WHERE `uuid` = ?",
                            wallsPlayer.kills, wallsPlayer.deaths, wallsPlayer.wins, wallsPlayer.uid);
                    Database.getConnection().executeUpdate("INSERT INTO `player_stats` (`kills`,`deaths`, `win`, `uuid`, `username`) VALUES (?,?,?,?,?)",
                            wallsPlayer.kills, wallsPlayer.deaths, wallsPlayer.wins, wallsPlayer.uid, wallsPlayer.username);
                } catch (final SQLException e) {
                    DatabaseUtil.this.walls.getLogger().warning("Failure to save stats: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this.walls);
    }

    public boolean setUsersClan(final String playerUID, final String clan) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `uuid` = ?", playerUID)) {
            if (set == null) return false;
            if (!set.next()) return false;
            Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `uuid` = ?", clan, playerUID);
            this.walls.getLogger().log(Level.INFO, "WALLS: user Clan set ! { " + playerUID + " }" + " to " + clan);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setRank(final String player, int rank) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `username` = ?", player)) {
            if (set == null) return;
            if (set.next()) {
                Database.getConnection().executeUpdate("UPDATE `accounts` SET `rank` = ? WHERE `username` = ?", rank, player);
                this.walls.getLogger().log(Level.INFO, "Pro set for user { " + player + " }");
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `accounts` (`username`, `rank`) VALUES (?,?)", player, rank);
                this.walls.getLogger().log(Level.INFO, "Pro set for user { " + player + " }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean disbandClanByName(String clan) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", clan)) {
            if (set == null || !set.first()) return false;
            String actualClanName = set.getString("name");
            Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `guild` = ?", null, actualClanName);
            Database.getConnection().executeUpdate("DELETE FROM `guilds` WHERE name = ?", actualClanName);
            return true;
        } catch (final SQLException e) {
            this.walls.getLogger().warning("Failure to disband clan by name : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean disbandClan(String clan) {
        try {
            Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `guild` = ?", "", clan);
            Database.getConnection().executeUpdate("DELETE FROM `guilds` WHERE name = ?", clan);
            return true;
        } catch (final SQLException e) {
            this.walls.getLogger().warning("Failure to disband clan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean kickClanMember(String personToKick, String clan) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `username` = ? AND `guild` = ?", personToKick, clan)) {
            if (set == null || !set.first()) return false;
            Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `username` = ?", null, personToKick);
            return true;
        } catch (final SQLException e) {
            this.walls.getLogger().warning("Failure to kick clan member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean staffRenameClan(String oldClanName, String newClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", ClanCmd.stripAllClanCharacters(newClanName))) {
            if (resultSet == null) return false;
            if (resultSet.first() && !ClanCmd.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newClanName)) {
                Bukkit.getLogger().info("StaffRenameClan: found same name clan - Cannot override.");
                return false;
            }
            try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", ClanCmd.stripAllClanCharacters(oldClanName))) {
                if (set == null) return false;
                if (!set.first()) {
                    Bukkit.getLogger().info("StaffRenameClan: could not find this clan in the guilds. ");
                    return false;
                }
                String oldFancyName = set.getString("name");
                Database.getConnection().executeUpdate("UPDATE `guilds` SET `name` = ?, `plain` = ? where `plain` = ?", newClanName, ClanCmd.stripAllClanCharacters(newClanName), ClanCmd.stripAllClanCharacters(oldClanName));
                if (Walls.debugMode) Bukkit.getLogger().info("Staff Rename Clan - old Name - " + oldFancyName);
                if (oldFancyName != null) {
                    Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `guild` = ?", newClanName, oldFancyName);
                }
                return true;
            }
        } catch (final SQLException e) {
            this.walls.getLogger().warning("Failure to change clan name by staff: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean renameClan(String oldClanName, String newClanName, String newPlainClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", newPlainClanName)) {
            if (resultSet == null || (resultSet.first() && !ClanCmd.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newPlainClanName))) {
                return false;
            } else {
                Database.getConnection().executeUpdate("UPDATE `guilds` SET  `name` = ?, `plain` = ? where `name` = ?", newClanName, newPlainClanName, oldClanName);
                Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `guild` = ?", newClanName, oldClanName);
                return true;
            }
        } catch (SQLException e) {
            this.walls.getLogger().warning("Failure to change clan name: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean setNewClanLeader(UUID clanOwner, String newClanOwnerName, UUID newClanOwner) {
        String oldOwnerUID = clanOwner.toString();
        String newOwnerUID = newClanOwner.toString();
        try {
            Database.getConnection().executeUpdate("UPDATE `guilds` SET  `leader` = ?, `uuid` = ? where `uuid` = ?", newClanOwnerName, newOwnerUID, oldOwnerUID);
            return true;
        } catch (SQLException e) {
            this.walls.getLogger().info("Failure to set new Clan leader: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean createClan(String clanName, String clanOwner, String clanOwnerUID, String plainClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", plainClanName)) {
            if (resultSet == null || !resultSet.first()) return true;
            Database.getConnection().executeUpdate("INSERT INTO `guilds` (`name`, `leader`, `uuid`, `plain`) VALUES (?,?,?,?)", clanName, clanOwner, clanOwnerUID, plainClanName);
            Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `uuid` = ?", clanName, clanOwnerUID);
            return this.setUsersClan(clanOwnerUID, clanName);
        } catch (SQLException e) {
            this.walls.getLogger().warning("Failure to create new clan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<String> listClanMembers(String clanName) {
        List<String> clanMembers = new ArrayList<>();
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `guild` = ?", clanName)) {
            while (resultSet != null && resultSet.next()) {
                clanMembers.add(resultSet.getString("username"));
            }
        } catch (SQLException e) {
            this.walls.getLogger().warning("Failure to load clan members: " + e.getMessage());
            e.printStackTrace();
        }
        return clanMembers;
    }

    public boolean isClanOwner(String aUID) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `uuid` = ?", aUID)) {
            return resultSet != null && resultSet.next();
        } catch (final SQLException e) {
            walls.getLogger().warning("Failure to find player in guilds table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
