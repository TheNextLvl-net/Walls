package net.nonswag.fvr.walls.commands;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class StaffChatCommand implements CommandExecutor {
    private final Walls walls;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player && !walls.getPlayer(((Player) sender).getUniqueId()).getRank().staff()) {
            Notifier.error(sender, "You can't use this command");
            return true;
        }
        Notifier.staff(walls, sender, String.join(" ", args));
        return true;
    }
}
