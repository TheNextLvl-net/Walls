package me.glennEboy.Walls.commands;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.WallsPlayer;
import me.glennEboy.Walls.utils.GameNotifications;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoutCmd implements CommandExecutor{
    
    TheWalls myWalls;
    
    public static int NUMBER_OF_VIP_YELLS = 3;
    public static int NUMBER_OF_PRO_YELLS = 5;
    private final Map<UUID, Integer> yells = new HashMap<>();

    public ShoutCmd(TheWalls tw){
        myWalls=tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (TheWalls.shhhhh && !sender.isOp()){
            return true;
        }

        
        final String message = StringUtils.join(args, " ");
        

        if (!(sender instanceof Player)){
            sender.sendMessage("Need to be in game for this command :(");
            return true;
        }
        if (this.myWalls.isSpec(((Player)sender).getUniqueId()) && !this.myWalls.isStaff(((Player)sender).getUniqueId())){
            GameNotifications.sendPlayerCommandError((Player)sender, "Need to be in the fight to use this command :-/");
            return true;
        }
        
        WallsPlayer twp = myWalls.getWallsPlayer(((Player)sender).getUniqueId());

        Player commandPlayer = (Player)sender;
        if (!this.yells.containsKey(commandPlayer.getUniqueId()) && myWalls.isVIP(commandPlayer.getUniqueId())) {
            this.yells.put(commandPlayer.getUniqueId(), 0);
        }else{
            if (!myWalls.isStaff(commandPlayer.getUniqueId()) && !sender.isOp() && !twp.legendary && !twp.youtuber){
                if (twp.pro){
                    int num = this.yells.get(commandPlayer.getUniqueId());
                    num = num + 1;
                    this.yells.put(commandPlayer.getUniqueId(), num);
                    if (this.yells.get(commandPlayer.getUniqueId()) >= NUMBER_OF_PRO_YELLS) {
                        GameNotifications.sendPlayerCommandError(commandPlayer,"You have already yelled out "+ShoutCmd.NUMBER_OF_PRO_YELLS+" times this game!");
                        return true;
                    }                
                }else if (twp.vip){                
                    int num = this.yells.get(commandPlayer.getUniqueId());
                    num = num + 1;
                    this.yells.put(commandPlayer.getUniqueId(), num);
                    if (this.yells.get(commandPlayer.getUniqueId()) >= NUMBER_OF_VIP_YELLS) {
                        GameNotifications.sendPlayerCommandError(commandPlayer,"You have already yelled out "+ShoutCmd.NUMBER_OF_VIP_YELLS+" times this game! ");
                        GameNotifications.sendPlayerSimpleMessage(commandPlayer, ChatColor.BLUE+"PRO "+ChatColor.WHITE+" players get 5 yells :)");
                        return true;
                    }
                }else{
                    GameNotifications.sendPlayerCommandError(commandPlayer,"You need a rank to be able to shout! Get "+ChatColor.BLUE+"PRO"+ChatColor.RED+" / "+ChatColor.GREEN+"VIP"+ChatColor.RED+" at mySite.com");
                    return true;
                }
            }

        }

        
        String gm = "";
        String mvp = "";
        String dmvp = "";
        String clan = "";
        
        
        if (twp.gm)    gm = ChatColor.AQUA + "[GM]";

        if (twp.mgm) gm = ChatColor.GOLD + "[MGM]";
        
        if (twp.admin) gm = ChatColor.RED + "[ADMIN]";
        
        if (twp.nMVP) mvp = ChatColor.YELLOW + "[MVP]";

        if (twp.dMVP) dmvp = ChatColor.DARK_AQUA + "[MVP]";

        if (twp.clan!=null) clan = ChatColor.DARK_RED + ""+ChatColor.BOLD+ChatColor.translateAlternateColorCodes('&', twp.clan)+ChatColor.WHITE+"◊";

        String the_message = gm + mvp + dmvp + clan + "" + TheWalls.teamChatColors[twp.playerState.ordinal()]+ ((Player)sender).getDisplayName() + ChatColor.GOLD + " YELLS out " + ChatColor.WHITE + message;

        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(the_message));
        return true;
    }




    public static void fakeShout(TheWalls myWalls, UUID playerUID, String message){
        WallsPlayer twp = myWalls.getWallsPlayer(playerUID);
        String gm = "";
        String mvp = "";
        String dmvp = "";
        String clan = "";
        
        
        if (twp.gm)    gm = ChatColor.AQUA + "[GM]";

        if (twp.mgm) gm = ChatColor.GOLD + "[MGM]";
        
        if (twp.admin) gm = ChatColor.RED + "[ADMIN]";
        
        if (twp.nMVP) mvp = ChatColor.YELLOW + "[MVP]";

        if (twp.dMVP) dmvp = ChatColor.DARK_AQUA + "[MVP]";

        if (twp.clan!=null) clan = ChatColor.DARK_RED + ""+ChatColor.BOLD+ChatColor.translateAlternateColorCodes('&', twp.clan)+ChatColor.WHITE+"◊";

        String the_message = gm + mvp + dmvp + clan + "" + TheWalls.teamChatColors[twp.playerState.ordinal()]+ twp.username + ChatColor.GOLD + " YELLS out " + ChatColor.WHITE + message;

        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(the_message));

    }
}
