package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.GameState;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.utils.Notifier;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCmd implements CommandExecutor {

    private final Walls walls;

    public SpawnCmd(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (walls.getGameState() != GameState.PEACETIME) {
            Notifier.error(sender, "Sorry /spawn is only available during peace time.");
            return true;
        }
        final Player player = (Player) sender;
        if (!walls.isVIP(player.getUniqueId()) && !walls.isStaff(player.getUniqueId()) && !sender.isOp()) {
            Notifier.error(player, "You need a rank to be able to /spawn! Get " + ChatColor.BLUE + "PRO" + ChatColor.RED + " / " + ChatColor.GREEN + "VIP" + ChatColor.RED + " at " +  Walls.DISCORD);
            return true;
        }
        WallsPlayer twp = walls.getWallsPlayer(player.getUniqueId());
        switch (twp.playerState) {
            case RED:
                player.teleport(Walls.team1Spawn);
                break;
            case YELLOW:
                player.teleport(Walls.team2Spawn);
                break;
            case GREEN:
                player.teleport(Walls.team3Spawn);
                break;
            case BLUE:
                player.teleport(Walls.team4Spawn);
                break;
            case SPECTATORS:
                return true;
        }
        Notifier.success(player, "Teleported to spawn!");
        return true;
    }
}
