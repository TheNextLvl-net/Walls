package me.glennEboy.Walls.utils;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.GameState;
import me.glennEboy.Walls.TheWalls.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.UUID;


public class PlayerScoreBoard {


    private final TheWalls myWalls;

    private final Scoreboard board;
    private final Team team1;
    private final Team team2;
    private final Team team3;
    private final Team team4;
    private final Team teamSpecs;
    private final Objective teamNumbersObjective;

    public PlayerScoreBoard(TheWalls aWalls) {
        myWalls = aWalls;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();

        teamSpecs = board.registerNewTeam("Specs");
        team1 = board.registerNewTeam(TheWalls.teamsNames[1]);
        team2 = board.registerNewTeam(TheWalls.teamsNames[2]);
        team3 = board.registerNewTeam(TheWalls.teamsNames[3]);
        team4 = board.registerNewTeam(TheWalls.teamsNames[4]);

        team1.setPrefix(TheWalls.teamChatColors[1] + "");
        team2.setPrefix(TheWalls.teamChatColors[2] + "");
        team3.setPrefix(TheWalls.teamChatColors[3] + "");
        team4.setPrefix(TheWalls.teamChatColors[4] + "");

        teamSpecs.setPrefix(TheWalls.teamChatColors[0] + "");
        teamSpecs.setCanSeeFriendlyInvisibles(true);
        teamSpecs.setAllowFriendlyFire(false);

        teamNumbersObjective = board.registerNewObjective("The Walls", "dummy");
        teamNumbersObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Objective killsObjective = board.registerNewObjective(ChatColor.BOLD + "Kills", "playerKillCount");
        killsObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        Objective healthObjective = board.registerNewObjective("showhealth", "health");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        healthObjective.setDisplayName("/ 20");

        updateScoreboardScores();

    }


    public void updateNumberOfPlayers() {
        if (myWalls.getGameState() == GameState.PREGAME && !myWalls.starting) {
            teamNumbersObjective.setDisplayName(((TheWalls.preGameAutoStartPlayers + 1) - myWalls.getNumberOfPlayers()) + " more players");
        }

    }

    public void updateClock(int timeInSeconds) {
        final int s = timeInSeconds % 60;
        String ss = String.valueOf(s);
        if (ss.length() == 1) {
            ss = "0" + ss;
        }

        final int m = (timeInSeconds - s) / 60;
        final String clock = m + ":" + ss;


        switch (myWalls.getGameState()) {
            case PREGAME:
                if (myWalls.starting) {
                    teamNumbersObjective.setDisplayName(ChatColor.LIGHT_PURPLE + "Starting.. " + ChatColor.WHITE + timeInSeconds);
                } else if (timeInSeconds == 0) {
                    teamNumbersObjective.setDisplayName("Peace: " + TheWalls.peaceTimeMins + ":00");
                } else {
                    teamNumbersObjective.setDisplayName(((TheWalls.preGameAutoStartPlayers + 1) - myWalls.getNumberOfPlayers()) + " more players");
                }
                break;
            case PEACETIME:
                teamNumbersObjective.setDisplayName("Peace: " + clock);
                break;
            case FIGHTING:
                teamNumbersObjective.setDisplayName("Fight: " + clock);
                break;
            default:
                break;
        }


    }

    public void updateScoreboardScores() {
        teamNumbersObjective.getScore(TheWalls.teamsNames[1]).setScore(this.team1.getSize());
        teamNumbersObjective.getScore(TheWalls.teamsNames[2]).setScore(this.team2.getSize());
        teamNumbersObjective.getScore(TheWalls.teamsNames[3]).setScore(this.team3.getSize());
        teamNumbersObjective.getScore(TheWalls.teamsNames[4]).setScore(this.team4.getSize());
    }

    public void addPlayerToTeam(UUID pUID, PlayerState ps) {
        switch (ps) {
            case SPECTATORS:
                this.teamSpecs.addEntry(Bukkit.getOfflinePlayer(pUID).getName());
                break;
            case RED:
                this.team1.addEntry(Bukkit.getOfflinePlayer(pUID).getName());
                break;
            case YELLOW:
                this.team2.addEntry(Bukkit.getOfflinePlayer(pUID).getName());
                break;
            case GREEN:
                this.team3.addEntry(Bukkit.getOfflinePlayer(pUID).getName());
                break;
            case BLUE:
                this.team4.addEntry(Bukkit.getOfflinePlayer(pUID).getName());
                break;
        }
        updateScoreboardScores();
    }

    public void setScoreBoard(UUID pUID) {
        Bukkit.getPlayer(pUID).setScoreboard(board);
        updateNumberOfPlayers();
    }

    public void removePlayerFromTeam(UUID pUID) {
        Team team = board.getTeam(Bukkit.getOfflinePlayer(pUID).getName());
        if (team != null) team.removeEntry(Bukkit.getOfflinePlayer(pUID).getName());
    }
}
