package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.utils.ClanUtils;
import net.nonswag.fvr.walls.utils.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;


public class ClanCmd implements CommandExecutor {

    private final Walls walls;

    public ClanCmd(Walls walls) {
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
            WallsPlayer twp = walls.getWallsPlayer(((Player) sender).getUniqueId());
            if (twp.clanLeader) {
                Player invitee = Bukkit.getPlayer(args[1]);
                if (invitee != null) {
                    Notifier.notify(invitee, twp.username + " invited you to join their clan: " + twp.clan);
                    Notifier.notify(invitee, "/clan accept to join ");
                    walls.clanInvites.put(invitee.getUniqueId(), twp.clan);
                    Notifier.notify(sender, twp.username + ": Invite sent to " + invitee.getName() + " to join " + twp.clan);
                } else {
                    Notifier.error(sender, "Player not found / online.. They need to be :-/");
                }
            }
        } else {
            sender.sendMessage("Need to be a player in game to make that work :(");
        }
    }

    private void list(CommandSender sender) {
        if (sender instanceof Player) {
            WallsPlayer twp = walls.getWallsPlayer(((Player) sender).getUniqueId());
            if (twp.clan != null) {
                List<String> members = walls.myDB.listClanMembers(twp.clan);
                if (!members.isEmpty()) Notifier.success(sender, String.join("§7, §a", members));
                else Notifier.error(sender, "The clan has no members");
            } else Notifier.error(sender, "You are not in a clan");
        } else {
            Notifier.error(sender, "Need to be a player in game to make that work :(");
        }
    }

    private void rename(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            WallsPlayer twp = walls.getWallsPlayer(((Player) sender).getUniqueId());
            Player player = ((Player) sender);
            if (args.length == 3 && (sender.isOp() || walls.getWallsPlayer(player.getUniqueId()).rank.mgm())) {
                String oldName = args[1];
                String newName = args[2];
                oldName = stripSpecialClanCharacters(oldName);
                if (newName.length() > 16) {
                    Notifier.error(sender, "Clan name is too long :-/");
                    return;
                }
                if (walls.myDB.staffRenameClan(oldName, newName)) {
                    Bukkit.getLogger().info("CLAN NAME CHANGE from " + ChatColor.translateAlternateColorCodes('&', twp.clan) + " to " + ChatColor.translateAlternateColorCodes('&', newName) + " by player (" + twp.username + ")");
                    Notifier.success(sender, "Clan name will change to : " + ChatColor.translateAlternateColorCodes('&', newName) + " in the next game.");
                    Notifier.success(sender, ChatColor.GRAY + "Inappropriate names will = ban :-/ choose carefully.");
                } else {
                    Notifier.error(sender, "Clan name NOT changed - already exists.. try another name :-/");
                }
            } else if (args.length == 2) {
                if (twp.clanLeader) {
                    String newName = args[1];
                    newName = stripSpecialClanCharacters(newName);
                    if (newName.length() > 16) {
                        Notifier.error(sender, "Clan name is too long :-/");
                        return;
                    }
                    if (walls.myDB.renameClan(twp.clan, newName, ClanCmd.stripAllClanCharacters(newName))) {
                        Bukkit.getLogger().info("CLAN NAME CHANGE from " + ChatColor.translateAlternateColorCodes('&', twp.clan) + " to " + ChatColor.translateAlternateColorCodes('&', newName) + " by player (" + twp.username + ")");
                        Notifier.success(sender, "Clan name will change to : " + newName + " in the next game.");
                        Notifier.success(sender, ChatColor.GRAY + "Inappropriate names will = ban :-/ choose carefully.");
                        ClanUtils.changeAllOnlineClanNames(walls, twp.clan, newName);
                        this.setClanName(((Player) sender).getUniqueId(), newName);
                    } else {
                        Notifier.error(sender, "Clan name NOT changed - already exists.. try another name :-/");
                    }
                } else {
                    Notifier.error(sender, "Only the clan leader can rename the clan :)");
                }
            } else {
                Notifier.error(sender, "try /clan rename NewName");
            }
        } else {
            sender.sendMessage("Need to be a player in game to make that work :(");
        }
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
            WallsPlayer twp = walls.getWallsPlayer(((Player) sender).getUniqueId());
            if (twp.clanLeader && args.length > 0) {
                String personToKick = args[1];
                if (sender.getName().equals(personToKick)) {
                    Notifier.error(sender, "Can't kick yourself. Try /clan disband if you're leader. Or /clan leave.");
                    return;
                }
                if (walls.myDB.kickClanMember(personToKick, twp.clan)) {
                    Player player = Bukkit.getPlayer(personToKick);
                    if (player != null) {
                        UUID pUID = player.getUniqueId();
                        WallsPlayer wp = walls.getWallsPlayer(pUID);
                        wp.clan = null;
                    }
                    Notifier.success(sender, personToKick + " has been kicked from " + ChatColor.translateAlternateColorCodes('&', twp.clan));
                } else {
                    Notifier.error(sender, "Player was not in the clan / something went wrong :-/");
                }
            } else {
                Notifier.error(sender, "Only the clan leader can kick players :)");
            }
        } else {
            sender.sendMessage("Need to be a player in game to make that work :(");
        }
    }


    private void accept(CommandSender sender) {
        if (walls.clanInvites.containsKey(((Player) sender).getUniqueId())) {
            if (walls.getWallsPlayer(((Player) sender).getUniqueId()).clanLeader) {
                Notifier.error(sender, "Nope. A leader can't just join another clan.. think of YOUR clan members!! (or /clan disband)");
                return;
            }
            String clanName = walls.clanInvites.get(((Player) sender).getUniqueId());
            this.setClanName(((Player) sender).getUniqueId(), clanName);
            for (UUID u : walls.getAllPlayers().keySet()) {
                if (Bukkit.getPlayer(u) != null) {
                    WallsPlayer anotherWP = walls.getWallsPlayer(u);
                    if ((anotherWP.clan != null && anotherWP.clan.equals(clanName)) || (Bukkit.getPlayer(u).isOp() && walls.staffListSnooper.contains(u))) {
                        Bukkit.getPlayer(u).sendMessage(Walls.CLANCHAT_PREFIX.replace("??", clanName) + sender.getName() + ChatColor.WHITE + " joined " + ChatColor.translateAlternateColorCodes('&', clanName));
                    }
                }
            }
        } else {
            Notifier.error(sender, "Aww man you don't have any invites to accept :(");
        }
    }

    private void disband(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            try {
                Player player = ((Player) sender);
                if (args.length == 2 && (sender.isOp() || walls.getWallsPlayer(player.getUniqueId()).rank.mgm())) {
                    if (walls.myDB.disbandClanByName(args[1])) {
                        Notifier.success(player, args[1] + " blew up - all gone! :(");
                        Notifier.staff(walls, args[1] + " was disbanded by " + player.getName());
                    } else {
                        Notifier.error(sender, "Nope. Something went wrong there :(");
                    }
                } else if (args.length == 1) {
                    if (walls.getWallsPlayer(player.getUniqueId()).clanLeader) {
                        if (walls.myDB.disbandClan(walls.getWallsPlayer(player.getUniqueId()).clan)) {
                            Notifier.success(player, "You're clan blew up - all gone! :(");
                            Notifier.staff(walls, ChatColor.translateAlternateColorCodes('&', walls.getWallsPlayer(player.getUniqueId()).clan) + " was disbanded.");
                            walls.getWallsPlayer(player.getUniqueId()).clan = "";
                            walls.getWallsPlayer(player.getUniqueId()).clanLeader = false;
                        } else {
                            Notifier.error(sender, "Nope. Something went wrong there :(");
                        }
                    } else {
                        Notifier.error(sender, "Nope. Only a leader can disband!! (you can /clan leave)");
                    }
                }
            } catch (Exception e) {
                Notifier.error(sender, "Nope. Something went wrong there :(");
                e.printStackTrace();
            }
        }
    }


    private void changeLeader(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender);
            if (walls.getWallsPlayer(player.getUniqueId()).clanLeader) {
                if (args.length == 2) {
                    Player newLeader = Bukkit.getPlayer(args[1]);
                    if (newLeader == null) {
                        Notifier.error(sender, "Nope. New leader must be online");
                        return;
                    }
                    WallsPlayer twpOldLeader = walls.getWallsPlayer(player.getUniqueId());
                    WallsPlayer twpNewLeader = walls.getWallsPlayer(newLeader.getUniqueId());
                    if (!twpOldLeader.clan.equalsIgnoreCase(twpNewLeader.clan)) {
                        Bukkit.getLogger().info("Old Leader Clan - " + twpOldLeader.clan + " & new - " + twpNewLeader.clan);
                        Notifier.error(sender, "Nope. New leader must be in your clan");
                        return;
                    }
                    if (walls.myDB.setNewClanLeader(player.getUniqueId(), newLeader.getName(), newLeader.getUniqueId())) {
                        Notifier.success(player, "You're no longer clan leader! :(");
                        Notifier.success(newLeader, "You're now clan leader! =)");
                        walls.getWallsPlayer(player.getUniqueId()).clanLeader = false;
                        walls.getWallsPlayer(newLeader.getUniqueId()).clanLeader = true;
                    } else {
                        Notifier.error(sender, "Nope. Couldn't find clan leader O_o. Let staff know pls :)");
                    }
                } else {
                    Notifier.error(sender, "Nope. Try /clan leader <IGN of NewLeader>  (New leader must be online)");
                }
            } else {
                Notifier.error(sender, "Nope. Only a leader can disband!! (you can /clan leave)");
            }
        }
    }

    private void leave(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = ((Player) sender);
            if (walls.getWallsPlayer(player.getUniqueId()).clanLeader) {
                Notifier.success(sender, "Nope. A leader can't just leave.. think of the members!! (or /clan disband)");
            } else if (walls.myDB.kickClanMember(sender.getName(), walls.getWallsPlayer(((Player) sender).getUniqueId()).clan)) {
                UUID pUID = player.getUniqueId();
                WallsPlayer twp = walls.getWallsPlayer(pUID);
                twp.clan = null;
                Notifier.success(player, "You're clanless! :(");
            } else {
                Notifier.success(sender, "Nope. Something went wrong there :(");
            }
        }
    }

    private void create(CommandSender sender, String[] args) {
        if (sender.isOp() && args.length == 3) {
            Player newLeader = Bukkit.getPlayer(args[2]);

            if (walls.getWallsPlayer(newLeader.getUniqueId()).clanLeader) {
                Notifier.error(sender, "Player is a leader already.. they need to /clan disband first!");

            } else {
                String newName = stripSpecialClanCharacters(args[1]);


                if (walls.myDB.createClan(newName, args[2], newLeader.getUniqueId().toString(), ClanCmd.stripAllClanCharacters(newName))) {
                    Notifier.success(sender, args[2] + " is now leader of " + newName);
                    Notifier.success(newLeader, "You are now leader of " + newName);
                    this.setClanName(newLeader.getUniqueId(), newName);
                    walls.getWallsPlayer(newLeader.getUniqueId()).clanLeader = true;
                } else {
                    Notifier.error(sender, "Clan NOT created - it already exists.. :-/");
                }

            }
        } else if (args.length == 2 && sender instanceof Player) {

            if (!this.walls.players.get(((Player) sender).getUniqueId()).rank.vip()) {
                Notifier.error(sender, "You need to be VIP and above to create clans.");
                return;
            }

            Player newLeader = (Player) sender;

            if (walls.getWallsPlayer(newLeader.getUniqueId()).clanLeader) {
                Notifier.error(sender, "Um... You're a leader already.. you need to /clan disband first!");

            } else {
                String newName = stripSpecialClanCharacters(args[1]);

                if (walls.myDB.createClan(newName, newLeader.getName(), newLeader.getUniqueId().toString(), ClanCmd.stripAllClanCharacters(newName))) {
                    Notifier.staff(this.walls, newLeader.getName() + " is now leader of " + newName);
                    Notifier.success(newLeader, "You are now leader of " + newName);
                    this.setClanName(newLeader.getUniqueId(), newName);
                    walls.getWallsPlayer(newLeader.getUniqueId()).clanLeader = true;
                } else {
                    Notifier.error(sender, "Clan NOT created - it already exists.. :-/");
                }

            }
        } else {
            Notifier.error(sender, "/clan create <clanName>");
        }
    }

    private void setClanName(UUID uidOfPlayer, String clanName) {
        String playerUID = uidOfPlayer.toString();
        Player player = Bukkit.getPlayer(uidOfPlayer);
        if (walls.myDB.setUsersClan(playerUID, clanName)) {
            if (player != null) {
                UUID pUID = player.getUniqueId();
                WallsPlayer twp = walls.getWallsPlayer(pUID);
                twp.clan = clanName;
                Notifier.success(player, "You're now part of the clan [" + ChatColor.translateAlternateColorCodes('&', clanName) + "] !");
            }
            walls.clanInvites.remove(uidOfPlayer);
        } else Notifier.success(player, "Nope. Something went wrong there :(");
    }
}
