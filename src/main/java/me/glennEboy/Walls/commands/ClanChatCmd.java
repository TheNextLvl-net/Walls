package me.glennEboy.Walls.commands;

import java.util.UUID;
import java.util.logging.Level;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ClanChatCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public ClanChatCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        final String messageToSend = StringUtils.join(args, " ");

        WallsPlayer tWP = myWalls.getWallsPlayer(((Player)sender).getUniqueId());
        
        
        if (tWP.clan==null){
            return true;
        }

        myWalls.getLogger().log(Level.INFO, TheWalls.chatPrefix + "/CC: " + sender.getName() + ": " + messageToSend);

        for (UUID u : myWalls.getAllPlayers().keySet()) {
            if (Bukkit.getPlayer(u)!=null){                
                WallsPlayer anotherWP = myWalls.getWallsPlayer(u);
                if ((anotherWP.clan!=null && anotherWP.clan.equals(tWP.clan)) || (Bukkit.getPlayer(u).isOp() && myWalls.staffListSnooper.contains(u))){                    
                    Bukkit.getPlayer(u).sendMessage(TheWalls.CLANCHAT_PREFIX.replace("??", ChatColor.translateAlternateColorCodes('&', tWP.clan)) + TheWalls.teamChatColors[tWP.playerState.ordinal()] + sender.getName() + ChatColor.WHITE + ": " + messageToSend);
                }
            }
        }

        return true;
    }
    
}
