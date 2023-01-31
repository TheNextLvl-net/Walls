package net.nonswag.fvr.walls.commands;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.PlayerJoinType;
import net.nonswag.fvr.walls.Walls.PlayerState;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.utils.GameStarter;
import net.nonswag.fvr.walls.utils.Notifier;
import net.nonswag.fvr.walls.utils.PlayerVisibility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WallsCmd implements CommandExecutor {
    Walls walls;

    public WallsCmd(Walls tw) {
        walls = tw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player) {
                Notifier.error(sender, "usage: /walls stats | chat | nostaffchat | start | stop | dropwalls | vip | addplayer | pro");
                if (sender.isOp()) {
                    Notifier.error(sender, "usage: dropwalls | players | vip | pro | gm| mgm | admin | update | autostartplayers | peacetimemins | clanbattle | captain | restricted | diamondonly | lobbytrail");
                }
            } else sender.sendMessage("usage: /walls start | stop | dropwalls | vip | addplayer | pro");
        } else if (args[0].equalsIgnoreCase("diamondonly")) {
            if (sender.isOp()) sender.sendMessage("Set to [" + (Walls.diamondONLY = !Walls.diamondONLY) + "]");
            else Notifier.error(sender, "You have no rights to do this");
        } else if (args[0].equalsIgnoreCase("stop")) stop(sender);
        else if (args[0].equalsIgnoreCase("stats")) myStats(sender);
        else if (args[0].equalsIgnoreCase("chat")) chatListener(sender);
        else if (args[0].equalsIgnoreCase("nostaffchat")) noStaffChat(sender);
        else if (args[0].equalsIgnoreCase("start")) startWalls(sender);
        else if (args[0].equalsIgnoreCase("addplayer")) addPlayer(sender, args);
        else if (args[0].equalsIgnoreCase("silence")) silenceComand(sender);
        else if (args[0].equalsIgnoreCase("debug")) switchDebug(sender);
        else if (args[0].equalsIgnoreCase("clan")) setClanName(sender, args);
        else if (args[0].equalsIgnoreCase("dropwalls")) dropWalls(sender);
        else if (args[0].equalsIgnoreCase("players")) showPlayers(sender);
        else if (args[0].equalsIgnoreCase("vip")) toggleVIPStatus(sender, args);
        else if (args[0].equalsIgnoreCase("pro")) togglePROStatus(sender, args);
        else if (args[0].equalsIgnoreCase("gm")) toggleGMStatus(sender, args);
        else if (args[0].equalsIgnoreCase("mgm")) toggleMGMStatus(sender, args);
        else if (args[0].equalsIgnoreCase("admin")) toggleAdminStatus(sender, args);
        else if (args[0].equalsIgnoreCase("update")) forceTagUpdate(sender, args);
        else if (args[0].equalsIgnoreCase("autostartplayers")) setAutoStartPlayers(sender, args);
        else if (args[0].equalsIgnoreCase("peacetimemins")) setPeaceTimeMins(sender, args);
        else if (args[0].equalsIgnoreCase("clanbattle")) toggleClanBattle(sender);
        else if (args[0].equalsIgnoreCase("restricted")) setPlayerJoinRestriction(sender, args);
        else if (args[0].equalsIgnoreCase("lobbytrail")) this.lobbyTrail(sender, args);
        else if (args[0].equalsIgnoreCase("captain")) this.addCaptain(sender, args);
        else if (args[0].equalsIgnoreCase("logPlayer")) this.logPlayer(sender, args);
        else Notifier.error(sender, args[0] + " is not a valid argument");
        return true;
    }

    private void stop(CommandSender sender) {
        if (sender.isOp() || (sender instanceof Player && (walls.isMGM(((Player) sender).getUniqueId())))) {
            if (walls.getGameState() != Walls.GameState.PREGAME && walls.getGameState() != Walls.GameState.FINISHED) {
                walls.setGameState(Walls.GameState.FINISHED);
                Notifier.broadcast("§cThe game was force closed");
                Bukkit.getScheduler().runTaskLater(walls, () -> System.exit(0), 60);
            } else Notifier.error(sender, "The game is not running!");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void myStats(CommandSender sender) {
        WallsPlayer twp = walls.getWallsPlayer(((Player) sender).getUniqueId());
        Notifier.success(sender, "Kills: " + twp.statsKills);
        Notifier.success(sender, "Deaths: " + twp.statsDeaths);
        Notifier.success(sender, "KD: " + (float) twp.statsKills / (float) twp.statsDeaths);
        Notifier.success(sender, "Wins: " + twp.statsWins);
        Notifier.success(sender, "Coins: " + twp.coins);
    }

    private void chatListener(CommandSender sender) {
        if (walls.isStaff(((Player) sender).getUniqueId()) || sender.isOp()) {
            if (walls.staffListSnooper.contains(((Player) sender).getUniqueId())) {
                walls.staffListSnooper.remove((((Player) sender).getUniqueId()));
                Notifier.success(sender, "You are no longer listening to all-chat.");
            } else {
                walls.staffListSnooper.add(((Player) sender).getUniqueId());
                Notifier.success(sender, "You are now listening to all-chat.");
            }
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void noStaffChat(CommandSender sender) {
        if (walls.isStaff(((Player) sender).getUniqueId()) || sender.isOp()) {
            if (walls.noStaffChat.contains(((Player) sender).getUniqueId())) {
                walls.noStaffChat.remove(((Player) sender).getUniqueId());
                Notifier.notify(sender, "You are now receiving staff chat messages again!");
            } else {
                Notifier.notify(sender, "You will no longer receive staff chat messages.");
                walls.noStaffChat.add(((Player) sender).getUniqueId());
            }
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void startWalls(CommandSender sender) {
        if (sender.isOp() || (sender instanceof Player && (walls.isMGM(((Player) sender).getUniqueId())))) {
            if (!walls.starting) {
                Notifier.broadcast("Game starts in " + ChatColor.LIGHT_PURPLE + "30" + ChatColor.WHITE + " seconds!");
                walls.clock.setClock(30, () -> GameStarter.startGame(walls.getAllPlayers(), walls));
                walls.starting = true;
            } else Notifier.error(sender, "The game is already starting!");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void addPlayer(CommandSender sender, String[] args) {
        if (sender.isOp()
                || (sender instanceof Player && walls.isMGM(((Player) sender).getUniqueId()))) {
            if (args.length < 3) {
                Notifier.error(sender, "Command is /walls addplayer <IGN> <teamNumber>");
                return;
            }
            Player player = Bukkit.getPlayer(args[1]);
            if (player != null) {
                int teamNumber;
                try {
                    teamNumber = Integer.parseInt(args[2]);
                } catch (final NumberFormatException e) {
                    Notifier.error(sender, "Try /walls addplayer <IGN> # {1, 2, 3 or 4}");
                    return;
                }
                if (teamNumber < 1 || teamNumber > 4) {
                    Notifier.error(sender, "Invalid team, please use a number between 1 and 4.");
                    return;
                }
                WallsPlayer twp = walls.getWallsPlayer(player.getUniqueId());

                twp.playerState = PlayerState.values()[teamNumber];
                walls.getAllPlayers().put(player.getUniqueId(), twp);
                player.setAllowFlight(false);
                player.getInventory().clear();
                player.teleport(Walls.spawns.get(twp.playerState.ordinal()));
                walls.playerScoreBoard.addPlayerToTeam(player.getUniqueId(), twp.playerState);
                PlayerVisibility.hideAllSpecs(walls, player);
                PlayerVisibility.makeInVisPlayerNowVisible(walls, player);
                player.setHealth(20);
                player.setFoodLevel(20);
                Notifier.success(player, "Gratz! You've been added to " + Walls.teamsNames[twp.playerState.ordinal()]);
                Notifier.success(sender, "Success! " + player.getName() + " been added to " + Walls.teamsNames[twp.playerState.ordinal()]);
            } else sender.sendMessage("§cThis player is not online");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void setClanName(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            try {
                @SuppressWarnings("deprecation")
                String uidOfPlayer = Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
                if (args.length == 3) {
                    if (walls.myDB.setUsersClan(uidOfPlayer, args[2])) {
                        Player player = Bukkit.getPlayerExact(args[1]);
                        if (player != null) {
                            UUID pUID = player.getUniqueId();
                            WallsPlayer twp = walls.getWallsPlayer(pUID);
                            twp.clan = ChatColor.translateAlternateColorCodes('&', args[2]);
                        }
                        sender.sendMessage(ChatColor.GREEN + args[1] + " is now part of [" + ChatColor.translateAlternateColorCodes('&', args[2]) + "] clan!");
                    } else {
                        Notifier.success(sender, "Nope. Something went wrong there :(");
                    }
                } else if (args.length == 2) {
                    if (walls.myDB.setUsersClan(args[1], null)) {
                        Player player = Bukkit.getPlayerExact(args[1]);
                        if (player != null) {
                            UUID pUID = player.getUniqueId();
                            WallsPlayer twp = walls.getWallsPlayer(pUID);
                            twp.clan = null;
                        }
                        sender.sendMessage(ChatColor.GREEN + args[1] + " had their clan removed!");
                    } else {
                        Notifier.success(sender, "Nope. Something went wrong there :(");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "clan <ign> <clanName>");
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Nope. That didn't work: and threw an exception :(");
                e.printStackTrace(System.out);
            }
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void silenceComand(CommandSender sender) {
        if (sender.isOp() || (sender instanceof Player && walls.isMGM(((Player) sender).getUniqueId()))) {
            if (Walls.shhhhh = !Walls.shhhhh) Notifier.broadcast("§cEVERYONE JUST GOT SHHHHH'D!!");
            else Notifier.broadcast("§aYou are Free. To speak. (ish)");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void switchDebug(CommandSender sender) {
        if (sender.isOp()) {
            sender.sendMessage("Done. debug set to " + (Walls.debugMode = !Walls.debugMode));
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void lobbyTrail(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            String options = "flowerpower / diamond / richboi / bodyguard / ez / hungry / blockbawz / random / flashy / sweets / dangerous";
            if (args.length == 2) {
                if (options.contains(args[1].toLowerCase())) {
                    Walls.lobbyTrail = args[1];
                } else {
                    Walls.lobbyTrail = null;
                    sender.sendMessage(ChatColor.RED + "try /walls lobbytrail " + options);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "try /walls lobbytrail " + options);
            }
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void dropWalls(CommandSender sender) {
        if (sender.isOp()) {
            if (walls.getGameState() == Walls.GameState.PEACETIME) {
                walls.dropWalls();
            } else sender.sendMessage("§cNot in peacetime anymore");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void setPlayerJoinRestriction(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            if (args.length < 2) {
                sender.sendMessage("/walls restricted ANYONE, VIP, PRO, LEGENDARY, STAFF");
                return;
            }
            try {
                Walls.playerJoinRestriction = PlayerJoinType.valueOf(args[1]);
            } catch (Exception e) {
                sender.sendMessage("Nope");
            }
            sender.sendMessage("Server restricted to :" + Walls.playerJoinRestriction.toString());
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void addCaptain(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Walls.teamCaptains.add(args[1]);
            sender.sendMessage(args[1] + " added to team captain list");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void showPlayers(CommandSender sender) {
        Notifier.notify(sender, "Getting all the players in game!");
        if (!walls.getAllPlayers().isEmpty()) {
            for (UUID uuid : walls.getAllPlayers().keySet()) {
                sender.sendMessage(Bukkit.getOfflinePlayer(uuid).getName() + " " + walls.getWallsPlayer(uuid).playerState.name());
            }
        } else Notifier.error(sender, "There are no players");
    }

    private void toggleVIPStatus(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            if (args.length > 1 && (player = Bukkit.getPlayer(args[1])) != null) {
                WallsPlayer twp = walls.getWallsPlayer(player.getUniqueId());
                twp.vip = !twp.vip;
                walls.getAllPlayers().put(player.getUniqueId(), twp);
                Notifier.success(sender, "Success. " + player.getName() + "'s vip status changed to " + twp.vip);
            } else Notifier.error(sender, "/walls vip <player>");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void togglePROStatus(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            if (args.length > 1 && (player = Bukkit.getPlayer(args[1])) != null) {
                UUID pUID = Bukkit.getPlayer(args[1]).getUniqueId();
                WallsPlayer twp = walls.getWallsPlayer(pUID);
                twp.pro = !twp.pro;
                walls.myDB.setPro(args[1], (twp.pro) ? 1 : 0);
                walls.getAllPlayers().put(pUID, twp);
                Notifier.success(sender, "Success. " + player.getName() + "'s §9PRO§f status changed to " + twp.pro);
            } else Notifier.error(sender, "/walls pro <player>");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void toggleGMStatus(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            if (args.length > 1 && (player = Bukkit.getPlayer(args[1])) != null) {
                WallsPlayer twp = walls.getWallsPlayer(player.getUniqueId());
                twp.gm = !twp.gm;
                walls.getAllPlayers().put(player.getUniqueId(), twp);
                Notifier.success(sender, "Success. " + player.getName() + "'s GM status changed to " + twp.gm);
            } else Notifier.error(sender, "/walls gm <player>");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void toggleMGMStatus(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            if (args.length > 1 && (player = Bukkit.getPlayer(args[1])) != null) {
                WallsPlayer twp = walls.getWallsPlayer(player.getUniqueId());
                twp.mgm = !twp.mgm;
                walls.getAllPlayers().put(player.getUniqueId(), twp);
                Notifier.success(sender, "Success. " + args[1] + "'s MGM status changed to " + twp.mgm);
            } else Notifier.error(sender, "/walls mgm <player>");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void toggleAdminStatus(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            if (args.length > 1 && (player = Bukkit.getPlayer(args[1])) != null) {
                WallsPlayer twp = walls.getWallsPlayer(player.getUniqueId());
                twp.admin = !twp.admin;
                walls.getAllPlayers().put(player.getUniqueId(), twp);
                Notifier.success(sender, "Success. " + args[1] + "'s ADMIN status changed to " + twp.admin);
            } else Notifier.error(sender, "/walls admin <player>");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void setAutoStartPlayers(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            if (args.length > 1) try {
                Notifier.success(sender, "Autostart set to " + (Walls.preGameAutoStartPlayers = Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                Notifier.error(sender, "/walls autostartplayers <number of players>");
            }
            else Notifier.error(sender, "/walls autostartplayers <number of players>");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void setPeaceTimeMins(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            if (args.length == 2) {
                try {
                    Walls.peaceTimeMins = Integer.parseInt(args[1]);
                    Notifier.success(sender, "Yup. Peace Time Minutes Set to " + Walls.peaceTimeMins);
                } catch (Exception e) {
                    Notifier.error(sender, "/walls peacetimemins <minutes>");
                }
            } else {
                Notifier.error(sender, "/walls peacetimemins <minutes>");
            }
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void toggleClanBattle(CommandSender sender) {
        if (sender.isOp()) {
            Walls.clanBattle = !Walls.clanBattle;
            Notifier.success(sender, "Yup. ClanBattle set to " + Walls.clanBattle);
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void forceTagUpdate(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            if (args.length > 1 && (player = Bukkit.getPlayer(args[1])) != null) {
                walls.myDB.forceLoadPlayer(args[1], player.getUniqueId());
            } else Notifier.error(sender, "/walls update <player>");
        } else Notifier.error(sender, "You have no rights to do this");
    }

    private void logPlayer(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            if (args.length == 2) {
                Walls.logPlayer = args[1];
                Notifier.success(sender, "Now Logging data for: " + Walls.logPlayer);
            } else {
                Notifier.error(sender, "Nope.");
            }
        } else Notifier.error(sender, "You have no rights to do this");
    }
}
