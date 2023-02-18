package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpChatCommand implements CommandExecutor {
    private static final String PREFIX = "§7[§cOPCHAT§7] ";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Notifier.error(sender, "You have no rights to do this");
            return true;
        }
        String message = String.join(" ", args);
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (all.isOp()) all.sendMessage(PREFIX + "§6" + sender.getName() + "§f: " + message);
        }
        return true;
    }
}
