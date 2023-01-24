package me.glennEboy.Walls.commands;

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

public class WhisperCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public WhisperCmd(TheWalls tw){
        myWalls=tw;
    }

    private String whisperFormat = ChatColor.GOLD+""+ChatColor.BOLD+"{"+ChatColor.RESET+"PM: "+ChatColor.GRAY+"<sender>"+ChatColor.RESET+""+ChatColor.GOLD+"}"+ChatColor.RESET+" <message>";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        

        if (TheWalls.shhhhh && !sender.isOp()){
            return true;
        }
        if (args.length < 2) {
            return false;
        } else {
            Player p = Bukkit.getServer().getPlayer(args[0]);

            if (p != null) {
                final StringBuilder whisper = new StringBuilder();
                for (int x = 1; x < args.length; x++) {
                    whisper.append(args[x]).append(" ");
                }
                whisper.setLength(whisper.length() - 1);
                
                String messageToSend = this.whisperFormat.replace("<sender>", sender.getName()).replace("<message>", whisper); 
                
                p.sendMessage(messageToSend);
                sender.sendMessage(" SENT to "+p.getName()+": "+ whisper);
                myWalls.getLogger().log(Level.INFO, ChatColor.GRAY + sender.getName() + " whispers to " + p.getName() + " --> " + whisper);

                myWalls.whispers.put(p.getUniqueId(),((Player)sender).getUniqueId());

            } else {
                sender.sendMessage(ChatColor.RED + "Player not found online :(");
            }
            return true;
        }
    }
}
