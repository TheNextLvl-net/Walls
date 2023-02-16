package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;


public class StaffChatCommand implements CommandExecutor {

    private final Walls walls;

    public StaffChatCommand(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player) || !walls.getPlayer(((Player) sender).getUniqueId()).getRank().staff()) {
            Notifier.error(sender, "You can't use this command");
            return true;
        }
        String message = String.join(" ", args);
        for (UUID uuid : walls.getStaffList()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || walls.noStaffChat.contains(uuid)) continue;
            player.sendMessage(Walls.STAFFCHATT_PREFIX + ChatColor.AQUA + sender.getName() + ChatColor.WHITE + ": " + message);
        }
        return true;
    }
}
