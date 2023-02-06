package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.utils.Notifier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCmd implements CommandExecutor {

    private final Walls walls;

    public TPCmd(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (args.length < 1) {
            Notifier.error(sender, "That didn't work.. try /tp <IGN>");
            return true;
        }

        if (sender instanceof Player && !walls.players.get(((Player) sender).getUniqueId()).rank.vip()) {
            Notifier.error(sender, "Sorry only VIP and above can use this command.");
            return true;
        }

        Player friend = Bukkit.getServer().getPlayer(args[0]);

        if (friend == null) {
            return true;
        }
        switch (walls.getGameState()) {
            case PEACETIME:
                if (!(sender instanceof Player)) {
                    Notifier.notify(sender, "This is a player command");
                    return true;
                }
                if (walls.isSpec(((Player) sender).getUniqueId())) {
                    ((Player) sender).teleport(friend.getLocation().add(0, +5, 0));
                    sender.sendMessage(ChatColor.GREEN + "You have been teleported to " + args[0]);
                    break;
                }

                if (walls.sameTeam(((Player) sender).getUniqueId(), friend.getUniqueId())) {
                    ((Player) sender).teleport(friend);
                    sender.sendMessage(ChatColor.GREEN + "You have been teleported to " + args[0]);
                    friend.sendMessage(sender.getName() + ChatColor.GREEN + " teleported to you.");
                } else {
                    Notifier.error(sender, "Sorry you can only TP to people on your team.");
                }
                break;
            case FIGHTING:
            case FINISHED:
                if (sender instanceof Player) {
                    if (walls.isSpec(((Player) sender).getUniqueId())) {
                        ((Player) sender).teleport(friend.getLocation().add(0, +5, 0));
                        sender.sendMessage(ChatColor.GREEN + "You have been teleported to " + args[0]);
                    } else Notifier.notify(sender, "You are not a spectator");
                } else Notifier.notify(sender, "This is a player command");
                break;
            default:
                break;
        }
        return true;
    }
}
