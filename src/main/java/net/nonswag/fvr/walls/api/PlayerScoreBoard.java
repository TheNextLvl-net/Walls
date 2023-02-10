package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.Walls.Team;
import net.nonswag.core.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.UUID;


public class PlayerScoreBoard {


    private final Walls walls;

    private final Scoreboard board;
    private final org.bukkit.scoreboard.Team team1;
    private final org.bukkit.scoreboard.Team team2;
    private final org.bukkit.scoreboard.Team team3;
    private final org.bukkit.scoreboard.Team team4;
    private final org.bukkit.scoreboard.Team teamSpecs;
    private final Objective teamNumbersObjective;

    public PlayerScoreBoard(Walls walls) {
        this.walls = walls;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();

        teamSpecs = board.registerNewTeam("Specs");
        team1 = board.registerNewTeam(Walls.teamNames[1]);
        team2 = board.registerNewTeam(Walls.teamNames[2]);
        team3 = board.registerNewTeam(Walls.teamNames[3]);
        team4 = board.registerNewTeam(Walls.teamNames[4]);

        team1.setPrefix(Walls.teamChatColors[1] + "");
        team2.setPrefix(Walls.teamChatColors[2] + "");
        team3.setPrefix(Walls.teamChatColors[3] + "");
        team4.setPrefix(Walls.teamChatColors[4] + "");

        teamSpecs.setPrefix(Walls.teamChatColors[0] + "");
        teamSpecs.setCanSeeFriendlyInvisibles(true);
        teamSpecs.setAllowFriendlyFire(false);

        teamNumbersObjective = board.registerNewObjective("The Walls", "dummy");
        teamNumbersObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Objective killsObjective = board.registerNewObjective(ChatColor.BOLD + "Kills", "playerKillCount");
        killsObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        Objective healthObjective = board.registerNewObjective("showhealth", "health");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        healthObjective.setDisplayName("HP");

        updateScoreboardScores();

    }


    public void updateNumberOfPlayers() {
        if (walls.getGameState() == GameState.PREGAME && !walls.starting) {
            teamNumbersObjective.setDisplayName((Walls.preGameAutoStartPlayers - walls.getNumberOfPlayers()) + " more players");
        }
    }

    public void updateClock(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        String clock = "";
        if (minutes > 0) clock += StringUtil.format("00", minutes) + "m ";
        clock += StringUtil.format("00", seconds) + "s";


        switch (walls.getGameState()) {
            case PREGAME:
                if (walls.starting) {
                    teamNumbersObjective.setDisplayName(ChatColor.LIGHT_PURPLE + "Starting.. " + ChatColor.WHITE + time);
                } else if (time == 0) {
                    teamNumbersObjective.setDisplayName("Peace: " + Walls.peaceTimeMins + ":00");
                } else {
                    teamNumbersObjective.setDisplayName((Walls.preGameAutoStartPlayers - walls.getNumberOfPlayers()) + " more players");
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
        teamNumbersObjective.getScore(Walls.teamNames[1]).setScore(this.team1.getSize());
        teamNumbersObjective.getScore(Walls.teamNames[2]).setScore(this.team2.getSize());
        teamNumbersObjective.getScore(Walls.teamNames[3]).setScore(this.team3.getSize());
        teamNumbersObjective.getScore(Walls.teamNames[4]).setScore(this.team4.getSize());
    }

    public void addPlayerToTeam(UUID pUID, Team ps) {
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
        org.bukkit.scoreboard.Team team = board.getTeam(Bukkit.getOfflinePlayer(pUID).getName());
        if (team != null) team.removeEntry(Bukkit.getOfflinePlayer(pUID).getName());
    }
}
