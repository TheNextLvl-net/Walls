package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.utils.Notifier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShareCmd implements CommandExecutor{
    
    Walls myWalls;
    
    public ShareCmd(Walls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (myWalls.getGameState() != GameState.PEACETIME){
            Notifier.error((sender),"You can only share during Peacetime :-/");
            return true;
        }
        
        if (args.length < 1){
            if (sender instanceof Player){                
                Notifier.error(sender, "Share whats in your hand - usage: /share <IGN>");
            }else{
                sender.sendMessage("you need to be a legit player in game :( sorry - try /give");
            }
            return true;
        }
        Player player = (Player)sender;
        if (!myWalls.isVIP(player.getUniqueId())){
            Notifier.error(player,"You need a rank to be able to share! Get "+ChatColor.BLUE+"PRO"+ChatColor.RED+" / "+ChatColor.GREEN+"VIP"+ChatColor.RED+" at mySite.com");
            return true;
        }

        if (myWalls.isSpec(player.getUniqueId())){
            Notifier.error(player,"You need to be in the fight to share!");
            return true;
        }

        Player friend = Bukkit.getPlayer(args[0]);
        if (friend!=null){
            
            if (myWalls.sameTeam(player.getUniqueId(), friend.getUniqueId())){

                int itemInHandSlot = player.getInventory().getHeldItemSlot();
                
                ItemStack sharedItemStack = player.getInventory().getItem(itemInHandSlot);
                
                friend.getInventory().addItem(sharedItemStack);
                player.getInventory().setItem(itemInHandSlot, new ItemStack(Material.AIR));
                Notifier.success(player,"You shared some " + sharedItemStack.getType().name() + " with "+friend.getName());
                Notifier.success(friend,player.getName()+" shared some "+sharedItemStack.getType().name()+" with you!");
                friend.updateInventory();
                player.updateInventory();
                
            }else{
                Notifier.error(sender, "You need to be on the same team to share stuff :(");
            }
        }
        
        return true;
    }

}
