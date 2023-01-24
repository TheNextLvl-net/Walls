package me.glennEboy.Walls.utils;

import java.util.UUID;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.GameState;
import me.glennEboy.Walls.TheWalls.PlayerState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;


public class PlayerScoreBoard {

    
    private TheWalls myWalls;
    
    private ScoreboardManager manager;
    private Scoreboard board;
    private Team team1, team2, team3, team4, teamSpecs;
    private Objective teamNumbersObjective, killsObjective, healthObjective;
    private String serverName;
    
    public PlayerScoreBoard(TheWalls aWalls){
        myWalls = aWalls;
        
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        
        teamSpecs = board.registerNewTeam("Specs");
        team1 = board.registerNewTeam(TheWalls.teamsNames[1]);
        team2 = board.registerNewTeam(TheWalls.teamsNames[2]);
        team3 = board.registerNewTeam(TheWalls.teamsNames[3]);
        team4 = board.registerNewTeam(TheWalls.teamsNames[4]);
        
        team1.setPrefix(TheWalls.teamChatColors[1]+"");
        team2.setPrefix(TheWalls.teamChatColors[2]+"");
        team3.setPrefix(TheWalls.teamChatColors[3]+"");
        team4.setPrefix(TheWalls.teamChatColors[4]+"");
        
        teamSpecs.setPrefix(TheWalls.teamChatColors[0]+"");
        teamSpecs.setCanSeeFriendlyInvisibles(true);
        teamSpecs.setAllowFriendlyFire(false);

        teamNumbersObjective = board.registerNewObjective("The Walls", "dummy");
        teamNumbersObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        killsObjective = board.registerNewObjective(ChatColor.BOLD+"Kills", "playerKillCount");
        killsObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        healthObjective = board.registerNewObjective("showhealth", "health");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        healthObjective.setDisplayName("/ 20");
        
        serverName = "[w"+TheWalls.serverNumber+"] ";

        updateScoreboardScores();

    }

    
    public void updateNumberOfPlayers(){
        if (myWalls.getGameState()==GameState.PREGAME && !myWalls.starting){
            teamNumbersObjective.setDisplayName(((TheWalls.preGameAutoStartPlayers + 1) - myWalls.getNumberOfPlayers()) + " more players");    
        }

    }
    
    public void updateClock(int timeInSeconds){
        final int s = timeInSeconds % 60;
        String ss = String.valueOf(s);
        if (ss.length() == 1) {
            ss = "0" + ss;
        }
        
        final int m = (timeInSeconds - s) / 60;
        final String clock = m + ":" + ss;


        switch (myWalls.getGameState()){
        case PREGAME:
            if (myWalls.starting){
                   teamNumbersObjective.setDisplayName(serverName+ChatColor.LIGHT_PURPLE+"Starting.. " + ChatColor.WHITE + timeInSeconds);
            }else if (timeInSeconds == 0){
                teamNumbersObjective.setDisplayName(serverName+"Peace: " + TheWalls.peaceTimeMins+":00");
            }else{                
                teamNumbersObjective.setDisplayName(serverName+((TheWalls.preGameAutoStartPlayers + 1) - myWalls.getNumberOfPlayers()) + " more players");
            }
               break;
        case PEACETIME:
            teamNumbersObjective.setDisplayName(serverName+"Peace: " + clock);
            break;
        case FIGHTING:
            teamNumbersObjective.setDisplayName(serverName+"Fight: " + clock);
            break;
        case FINISHED:
            break;
        default:
            break;
        }

        
            
    }
    
    @SuppressWarnings("deprecation")
    public void updateScoreboardScores(){

        teamNumbersObjective.getScore(Bukkit.getOfflinePlayer(TheWalls.teamsNames[1])).setScore(this.team1.getSize());
        teamNumbersObjective.getScore(Bukkit.getOfflinePlayer(TheWalls.teamsNames[2])).setScore(this.team2.getSize());
        teamNumbersObjective.getScore(Bukkit.getOfflinePlayer(TheWalls.teamsNames[3])).setScore(this.team3.getSize());
        teamNumbersObjective.getScore(Bukkit.getOfflinePlayer(TheWalls.teamsNames[4])).setScore(this.team4.getSize());

    }
    
    public void addPlayerToTeam(UUID pUID, PlayerState ps){

        switch (ps){
        case SPEC:
            this.teamSpecs.addPlayer(Bukkit.getOfflinePlayer(pUID));
            break;
        case TEAM1:
            this.team1.addPlayer(Bukkit.getOfflinePlayer(pUID));
            break;
        case TEAM2:
            this.team2.addPlayer(Bukkit.getOfflinePlayer(pUID));
            break;
        case TEAM3:
            this.team3.addPlayer(Bukkit.getOfflinePlayer(pUID));
            break;
        case TEAM4:
            this.team4.addPlayer(Bukkit.getOfflinePlayer(pUID));
            break;
        }
        updateScoreboardScores();
    }
    
    public void setScoreBoard(UUID pUID){
        Bukkit.getPlayer(pUID).setScoreboard(board);
        updateNumberOfPlayers();
    }

    public void removePlayerFromTeam(UUID pUID){
        if (board.getPlayerTeam(Bukkit.getOfflinePlayer(pUID))!=null){
            board.getPlayerTeam(Bukkit.getOfflinePlayer(pUID)).removePlayer(Bukkit.getOfflinePlayer(pUID));
        }
    }
    
}
