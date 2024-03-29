package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;


public class ClanCommand implements CommandExecutor {

    private final Walls walls;

    public ClanCommand(Walls walls) {
        this.walls = walls;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player) {
                Notifier.error(sender, "usage: /clan list | invite | accept | kick | leave | rename | create | disband | leader");
                if (sender.isOp()) Notifier.error(sender, "usage: /clan list | invite | accept | kick | create ");
            } else Notifier.error(sender, "usage: /clan invite | accept | kick | create ");
            return true;
        }
        if (args[0].equalsIgnoreCase("invite")) invite(sender, args);
        else if (args[0].equalsIgnoreCase("kick")) kick(sender, args);
        else if (args[0].equalsIgnoreCase("accept")) accept(sender);
        else if (args[0].equalsIgnoreCase("leave")) leave(sender);
        else if (args[0].equalsIgnoreCase("list")) list(sender);
        else if (args[0].equalsIgnoreCase("rename")) rename(sender, args);
        else if (args[0].equalsIgnoreCase("disband")) disband(sender, args);
        else if (args[0].equalsIgnoreCase("leader")) changeLeader(sender, args);
        else if (args[0].equalsIgnoreCase("create")) create(sender, args);
        else Notifier.error(sender, args[0] + " is not a valid argument");
        return true;
    }

    private void invite(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            WallsPlayer player = walls.getPlayer((Player) sender);
            if (player.isClanLeader()) {
                Player invitee = Bukkit.getPlayer(args[1]);
                if (invitee != null) {
                    Notifier.notify(invitee, player.getName() + " invited you to join their clan: " + player.getClan());
                    Notifier.notify(invitee, "/clan accept to join ");
                    walls.clanInvites.put(invitee.getUniqueId(), player.getClan());
                    Notifier.notify(sender, "Invited " + invitee.getName() + " to join the clan");
                } else Notifier.error(sender, "Player not found / online.. They need to be :-/");
            } else Notifier.error(sender, "Only the clan leader can invite people");
        } else Notifier.error(sender, "This is a player command");
    }

    private void list(CommandSender sender) {
        if (sender instanceof Player) {
            WallsPlayer player = walls.getPlayer((Player) sender);
            if (player.getClan() != null) {
                List<String> members = walls.database.listClanMembers(player.getClan());
                if (!members.isEmpty()) Notifier.success(sender, String.join("§7, §a", members));
                else Notifier.error(sender, "The clan has no members");
            } else Notifier.error(sender, "You are not in a clan");
        } else Notifier.error(sender, "This is a player command");
    }

    private void rename(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            WallsPlayer player = walls.getPlayer((Player) sender);
            if (args.length == 3 && (sender.isOp() || player.getRank().mgm())) {
                String oldName = args[1];
                String newName = args[2];
                oldName = stripSpecialClanCharacters(oldName);
                if (newName.length() > 16) {
                    Notifier.error(sender, "Clan name is too long :-/");
                    return;
                }
                if (walls.database.staffRenameClan(oldName, newName)) {
                    Notifier.success(sender, "Clan name will change to : " + ChatColor.translateAlternateColorCodes('&', newName) + " in the next game.");
                    Notifier.success(sender, ChatColor.GRAY + "Inappropriate names will = ban :-/ choose carefully.");
                } else {
                    Notifier.error(sender, "Clan name NOT changed - already exists.. try another name :-/");
                }
            } else if (args.length == 2) {
                if (player.isClanLeader()) {
                    String newName = args[1];
                    newName = stripSpecialClanCharacters(newName);
                    if (newName.length() > 16) {
                        Notifier.error(sender, "Clan name is too long :-/");
                        return;
                    }
                    if (walls.database.setClanName(player.getClan(), newName, ClanCommand.stripAllClanCharacters(newName))) {
                        Notifier.success(sender, "Clan name will change to : " + newName + " in the next game.");
                        renameClan(player.getClan(), newName);
                        this.setClanName((Player) sender, newName);
                    } else {
                        Notifier.error(sender, "Clan name NOT changed - already exists.. try another name :-/");
                    }
                } else Notifier.error(sender, "Only the clan leader can rename the clan :)");
            } else Notifier.error(sender, "/clan rename <new name>");
        } else Notifier.error(sender, "This is a player command");
    }

    private String stripSpecialClanCharacters(String clanName) {
        StringBuilder finalString = new StringBuilder();
        for (int i = 0; i < clanName.length(); i++) {
            int ASCIIValue = clanName.charAt(i);
            if ((ASCIIValue > 47 && ASCIIValue < 58) || (ASCIIValue > 64 && ASCIIValue < 91) || (ASCIIValue > 96 && ASCIIValue < 123) || (ASCIIValue == 38)) {
                finalString.append(clanName.charAt(i));
            }
        }
        return finalString.toString().replace("&k", "").replace("&l", "").replace("&m", "").replace("&n", "").replace("&o", "").replace("&r", "")
                .replace("&K", "").replace("&L", "").replace("&M", "").replace("&N", "").replace("&O", "").replace("&R", "");
    }

    public static String stripAllClanCharacters(String clanName) {
        return clanName.replace("&k", "").replace("&l", "").replace("&m", "").replace("&n", "").replace("&o", "").replace("&r", "")
                .replace("&1", "").replace("&2", "").replace("&3", "").replace("&4", "").replace("&5", "").replace("&6", "")
                .replace("&7", "").replace("&8", "").replace("&9", "").replace("&0", "")
                .replace("&a", "").replace("&b", "").replace("&c", "").replace("&d", "").replace("&e", "").replace("&f", "")
                .replace("&A", "").replace("&B", "").replace("&C", "").replace("&D", "").replace("&E", "").replace("&F", "")
                .replace("&K", "").replace("&L", "").replace("&M", "").replace("&N", "").replace("&O", "").replace("&R", "");
    }

    private void kick(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            WallsPlayer wallsPlayer = walls.getPlayer(((Player) sender).getUniqueId());
            if (wallsPlayer.isClanLeader() && args.length > 0) {
                String target = args[1];
                if (walls.database.kickClanMember(target, wallsPlayer.getClan())) {
                    Player player = Bukkit.getPlayer(target);
                    if (sender.equals(player)) {
                        Notifier.error(sender, "Can't kick yourself. Try /clan disband if you're leader. Or /clan leave.");
                        return;
                    }
                    if (player != null) walls.getPlayer(player).setClan(null);
                    Notifier.success(sender, target + " has been kicked from " + ChatColor.translateAlternateColorCodes('&', wallsPlayer.getClan()));
                } else {
                    Notifier.error(sender, "Player was not in the clan / something went wrong :-/");
                }
            } else {
                Notifier.error(sender, "Only the clan leader can kick players :)");
            }
        } else Notifier.error(sender, "This is a player command");
    }

    private void accept(CommandSender sender) {
        if (walls.clanInvites.containsKey(((Player) sender).getUniqueId())) {
            if (walls.getPlayer(((Player) sender).getUniqueId()).isClanLeader()) {
                Notifier.error(sender, "Nope. A leader can't just join another clan.. think of YOUR clan members!! (or /clan disband)");
                return;
            }
            String clanName = walls.clanInvites.get(((Player) sender).getUniqueId());
            this.setClanName((Player) sender, clanName);
            for (UUID u : walls.getPlayers().keySet()) {
                Player player = Bukkit.getPlayer(u);
                if (player != null) {
                    WallsPlayer anotherWP = walls.getPlayer(u);
                    if ((anotherWP.getClan() != null && anotherWP.getClan().equals(clanName)) || (player.isOp() && walls.staffListSnooper.contains(u))) {
                        player.sendMessage(Walls.CLANCHAT_PREFIX.replace("??", clanName) + sender.getName() + ChatColor.WHITE + " joined " + ChatColor.translateAlternateColorCodes('&', clanName));
                    }
                }
            }
        } else Notifier.error(sender, "Aww man you don't have any invites to accept :(");
    }

    private void disband(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            WallsPlayer wallsPlayer = walls.getPlayer(((Player) sender).getUniqueId());
            if (args.length == 2 && (sender.isOp() || wallsPlayer.getRank().mgm())) {
                if (walls.database.disbandClanByName(args[1])) {
                    Notifier.success(sender, args[1] + " blew up - all gone! :(");
                } else Notifier.error(sender, "Nope. Something went wrong there :(");
            } else if (args.length == 1) {
                if (wallsPlayer.isClanLeader()) {
                    if (walls.database.disbandClan(wallsPlayer.getClan())) {
                        Notifier.success(sender, "You're clan blew up - all gone! :(");
                        wallsPlayer.setClan(null);
                        wallsPlayer.setClanLeader(false);
                    } else {
                        Notifier.error(sender, "Nope. Something went wrong there :(");
                    }
                } else {
                    Notifier.error(sender, "Nope. Only a leader can disband!! (you can /clan leave)");
                }
            }
        } else Notifier.error(sender, "This is a player command");
    }


    private void changeLeader(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            WallsPlayer player = walls.getPlayer(((Player) sender).getUniqueId());
            if (player.isClanLeader()) {
                if (args.length == 2) {
                    Player newLeader = Bukkit.getPlayer(args[1]);
                    if (newLeader == null) {
                        Notifier.error(sender, "Nope. New leader must be online");
                        return;
                    }
                    WallsPlayer target = walls.getPlayer(newLeader.getUniqueId());
                    if (!player.getClan().equalsIgnoreCase(target.getClan())) {
                        Notifier.error(sender, "Nope. New leader must be in your clan");
                        return;
                    }
                    if (walls.database.setNewClanLeader(((Player) sender).getUniqueId(), newLeader.getUniqueId())) {
                        Notifier.success(sender, "You're no longer clan leader! :(");
                        Notifier.success(newLeader, "You're now clan leader! =)");
                        player.setClanLeader(false);
                        walls.getPlayer(newLeader.getUniqueId()).setClanLeader(true);
                    } else {
                        Notifier.error(sender, "Nope. Couldn't find clan leader O_o. Let staff know pls :)");
                    }
                } else {
                    Notifier.error(sender, "Nope. Try /clan leader <IGN of NewLeader>  (New leader must be online)");
                }
            } else {
                Notifier.error(sender, "Nope. Only a leader can disband!! (you can /clan leave)");
            }
        } else Notifier.error(sender, "This is a player command");
    }

    private void leave(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = ((Player) sender);
            if (walls.getPlayer(player).isClanLeader()) {
                Notifier.success(sender, "Nope. A leader can't just leave.. think of the members!! (or /clan disband)");
            } else if (walls.database.kickClanMember(sender.getName(), walls.getPlayer(((Player) sender).getUniqueId()).getClan())) {
                UUID pUID = player.getUniqueId();
                WallsPlayer twp = walls.getPlayer(pUID);
                twp.setClan(null);
                Notifier.success(player, "You're clanless! :(");
            } else {
                Notifier.success(sender, "Nope. Something went wrong there :(");
            }
        } else Notifier.error(sender, "This is a player command");
    }

    private void create(CommandSender sender, String[] args) {
        if (sender.isOp() && args.length == 3) {
            Player newLeader = Bukkit.getPlayer(args[2]);
            if (walls.getPlayer(newLeader.getUniqueId()).isClanLeader()) {
                Notifier.error(sender, "Player is a leader already.. they need to /clan disband first!");
            } else {
                String newName = stripSpecialClanCharacters(args[1]);
                if (walls.database.createClan(newName, newLeader.getUniqueId(), ClanCommand.stripAllClanCharacters(newName))) {
                    Notifier.success(sender, args[2] + " is now leader of " + newName);
                    Notifier.success(newLeader, "You are now leader of " + newName);
                    this.setClanName(newLeader, newName);
                    walls.getPlayer(newLeader.getUniqueId()).setClanLeader(true);
                } else {
                    Notifier.error(sender, "Clan NOT created - it already exists.. :-/");
                }

            }
        } else if (args.length == 2 && sender instanceof Player) {

            if (!walls.getPlayer(((Player) sender).getUniqueId()).getRank().vip()) {
                Notifier.error(sender, "You need to be VIP and above to create clans.");
                return;
            }

            Player newLeader = (Player) sender;
            WallsPlayer player = walls.getPlayer(newLeader.getUniqueId());
            if (!player.isClanLeader()) {
                String newName = stripSpecialClanCharacters(args[1]);
                if (walls.database.createClan(newName, newLeader.getUniqueId(), ClanCommand.stripAllClanCharacters(newName))) {
                    Notifier.success(newLeader, "You are now leader of " + newName);
                    this.setClanName(newLeader, newName);
                    player.setClanLeader(true);
                } else Notifier.error(sender, "Clan NOT created - it already exists.. :-/");
            } else {
                Notifier.error(sender, "Um... You're a leader already.. you need to /clan disband first!");
            }
        } else Notifier.error(sender, "/clan create <name>");
    }

    private void setClanName(Player owner, String name) {
        if (walls.database.setClanName(owner.getUniqueId(), name)) {
            WallsPlayer player = walls.getPlayer(owner);
            walls.clanInvites.remove(owner.getUniqueId());
            if (player == null) return;
            player.setClan(name);
            Notifier.success(owner, "You're now part of the clan " + ChatColor.translateAlternateColorCodes('&', name) + " !");
        } else Notifier.success(owner, "Nope. Something went wrong there :(");
    }

    private void renameClan(String oldClan, String newClan) {
        for (UUID uuid : walls.getPlayers().keySet()) {
            WallsPlayer player = walls.getPlayer(uuid);
            if (player == null || player.getClan() == null) continue;
            if (player.getClan().equals(oldClan)) player.setClan(newClan);
        }
    }
}
