package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SurfaceCmd implements CommandExecutor {

    private final Walls walls;

    public SurfaceCmd(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (walls.getGameState() != GameState.PEACETIME) {
            Notifier.error(sender, "Sorry /surface is only available during peace time.");
            return true;
        }
        final Player player = (Player) sender;
        if (walls.getPlayer(player.getUniqueId()).rank.vip() || player.isOp()) {
            final Location loc = new Location(Bukkit.getWorld(Walls.levelName), player.getLocation().getBlockX(),
                    Bukkit.getWorld(Walls.levelName).getHighestBlockYAt(player.getLocation().getBlockX(),
                            player.getLocation().getBlockZ()), player.getLocation().getBlockZ());
            if (loc.getY() > Walls.buildHeight) {
                player.sendMessage(ChatColor.RED + "Surface is too high! Can't teleport here :(");
                return true;
            }
            if (loc.getBlock().getType() == Material.LAVA || loc.getBlock().getType() == Material.STATIONARY_LAVA) {
                player.sendMessage(ChatColor.RED + "Its a little hot up there.. I don't think you can swim in lava :-/ try surfacing somewhere else!");
                return true;
            }
            player.teleport(loc);
            player.sendMessage(ChatColor.GREEN + "Teleported to the surface!");
        }
        return true;
    }
}
