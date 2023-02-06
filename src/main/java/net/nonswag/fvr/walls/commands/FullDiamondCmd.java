package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.kits.FullDiamondKit;
import net.nonswag.fvr.walls.kits.FullIronKit;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FullDiamondCmd implements CommandExecutor {

    Walls myWalls;

    public FullDiamondCmd(Walls tw) {
        myWalls = tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {


        if (args.length < 1) {
            return true;
        }
        if (!sender.isOp()) {
            return true;
        }
        switch (args[0]) {
            case "diamond":
                fullDiamond(this.myWalls);
                return true;
            case "iron":
                fullIron(this.myWalls);
                return true;
        }

        return true;
    }

    public static void fullIron(Walls myWalls) {
        Notifier.broadcast("T'Dah! You are now " + ChatColor.WHITE + "STACKED!");
        FullIronKit fdk = new FullIronKit(myWalls);
        for (Player p : myWalls.getServer().getOnlinePlayers()) {
            fdk.givePlayerKit(p);
        }

    }

    public static void fullDiamond(Walls myWalls) {
        Notifier.broadcast("T'Dah! You are now " + ChatColor.AQUA + "STACKED!");
        FullDiamondKit fdk = new FullDiamondKit(myWalls);
        for (Player p : myWalls.getServer().getOnlinePlayers()) {
            fdk.givePlayerKit(p);
        }
    }
}
