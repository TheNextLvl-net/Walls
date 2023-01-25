package me.glennEboy.Walls.utils;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.GameState;
import me.glennEboy.Walls.TheWalls.PlayerState;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.commands.FullDiamondCmd;
import me.glennEboy.Walls.kits.BasicPlayerKit;
import me.glennEboy.Walls.kits.ProStartPlayerKitPerks;
import me.glennEboy.Walls.kits.VipStartPlayerKitPerks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStarter {

    private static final int[] numberAddedToTeam = new int[5];

    public static void startGame(Map<UUID, WallsPlayer> players, final TheWalls myWalls) {

        for (int i = 0; i < 5; i++) {
            numberAddedToTeam[i] = 0;
        }

        Map<UUID, PlayerState> assignedPlayers = new HashMap<UUID, PlayerState>();

        BasicPlayerKit basicKit = new BasicPlayerKit();
        VipStartPlayerKitPerks vipPerks = new VipStartPlayerKitPerks();
        ProStartPlayerKitPerks proPerks = new ProStartPlayerKitPerks();


        synchronized (players) {
            for (UUID pUID : players.keySet()) {

                Player p = Bukkit.getPlayer(pUID);
                if (p != null) {

                    p.closeInventory();
                    p.getInventory().clear();

                    basicKit.givePlayerKit(p);

                    p.setFallDistance(0f);

                    WallsPlayer tempWallsPlayer = players.get(pUID);

                    switch (tempWallsPlayer.playerState) {
                        case SPEC:
                            int rand = GameStarter.getSmallestTeam(myWalls);
                            if (TheWalls.debugMode) myWalls.getLogger().info("creating random for team " + rand);
                            assignedPlayers.put(pUID, PlayerState.values()[rand]);
                            p.teleport(TheWalls.spawns.get(rand));
                            p.sendMessage(TheWalls.chatPrefix + "you have been assigned to " + TheWalls.teamsNames[rand]);
                            break;
                        case TEAM1:
                            p.teleport(TheWalls.team1Spawn);
                            myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.TEAM1);

                            break;
                        case TEAM2:
                            p.teleport(TheWalls.team2Spawn);
                            myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.TEAM2);

                            break;
                        case TEAM3:
                            p.teleport(TheWalls.team3Spawn);
                            myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.TEAM3);

                            break;
                        case TEAM4:
                            p.teleport(TheWalls.team4Spawn);
                            myWalls.playerScoreBoard.addPlayerToTeam(pUID, PlayerState.TEAM4);

                            break;

                        default:
                            break;

                    }

                    if (myWalls.isPRO(pUID)) {
                        proPerks.givePlayerKit(p);
                        if (TheWalls.debugMode)
                            myWalls.getLogger().info("Gave PRO + stuff to player " + pUID.toString());
                    } else if (myWalls.isVIP(pUID)) {
                        vipPerks.givePlayerKit(p);
                        if (TheWalls.debugMode)
                            myWalls.getLogger().info("Gave VIP + stuff to player " + pUID.toString());
                    }

                    myWalls.playerScoreBoard.setScoreBoard(pUID);
                }

            }

            for (UUID pUID : assignedPlayers.keySet()) {
                WallsPlayer twp = players.get(pUID);
                twp.playerState = assignedPlayers.get(pUID);
                players.put(pUID, twp);
                myWalls.playerScoreBoard.addPlayerToTeam(pUID, twp.playerState);
            }
        }

        myWalls.setGameState(GameState.PEACETIME);
        GameNotifications.broadcastMessage(TheWalls.peaceTimeMins + " minutes until the wall drops! " + ChatColor.BOLD + "GOOD LUCK EVERYONE!");

        myWalls.kickOffCompassThread();
        GameNotifications.broadcastMessage("Enemy Finder 3000 Compass now activated.");

        myWalls.getLogger().info("++==============================================++");
        myWalls.getLogger().info(TheWalls.chatPrefix + " GAME STARTING - PEACE TIME !");
        myWalls.getLogger().info("++==============================================++");


        if (TheWalls.diamondONLY) {
            FullDiamondCmd.fullDiamond(myWalls);

        } else if (TheWalls.ironONLY) {

            FullDiamondCmd.fullIron(myWalls);

        } else if (TheWalls.fullDiamond) {
            int whichGame = TheWalls.random.nextInt(4);
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
        myWalls.clock.setClock(TheWalls.peaceTimeMins * 60, myWalls::dropWalls);
        myWalls.playerScoreBoard.updateScoreboardScores();
    }


    private static int getSmallestTeam(TheWalls myWalls) {

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
