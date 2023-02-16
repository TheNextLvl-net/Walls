package net.nonswag.fvr.walls.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nonswag.core.api.sql.Database;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.commands.ClanCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseUtil {
    private final Walls walls;

    public DatabaseUtil(Walls walls) {
        this.walls = walls;
        try {
            Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `profiles` (uuid varchar(255), name varchar(255), lastSeen long)");
            Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `accounts` (uuid varchar(255), rank int, guild varchar(255))");
            Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `guilds` (plain varchar(255), name varchar(255), uuid varchar(255))");
            Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `stats` (uuid varchar(255), kills int, deaths int, wins int, kd double)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadPlayer(OfflinePlayer player) {
        Walls.WallsPlayer wallsPlayer = walls.getPlayers().getOrDefault(player.getUniqueId(), new Walls.WallsPlayer(player));
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE uuid = ?", wallsPlayer.toString())) {
            if (resultSet == null || !resultSet.first()) return;
            wallsPlayer.setRank(Walls.Rank.values()[resultSet.getInt("rank")]);
            wallsPlayer.setClan(resultSet.getString("guild"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            wallsPlayer.setClanLeader(isClanOwner(wallsPlayer.getUuid()));
            walls.getPlayers().put(wallsPlayer.getUuid(), wallsPlayer);
        }
    }

    public void loadStats(OfflinePlayer player) {
        Walls.WallsPlayer wallsPlayer = walls.getPlayers().getOrDefault(player.getUniqueId(), new Walls.WallsPlayer(player));
        try (ResultSet result = Database.getConnection().executeQuery("SELECT * FROM `stats` WHERE uuid = ?", wallsPlayer.getUuid().toString())) {
            if (result == null || !result.first()) return;
            wallsPlayer.setStatsKills(result.getInt("kills"));
            wallsPlayer.setStatsDeaths(result.getInt("deaths"));
            wallsPlayer.setStatsWins(result.getInt("wins"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            walls.getPlayers().put(player.getUniqueId(), wallsPlayer);
        }
    }

    public Walls.WallsPlayer bestKiller() {
        try (ResultSet result = Database.getConnection().executeQuery("SELECT `uuid`, `kills` FROM `stats` WHERE kills = (SELECT MAX(`kills`) FROM `stats`)")) {
            if (result == null || !result.first()) return null;
            UUID uuid = UUID.fromString(result.getString("uuid"));
            Walls.WallsPlayer player = new Walls.WallsPlayer(Bukkit.getOfflinePlayer(uuid));
            player.setStatsKills(result.getInt("kills"));
            return player;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Walls.WallsPlayer mostWins() {
        try (ResultSet result = Database.getConnection().executeQuery("SELECT `uuid`, `wins` FROM `stats` WHERE wins = (SELECT MAX(`wins`) FROM `stats`)")) {
            if (result == null || !result.first()) return null;
            UUID uuid = UUID.fromString(result.getString("uuid"));
            Walls.WallsPlayer player = new Walls.WallsPlayer(Bukkit.getOfflinePlayer(uuid));
            player.setStatsWins(result.getInt("wins"));
            return player;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Walls.WallsPlayer kdRatio() {
        try (ResultSet result = Database.getConnection().executeQuery("SELECT `uuid`, `kd` FROM `stats` WHERE kd = (SELECT MAX(`kd`) FROM `stats`)")) {
            if (result == null || !result.first()) return null;
            UUID uuid = UUID.fromString(result.getString("uuid"));
            Walls.WallsPlayer player = new Walls.WallsPlayer(Bukkit.getOfflinePlayer(uuid));
            player.setStatsKD(result.getDouble("kd"));
            return player;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveAllData() {
        walls.getPlayers().values().forEach(this::saveWallsStats);
    }

    private void saveWallsStats(Walls.WallsPlayer player) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `uuid` = ?", player.getUuid().toString())) {
            if (set != null && set.next()) {
                Database.getConnection().executeUpdate("UPDATE `stats` SET `kills` = ?, `deaths` = ?, `wins` = ?, `kd` = ? WHERE `uuid` = ?",
                        player.getKills(), player.getDeaths(), player.getWins(), player.getKD(), player.getUuid().toString());
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `stats` (`kills`,`deaths`, `wins`, `kd`, `uuid`) VALUES (?,?,?,?,?)",
                        player.getKills(), player.getDeaths(), player.getWins(), player.getKD(), player.getUuid().toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean setClanName(UUID uuid, String clan) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `uuid` = ?", uuid.toString())) {
            if (set == null || !set.next()) return false;
            Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `uuid` = ?", clan, uuid.toString());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setRank(UUID uuid, int rank) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `uuid` = ?", uuid.toString())) {
            if (set != null && set.next()) {
                Database.getConnection().executeUpdate("UPDATE `accounts` SET `rank` = ? WHERE `uuid` = ?", rank, uuid.toString());
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `accounts` (`uuid`, `rank`) VALUES (?,?)", uuid.toString(), rank);
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean disbandClan(String clan) {
        try {
            Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `guild` = ?", "", clan);
            Database.getConnection().executeUpdate("DELETE FROM `guilds` WHERE name = ?", clan);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean kickClanMember(String personToKick, String clan) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `username` = ? AND `guild` = ?", personToKick, clan)) {
            if (set == null || !set.first()) return false;
            Database.getConnection().executeUpdate("UPDATE `accounts` SET `guild` = ? WHERE `username` = ?", null, personToKick);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean staffRenameClan(String oldClanName, String newClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", ClanCommand.stripAllClanCharacters(newClanName))) {
            if (resultSet == null) return false;
            if (resultSet.first() && !ClanCommand.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newClanName)) {
                return false;
            }
            try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", ClanCommand.stripAllClanCharacters(oldClanName))) {
                if (set == null) return false;
                if (!set.first()) return false;
                String oldFancyName = set.getString("name");
                Database.getConnection().executeUpdate("UPDATE `guilds` SET `name` = ?, `plain` = ? where `plain` = ?", newClanName, ClanCommand.stripAllClanCharacters(newClanName), ClanCommand.stripAllClanCharacters(oldClanName));
                if (oldFancyName != null) {
                    Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `guild` = ?", newClanName, oldFancyName);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setClanName(String oldClanName, String newClanName, String newPlainClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", newPlainClanName)) {
            if (resultSet == null || (resultSet.first() && !ClanCommand.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newPlainClanName))) {
                return false;
            } else {
                Database.getConnection().executeUpdate("UPDATE `guilds` SET  `name` = ?, `plain` = ? where `name` = ?", newClanName, newPlainClanName, oldClanName);
                Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `guild` = ?", newClanName, oldClanName);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setNewClanLeader(UUID oldOwner, UUID newOwner) {
        try {
            Database.getConnection().executeUpdate("UPDATE `guilds` SET `owner` = ? where `owner` = ?", newOwner.toString(), oldOwner.toString());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createClan(String clanName, UUID owner, String plainClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", plainClanName)) {
            if (resultSet == null || !resultSet.first()) return true;
            Database.getConnection().executeUpdate("INSERT INTO `guilds` (`name`, `owner`, `plain`) VALUES (?,?,?)", clanName, owner.toString(), plainClanName);
            Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `uuid` = ?", clanName, owner.toString());
            return this.setClanName(owner, clanName);
        } catch (SQLException e) {
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
            e.printStackTrace();
        }
        return clanMembers;
    }

    public boolean isClanOwner(UUID uuid) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `uuid` = ?", uuid.toString())) {
            return resultSet != null && resultSet.next();
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void save(Player player) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `profiles` WHERE `uuid` = ?", player.getUniqueId().toString())) {
            if (set != null && set.next()) {
                Database.getConnection().executeUpdate("UPDATE `profiles` SET `name` = ?, `lastSeen` = ? WHERE `uuid` = ?", player.getName(), System.currentTimeMillis(), player.getUniqueId().toString());
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `profiles` (`uuid`, `name`, `lastSeen`) VALUES (?,?,?)", player.getUniqueId().toString(), player.getName(), System.currentTimeMillis());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Profile lookup(String user) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `profiles` WHERE UPPER(`name`) = ?", user.toUpperCase())) {
            return resultSet != null && resultSet.next() ? new Profile(resultSet.getString("name"), UUID.fromString(resultSet.getString("uuid")), resultSet.getLong("lastSeen")) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Profile {
        private final String name;
        private final UUID uniqueId;
        private final long lastSeen;
    }
}
