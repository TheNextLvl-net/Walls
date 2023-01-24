package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.kits.FullChainKit;
import me.glennEboy.Walls.kits.FullDiamondKit;
import me.glennEboy.Walls.kits.FullGoldKit;
import me.glennEboy.Walls.kits.FullIronKit;
import me.glennEboy.Walls.utils.GameNotifications;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FullDiamondCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public FullDiamondCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


        if (args.length<1){
            return true;
        }
        if (!sender.isOp()){
            return true;
        }
        if (args[0].equals("diamond")){   
            fullDiamond(this.myWalls);
            return true;
        }else if (args[0].equals("gold")){
            fullGold(this.myWalls);
            return true;            
        }else if (args[0].equals("iron")){
            fullIron(this.myWalls);            return true;            
        }else if (args[0].equals("chain")){
            fullChain(this.myWalls);
            return true;            
        }

        return true;
    }
    


    public static void fullIron(TheWalls myWalls){
        GameNotifications.broadcastMessage("T'Dah! You are now "+ChatColor.WHITE+"STACKED!");
        FullIronKit fdk = new FullIronKit(myWalls);
        for (Player p : myWalls.getServer().getOnlinePlayers()){
            fdk.givePlayerKit(p);
        }

    }

    public static void fullChain(TheWalls myWalls){
        GameNotifications.broadcastMessage("T'Dah! You are now "+ChatColor.DARK_GRAY+"STACKED!");
        FullChainKit fdk = new FullChainKit(myWalls);
        for (Player p : myWalls.getServer().getOnlinePlayers()){
            fdk.givePlayerKit(p);
        }

    }

    
    public static void fullGold(TheWalls myWalls){
        GameNotifications.broadcastMessage("T'Dah! You are now "+ChatColor.GOLD+"STACKED!");
        FullGoldKit fdk = new FullGoldKit(myWalls);
        for (Player p : myWalls.getServer().getOnlinePlayers()){
            fdk.givePlayerKit(p);
        }

    }
    
    public static void fullDiamond(TheWalls myWalls){
        GameNotifications.broadcastMessage("T'Dah! You are now "+ChatColor.AQUA+"STACKED!");
        FullDiamondKit fdk = new FullDiamondKit(myWalls);
        for (Player p : myWalls.getServer().getOnlinePlayers()){
            fdk.givePlayerKit(p);
        }
    }
}
