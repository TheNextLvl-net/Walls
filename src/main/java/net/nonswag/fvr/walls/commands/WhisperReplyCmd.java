package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class WhisperReplyCmd implements CommandExecutor{
    
    Walls myWalls;
    
    public WhisperReplyCmd(Walls tw){
        myWalls=tw;
    }

    private final String whisperFormat = ChatColor.GOLD+""+ChatColor.BOLD+"{"+ChatColor.RESET+"PM: "+ChatColor.GRAY+"<sender>"+ChatColor.RESET+""+ChatColor.GOLD+"}"+ChatColor.RESET+" <message>";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        

        if (Walls.shhhhh && !sender.isOp()){
            return true;
        }
        if (args.length < 1) {
            
            return false;
            
        }else if (!myWalls.whispers.containsKey(((Player)sender).getUniqueId())){
                
            Notifier.error(sender, "Someone needs to whisper for you to be able to reply :)");
            return true;
            
        } else {

            Player p = Bukkit.getPlayer(myWalls.whispers.get(((Player)sender).getUniqueId()));

            if (p != null) {
                final StringBuilder whisper = new StringBuilder();
                for (String arg : args) {
                    whisper.append(arg).append(" ");
                }
                whisper.setLength(whisper.length() - 1);
                
                String messageToSend = this.whisperFormat.replace("<sender>", sender.getName()).replace("<message>", whisper); 
                
                p.sendMessage(messageToSend);
                sender.sendMessage(" SENT!  "+ messageToSend);
                myWalls.getLogger().log(Level.INFO, ChatColor.GRAY + sender.getName() + " whispers to " + p.getName() + " --> " + whisper);

                myWalls.whispers.put(p.getUniqueId(),((Player)sender).getUniqueId());


            } else {
                sender.sendMessage(ChatColor.RED + "Looks like they left the building - Player not found online :(");
            }
            return true;
        }
    }
}
