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


public class OpChatCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public OpChatCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        final String messageToSend = StringUtils.join(args, " ");

        if (!sender.isOp()){
            return true;
        }

        myWalls.getLogger().log(Level.INFO, TheWalls.chatPrefix + "/OC: " + sender + ": " + messageToSend);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) p.sendMessage(TheWalls.OPCHAT_PREFIX + ChatColor.GOLD + sender.getName() + ChatColor.WHITE + ": " + messageToSend);
        }

        return true;
    }
}
