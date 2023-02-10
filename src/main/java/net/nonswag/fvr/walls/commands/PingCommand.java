package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length > 0 && (target = Bukkit.getPlayer(args[0])) != null) {
            Notifier.success(sender, target.getName() + "'s latency is around " + ((CraftPlayer) target).getHandle().ping + "ms");
        } else if (sender instanceof Player) {
            Notifier.success(sender, "Your latency is around " + ((CraftPlayer) sender).getHandle().ping + "ms");
        } else return false;
        return true;
    }
}
