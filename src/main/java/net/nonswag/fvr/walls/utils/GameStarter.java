package net.nonswag.fvr.walls.utils;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.Walls.PlayerState;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.commands.FullDiamondCmd;
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

    public static void startGame(Map<UUID, WallsPlayer> players, final Walls myWalls) {

        for (int i = 0; i < 5; i++) {
            numberAddedToTeam[i] = 0;
        }

        Map<UUID, PlayerState> assignedPlayers = new HashMap<>();

        BasicPlayerKit basicKit = new BasicPlayerKit();
        VipStartPlayerKitPerks vipPerks = new VipStartPlayerKitPerks();
        ProStartPlayerKitPerks proPerks = new ProStartPlayerKitPerks();


        for (UUID pUID : players.keySet()) {

            Player p = Bukkit.getPlayer(pUID);
            if (p != null) {

                p.closeInventory();
                p.getInventory().clear();

                basicKit.givePlayerKit(p);

                p.setFallDistance(0f);

                WallsPlayer tempWallsPlayer = players.get(pUID);

                switch (tempWallsPlayer.playerState) {
                    case SPECTATORS:
                        int rand = GameStarter.getSmallestTeam(myWalls);
                        if (Walls.debugMode) myWalls.getLogger().info("creating random for team " + rand);
                        assignedPlayers.put(pUID, PlayerState.values()[rand]);
                        p.teleport(Walls.spawns.get(rand));
                        Notifier.notify(p, "You have been assigned to " + Walls.teamsNames[rand]);
                        break;
                    case RED:
                        p.teleport(Walls.team1Spawn);
                        myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.RED);

                        break;
                    case YELLOW:
                        p.teleport(Walls.team2Spawn);
                        myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.YELLOW);

                        break;
                    case GREEN:
                        p.teleport(Walls.team3Spawn);
                        myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.GREEN);

                        break;
                    case BLUE:
                        p.teleport(Walls.team4Spawn);
                        myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.BLUE);

                        break;

                    default:
                        break;

                }

                if (myWalls.isPRO(pUID)) {
                    proPerks.givePlayerKit(p);
                    if (Walls.debugMode)
                        myWalls.getLogger().info("Gave PRO + stuff to player " + pUID.toString());
                } else if (myWalls.isVIP(pUID)) {
                    vipPerks.givePlayerKit(p);
                    if (Walls.debugMode)
                        myWalls.getLogger().info("Gave VIP + stuff to player " + pUID.toString());
                }

                myWalls.playerScoreBoard.setScoreBoard(pUID);
            }

            for (UUID uuid : assignedPlayers.keySet()) {
                WallsPlayer twp = players.get(uuid);
                twp.playerState = assignedPlayers.get(uuid);
                players.put(uuid, twp);
                myWalls.playerScoreBoard.addPlayerToTeam(uuid, twp.playerState);
            }
        }

        myWalls.setGameState(GameState.PEACETIME);
        Notifier.broadcast(Walls.peaceTimeMins + " minutes until the wall drops! " + ChatColor.BOLD + "GOOD LUCK EVERYONE!");

        myWalls.kickOffCompassThread();
        Notifier.broadcast("Enemy Finder Compass now activated.");


        if (Walls.diamondONLY) {
            FullDiamondCmd.fullDiamond(myWalls);

        } else if (Walls.ironONLY) {

            FullDiamondCmd.fullIron(myWalls);

        } else if (Walls.fullDiamond) {
            int whichGame = Walls.random.nextInt(4);
            switch (whichGame) {
                case 0:
                    FullDiamondCmd.fullChain(myWalls);
                    break;
                case 1:
                    FullDiamondCmd.fullIron(myWalls);
                    break;
                case 2:
                case 3:
                    FullDiamondCmd.fullDiamond(myWalls);
                    break;

            }
        }
        myWalls.clock.setClock(Walls.peaceTimeMins * 60, myWalls::dropWalls);
        myWalls.playerScoreBoard.updateScoreboardScores();
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
