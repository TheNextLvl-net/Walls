package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class OpChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final String messageToSend = StringUtils.join(args, " ");
        if (!sender.isOp()) {
            Notifier.error(sender, "You have no rights to do this");
            return true;
        }
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (all.isOp()) all.sendMessage(Walls.OPCHAT_PREFIX + "§6" + sender.getName() + "§f: " + messageToSend);
        }
        return true;
    }
}
