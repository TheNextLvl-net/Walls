package net.nonswag.fvr.walls.commands;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.PlayerJoinType;
import net.nonswag.fvr.walls.Walls.Team;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import net.nonswag.fvr.walls.api.GameStarter;
import net.nonswag.fvr.walls.api.Notifier;
import net.nonswag.fvr.walls.api.PlayerVisibility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static net.nonswag.fvr.walls.api.Notifier.*;

@RequiredArgsConstructor
public class WallsCommand implements CommandExecutor {
    private final Walls walls;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player) error(sender, "usage: /walls voteStart | voteIron | voteDiamond");
            if (sender.isOp()) error(sender, "usage: /walls drop | rank | autoStartPlayers | clanRename | " +
                    "clanBattle | captain | restricted | diamondWalls | ironWalls | fixdb | stop | chat | " +
                    "noStaffChat | start | addPlayer | silence | captain");
        } else if (args[0].equalsIgnoreCase("voteDiamond")) voteDiamond(sender);
        else if (args[0].equalsIgnoreCase("voteIron")) voteIron(sender);
        else if (args[0].equalsIgnoreCase("diamondWalls")) diamondWalls(sender);
        else if (args[0].equalsIgnoreCase("ironWalls")) ironWalls(sender);
        else if (args[0].equalsIgnoreCase("stop")) stop(sender);
        else if (args[0].equalsIgnoreCase("fixdb")) fixdb(sender);
        else if (args[0].equalsIgnoreCase("voteStart")) voteStart(sender);
        else if (args[0].equalsIgnoreCase("chat")) chatListener(sender);
        else if (args[0].equalsIgnoreCase("noStaffChat")) noStaffChat(sender);
        else if (args[0].equalsIgnoreCase("start")) startWalls(sender);
        else if (args[0].equalsIgnoreCase("addPlayer")) addPlayer(sender, args);
        else if (args[0].equalsIgnoreCase("silence")) silenceComand(sender);
        else if (args[0].equalsIgnoreCase("clanRename")) setClanName(sender, args);
        else if (args[0].equalsIgnoreCase("drop")) drop(sender);
        else if (args[0].equalsIgnoreCase("rank")) setRank(sender, args);
        else if (args[0].equalsIgnoreCase("autoStartPlayers")) setAutoStartPlayers(sender, args);
        else if (args[0].equalsIgnoreCase("clanBattle")) toggleClanBattle(sender);
        else if (args[0].equalsIgnoreCase("restricted")) setPlayerJoinRestriction(sender, args);
        else if (args[0].equalsIgnoreCase("captain")) addCaptain(sender, args);
        else error(sender, args[0] + " is not a valid argument");
        return true;
    }

    public static final Set<Player> DIAMOND_VOTES = Collections.newSetFromMap(new WeakHashMap<>());
    public static final Set<Player> IRON_VOTES = Collections.newSetFromMap(new WeakHashMap<>());

    private void vote(CommandSender sender, Set<Player> set, String type) {
        if (sender instanceof Player) {
            if (set.equals(IRON_VOTES)) DIAMOND_VOTES.remove(sender);
            if (set.equals(DIAMOND_VOTES)) IRON_VOTES.remove(sender);
            if (!set.contains(sender)) {
                set.add((Player) sender);
                Notifier.broadcast(sender.getName() + " voted for " + type + " walls §8(§7" + set.size() + "§8)");
            } else Notifier.error(sender, "You already voted for " + type + " walls");
        } else error(sender, "This is a player command");
    }

    private void voteDiamond(CommandSender sender) {
        vote(sender, DIAMOND_VOTES, "diamond");
    }

    private void voteIron(CommandSender sender) {
        vote(sender, IRON_VOTES, "iron");
    }

    private static void diamondWalls(CommandSender sender) {
        if (sender.isOp()) broadcast("Set diamondwalls to " + (Walls.diamondWalls = !Walls.diamondWalls));
        else error(sender, "You have no rights to do this");
    }

    private static void ironWalls(CommandSender sender) {
        if (sender.isOp()) broadcast("Set ironwalls to " + (Walls.ironWalls = !Walls.ironWalls));
        else error(sender, "You have no rights to do this");
    }

    public static boolean FIX_DB = false;

    private void fixdb(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            if (FIX_DB = !FIX_DB) {
                error(sender, "The database structure will be recreated");
                error(sender, "To apply the changes you have to §4restart the server");
                error(sender, "Every data on the database will be §4gone forever");
                error(sender, "If you don't want this to happen §4execute this command again");
            } else success(sender, "The database will §6not§a be deleted");
        } else error(sender, "For security measure this can only be executed by the console");
    }

    public static final List<UUID> VOTES = new ArrayList<>();

    private void voteStart(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (walls.starting) error(player, "The game is already starting");
            else if (!walls.getGameState().equals(Walls.GameState.PREGAME)) {
                error(player, "This is only possible during the lobby phase");
            } else if (!VOTES.contains(player.getUniqueId())) {
                success(player, "You voted for a faster start");
                VOTES.add(player.getUniqueId());
                autoStart();
            } else error(player, "You already voted for a faster start");
        } else error(sender, "This is a player command");
    }

    private void autoStart() {
        if (VOTES.size() > Walls.preGameAutoStartPlayers / 2) {
            broadcast("Game starts in " + ChatColor.LIGHT_PURPLE + Walls.gameStartSeconds + ChatColor.WHITE + " seconds!");
            walls.clock.setClock(Walls.gameStartSeconds, () -> GameStarter.startGame(walls.getPlayers(), walls));
            walls.starting = true;
        } else {
            int players = Walls.preGameAutoStartPlayers / 2 - VOTES.size();
            broadcast(players + " more vote" + (players != 1 ? "s are" : " is") + " needed §8(§7/walls voteStart§8)");
        }
    }

    private void stop(CommandSender sender) {
        if (sender.isOp() || (sender instanceof Player && (walls.getPlayer(((Player) sender).getUniqueId()).getRank().staff()))) {
            if (walls.getGameState() != Walls.GameState.PREGAME && walls.getGameState() != Walls.GameState.FINISHED) {
                walls.setGameState(Walls.GameState.FINISHED);
                broadcast("§cThe game was force closed");
                Bukkit.getScheduler().runTaskLater(walls, () -> System.exit(0), 60);
            } else error(sender, "The game is not running!");
        } else error(sender, "You have no rights to do this");
    }

    private void chatListener(CommandSender sender) {
        if (sender instanceof Player) {
            if (walls.getPlayer(((Player) sender).getUniqueId()).getRank().staff() || sender.isOp()) {
                if (walls.staffListSnooper.contains(((Player) sender).getUniqueId())) {
                    walls.staffListSnooper.remove((((Player) sender).getUniqueId()));
                    success(sender, "You are no longer listening to all-chat.");
                } else {
                    walls.staffListSnooper.add(((Player) sender).getUniqueId());
                    success(sender, "You are now listening to all-chat.");
                }
            } else error(sender, "You have no rights to do this");
        } else error(sender, "This is a player command");
    }

    private void noStaffChat(CommandSender sender) {
        if (sender instanceof Player) {
            if (walls.getPlayer(((Player) sender).getUniqueId()).getRank().staff() || sender.isOp()) {
                if (walls.noStaffChat.contains(((Player) sender).getUniqueId())) {
                    walls.noStaffChat.remove(((Player) sender).getUniqueId());
                    Notifier.notify(sender, "You are now receiving staff chat messages again!");
                } else {
                    Notifier.notify(sender, "You will no longer receive staff chat messages.");
                    walls.noStaffChat.add(((Player) sender).getUniqueId());
                }
            } else error(sender, "You have no rights to do this");
        } else error(sender, "This is a player command");
    }

    private void startWalls(CommandSender sender) {
        if (sender.isOp() || (sender instanceof Player && (walls.getPlayer(((Player) sender).getUniqueId())).getRank().mgm())) {
            if (!walls.starting) {
                broadcast("Game starts in " + ChatColor.LIGHT_PURPLE + Walls.gameStartSeconds + ChatColor.WHITE + " seconds!");
                walls.clock.setClock(Walls.gameStartSeconds, () -> GameStarter.startGame(walls.getPlayers(), walls));
                walls.starting = true;
            } else error(sender, "The game is already starting!");
        } else error(sender, "You have no rights to do this");
    }

    private void addPlayer(CommandSender sender, String[] args) {
        if (sender.isOp() || (sender instanceof Player && walls.getPlayer(((Player) sender).getUniqueId()).getRank().mgm())) {
            if (args.length < 3) {
                error(sender, "Command is /walls addplayer <IGN> <teamNumber>");
                return;
            }
            Player player = Bukkit.getPlayer(args[1]);
            if (player != null) {
                int teamNumber;
                try {
                    teamNumber = Integer.parseInt(args[2]);
                } catch (final NumberFormatException e) {
                    error(sender, "Try /walls addplayer <IGN> # {1, 2, 3 or 4}");
                    return;
                }
                if (teamNumber < 1 || teamNumber > 4) {
                    error(sender, "Invalid team, please use a number between 1 and 4.");
                    return;
                }
                WallsPlayer wallsPlayer = walls.getPlayer(player);
                wallsPlayer.setTeam(Team.values()[teamNumber]);
                player.setAllowFlight(false);
                player.getInventory().clear();
                player.teleport(walls.getSpawns().get(wallsPlayer.getTeam().ordinal()));
                walls.getPlayerScoreBoard().addPlayerToTeam(player, wallsPlayer.getTeam());
                PlayerVisibility.hideAllSpecs(walls, player);
                PlayerVisibility.makeInVisPlayerNowVisible(player);
                player.setHealth(20);
                player.setFoodLevel(20);
                success(player, "Gratz! You've been added to " + Walls.teamNames[wallsPlayer.getTeam().ordinal()]);
                success(sender, "Success! " + player.getName() + " been added to " + Walls.teamNames[wallsPlayer.getTeam().ordinal()]);
            } else sender.sendMessage("§cThis player is not online");
        } else error(sender, "You have no rights to do this");
    }

    private void setClanName(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            if (args.length <= 1) {
                error(sender, "/walls clanrename <player> <clan name>");
                return;
            }
            Player player = Bukkit.getPlayerExact(args[1]);
            if (args.length >= 3 && player != null) {
                if (walls.database.setClanName(player.getUniqueId(), args[2])) {
                    UUID pUID = player.getUniqueId();
                    WallsPlayer twp = walls.getPlayer(pUID);
                    twp.setClan(ChatColor.translateAlternateColorCodes('&', args[2]));
                    sender.sendMessage(ChatColor.GREEN + args[1] + " is now part of [" + ChatColor.translateAlternateColorCodes('&', args[2]) + "] clan!");
                } else error(sender, "Database error");
            } else if (args.length == 2 && player != null) {
                if (walls.database.setClanName(player.getUniqueId(), null)) {
                    WallsPlayer wallsPlayer = walls.getPlayer(player);
                    wallsPlayer.setClan(null);
                    success(sender, player.getName() + " had their clan removed!");
                } else error(sender, "Database error");
            } else {
                error(sender, "/walls clanrename <player> <clan name>");
            }
        } else error(sender, "You have no rights to do this");
    }

    private void silenceComand(CommandSender sender) {
        if (sender.isOp() || (sender instanceof Player && walls.getPlayer(((Player) sender).getUniqueId()).getRank().mgm())) {
            if (Walls.shhhhh = !Walls.shhhhh) broadcast("§cEVERYONE JUST GOT SHHHHH'D!!");
            else broadcast("§aYou are Free. To speak. (ish)");
        } else error(sender, "You have no rights to do this");
    }

    private void drop(CommandSender sender) {
        if (sender.isOp()) {
            if (walls.getGameState() == Walls.GameState.PEACETIME) walls.dropWalls();
            else error(sender, "Not in peacetime anymore");
        } else error(sender, "You have no rights to do this");
    }

    private void setPlayerJoinRestriction(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            if (args.length < 2) {
                sender.sendMessage("/walls restricted ANYONE, VIP, PRO, STAFF");
                return;
            }
            try {
                Walls.playerJoinRestriction = PlayerJoinType.valueOf(args[1]);
            } catch (Exception e) {
                sender.sendMessage("Nope");
            }
            sender.sendMessage("Server restricted to :" + Walls.playerJoinRestriction.toString());
        } else error(sender, "You have no rights to do this");
    }

    private void addCaptain(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            if (args.length >= 2 && (player = Bukkit.getPlayer(args[1])) != null) {
                Walls.teamCaptains.add(player.getUniqueId());
                success(sender, player.getName() + " added to team captain list");
            } else success(sender, "/walls captain <player>");
        } else error(sender, "You have no rights to do this");
    }

    private void setRank(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            Player player;
            Walls.Rank rank;
            if (args.length > 1 && (player = Bukkit.getPlayer(args[1])) != null) {
                if (args.length > 2 && (rank = Walls.Rank.parse(args[2])) != null) {
                    walls.getPlayer(player).setRank(rank);
                    walls.database.setRank(player.getUniqueId(), rank.ordinal());
                    success(sender, player.getName() + "'s new rank is now " + rank.name().toLowerCase());
                } else error(sender, "/walls rank " + player.getName() + " <none,vip,pro,gm,mgm,admin>");
            } else error(sender, "/walls rank <player> <none,vip,pro,gm,mgm,admin>");
        } else error(sender, "You have no rights to do this");
    }

    private void setAutoStartPlayers(CommandSender sender, String[] args) {
        if (sender.isOp()) {
            if (args.length > 1) try {
                success(sender, "Autostart set to " + (Walls.preGameAutoStartPlayers = Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                error(sender, "/walls autostartplayers <number of players>");
            }
            else error(sender, "/walls autostartplayers <number of players>");
        } else error(sender, "You have no rights to do this");
    }

    private void toggleClanBattle(CommandSender sender) {
        if (sender.isOp()) {
            Walls.clanBattle = !Walls.clanBattle;
            success(sender, "Yup. ClanBattle set to " + Walls.clanBattle);
        } else error(sender, "You have no rights to do this");
    }
}
