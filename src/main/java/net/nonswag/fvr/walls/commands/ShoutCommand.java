package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoutCommand implements CommandExecutor{
    
    private final Walls walls;
    
    public static int NUMBER_OF_VIP_YELLS = 3;
    public static int NUMBER_OF_PRO_YELLS = 5;
    private final Map<UUID, Integer> yells = new HashMap<>();

    public ShoutCommand(Walls walls){
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
        if (this.walls.isSpectator(player) && !wallsPlayer.getRank().staff()) {
            Notifier.error(sender, "Need to be in the fight to use this command :-/");
            return true;
        }
        if (!this.yells.containsKey(player.getUniqueId()) && wallsPlayer.getRank().vip()) {
            this.yells.put(player.getUniqueId(), 0);
        } else {
            if (!walls.getPlayer(player.getUniqueId()).getRank().staff() && !sender.isOp()) {
                if (wallsPlayer.getRank().pro()) {
                    int num = this.yells.get(player.getUniqueId());
                    num = num + 1;
                    this.yells.put(player.getUniqueId(), num);
                    if (this.yells.get(player.getUniqueId()) >= NUMBER_OF_PRO_YELLS) {
                        Notifier.error(player, "You have already yelled out " + ShoutCommand.NUMBER_OF_PRO_YELLS + " times this game!");
                        return true;
                    }
                } else if (wallsPlayer.getRank().vip()) {
                    int num = this.yells.get(player.getUniqueId());
                    num = num + 1;
                    this.yells.put(player.getUniqueId(), num);
                    if (this.yells.get(player.getUniqueId()) >= NUMBER_OF_VIP_YELLS) {
                        Notifier.error(player, "You have already yelled out " + ShoutCommand.NUMBER_OF_VIP_YELLS + " times this game! ");
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
        String arguments = String.join(" ", args);
        if (wallsPlayer.getClan() != null)
            clan = ChatColor.DARK_RED + "" + ChatColor.BOLD + ChatColor.translateAlternateColorCodes('&', wallsPlayer.getClan()) + ChatColor.WHITE + "◊";
        String message = wallsPlayer.getRank().display() + clan + "" + Walls.teamChatColors[wallsPlayer.getPlayerState().ordinal()] + player.getDisplayName() + ChatColor.GOLD + " YELLS out " + ChatColor.WHITE + arguments;
        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(message));
        return true;
    }
}
