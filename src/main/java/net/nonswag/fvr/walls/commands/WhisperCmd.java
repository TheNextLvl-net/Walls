package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class WhisperCmd implements CommandExecutor {

    private final Walls walls;

    public WhisperCmd(Walls walls) {
        this.walls = walls;
    }

    private final String whisperFormat = ChatColor.GOLD + "" + ChatColor.BOLD + "{" + ChatColor.RESET + "PM: " + ChatColor.GRAY + "<sender>" + ChatColor.RESET + "" + ChatColor.GOLD + "}" + ChatColor.RESET + " <message>";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (Walls.shhhhh && !sender.isOp()) {
            Notifier.error(sender, "You can't chat right now");
            return true;
        }
        if (args.length < 2) {
            Notifier.error(sender, "/" + commandLabel + " <player> <message>");
            return false;
        } else {
            Player p = Bukkit.getPlayer(args[0]);
            if (p != null) {
                final StringBuilder whisper = new StringBuilder();
                for (int x = 1; x < args.length; x++) {
                    whisper.append(args[x]).append(" ");
                }
                whisper.setLength(whisper.length() - 1);
                String messageToSend = this.whisperFormat.replace("<sender>", sender.getName()).replace("<message>", whisper);
                p.sendMessage(messageToSend);
                sender.sendMessage(" SENT to " + p.getName() + ": " + whisper);
                walls.getLogger().log(Level.INFO, ChatColor.GRAY + sender.getName() + " whispers to " + p.getName() + " --> " + whisper);
                walls.whispers.put(p.getUniqueId(), ((Player) sender).getUniqueId());
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found online :(");
            }
            return true;
        }
    }
}
