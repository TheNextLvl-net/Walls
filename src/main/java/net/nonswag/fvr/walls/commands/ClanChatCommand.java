package net.nonswag.fvr.walls.commands;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class ClanChatCommand implements CommandExecutor {
    private final Walls walls;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Walls.WallsPlayer wallsPlayer = walls.getPlayer(((Player) sender).getUniqueId());
        if (wallsPlayer.getClan() == null) {
            Notifier.error(sender, "You are not in a clan");
            return true;
        }
        String message = String.join(" ", args);
        for (UUID uuid : walls.getPlayers().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            Walls.WallsPlayer anotherWP = walls.getPlayer(uuid);
            if ((anotherWP.getClan() != null && anotherWP.getClan().equals(wallsPlayer.getClan())) || (player.isOp() && walls.staffListSnooper.contains(uuid))) {
                player.sendMessage(Walls.CLANCHAT_PREFIX.replace("??", ChatColor.translateAlternateColorCodes('&', wallsPlayer.getClan())) + Walls.teamChatColors[wallsPlayer.getTeam().ordinal()] + sender.getName() + ChatColor.WHITE + ": " + message);
            }
        }
        return true;
    }
}
