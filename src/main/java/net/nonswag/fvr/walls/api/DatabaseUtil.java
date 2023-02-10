package net.nonswag.fvr.walls.api;

import net.nonswag.core.api.sql.Database;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.commands.ClanCommand;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseUtil {
    private final List<UUID> queue = new ArrayList<>();
    private final Walls walls;

    public DatabaseUtil(Walls walls) {
        this.walls = walls;
        try {
            Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `accounts` (uuid varchar(255), rank int, guild varchar(255))");
            Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `guilds` (plain varchar(255), name varchar(255), uuid varchar(255))");
            Database.getConnection().executeUpdate("CREATE TABLE IF NOT EXISTS `stats` (uuid varchar(255), kills int, deaths int, wins int)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(walls, () -> {
            if (this.walls.getGameState().equals(Walls.GameState.FINISHED)) return;
            queue.forEach(uuid -> {
                if (Bukkit.getPlayer(uuid) == null) return;
                loadPlayer(uuid);
                loadStats(uuid);
            });
            queue.clear();
        }, 0, 12);
    }

    public void queuePlayer(UUID uuid) {
        if (!walls.getPlayers().containsKey(uuid)) queue.add(uuid);
    }

    private void loadPlayer(UUID uuid) {
        Walls.WallsPlayer player = this.walls.getPlayers().getOrDefault(uuid, new Walls.WallsPlayer());
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE uuid = ?", uuid.toString())) {
            if (resultSet == null || !resultSet.first()) return;
            player.rank = Walls.Rank.values()[resultSet.getInt("rank")];
            player.clan = resultSet.getString("guild");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            player.clanLeader = isClanOwner(uuid);
            walls.getPlayers().put(uuid, player);
        }
    }

    private void loadStats(UUID uuid) {
        Walls.WallsPlayer wallsPlayer = walls.getPlayers().getOrDefault(uuid, new Walls.WallsPlayer());
        wallsPlayer.uuid = uuid;
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `stats` WHERE uuid = ?", uuid.toString())) {
            if (resultSet == null || !resultSet.first()) return;
            wallsPlayer.statsKills = resultSet.getInt("kills");
            wallsPlayer.statsDeaths = resultSet.getInt("deaths");
            wallsPlayer.statsWins = resultSet.getInt("wins");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            walls.getPlayers().put(uuid, wallsPlayer);
        }
    }

    public void saveAllData() {
        walls.getPlayers().values().forEach(this::saveWallsStats);
    }

    private void saveWallsStats(Walls.WallsPlayer player) {
        try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `accounts` WHERE `uuid` = ?", player.uuid.toString())) {
            if (set != null && set.next()) {
                Database.getConnection().executeUpdate("UPDATE `stats` SET `kills` = ?, `deaths` = ?, `wins` = ? WHERE `uuid` = ?",
                        player.kills, player.deaths, player.wins, player.uuid.toString());
            } else {
                Database.getConnection().executeUpdate("INSERT INTO `stats` (`kills`,`deaths`, `wins`, `uuid`) VALUES (?,?,?,?)",
                        player.kills, player.deaths, player.wins, player.uuid.toString());
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
        } catch (final SQLException e) {
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
            e.printStackTrace();
            return false;
        }
    }

    public boolean staffRenameClan(String oldClanName, String newClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", ClanCommand.stripAllClanCharacters(newClanName))) {
            if (resultSet == null) return false;
            if (resultSet.first() && !ClanCommand.stripAllClanCharacters(oldClanName).equalsIgnoreCase(newClanName)) {
                Bukkit.getLogger().info("StaffRenameClan: found same name clan - Cannot override.");
                return false;
            }
            try (ResultSet set = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", ClanCommand.stripAllClanCharacters(oldClanName))) {
                if (set == null) return false;
                if (!set.first()) {
                    Bukkit.getLogger().info("StaffRenameClan: could not find this clan in the guilds. ");
                    return false;
                }
                String oldFancyName = set.getString("name");
                Database.getConnection().executeUpdate("UPDATE `guilds` SET `name` = ?, `plain` = ? where `plain` = ?", newClanName, ClanCommand.stripAllClanCharacters(newClanName), ClanCommand.stripAllClanCharacters(oldClanName));
                if (Walls.debugMode) Bukkit.getLogger().info("Staff Rename Clan - old Name - " + oldFancyName);
                if (oldFancyName != null) {
                    Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `guild` = ?", newClanName, oldFancyName);
                }
                return true;
            }
        } catch (final SQLException e) {
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

    public boolean setNewClanLeader(UUID clanOwner, String newClanOwnerName, UUID newClanOwner) {
        String oldOwnerUID = clanOwner.toString();
        String newOwnerUID = newClanOwner.toString();
        try {
            Database.getConnection().executeUpdate("UPDATE `guilds` SET  `leader` = ?, `uuid` = ? where `uuid` = ?", newClanOwnerName, newOwnerUID, oldOwnerUID);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createClan(String clanName, String clanOwner, UUID owner, String plainClanName) {
        try (ResultSet resultSet = Database.getConnection().executeQuery("SELECT * FROM `guilds` WHERE `plain` = ?", plainClanName)) {
            if (resultSet == null || !resultSet.first()) return true;
            Database.getConnection().executeUpdate("INSERT INTO `guilds` (`name`, `leader`, `uuid`, `plain`) VALUES (?,?,?,?)", clanName, clanOwner, owner, plainClanName);
            Database.getConnection().executeUpdate("UPDATE `accounts` SET  `guild` = ? where `uuid` = ?", clanName, owner);
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
}
