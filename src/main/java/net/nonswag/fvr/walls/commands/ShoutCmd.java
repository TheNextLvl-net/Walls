package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.utils.Notifier;
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
    
    private final Walls walls;
    
    public static int NUMBER_OF_VIP_YELLS = 3;
    public static int NUMBER_OF_PRO_YELLS = 5;
    private final Map<UUID, Integer> yells = new HashMap<>();

    public ShoutCmd(Walls walls){
        this.walls =walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (Walls.shhhhh && !sender.isOp()) return true;
        if (!(sender instanceof Player)) {
            sender.sendMessage("Need to be in game for this command :(");
            return true;
        }
        Player player = (Player) sender;
        WallsPlayer wallsPlayer = walls.getPlayer(player.getUniqueId());
        if (this.walls.isSpectator(player) && !wallsPlayer.rank.staff()) {
            Notifier.error(sender, "Need to be in the fight to use this command :-/");
            return true;
        }
        if (!this.yells.containsKey(player.getUniqueId()) && wallsPlayer.rank.vip()) {
            this.yells.put(player.getUniqueId(), 0);
        } else {
            if (!walls.getPlayer(player.getUniqueId()).rank.staff() && !sender.isOp()) {
                if (wallsPlayer.rank.pro()) {
                    int num = this.yells.get(player.getUniqueId());
                    num = num + 1;
                    this.yells.put(player.getUniqueId(), num);
                    if (this.yells.get(player.getUniqueId()) >= NUMBER_OF_PRO_YELLS) {
                        Notifier.error(player, "You have already yelled out " + ShoutCmd.NUMBER_OF_PRO_YELLS + " times this game!");
                        return true;
                    }
                } else if (wallsPlayer.rank.vip()) {
                    int num = this.yells.get(player.getUniqueId());
                    num = num + 1;
                    this.yells.put(player.getUniqueId(), num);
                    if (this.yells.get(player.getUniqueId()) >= NUMBER_OF_VIP_YELLS) {
                        Notifier.error(player, "You have already yelled out " + ShoutCmd.NUMBER_OF_VIP_YELLS + " times this game! ");
                        Notifier.notify(player, ChatColor.BLUE + "PRO " + ChatColor.WHITE + " players get 5 yells :)");
                        return true;
                    }
                } else {
                    Notifier.error(player, "You need a rank to be able to shout! Get " + ChatColor.BLUE + "PRO" + ChatColor.RED + " / " + ChatColor.GREEN + "VIP" + ChatColor.RED + " at " + Walls.DISCORD);
                    return true;
                }
            }
        }
        String clan = "";
        final String arguments = StringUtils.join(args, " ");
        if (wallsPlayer.clan != null)
            clan = ChatColor.DARK_RED + "" + ChatColor.BOLD + ChatColor.translateAlternateColorCodes('&', wallsPlayer.clan) + ChatColor.WHITE + "◊";
        String message = wallsPlayer.rank.display() + clan + "" + Walls.teamChatColors[wallsPlayer.playerState.ordinal()] + player.getDisplayName() + ChatColor.GOLD + " YELLS out " + ChatColor.WHITE + arguments;
        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(message));
        return true;
    }
}
