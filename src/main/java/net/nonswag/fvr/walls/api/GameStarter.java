package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.Walls.Team;
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
        for (int i = 0; i < 5; i++) numberAddedToTeam[i] = 0;
        Map<UUID, Team> assignedPlayers = new HashMap<>();
        BasicPlayerKit basicKit = new BasicPlayerKit();
        VipStartPlayerKitPerks vipPerks = new VipStartPlayerKitPerks();
        ProStartPlayerKitPerks proPerks = new ProStartPlayerKitPerks();
        for (UUID all : players.keySet()) {
            Player player = Bukkit.getPlayer(all);
            if (player == null) continue;
            player.closeInventory();
            player.getInventory().clear();
            basicKit.givePlayerKit(player);
            player.setFallDistance(0f);
            WallsPlayer tempWallsPlayer = walls.getPlayer(all);
            switch (tempWallsPlayer.getTeam()) {
                case SPECTATORS:
                    int smallestTeam = GameStarter.getSmallestTeam(walls);
                    System.out.print("smallest team: " + smallestTeam);
                    System.out.println("team is: " + Team.values()[smallestTeam]);
                    System.out.println("available team spawns: " + walls.getSpawns().size());
                    assignedPlayers.put(all, Team.values()[smallestTeam]);
                    if (smallestTeam >= walls.getSpawns().size()) return;
                    player.teleport(walls.getSpawns().get(smallestTeam));
                    Notifier.notify(player, "You have been assigned to " + Walls.teamNames[smallestTeam]);
                    break;
                case RED:
                    player.teleport(walls.getTeam1Spawn());
                    walls.getPlayerScoreBoard().addPlayerToTeam(player, Team.RED);
                    break;
                case YELLOW:
                    player.teleport(walls.getTeam2Spawn());
                    walls.getPlayerScoreBoard().addPlayerToTeam(player, Team.YELLOW);
                    break;
                case GREEN:
                    player.teleport(walls.getTeam3Spawn());
                    walls.getPlayerScoreBoard().addPlayerToTeam(player, Team.GREEN);
                    break;
                case BLUE:
                    player.teleport(walls.getTeam4Spawn());
                    walls.getPlayerScoreBoard().addPlayerToTeam(player, Team.BLUE);
                    break;
                default:
                    break;
            }
            if (walls.getPlayer(all).getRank().pro()) proPerks.givePlayerKit(player);
            else if (walls.getPlayer(all).getRank().vip()) vipPerks.givePlayerKit(player);
            walls.getPlayerScoreBoard().setScoreBoard(player);
        }

        assignedPlayers.forEach((uuid, team) -> {
            WallsPlayer wallsPlayer = walls.getPlayer(uuid);
            if (wallsPlayer == null) return;
            wallsPlayer.setTeam(team);
            players.put(uuid, wallsPlayer);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) walls.getPlayerScoreBoard().addPlayerToTeam(player, team);
        });

        walls.setGameState(GameState.PEACETIME);
        Bukkit.getWorlds().forEach(world -> world.setGameRuleValue("doDaylightCycle", "true"));
        Notifier.broadcast(walls.getPeaceTimeMins() + " minutes until the wall drops! " + ChatColor.BOLD + "GOOD LUCK EVERYONE!");

        walls.kickOffCompassThread();
        Notifier.broadcast("Enemy Finder Compass now activated.");

        if (Walls.diamondWalls) FullKitCommand.fullDiamond(walls);
        else if (Walls.ironWalls) FullKitCommand.fullIron(walls);
        walls.clock.setClock(walls.getPeaceTimeMins() * 60, walls::dropWalls);
        walls.getPlayerScoreBoard().updateScoreboardScores();
    }


    private static int getSmallestTeam(Walls myWalls) {
        int smallestTeam = 100;
        int teamWithLowestNumberOfPlayers = 0;
        for (int i = 1; i < 5; i++) {
            if ((myWalls.getTeamSize(Team.values()[i]) + numberAddedToTeam[i]) < smallestTeam) {
                teamWithLowestNumberOfPlayers = i;
                smallestTeam = (myWalls.getTeamSize(Team.values()[i]) + numberAddedToTeam[i]);
            }
        }
        ++numberAddedToTeam[teamWithLowestNumberOfPlayers];
        return teamWithLowestNumberOfPlayers;
    }
}
