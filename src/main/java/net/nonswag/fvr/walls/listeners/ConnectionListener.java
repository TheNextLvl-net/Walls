package net.nonswag.fvr.walls.listeners;

import lombok.RequiredArgsConstructor;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.GameStarter;
import net.nonswag.fvr.walls.api.Notifier;
import net.nonswag.fvr.walls.api.PlayerVisibility;
import net.nonswag.fvr.walls.commands.WallsCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {
    private static final List<UUID> quitters = new ArrayList<>();
    private static final Map<UUID, BukkitTask> quitterTasks = new HashMap<>();
    private final Walls walls;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        walls.database.loadPlayer(event.getPlayer());
        walls.database.loadStats(event.getPlayer());
        Walls.WallsPlayer player = walls.getPlayer(event.getPlayer());
        wallsJoinMessage(event);
        switch (walls.getGameState()) {
            case PREGAME:
                player.setPlayerState(Walls.Team.SPECTATORS);
                walls.getSpectatorKit().givePlayerKit(event.getPlayer());
                event.getPlayer().setHealth(20);
                event.getPlayer().setFoodLevel(20);
                walls.getPlayerScoreBoard().setScoreBoard(event.getPlayer());
                walls.getPlayerScoreBoard().updateScoreboardScores();
                if (!walls.starting && walls.getPlayers().size() >= Walls.preGameAutoStartPlayers && !Walls.clanBattle && !Walls.tournamentMode) {
                    Notifier.broadcast("Game starts in " + ChatColor.LIGHT_PURPLE + Walls.preGameAutoStartSeconds + ChatColor.WHITE + " seconds!!");
                    walls.clock.setClock(Walls.preGameAutoStartSeconds, () -> GameStarter.startGame(walls.getPlayers(), walls));
                    walls.starting = true;
                }
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                if (!quitters.contains(event.getPlayer().getUniqueId())) {
                    walls.getSpectatorKit().givePlayerKit(event.getPlayer());
                    walls.getPlayerScoreBoard().addPlayerToTeam(event.getPlayer(), Walls.Team.SPECTATORS);
                    event.getPlayer().setHealth(20);
                    event.getPlayer().setFoodLevel(20);
                    PlayerVisibility.makeSpecInvisible(walls, event.getPlayer());
                    PlayerVisibility.makeSpecVisToSpecs(walls, event.getPlayer());
                } else {
                    quitters.remove(event.getPlayer().getUniqueId());
                    walls.getInventory().remove(event.getPlayer().getUniqueId());
                    quitterTasks.get(event.getPlayer().getUniqueId()).cancel();
                    PlayerVisibility.hideAllSpecs(walls, event.getPlayer());
                }
                walls.getPlayerScoreBoard().setScoreBoard(event.getPlayer());
                break;
            default:
                break;
        }
    }

    private void wallsJoinMessage(PlayerJoinEvent event) {
        event.setJoinMessage("");
        Walls.WallsPlayer player = walls.getPlayer(event.getPlayer());
        if (player == null) return;
        if (!event.getPlayer().isOp() && !player.getRank().staff()) {
            if (Walls.clanBattle) {
                if (player.getClan() == null) {
                    event.getPlayer().kickPlayer("§cSorry, this game is a Clan Battle!!");
                    return;
                }
                if (!Walls.clans.contains(player.getClan())) {
                    event.getPlayer().kickPlayer("Sorry, this game is a Clan Battle!!");
                    return;
                }
            }
            if (Walls.playerJoinRestriction != Walls.PlayerJoinType.ANYONE) {
                boolean kick = false;
                switch (Walls.playerJoinRestriction) {
                    case VIP:
                        kick = !player.getRank().vip();
                        break;
                    case PRO:
                        kick = !player.getRank().pro();
                        break;
                    case STAFF:
                        kick = !player.getRank().staff();
                        break;
                }
                if (kick) {
                    event.getPlayer().kickPlayer("§cSorry, this game is " + Walls.playerJoinRestriction.toString() + " only just now.");
                    return;
                }
            }
        }
        Notifier.broadcast(ChatColor.GRAY + event.getPlayer().getName() + " joined the server.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(walls, () -> walls.database.save(player));
        WallsCommand.VOTES.remove(player.getUniqueId());
        Walls.WallsPlayer wallsPlayer = walls.getPlayer(player);
        switch (walls.getGameState()) {
            case PREGAME:
                player.getInventory().clear();
                walls.getPlayers().remove(player.getUniqueId());
                walls.getPlayerScoreBoard().removePlayerFromTeam(player);
                WallsCommand.VOTES.remove(player.getUniqueId());
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                event.setQuitMessage("");
                if (!walls.isSpectator(player)) {
                    if (walls.getInCombat().containsKey(player.getUniqueId())) {
                        Notifier.broadcast(player.getDisplayName() + " just left while in combat. (Grab their stuff! x:"
                                + player.getLocation().getBlockX()
                                + " y:" + player.getLocation().getBlockY()
                                + " z:" + player.getLocation().getBlockZ() + ")");
                        World world = Bukkit.getWorld(Walls.levelName);
                        for (ItemStack item : player.getInventory()) {
                            if (item == null) continue;
                            world.dropItemNaturally(player.getLocation(), item);
                        }
                        HolographicDisplaysAPI.get(walls).createHologram(player.getLocation().add(0, 2.5, 0)).getLines().appendItem(new ItemStack(Material.RAW_CHICKEN));
                        Hologram tempHologram = HolographicDisplaysAPI.get(walls).createHologram(player.getLocation().add(0, 2, 0));
                        tempHologram.getLines().appendText(Walls.teamChatColors[wallsPlayer.getPlayerState().ordinal()] + player.getDisplayName());
                        tempHologram.getLines().appendText("§4COMBAT LOGGED HERE");
                        tempHologram.getLines().appendText("Chicken.");
                        walls.getInCombat().remove(player.getUniqueId());
                        walls.foodTime = 0;
                        if (walls.foodDisabled) {
                            walls.foodDisabled = false;
                            Notifier.broadcast("You can now eat again!");
                        }
                        wallsPlayer.setPlayerState(Walls.Team.SPECTATORS);
                        wallsPlayer.setDeaths(wallsPlayer.getDeaths() + 5);
                        wallsPlayer.setKills(0);
                        wallsPlayer.setWins(0);
                        player.closeInventory();
                        player.getInventory().clear();
                        walls.getPlayerScoreBoard().removePlayerFromTeam(player);
                        walls.getPlayerScoreBoard().addPlayerToTeam(player, Walls.Team.SPECTATORS);
                        walls.getPlayerScoreBoard().updateScoreboardScores();
                        if (walls.calculateTeamsLeft() < 2) {
                            walls.setGameState(Walls.GameState.FINISHED);
                            Notifier.broadcast("Server restarting in " + walls.restartTimer + " seconds!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(walls, () -> System.exit(0), 20L * walls.restartTimer);
                        }
                    } else {
                        Notifier.broadcast(player.getDisplayName() + " may have just left the game..");
                        if (!walls.getInventory().containsKey(player.getUniqueId())) {
                            walls.getInventory().put(player.getUniqueId(), player.getInventory());
                        }
                        quitters.add(player.getUniqueId());
                        quitterTasks.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(walls, () -> {
                            if (!quitters.contains(player.getUniqueId())) return;
                            walls.getPlayerScoreBoard().removePlayerFromTeam(player);
                            walls.getPlayerScoreBoard().addPlayerToTeam(player, Walls.Team.SPECTATORS);
                            quitters.remove(player.getUniqueId());
                            if (!Bukkit.getOfflinePlayer(player.getUniqueId()).isOnline()) {
                                for (ItemStack item : walls.getInventory().get(player.getUniqueId())) {
                                    if (item == null) continue;
                                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                                }
                                Notifier.broadcast("Yup " + player.getDisplayName()
                                        + " left the game.. (Grab their stuff! x:"
                                        + player.getLocation().getBlockX()
                                        + " y:" + player.getLocation().getBlockY()
                                        + " z:" + player.getLocation().getBlockZ() + ")");
                            }
                            walls.foodTime = 0;
                            if (walls.foodDisabled) {
                                walls.foodDisabled = false;
                                Notifier.broadcast("You can now eat again!");
                            }
                            walls.getPlayer(player).setPlayerState(Walls.Team.SPECTATORS);
                            player.closeInventory();
                            player.getInventory().clear();
                            if (walls.calculateTeamsLeft() > 1) return;
                            walls.setGameState(Walls.GameState.FINISHED);
                            Notifier.broadcast("Server restarting in " + walls.restartTimer + " seconds!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(walls, () -> System.exit(0), 20L * walls.restartTimer);
                        }, walls.relogTime * 20L));
                    }
                } else player.getInventory().clear();
                walls.getPlayerScoreBoard().updateScoreboardScores();
                break;
            default:
                break;
        }
    }
}
