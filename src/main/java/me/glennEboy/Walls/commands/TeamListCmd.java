package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.PlayerState;
import me.glennEboy.Walls.utils.GameNotifications;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamListCmd implements CommandExecutor{
	
	TheWalls myWalls;
	
	public TeamListCmd(TheWalls tw){
		myWalls=tw;
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


    	switch (myWalls.getGameState()){
		case PREGAME:
		case PEACETIME:
		case FIGHTING:
		case FINISHED:

			if (args.length == 0) {
				GameNotifications.sendPlayerCommandError((Player)sender,"Try /teamlist # {1, 2, 3 or 4}");
				return true;
			}
			int teamNumber = -1;
			try {
				teamNumber = Integer.parseInt(args[0]);
			} catch (final NumberFormatException e) {
				GameNotifications.sendPlayerCommandError((Player)sender, "Try /teamlist # {1, 2, 3 or 4}");
				return true;
			}
	        if (teamNumber < 1 && teamNumber > 4) {
	        	GameNotifications.sendPlayerCommandError((Player)sender, "Invalid team, please use a number between 1 and 4.");
	            return true;
	        }
	        myWalls.printTeamMates((Player)sender, PlayerState.values()[teamNumber]);
			break;
		default:
			break;
		}

    	return true;
    }
    
}
