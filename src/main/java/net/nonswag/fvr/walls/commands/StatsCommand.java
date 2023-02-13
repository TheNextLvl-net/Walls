package net.nonswag.fvr.walls.commands;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class StatsCommand implements CommandExecutor {
    private final Walls walls;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Walls.WallsPlayer player;
        if (args.length >= 1) {
            if ((player = walls.getPlayer(Bukkit.getPlayer(args[0]))) == null) return false;
            Notifier.notify(sender, "§7Viewing the stats of §a" + player.getName());
        } else if (sender instanceof Player) player = walls.getPlayer((Player) sender);
        else return false;
        Notifier.notify(sender, "§7Kills§8: §a" + player.getStatsKills() + player.getKills());
        Notifier.notify(sender, "§7Deaths§8: §a" + player.getStatsDeaths() + player.getDeaths());
        Notifier.notify(sender, "§7KD§8: §a" + player.getKD());
        Notifier.notify(sender, "§7Wins§8: §a" + player.getStatsWins());
        return true;
    }
}
