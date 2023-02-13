package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.api.Notifier;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final Walls walls;

    public SpawnCommand(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (walls.getGameState() != GameState.PEACETIME) {
            Notifier.error(sender, "Sorry /spawn is only available during peace time.");
            return true;
        }
        final Player player = (Player) sender;
        if (!walls.getPlayer(player.getUniqueId()).getRank().vip() && !sender.isOp()) {
            Notifier.error(player, "You need a rank to be able to /spawn! Get " + ChatColor.BLUE + "PRO" + ChatColor.RED + " / " + ChatColor.GREEN + "VIP" + ChatColor.RED + " at " +  Walls.DISCORD);
            return true;
        }
        WallsPlayer twp = walls.getPlayer(player.getUniqueId());
        switch (twp.getPlayerState()) {
            case RED:
                player.teleport(walls.getTeam1Spawn());
                break;
            case YELLOW:
                player.teleport(walls.getTeam2Spawn());
                break;
            case GREEN:
                player.teleport(walls.getTeam3Spawn());
                break;
            case BLUE:
                player.teleport(walls.getTeam4Spawn());
                break;
            case SPECTATORS:
                return true;
        }
        Notifier.success(player, "Teleported to spawn!");
        return true;
    }
}
