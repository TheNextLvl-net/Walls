package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.utils.Notifier;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;


public class ClanChatCmd implements CommandExecutor {

    Walls walls;

    public ClanChatCmd(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) return true;
        String messageToSend = StringUtils.join(args, " ");
        Walls.WallsPlayer tWP = walls.getPlayer(((Player) sender).getUniqueId());
        if (tWP.clan == null) {
            Notifier.error(sender, "You are not in a clan");
            return true;
        }
        for (UUID u : walls.getPlayers().keySet()) {
            Player player = Bukkit.getPlayer(u);
            if (player == null) continue;
            Walls.WallsPlayer anotherWP = walls.getPlayer(u);
            if ((anotherWP.clan != null && anotherWP.clan.equals(tWP.clan)) || (player.isOp() && walls.staffListSnooper.contains(u))) {
                player.sendMessage(Walls.CLANCHAT_PREFIX.replace("??", ChatColor.translateAlternateColorCodes('&', tWP.clan)) + Walls.teamChatColors[tWP.playerState.ordinal()] + sender.getName() + ChatColor.WHITE + ": " + messageToSend);
            }
        }
        return true;
    }
}
