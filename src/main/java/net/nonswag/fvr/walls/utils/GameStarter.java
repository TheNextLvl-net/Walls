package net.nonswag.fvr.walls.utils;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.Walls.PlayerState;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.commands.FullKitCommand;
import net.nonswag.fvr.walls.kits.BasicPlayerKit;
import net.nonswag.fvr.walls.kits.ProStartPlayerKitPerks;
import net.nonswag.fvr.walls.kits.VipStartPlayerKitPerks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStarter {

    private static final int[] numberAddedToTeam = new int[5];

    public static void startGame(Map<UUID, WallsPlayer> players, final Walls walls) {
        for (int i = 0; i < 5; i++) {
            numberAddedToTeam[i] = 0;
        }

        Map<UUID, PlayerState> assignedPlayers = new HashMap<>();

        BasicPlayerKit basicKit = new BasicPlayerKit();
        VipStartPlayerKitPerks vipPerks = new VipStartPlayerKitPerks();
        ProStartPlayerKitPerks proPerks = new ProStartPlayerKitPerks();


        for (UUID all : players.keySet()) {

            Player p = Bukkit.getPlayer(all);
            if (p != null) {

                p.closeInventory();
                p.getInventory().clear();

                basicKit.givePlayerKit(p);

                p.setFallDistance(0f);

                WallsPlayer tempWallsPlayer = walls.getPlayer(all);

                switch (tempWallsPlayer.playerState) {
                    case SPECTATORS:
                        int rand = GameStarter.getSmallestTeam(walls);
                        if (Walls.debugMode) walls.getLogger().info("creating random for team " + rand);
                        assignedPlayers.put(all, PlayerState.values()[rand]);
                        p.teleport(Walls.spawns.get(rand));
                        Notifier.notify(p, "You have been assigned to " + Walls.teamsNames[rand]);
                        break;
                    case RED:
                        p.teleport(Walls.team1Spawn);
                        walls.playerScoreBoard.addPlayerToTeam(all, PlayerState.RED);

                        break;
                    case YELLOW:
                        p.teleport(Walls.team2Spawn);
                        walls.playerScoreBoard.addPlayerToTeam(all, PlayerState.YELLOW);

                        break;
                    case GREEN:
                        p.teleport(Walls.team3Spawn);
                        walls.playerScoreBoard.addPlayerToTeam(all, PlayerState.GREEN);

                        break;
                    case BLUE:
                        p.teleport(Walls.team4Spawn);
                        walls.playerScoreBoard.addPlayerToTeam(all, PlayerState.BLUE);

                        break;

                    default:
                        break;

                }

                if (walls.getPlayer(all).rank.pro()) {
                    proPerks.givePlayerKit(p);
                    if (Walls.debugMode)
                        walls.getLogger().info("Gave PRO + stuff to player " + all.toString());
                } else if (walls.getPlayer(all).rank.vip()) {
                    vipPerks.givePlayerKit(p);
                    if (Walls.debugMode)
                        walls.getLogger().info("Gave VIP + stuff to player " + all.toString());
                }

                walls.playerScoreBoard.setScoreBoard(all);
            }

            for (UUID uuid : assignedPlayers.keySet()) {
                WallsPlayer twp = walls.getPlayer(uuid);
                twp.playerState = assignedPlayers.get(uuid);
                players.put(uuid, twp);
                walls.playerScoreBoard.addPlayerToTeam(uuid, twp.playerState);
            }
        }

        walls.setGameState(GameState.PEACETIME);
        Bukkit.getWorlds().forEach(world -> world.setGameRuleValue("doDaylightCycle", "true"));
        Notifier.broadcast(Walls.peaceTimeMins + " minutes until the wall drops! " + ChatColor.BOLD + "GOOD LUCK EVERYONE!");

        walls.kickOffCompassThread();
        Notifier.broadcast("Enemy Finder Compass now activated.");


        if (Walls.diamondONLY) {
            FullKitCommand.fullDiamond(walls);

        } else if (Walls.ironONLY) {

            FullKitCommand.fullIron(walls);

        } else if (Walls.fullDiamond) {
            int whichGame = Walls.random.nextInt(4);
            switch (whichGame) {
                case 1:
                    FullKitCommand.fullIron(walls);
                    break;
                case 2:
                case 3:
                    FullKitCommand.fullDiamond(walls);
                    break;

            }
        }
        walls.clock.setClock(Walls.peaceTimeMins * 60, walls::dropWalls);
        walls.playerScoreBoard.updateScoreboardScores();
    }


    private static int getSmallestTeam(Walls myWalls) {

        int smallestTeam = 100;
        int teamWithLowestNumberOfPlayers = 0;

        for (int i = 1; i < 5; i++) {
            if ((myWalls.getTeamSize(PlayerState.values()[i]) + numberAddedToTeam[i]) < smallestTeam) {
                teamWithLowestNumberOfPlayers = i;
                smallestTeam = (myWalls.getTeamSize(PlayerState.values()[i]) + numberAddedToTeam[i]);
            }
        }
        ++numberAddedToTeam[teamWithLowestNumberOfPlayers];
        return teamWithLowestNumberOfPlayers;
    }
}
