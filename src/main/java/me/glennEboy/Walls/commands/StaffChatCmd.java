package me.glennEboy.Walls.commands;

import java.util.UUID;
import java.util.logging.Level;

import me.glennEboy.Walls.TheWalls;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class StaffChatCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public StaffChatCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        final String messageToSend = StringUtils.join(args, " ");


        if (!myWalls.isStaff(((Player)sender).getUniqueId())){
            return true;
        }

        myWalls.getLogger().log(Level.INFO, TheWalls.chatPrefix + "/SC: " + sender.getName() + ": " + messageToSend);

        for (UUID u : myWalls.getStaffList()) {
            if (Bukkit.getPlayer(u)!=null && !myWalls.noStaffChat.contains(u)){                
                if (TheWalls.debugMode){
                    myWalls.getLogger().info("/sc sent to : u whcih is username "+Bukkit.getPlayer(u).getName());
                }
                Bukkit.getPlayer(u).sendMessage(TheWalls.STAFFCHATT_PREFIX + ChatColor.AQUA + sender.getName() + ChatColor.WHITE + ": " + messageToSend);
            }
        }

        return true;
    }
    
}
