package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.PlayerState;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.utils.GameNotifications;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public TeamCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        
        if (!TheWalls.allowPickTeams){
            GameNotifications.sendPlayerCommandError((Player)sender,"You cannot pick teams this time.");
            return true;
        }


        switch (myWalls.getGameState()){
        case PREGAME:
            
            if (args.length == 0) {
                GameNotifications.sendPlayerCommandError((Player)sender,"Try /team # {1, 2, 3 or 4}");
                return true;
            }
            int teamNumber = -1;
            try {
                teamNumber = Integer.parseInt(args[0]);
            } catch (final NumberFormatException e) {
                GameNotifications.sendPlayerCommandError((Player)sender, "Try /team # {1, 2, 3 or 4}");
                return true;
            }
            if (teamNumber < 1 || teamNumber > 4) {
                GameNotifications.sendPlayerCommandError((Player)sender, "Invalid team, please use a number between 1 and 4.");
                return true;
            }
            final Player player = (Player) sender;

            WallsPlayer twp = myWalls.getWallsPlayer(player.getUniqueId()); 
            if (twp.playerState.compareTo(PlayerState.values()[teamNumber]) == 0) {
                sender.sendMessage(TheWalls.chatPrefix + ChatColor.GOLD + "You're already in this team!");
                return true;
            }
                        
            if (myWalls.checkEnoughSpaceInTeam(teamNumber)){                
//                if (twp.playerState!= PlayerState.SPEC){                                                
//                    myWalls.playerScoreBoard.removePlayerFromTeam(((Player)sender).getUniqueId());
//                }
//                myWalls.playerScoreBoard.addPlayerToTeam(((Player)sender).getUniqueId(), PlayerState.values()[teamNumber]);
//                myWalls.playerScoreBoard.updateScoreboardScores();

                GameNotifications.teamMessage(myWalls, PlayerState.values()[teamNumber], ((Player)sender).getName()+" joined " + TheWalls.teamsNames[teamNumber]);
                this.setTeam(player.getUniqueId(), PlayerState.values()[teamNumber]);
                myWalls.printTeamMates((Player)sender, PlayerState.values()[teamNumber]);
                
                

            }else{
                GameNotifications.sendPlayerCommandError((Player)sender,TheWalls.teamsNames[teamNumber] + ChatColor.WHITE + " is full :(");
            }
            break;
        case PEACETIME:
        case FIGHTING:
            GameNotifications.sendPlayerCommandError((Player)sender,"Its a little late to change your mind.. You're already in a team.");
            break;
        case FINISHED:
            GameNotifications.sendPlayerCommandError((Player)sender,"Its a little late the game is over..");
            break;
        default:
            break;
        }

        return true;
    }
    
    
    private void setTeam(UUID uid, PlayerState ps){
        myWalls.getWallsPlayer(uid).playerState = ps;
//        myWalls.playerScoreBoard.removePlayerFromTeam(uid);
//        myWalls.playerScoreBoard.addPlayerToTeam(uid, ps);

    }
    
    
}
