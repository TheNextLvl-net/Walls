package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.kits.FullDiamondKit;
import net.nonswag.fvr.walls.kits.FullIronKit;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FullKitCommand implements CommandExecutor {

    private final Walls walls;

    public FullKitCommand(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp()) {
            if (walls.getGameState().equals(Walls.GameState.PEACETIME) || walls.getGameState().equals(Walls.GameState.FIGHTING)) {
                if (args.length > 0 && args[0].equalsIgnoreCase("diamond")) fullDiamond(this.walls);
                else if (args.length > 0 && args[0].equalsIgnoreCase("iron")) fullIron(this.walls);
                else Notifier.error(sender, "/giveall <diamond | iron>");
            } else Notifier.error(sender, "Too late :(. Kits only available during game time.");
        } else Notifier.error(sender, "You have no rights to do this");
        return true;
    }

    public static void fullIron(Walls walls) {
        Notifier.broadcast("T'Dah! You are now " + ChatColor.WHITE + "STACKED!");
        FullIronKit kit = new FullIronKit(walls);
        Bukkit.getOnlinePlayers().forEach(kit::givePlayerKit);
    }

    public static void fullDiamond(Walls walls) {
        Notifier.broadcast("T'Dah! You are now " + ChatColor.AQUA + "STACKED!");
        FullDiamondKit kit = new FullDiamondKit(walls);
        Bukkit.getOnlinePlayers().forEach(kit::givePlayerKit);
    }
}
