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


public class StaffChatCmd implements CommandExecutor {

    private final Walls walls;

    public StaffChatCmd(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final String messageToSend = StringUtils.join(args, " ");
        if (!(sender instanceof Player) || !walls.getPlayer(((Player) sender).getUniqueId()).rank.staff()) {
            Notifier.error(sender, "You can't use this command");
            return true;
        }
        for (UUID uuid : walls.getStaffList()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || walls.noStaffChat.contains(uuid)) continue;
            player.sendMessage(Walls.STAFFCHATT_PREFIX + ChatColor.AQUA + sender.getName() + ChatColor.WHITE + ": " + messageToSend);
        }
        return true;
    }
}
