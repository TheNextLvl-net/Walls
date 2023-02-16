package net.nonswag.fvr.walls.commands;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.DatabaseUtil;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FindCommand implements TabExecutor {
    private final Walls walls;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) Bukkit.getScheduler().runTaskAsynchronously(walls, () -> {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                DatabaseUtil.Profile profile = walls.database.lookup(args[0]);
                if (profile != null) {
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy (hh:mm:ss a)", Locale.ENGLISH);
                    String seen = format.format(new Date(profile.getLastSeen()));
                    Notifier.notify(sender, "§7" + profile.getName() + " was last seen on the §b" + seen);
                } else Notifier.error(sender, args[0] + " is unknown to us");
            } else Notifier.notify(sender, "§7" + target.getName() + " is§a online");
        });
        else Notifier.error(sender, command.getUsage());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) return new ArrayList<>();
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList());
    }
}
