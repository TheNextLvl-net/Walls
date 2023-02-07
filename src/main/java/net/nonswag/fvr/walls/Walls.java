package net.nonswag.fvr.walls;

import com.sk89q.worldedit.WorldEdit;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.nonswag.core.api.file.formats.PropertiesFile;
import net.nonswag.core.api.math.MathUtil;
import net.nonswag.core.api.sql.Database;
import net.nonswag.fvr.walls.commands.*;
import net.nonswag.fvr.walls.kits.SpecPlayerKit;
import net.nonswag.fvr.walls.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

public class Walls extends JavaPlugin implements Listener {

    public enum Rank {
        NONE, VIP, PRO, GM, MGM, ADMIN;

        public boolean vip() {
            return equals(VIP) || pro();
        }

        public boolean pro() {
            return equals(PRO) || staff();
        }

        public boolean staff() {
            return equals(GM) || mgm();
        }

        public boolean mgm() {
            return equals(MGM) || admin();
        }

        public boolean admin() {
            return equals(ADMIN);
        }

        public String display() {
            if (admin()) return "§c[ADMIN]";
            if (mgm()) return "§6[MGM]";
            if (staff()) return "§b[GM]";
            if (pro()) return "§9[PRO]";
            if (vip()) return "§a[VIP]";
            return "";
        }

        public static Rank parse(String string) {
            try {
                return valueOf(string.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public static class WallsPlayer {
        public String clan = null;
        public String username = null;
        public String uid = null;
        public String paidKits = null;
        public Rank rank = Rank.NONE;
        public boolean clanLeader = false;
        public boolean compassPointsToEnemy = true;
        public int statsKills = 0;
        public int statsDeaths = 0;
        public int statsWins = 0;
        public int kills = 0;
        public int deaths = 0;
        public int minutes = 0;
        public int wins = 0;
        public PlayerState playerState = PlayerState.SPECTATORS;
    }

    public static final String STAFFCHATT_PREFIX = ChatColor.RED + "[" + ChatColor.AQUA + "StaffChat" + ChatColor.RED + "] ";
    public static final String CLANCHAT_PREFIX = ChatColor.RED + "[" + ChatColor.DARK_AQUA + "??" + ChatColor.RED + "] ";
    public static final String OPCHAT_PREFIX = ChatColor.RED + "[" + ChatColor.RED + "OPCHAT" + ChatColor.RED + "] ";
    public static String[] teamsNames = {ChatColor.LIGHT_PURPLE + "Specs", ChatColor.RED + "Team 1", ChatColor.YELLOW + "Team 2", ChatColor.GREEN + "Team 3", ChatColor.BLUE + "Team 4"};
    public static ChatColor[] teamChatColors = {ChatColor.LIGHT_PURPLE, ChatColor.RED, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.BLUE};

    private final Map<UUID, Integer> mutedPlayers = new HashMap<>();
    private final Map<UUID, WallsPlayer> players = new HashMap<>();

    private final Map<UUID, Long> inCombat = new HashMap<>();

    public final Map<UUID, UUID> whispers = new HashMap<>();
    public final Map<UUID, String> clanInvites = new HashMap<>();

    public final List<UUID> staffListSnooper = new ArrayList<>();
    public final List<UUID> noStaffChat = new ArrayList<>();

    public static final List<String> teamCaptains = new ArrayList<>();

    public static String logPlayer = null;
    public static final String levelName;
    private static String nextMap;
    public static final String DISCORD = "https://discord.gg/vpAgZxQ";

    static {
        PropertiesFile properties = new PropertiesFile("server.properties");
        levelName = properties.getRoot().getString("level-name");
    }


    public static Location gameSpawn;
    public static Location team1Spawn, team2Spawn, team3Spawn, team4Spawn;
    public static Location team1Corner, team2Corner, team3Corner, team4Corner;
    public static List<Location> spawns = new ArrayList<>();
    public static List<Location> corners = new ArrayList<>();


    private SpecPlayerKit spectatorKit;
    public PlayerScoreBoard playerScoreBoard;

    private final Map<Location, PlayerState> boom = new HashMap<>();
    public Map<UUID, String> assassinTargets = new HashMap<>();
    public Map<UUID, Integer> leprechaunOwners = new HashMap<>();
    public Map<UUID, Integer> thorOwners = new HashMap<>();


    public enum GameState {PREGAME, PEACETIME, FIGHTING, FINISHED}

    public enum PlayerState {SPECTATORS, RED, YELLOW, GREEN, BLUE}

    public enum PlayerJoinType {ANYONE, VIP, PRO, STAFF}

    private final List<EntityType> allowedMobs = new ArrayList<>();
    private final List<Selection> selections = new ArrayList<>();
    public Clock clock;
    private GameState gameState = GameState.PREGAME;
    private final Map<UUID, PlayerInventory> inventory = new HashMap<>();
    private KitCmd myKitCmd;
    public static boolean debugMode = false;
    public static boolean UHC = false;
    public static int peaceTimeMins = 15;
    public static int preGameAutoStartPlayers = 4;
    public static int preGameAutoStartSeconds = 30;
    public static PlayerJoinType playerJoinRestriction = PlayerJoinType.ANYONE;
    public static boolean fullDiamond = false;
    public static boolean diamondONLY = false;
    public static boolean ironONLY = false;
    public static String lobbyTrail = "";
    public static boolean clanBattle = false;
    public static boolean tournamentMode = false;
    public static boolean allowPickTeams = false;
    public static boolean shhhhh = false;
    public static String advert = null;
    public static List<String> clans = new ArrayList<>();
    public static int combatLogTimeInSeconds = 7;
    private final int restartTimer = 15;
    public boolean starting = false;
    private final Set<ProtectedContainer> protectedContainers = new HashSet<>();
    private final List<UUID> quitters = new ArrayList<>();
    private final Map<UUID, BukkitTask> quitterTasks = new HashMap<>();
    public static final Random random = new Random();
    private int relogTime = 60;
    private int teams = 4;
    private int winningTeam = 0;

    public DatabaseUtil myDB;

    private boolean foodDisabled = false;
    private int foodTime = 0;
    public static final int buildHeight = 180;
    public static final int liquidBuildHeight = 170;

    public HashMap<UUID, ArrayList<Entity>> droppedItems = new HashMap<>();
    public ArrayList<Entity> allitems = new ArrayList<>();

    @Override
    public void onEnable() {
        try {
            WorldEdit.getInstance().getConfiguration().navigationWand = -1;
            this.getConfig().save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getWorlds().forEach(world -> {
            world.setGameRuleValue("doFireTick", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setThundering(false);
            world.setStorm(false);
            world.setTime(1000);
        });

        Walls.debugMode = this.getConfig().getBoolean("debugMode");
        Walls.UHC = this.getConfig().getBoolean("UHCMode");
        Walls.peaceTimeMins = this.getConfig().getInt("peaceTimeMins");
        Walls.preGameAutoStartPlayers = this.getConfig().getInt("preGameAutoStartPlayers");
        Walls.preGameAutoStartSeconds = this.getConfig().getInt("preGameAutoStartSeconds");
        this.relogTime = this.getConfig().getInt("relogTime");
        Walls.fullDiamond = this.getConfig().getBoolean("fullDiamond");
        Walls.diamondONLY = this.getConfig().getBoolean("diamondONLY");
        Walls.ironONLY = this.getConfig().getBoolean("ironONLY");
        Walls.lobbyTrail = this.getConfig().getString("lobbyTrail");
        Walls.clanBattle = this.getConfig().getBoolean("clanBattle");
        Walls.tournamentMode = this.getConfig().getBoolean("tournamentMode");
        Walls.playerJoinRestriction = PlayerJoinType.valueOf(this.getConfig().getString("playerJoinRestriction"));
        Walls.allowPickTeams = this.getConfig().getBoolean("allowPickTeams");
        Walls.combatLogTimeInSeconds = this.getConfig().getInt("combatLogTimeInSeconds");

        Walls.advert = this.getConfig().getString("wallsAdvert");

        if (Walls.clanBattle || Walls.tournamentMode) {
            Walls.teamsNames[1] = Walls.teamChatColors[1] + this.getConfig().getString("clan-1");
            Walls.teamsNames[2] = Walls.teamChatColors[2] + this.getConfig().getString("clan-2");
            Walls.teamsNames[3] = Walls.teamChatColors[3] + this.getConfig().getString("clan-3");
            Walls.teamsNames[4] = Walls.teamChatColors[4] + this.getConfig().getString("clan-4");
            List<String> clans = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                String string = this.getConfig().getString("clan-" + i);
                Walls.teamsNames[i] = Walls.teamChatColors[i] + string;
                clans.add(string);
            }
            Walls.clans = clans;
        }

        World world = Bukkit.getWorlds().get(0);
        Walls.gameSpawn = new Location(world, -2, world.getHighestBlockYAt(-2, 130), 130);

        Walls.team1Spawn = new Location(world, -149, 65, -17);
        Walls.team2Spawn = new Location(world, 144, 65, -17);
        Walls.team3Spawn = new Location(world, 144, 65, 276);
        Walls.team4Spawn = new Location(world, -149, 65, 276);
        spawns.add(Walls.gameSpawn);
        spawns.add(Walls.team1Spawn);
        spawns.add(Walls.team2Spawn);
        spawns.add(Walls.team3Spawn);
        spawns.add(Walls.team4Spawn);

        Walls.team1Corner = new Location(world, -23, 62, 108);
        Walls.team2Corner = new Location(world, 19, 62, 108);
        Walls.team3Corner = new Location(world, 19, 62, 151);
        Walls.team4Corner = new Location(world, -23, 62, 151);
        corners.add(Walls.gameSpawn);
        corners.add(Walls.team1Corner);
        corners.add(Walls.team2Corner);
        corners.add(Walls.team3Corner);
        corners.add(Walls.team4Corner);

        this.allowedMobs.add(EntityType.CAVE_SPIDER);
        this.allowedMobs.add(EntityType.ZOMBIE);
        this.allowedMobs.add(EntityType.SKELETON);
        this.allowedMobs.add(EntityType.SILVERFISH);
        this.allowedMobs.add(EntityType.SPIDER);
        this.allowedMobs.add(EntityType.CHICKEN);
        this.allowedMobs.add(EntityType.SHEEP);
        this.allowedMobs.add(EntityType.PIG);
        this.allowedMobs.add(EntityType.CHICKEN);
        this.allowedMobs.add(EntityType.COW);
        this.allowedMobs.add(EntityType.GHAST);
        this.allowedMobs.add(EntityType.BLAZE);

        spectatorKit = new SpecPlayerKit(this);

        playerScoreBoard = new PlayerScoreBoard(this);

        DeathMessages.initializeDeathMessages();

        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("ping").setExecutor(new PingCommand());
        getCommand("shout").setExecutor(new ShoutCmd(this));
        getCommand("tp").setExecutor(new TPCmd(this));
        getCommand("walls").setExecutor(new WallsCmd(this));
        getCommand("giveall").setExecutor(new FullKitCommand(this));
        getCommand("spawn").setExecutor(new SpawnCmd(this));
        getCommand("corner").setExecutor(new CornerCmd(this));
        getCommand("surface").setExecutor(new SurfaceCmd(this));
        getCommand("tell").setExecutor(new WhisperCmd(this));
        getCommand("w").setExecutor(new WhisperCmd(this));
        getCommand("msg").setExecutor(new WhisperCmd(this));
        getCommand("r").setExecutor(new WhisperReplyCmd(this));
        myKitCmd = new KitCmd(this);
        getCommand("kit").setExecutor(myKitCmd);
        getCommand("sc").setExecutor(new StaffChatCmd(this));
        getCommand("oc").setExecutor(new OpChatCmd());
        getCommand("cc").setExecutor(new ClanChatCmd(this));
        getCommand("share").setExecutor(new ShareCmd(this));
        getCommand("clan").setExecutor(new ClanCmd(this));

        defineArena();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.clock = new Clock(this);

        this.myDB = new DatabaseUtil(this);


        if (Walls.advert != null && !Walls.advert.equals("")) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> Bukkit.getLogger().info(advert), 20L * 40, 20L * 240);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (WallsCmd.FIX_DB) fixDatabase();
            changeLevelName(nextMap);
            freeUpSpace();
        }));
        nextMap = selectNextMap();
    }

    @Override
    public void onDisable() {
        this.clock.interrupt();
        Database.disconnect();
    }

    private String selectNextMap() {
        try {
            File worlds = Bukkit.getWorldContainer();
            File[] files = new File(worlds, "Templates").listFiles();
            if (files == null) throw new FileNotFoundException("Found no worlds to pick from");
            File file = randomFile(files);
            copy(file, new File(worlds, file.getName()));
            return file.getName();
        } catch (IOException e) {
            System.err.println("Failed to select a random map, You have to do it manually");
            e.printStackTrace();
            return null;
        }
    }

    private void copy(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.mkdirs()) return;
            File[] files = source.listFiles();
            if (files == null) return;
            for (File file : files) copy(file, new File(destination, file.getName()));
        } else Files.copy(source.toPath(), destination.toPath());
    }

    private void freeUpSpace() {
        try {
            delete(new File(Bukkit.getWorldContainer(), levelName));
        } catch (IOException e) {
            System.err.println("Failed to delete the old map, You have to do it manually");
            e.printStackTrace();
        }
    }

    private void fixDatabase() {
        try {
            Database.getConnection().executeUpdate("DROP TABLE `paidkits`");
            Database.getConnection().executeUpdate("DROP TABLE `accounts`");
            Database.getConnection().executeUpdate("DROP TABLE `guilds`");
            Database.getConnection().executeUpdate("DROP TABLE `player_stats`");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private File randomFile(File[] files) {
        File file;
        int cap = 0;
        do {
            file = files[MathUtil.randomInteger(0, files.length - 1)];
        } while (levelName.equals(file.getName()) && cap++ < 5);
        return file;
    }

    private void changeLevelName(String name) {
        PropertiesFile properties = new PropertiesFile("server.properties");
        properties.getRoot().set("level-name", name);
        properties.save();
    }

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) for (File all : files) delete(all);
        }
        Files.deleteIfExists(file.toPath());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerListPing(ServerListPingEvent event) {
        switch (getGameState()) {
            case PREGAME:
                event.setMotd("§aWaiting for more players to start\n§6Click to join");
                break;
            case PEACETIME:
                event.setMotd("§9The players are preparing to fight\n§bConnect to spectate");
                break;
            case FIGHTING:
                event.setMotd("§cThe walls have fallen\n§4Everyone is fighting");
                break;
            case FINISHED:
                event.setMotd("§3The game has ended\n§bRestarting...");
                break;
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if ((event.getSpawnReason() == SpawnReason.NATURAL) && !this.allowedMobs.contains(event.getEntityType())) {
            event.setCancelled(true);
        } else if (event.getSpawnReason() == SpawnReason.BUILD_WITHER) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(Walls.gameSpawn);
        if (getPlayer(event.getPlayer().getUniqueId()).rank.vip()) {
            this.spectatorKit.givePlayerKit(event.getPlayer());
            PlayerVisibility.makeSpecInvisible(this, event.getPlayer());
            PlayerVisibility.makeSpecVisToSpecs(this, event.getPlayer());
        } else {
            Notifier.notify(event.getPlayer(), "You Died :( RIP. Want to fly spectate /surface /spawn ? Get Walls " + ChatColor.GREEN + "VIP" + ChatColor.WHITE + " at " + Walls.DISCORD);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        switch (getGameState()) {
            case PREGAME:
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
                break;
            case PEACETIME:
                Block block = event.getBlock();
                for (final Selection s : this.selections) {
                    if (s.containsType(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 1)) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "You are not allowed to break blocks here.");
                        return;
                    }
                }

                if (this.isInASpawn(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You are not allowed to break blocks here.");
                    return;
                }
                final Material clickedMaterial = event.getBlock().getType();
                if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)
                        || (clickedMaterial == Material.ENCHANTMENT_TABLE) || (clickedMaterial == Material.WORKBENCH)) {

                    for (ProtectedContainer container : this.protectedContainers) {
                        final boolean owned = container.getOwner().equals(event.getPlayer().getName());
                        if (owned) {
                            this.protectedContainers.remove(container);
                            return;
                        }
                    }
                }


                if ((block.getType() == Material.IRON_ORE) || (block.getType() == Material.GOLD_ORE)
                        || (block.getType() == Material.DIAMOND_ORE) || (block.getType() == Material.EMERALD_ORE)) {
                    if (Walls.random.nextDouble() < 0.2D) {
                        if (leprechaunOwners.containsKey(event.getPlayer().getUniqueId())) {
                            checkLeprechaunDrop(block);
                        }
                    }
                }
            case FIGHTING:

                if (this.isInASpawn(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You are not allowed to break blocks here.");
                    return;
                }

                block = event.getBlock();
                if ((block.getType() == Material.IRON_ORE) || (block.getType() == Material.GOLD_ORE)
                        || (block.getType() == Material.DIAMOND_ORE) || (block.getType() == Material.EMERALD_ORE)) {
                    if (Walls.random.nextDouble() < 0.2D) {
                        if (leprechaunOwners.containsKey(event.getPlayer().getUniqueId())) {
                            checkLeprechaunDrop(block);
                        }
                        return;
                    }
                }
                break;
            default:
                break;
        }

    }

    private void wallsJoinMessage(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) return;
        if (getPlayer(event.getPlayer().getUniqueId()).rank.staff()) {
            event.setJoinMessage("");
            Notifier.staff(this, event.getPlayer().getName() + " joined the server.");
            WallsPlayer player = this.getPlayer(event.getPlayer().getUniqueId());
            this.getPlayers().put(event.getPlayer().getUniqueId(), player);
        } else {
            WallsPlayer player = this.getPlayer(event.getPlayer().getUniqueId());
            if (Walls.clanBattle) {
                if (player != null && player.clan == null) {
                    event.getPlayer().kickPlayer("§cSorry, this game is a Clan Battle!!");
                    return;
                }
                if (player != null && !Walls.clans.contains(player.clan)) {
                    event.getPlayer().kickPlayer("Sorry, this game is a Clan Battle!!");
                    return;
                }
            }
            if (playerJoinRestriction != PlayerJoinType.ANYONE) {
                boolean kick = false;
                switch (playerJoinRestriction) {
                    case VIP:
                        kick = player == null || !player.rank.vip();
                        break;
                    case PRO:
                        kick = player == null || !player.rank.pro();
                        break;
                    case STAFF:
                        kick = player == null || !player.rank.staff();
                        break;
                }
                if (kick) {
                    event.getPlayer().kickPlayer("§cSorry, this game is " + Walls.playerJoinRestriction.toString() + " only just now.");
                    return;
                }
            }

            if (getGameState() != GameState.PREGAME) {
                if (isSpectator(event.getPlayer()) && player != null && !player.rank.vip()) {
                    event.getPlayer().kickPlayer("§cSorry, the game is already in progress, you need VIP and up to spectate - " + Walls.DISCORD + " !");
                    return;
                }
            }
            Notifier.broadcast(ChatColor.GRAY + event.getPlayer().getName() + " joined the server.");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.setJoinMessage("");

        Bukkit.getScheduler().runTaskLater(this, () -> wallsJoinMessage(event), 2 * 20L);

        WallsPlayer wallsPlayer = new WallsPlayer();

        switch (getGameState()) {
            case PREGAME:
                if (!getPlayers().containsKey(event.getPlayer().getUniqueId())) {
                    wallsPlayer.playerState = PlayerState.SPECTATORS;
                    this.getPlayers().put(event.getPlayer().getUniqueId(), wallsPlayer);
                }
                this.spectatorKit.givePlayerKit(event.getPlayer());
                event.getPlayer().setHealth(20);
                event.getPlayer().setFoodLevel(20);
                playerScoreBoard.setScoreBoard(event.getPlayer().getUniqueId());
                playerScoreBoard.updateScoreboardScores();
                if (!starting && getPlayers().size() >= Walls.preGameAutoStartPlayers && !Walls.clanBattle && !Walls.tournamentMode) {
                    Notifier.broadcast("Game starts in " + ChatColor.LIGHT_PURPLE + preGameAutoStartSeconds + ChatColor.WHITE + " seconds!!");
                    this.clock.setClock(preGameAutoStartSeconds, () -> GameStarter.startGame(getPlayers(), Walls.this));
                    this.starting = true;
                }
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                if (!this.quitters.contains(event.getPlayer().getUniqueId())) {
                    this.spectatorKit.givePlayerKit(event.getPlayer());
                    playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), PlayerState.SPECTATORS);
                    event.getPlayer().setHealth(20);
                    event.getPlayer().setFoodLevel(20);
                    PlayerVisibility.makeSpecInvisible(this, event.getPlayer());
                    PlayerVisibility.makeSpecVisToSpecs(this, event.getPlayer());
                } else {
                    this.quitters.remove(event.getPlayer().getUniqueId());
                    this.inventory.remove(event.getPlayer().getUniqueId());
                    this.quitterTasks.get(event.getPlayer().getUniqueId()).cancel();
                    PlayerVisibility.hideAllSpecs(this, event.getPlayer());
                }
                playerScoreBoard.setScoreBoard(event.getPlayer().getUniqueId());
                break;
            default:
                break;
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerChatHandler.playerChat(event, this);
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {

        if (!getPlayers().containsKey(event.getUniqueId())) {
            this.myDB.loadPlayer(event.getName(), event.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrop(PlayerDropItemEvent event) {
        if (getGameState() == GameState.PREGAME) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getLocation().getY() > buildHeight) event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        switch (getGameState()) {
            case PEACETIME:
            case FIGHTING:
                event.setDeathMessage("");
                Player player = event.getEntity();
                DeathMessages.getDeathMessage(event, this);
                if (Walls.UHC) {
                    event.getEntity().getKiller().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
                    Notifier.success(event.getEntity().getKiller(), "You got a Golden Apple for that Kill!! Eat it for health!");
                }
                playerScoreBoard.removePlayerFromTeam(event.getEntity().getUniqueId());
                playerScoreBoard.addPlayerToTeam(event.getEntity().getUniqueId(), PlayerState.SPECTATORS);
                WallsPlayer deadWallsPlayer = getPlayer(event.getEntity().getUniqueId());
                deadWallsPlayer.deaths = 1;
                deadWallsPlayer.minutes = (this.clock.getSecondsRemaining() / 60);
                getPlayers().put(event.getEntity().getUniqueId(), deadWallsPlayer);
                if (deadWallsPlayer.rank.equals(Rank.VIP)) {
                    player.setAllowFlight(true);
                }
                if (Walls.clanBattle || Walls.tournamentMode) {
                    event.getEntity().kickPlayer("§cGG. No Specs in this game I'm afraid.");
                }
                this.foodTime = 0;
                if (this.foodDisabled) {
                    this.foodDisabled = false;
                    Notifier.broadcast("You can now eat again!");
                }
                WallsPlayer wallsPlayer = getPlayer(player.getUniqueId());
                wallsPlayer.playerState = PlayerState.SPECTATORS;
                player.closeInventory();
                player.getInventory().clear();
                getPlayers().put(player.getUniqueId(), wallsPlayer);
                if (calculateTeamsLeft() < 2) {
                    setGameState(GameState.FINISHED);
                    Notifier.broadcast("Server restarting in " + restartTimer + " seconds!");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Walls.this, () -> System.exit(0), 20L * this.restartTimer);
                }
                playerScoreBoard.updateScoreboardScores();
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        WallsCmd.VOTES.remove(player.getUniqueId());
        WallsPlayer wallsPlayer = getPlayer(player);
        switch (getGameState()) {
            case PREGAME:
                player.getInventory().clear();
                getPlayers().remove(player.getUniqueId());
                playerScoreBoard.removePlayerFromTeam(player.getUniqueId());
                WallsCmd.VOTES.remove(player.getUniqueId());
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                event.setQuitMessage("");
                if (!this.isSpectator(player)) {
                    if (this.inCombat.containsKey(player.getUniqueId())) {
                        System.out.println();
                        System.out.println();
                        System.out.println();
                        System.out.println(player.getName() + " left while in combat");
                        System.out.println();
                        System.out.println();
                        System.out.println();
                        Notifier.broadcast(player.getDisplayName() + " just left while in combat. (Grab their stuff! x:"
                                + player.getLocation().getBlockX()
                                + " y:" + player.getLocation().getBlockY()
                                + " z:" + player.getLocation().getBlockZ() + ")");
                        for (final ItemStack item : player.getInventory()) {
                            if (item != null) {
                                Bukkit.getWorld(Walls.levelName).dropItemNaturally(player.getLocation(), item);
                            }
                        }
                        HolographicDisplaysAPI.get(this).createHologram(player.getLocation().add(0, 2.5, 0)).getLines().appendItem(new ItemStack(Material.RAW_CHICKEN));
                        Hologram tempHologram = HolographicDisplaysAPI.get(this).createHologram(player.getLocation().add(0, 2, 0));
                        tempHologram.getLines().appendText(Walls.teamChatColors[wallsPlayer.playerState.ordinal()] + player.getDisplayName());
                        tempHologram.getLines().appendText(ChatColor.WHITE + " COMBAT LOGGED HERE");
                        tempHologram.getLines().appendText("Chicken.");
                        this.inCombat.remove(player.getUniqueId());
                        foodTime = 0;
                        if (foodDisabled) {
                            foodDisabled = false;
                            Notifier.broadcast("You can now eat again!");
                        }
                        wallsPlayer.playerState = PlayerState.SPECTATORS;
                        wallsPlayer.deaths = wallsPlayer.deaths + 5;
                        wallsPlayer.kills = 0;
                        wallsPlayer.wins = 0;
                        player.closeInventory();
                        player.getInventory().clear();
                        getPlayers().put(player.getUniqueId(), wallsPlayer);
                        playerScoreBoard.removePlayerFromTeam(player.getUniqueId());
                        playerScoreBoard.addPlayerToTeam(player.getUniqueId(), PlayerState.SPECTATORS);
                        playerScoreBoard.updateScoreboardScores();
                        if (calculateTeamsLeft() < 2) {
                            setGameState(GameState.FINISHED);
                            Notifier.broadcast("Server restarting in " + restartTimer + " seconds!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Walls.this, () -> System.exit(0), 20L * restartTimer);
                        }
                    } else {
                        System.out.println();
                        System.out.println();
                        System.out.println();
                        System.out.println(player.getName() + " may have left");
                        System.out.println();
                        System.out.println();
                        System.out.println();
                        Notifier.broadcast(player.getDisplayName() + " may have just left the game..");
                        if (!this.inventory.containsKey(player.getUniqueId())) {
                            this.inventory.put(player.getUniqueId(), player.getInventory());
                        }
                        quitters.add(player.getUniqueId());
                        quitterTasks.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(this, () -> {
                            if (!quitters.contains(player.getUniqueId())) return;
                            playerScoreBoard.removePlayerFromTeam(player.getUniqueId());
                            playerScoreBoard.addPlayerToTeam(player.getUniqueId(), PlayerState.SPECTATORS);
                            quitters.remove(player.getUniqueId());
                            if (!Bukkit.getOfflinePlayer(player.getUniqueId()).isOnline()) {
                                for (ItemStack item : inventory.get(player.getUniqueId())) {
                                    if (item == null) continue;
                                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                                }
                                Notifier.broadcast("Yup " + player.getDisplayName()
                                        + " left the game.. (Grab their stuff! x:"
                                        + player.getLocation().getBlockX()
                                        + " y:" + player.getLocation().getBlockY()
                                        + " z:" + player.getLocation().getBlockZ() + ")");
                            }
                            foodTime = 0;
                            if (foodDisabled) {
                                foodDisabled = false;
                                Notifier.broadcast("You can now eat again!");
                            }
                            WallsPlayer updatedPlayer = getPlayer(player.getUniqueId());
                            updatedPlayer.playerState = PlayerState.SPECTATORS;
                            player.closeInventory();
                            player.getInventory().clear();
                            getPlayers().put(player.getUniqueId(), updatedPlayer);
                            if (calculateTeamsLeft() > 1) return;
                            setGameState(GameState.FINISHED);
                            Notifier.broadcast("Server restarting in " + restartTimer + " seconds!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Walls.this, () -> System.exit(0), 20L * restartTimer);
                        }, relogTime * 20L));
                    }
                } else player.getInventory().clear();
                playerScoreBoard.updateScoreboardScores();
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDispense(BlockDispenseEvent event) {
        if (this.foodDisabled && event.getItem().getType() == Material.POTION) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        switch (getGameState()) {
            case PREGAME:
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (player.getInventory().getItemInHand().getType() == Material.SNOW_BALL) {
                        player.setHealth(20);
                    } else {
                        event.setCancelled(true);
                    }
                }
                break;
            case PEACETIME:
            case FIGHTING:
                if (isSpectator(event.getEntity())) {
                    event.setCancelled(true);
                }
                break;
            case FINISHED:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler
    void PlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.ENDER_PEARL) {
            if (event.getTo().getBlockY() >= (Walls.buildHeight + 10)) {
                Notifier.error(event.getPlayer(), "Pearl landed a little too high.. you need to aim lower :(");
                event.setCancelled(true);
            } else if (this.isInASpawn(event.getTo()) && event.getTo().getBlockY() > 82) {
                event.setCancelled(true);
                Notifier.error(event.getPlayer(), "Aww man you cant pearl there :(");
            }
        }
    }

    private boolean checkSpecBlockingProjectile(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            if (event.getEntity() instanceof Player && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
                Arrow arrow = (Arrow) event.getDamager();

                Vector velocity = arrow.getVelocity();

                Player shooter = (Player) arrow.getShooter();
                Player damaged = (Player) event.getEntity();

                try {
                    if (this.isSpectator(event.getEntity())) {
                        damaged.teleport(event.getDamager().getLocation().add(0, 5, 0));
                        damaged.setFlying(true);

                        Arrow newArrow = shooter.launchProjectile(Arrow.class);
                        newArrow.setShooter(shooter);
                        newArrow.setVelocity(velocity);
                        newArrow.setBounce(false);

                        event.setCancelled(true);
                        arrow.remove();
                        return true;
                    }

                } catch (Exception e) {
                    this.getLogger().info("ERROR: failed to fixed spec block enderpearl");
                }

            }
        } else if (event.getDamager() instanceof Snowball) {
            if (event.getEntity() instanceof Player && ((Snowball) event.getDamager()).getShooter() instanceof Player) {
                Snowball snowBall = (Snowball) event.getDamager();

                Vector velocity = snowBall.getVelocity();

                Player shooter = (Player) snowBall.getShooter();
                Player damaged = (Player) event.getEntity();

                try {
                    if (this.isSpectator(event.getEntity())) {
                        damaged.teleport(event.getEntity().getLocation().add(0, 5, 0));
                        damaged.setFlying(true);

                        Snowball newSnowBall = shooter.launchProjectile(Snowball.class);
                        newSnowBall.setShooter(shooter);
                        newSnowBall.setVelocity(velocity);
                        newSnowBall.setBounce(false);

                        event.setCancelled(true);
                        snowBall.remove();
                        return true;
                    }
                } catch (Exception e) {
                    this.getLogger().info("ERROR: failed to fixed spec block enderpearl");
                }

            }
        } else if (event.getDamager() instanceof EnderPearl) {
            if (event.getEntity() instanceof Player && ((EnderPearl) event.getDamager()).getShooter() instanceof Player) {
                EnderPearl enderPearl = (EnderPearl) event.getDamager();

                Vector velocity = enderPearl.getVelocity();

                Player shooter = (Player) enderPearl.getShooter();
                Player damaged = (Player) event.getEntity();

                // spec block
                try {
                    if (this.isSpectator(event.getEntity())) {
                        damaged.teleport(event.getEntity().getLocation().add(0, 5, 0));
                        damaged.setFlying(true);

                        EnderPearl newEnderPearl = shooter.launchProjectile(EnderPearl.class);
                        newEnderPearl.setShooter(shooter);
                        newEnderPearl.setVelocity(velocity);
                        newEnderPearl.setBounce(false);

                        event.setCancelled(true);
                        enderPearl.remove();
                        return true;
                    }
                } catch (Exception e) {
                    this.getLogger().info("ERROR: failed to fixed spec block enderpearl");
                }
            }
        }
        return false;

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHurtPlayer(EntityDamageByEntityEvent event) {

        Entity entityDamager = event.getDamager();


        switch (getGameState()) {
            case PREGAME:
                if (entityDamager instanceof Player) {
                    Player damager = (Player) entityDamager;
                    if (damager.getInventory().getItemInHand().getType() == Material.SNOW_BALL) {
                        damager.setHealth(20);
                    } else {
                        event.setCancelled(true);
                    }
                }
                break;
            case PEACETIME:
            case FIGHTING:

                if (checkSpecBlockingProjectile(event)) {
                    return;
                }

                if (entityDamager instanceof Player && this.isSpectator(entityDamager)) {
                    event.setCancelled(true);
                    break;
                }

                if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                    if (isSpectator(event.getEntity()) || isSpectator(event.getDamager())) {
                        event.setCancelled(true);
                        break;
                    }
                    if (sameTeam(event.getEntity().getUniqueId(), event.getDamager().getUniqueId())) {
                        event.setCancelled(true);
                        break;
                    }

                    if (!Walls.tournamentMode) {


                        UUID hitter = event.getDamager().getUniqueId();
                        if (!this.inCombat.containsKey(hitter)) {
                            Notifier.error(Bukkit.getPlayer(hitter), "You are now in combat ! Do not LOG out.");
                        }
                        UUID beingHit = event.getEntity().getUniqueId();
                        if (!this.inCombat.containsKey(beingHit)) {
                            Notifier.error(Bukkit.getPlayer(beingHit), "You are now in combat ! Do not LOG out.");
                        }
                        this.inCombat.put(hitter, System.currentTimeMillis());
                        this.inCombat.put(beingHit, System.currentTimeMillis());
                    }
                }


                if (event.getDamager() instanceof Projectile) {
                    final ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                    UUID hitter = null;
                    if (shooter instanceof Player && event.getEntity() instanceof Player) {
                        if (sameTeam(event.getEntity().getUniqueId(), ((Entity) shooter).getUniqueId())) {
                            event.setCancelled(true);
                            break;
                        }
                        hitter = ((Player) ((Projectile) event.getDamager()).getShooter()).getUniqueId();
                    }

                    if (!Walls.tournamentMode) {
                        if (hitter != null && !this.inCombat.containsKey(hitter)) {
                            Notifier.error(Bukkit.getPlayer(hitter), "You are now in combat ! Do not LOG out.");
                            this.inCombat.put(hitter, System.currentTimeMillis());
                        }

                        if (event.getEntity() instanceof Player) {
                            UUID beingHit = event.getEntity().getUniqueId();
                            if (!this.inCombat.containsKey(beingHit)) {
                                Notifier.error(Bukkit.getPlayer(beingHit), "You are now in combat ! Do not LOG out.");
                            }
                            this.inCombat.put(beingHit, System.currentTimeMillis());
                        }
                    }
                }


                break;

            case FINISHED:
                if (event.getEntity() instanceof Player) {
                    event.setCancelled(true);
                }
                if (event.getDamager() instanceof Player) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }

    }


    private void checkForSpecBlocking(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.isBlockInHand()) {
                for (Entity entity : event.getPlayer().getNearbyEntities(2, 2, 2)) {
                    if (entity instanceof Player) {
                        Player spectator = (Player) entity;
                        if (this.isSpectator(entity)) {
                            spectator.setFlying(true);
                            spectator.teleport(entity.getLocation().add(0, 5, 0));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void checkForCompassSwitch(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getPlayer().getItemInHand().getType() == Material.COMPASS) {
                WallsPlayer twp = this.getPlayer(event.getPlayer().getUniqueId());
                twp.compassPointsToEnemy = !twp.compassPointsToEnemy;
                getPlayers().put(event.getPlayer().getUniqueId(), twp);
                ItemMeta compass = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
                if (twp.compassPointsToEnemy) {
                    Notifier.error(event.getPlayer(), "Compass Points to Enemy!!");
                    compass.setDisplayName("Enemy Finder");
                    event.getPlayer().getItemInHand().removeEnchantment(Enchantment.DAMAGE_ARTHROPODS);
                } else {
                    Notifier.success(event.getPlayer(), "Compass Points to Friend!!");
                    compass.setDisplayName(ChatColor.GREEN + "Find MY TEAM!!");
                    ItemStackTools.enchantItem(event.getPlayer().getItemInHand(), Enchantment.DAMAGE_ARTHROPODS, 1);
                }
                event.getPlayer().getItemInHand().setItemMeta(compass);
                List<Player> fightingPlayers = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!isSpectator(player)) fightingPlayers.add(player);
                }
                try {
                    processCompass(event.getPlayer(), fightingPlayers);
                } catch (Exception e) {
                    getLogger().info("Could not process compass so skipping it..");
                }
            }
        }
    }

    public void kickOffCombatThread() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (UUID uuid : inCombat.keySet()) {
                if (System.currentTimeMillis() - inCombat.get(uuid) <= Walls.combatLogTimeInSeconds * 1000L) continue;
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) Notifier.success(player, "You are now no longer in combat !");
            }
            inCombat.clear();
        }, 0L, 20L);
    }

    private void checkForContainerProtection(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            final Material clickedMaterial = event.getClickedBlock().getType();
            if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)
                    || (clickedMaterial == Material.ENCHANTMENT_TABLE) || (clickedMaterial == Material.WORKBENCH)) {
                Location location = event.getClickedBlock().getLocation();
                for (ProtectedContainer container : this.protectedContainers) {
                    if (container.matches(location, null)) {
                        if (container.getOwner().equals(event.getPlayer().getName())) {
                            if (event.getPlayer().isSneaking()) {
                                this.protectedContainers.remove(container);
                                Notifier.success(event.getPlayer(), clickedMaterial.name() + " no longer protected.");
                                event.setCancelled(true);
                            }
                        } else {
                            Notifier.error(event.getPlayer(), "This is owned by " + container.getOwner());
                            event.setCancelled(true);
                        }
                        return;
                    }
                }
            }
        }

        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            final Material clickedMaterial = event.getClickedBlock().getType();
            if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)) {
                final Location location = event.getClickedBlock().getLocation();
                Location second = null;
                if (clickedMaterial == Material.CHEST) {
                    final Block north = event.getClickedBlock().getRelative(BlockFace.NORTH);
                    final Block south = event.getClickedBlock().getRelative(BlockFace.SOUTH);
                    final Block east = event.getClickedBlock().getRelative(BlockFace.EAST);
                    final Block west = event.getClickedBlock().getRelative(BlockFace.WEST);
                    if (north.getType() == Material.CHEST) {
                        second = north.getLocation();
                    } else if (south.getType() == Material.CHEST) {
                        second = south.getLocation();
                    } else if (east.getType() == Material.CHEST) {
                        second = east.getLocation();
                    } else if (west.getType() == Material.CHEST) {
                        second = west.getLocation();
                    }
                }
                for (final ProtectedContainer container : this.protectedContainers) {
                    if (container.matches(location, second)) {
                        if (!container.getOwner().equals(event.getPlayer().getName())) {
                            event.setCancelled(true);
                            Notifier.error(event.getPlayer(), "This is owned by " + container.getOwner());
                        }
                    }
                }
            }
        }
    }

    private void checkForKitButtonPressed(PlayerInteractEvent event) {
        switch (getGameState()) {
            case PEACETIME:
            case FIGHTING:
                if ((event.getAction() == Action.LEFT_CLICK_BLOCK) || (event.getAction() == Action.RIGHT_CLICK_BLOCK) && !this.isSpectator(event.getPlayer())) {
                    final Block block = event.getClickedBlock();
                    if ((block != null) && (block.getType() == Material.WOOD_BUTTON || block.getType() == Material.STONE_BUTTON) && !this.isSpectator(event.getPlayer())) {
                        final Block above = block.getRelative(BlockFace.DOWN);
                        if ((above != null) && (above.getType() == Material.WALL_SIGN)) {
                            final Sign sign = (Sign) above.getState();
                            if (ChatColor.stripColor(sign.getLine(2)).equalsIgnoreCase("kit")) {
                                final String choice = ChatColor.stripColor(sign.getLine(1)).toLowerCase().replace(" ", "");
                                myKitCmd.playerChoice(event.getPlayer(), choice);
                            }
                        }
                    }
                }
                break;
            case FINISHED:
                event.setCancelled(true);
                break;
            default:
                break;
        }

    }

    private void checkSpecPlayerFinder(PlayerInteractEvent event) {
        if (isSpectator(event.getPlayer())) {
            if (event.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) {
                Inventory chest = Bukkit.createInventory(null, 6 * 9, "Player Finder :)");
                for (UUID uuid : getPlayers().keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || isSpectator(player)) continue;
                    ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                    chest.addItem(ItemStackTools.changeItemName(skull, player.getName()));
                }
                event.getPlayer().openInventory(chest);
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        switch (getGameState()) {
            case PREGAME:
                if (event.getPlayer().getItemInHand().getType() == Material.WOOL) {
                    final String itemName = ChatColor.stripColor(event.getPlayer().getItemInHand().getItemMeta().getDisplayName());
                    PlayerState tps = PlayerState.SPECTATORS;
                    if (itemName.equals(ChatColor.stripColor(Walls.teamsNames[1]))) {
                        tps = PlayerState.RED;
                    } else if (itemName.equals(ChatColor.stripColor(Walls.teamsNames[2]))) {
                        tps = PlayerState.YELLOW;
                    } else if (itemName.equals(ChatColor.stripColor(Walls.teamsNames[3]))) {
                        tps = PlayerState.GREEN;
                    } else if (itemName.equals(ChatColor.stripColor(Walls.teamsNames[4]))) {
                        tps = PlayerState.BLUE;
                    }

                    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

                        WallsPlayer twp = getPlayer(event.getPlayer().getUniqueId());

                        if (this.checkEnoughSpaceInTeam(tps.ordinal())) {
                            if (twp.playerState.compareTo(tps) == 0) {
                                event.setCancelled(true);
                                return;
                            }
                            Notifier.team(this, twp.playerState, event.getPlayer().getName() + " joined " + Walls.teamsNames[tps.ordinal()]);
                            playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), tps);
                            twp.playerState = tps;
                            event.setCancelled(true);
                        } else {
                            Notifier.error(event.getPlayer(), Walls.teamsNames[tps.ordinal()] + ChatColor.WHITE + " is full :(");
                        }
                    } else printTeamMates(event.getPlayer(), tps);
                } else if (event.getPlayer().getItemInHand().getType() == Material.SNOW_BALL) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                        event.getPlayer().updateInventory();
                    }
                }

                break;
            case PEACETIME:
                checkSpecPlayerFinder(event);
                checkForSpecBlocking(event);
                checkForContainerProtection(event);
                checkForKitButtonPressed(event);

                if (event.getAction() == Action.PHYSICAL && this.isSpectator(event.getPlayer())) {
                    event.setCancelled(true);
                }


                if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                    if (event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "You can't use your pearls while the walls are up!");
                        return;
                    }
                }
                if (event.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You can't use flint & steel while the walls are up!");
                    return;
                }
                if (event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You can't use your pearls while the walls are up!");
                    return;
                }
                break;

            case FIGHTING:
                checkSpecPlayerFinder(event);
                checkForSpecBlocking(event);
                checkForKitButtonPressed(event);
                checkForCompassSwitch(event);

                if (event.getAction() == Action.PHYSICAL && this.isSpectator(event.getPlayer())) {
                    event.setCancelled(true);
                }


                if (event.getAction() == Action.PHYSICAL) {
                    if (isSpectator(event.getPlayer())) {
                        event.setCancelled(true);
                        return;
                    }
                    if (event.getClickedBlock().getType() == Material.TRIPWIRE && event.getClickedBlock().getLocation().distance(new Location(event.getClickedBlock().getWorld(), -3, 51, 129)) < 10 && !isSpectator(event.getPlayer())) {
                        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(event.getPlayer().getName() + ChatColor.DARK_PURPLE + " is at THE CENTER TRAP trying to take stuff!!"));
                    }
                }

            case FINISHED:
                checkSpecPlayerFinder(event);
                handleKitInteraction(event);
                if (isSpectator(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
                break;
            default:
                break;
        }

    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!foodDisabled || !event.getItem().getType().isEdible()) return;
        Notifier.error(event.getPlayer(), "Food is currently disabled!");
        event.setCancelled(true);
    }

    private void handleKitInteraction(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final ItemStack stack = player.getItemInHand();
        if (event.getAction() == Action.PHYSICAL) {
            if ((block != null) && (block.getType() == Material.STONE_PLATE)
                    && (block.getRelative(0, -1, 0).getType() == Material.GRAVEL)) {
                final PlayerState team = this.boom.get(block.getLocation());
                if ((team != null) && team != getPlayer(player.getUniqueId()).playerState) {
                    block.getWorld().createExplosion(block.getLocation(), 3F);
                    this.boom.remove(block.getLocation());
                }
            }
        }
        if (this.loreMatch(stack, "Weapon of the Gods!")) {
            if (thorOwners.containsKey(event.getPlayer().getUniqueId())) {
                int numberOfUsesLeft = thorOwners.get(event.getPlayer().getUniqueId());

                Location target = null;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    target = block == null ? null : block.getLocation();
                }
                if (event.getAction() == Action.LEFT_CLICK_AIR) {
                    final Block targetBlock = player.getTargetBlock((HashSet<Material>) null, 50);
                    if (targetBlock != null) {
                        target = targetBlock.getLocation();

                    }
                }
                if (target != null) {
                    target.getWorld().strikeLightning(target);
                    stack.setDurability((short) (stack.getDurability() - 10));
                    numberOfUsesLeft = numberOfUsesLeft - 1;
                    thorOwners.put(event.getPlayer().getUniqueId(), numberOfUsesLeft);
                    if (numberOfUsesLeft > 0) {
                        Notifier.notify(event.getPlayer(), "Thor Hammer has " + numberOfUsesLeft + " remaining lightning bolts!");
                    } else {
                        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
                        player.getInventory().removeItem(player.getInventory().getItemInHand());
                    }
                }

            } else {
                event.getPlayer().sendMessage("§cYou cannot use Mjölnir");
            }

        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {

        switch (getGameState()) {
            case PREGAME:

                int lobbyHeight = Walls.gameSpawn.getBlockY();

                if (event.getTo().getBlockY() < (lobbyHeight - 6) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    event.getPlayer().setFallDistance(0f);
                    event.getPlayer().teleport(Walls.gameSpawn);
                }

                if (Math.random() * 100 < 10 && Walls.lobbyTrail != null && !Walls.lobbyTrail.equals("")) {

                    if (this.droppedItems.containsKey(event.getPlayer().getUniqueId())) {
                        ItemStack i = TrailGenerator.getItem(Walls.lobbyTrail);
                        if (i != null) {
                            Entity droppedItem = event.getPlayer().getWorld().dropItem(event.getFrom(), i);
                            this.droppedItems.get(event.getPlayer().getUniqueId()).add(droppedItem);
                            this.allitems.add(droppedItem);

                            final Player p = event.getPlayer();
                            Bukkit.getScheduler().runTaskLater(this, () -> {

                                if (!(droppedItems.get(p.getUniqueId())).isEmpty()) {
                                    Iterator<Entity> iterator = droppedItems.get(p.getUniqueId()).iterator();
                                    Entity next1 = iterator.next();
                                    droppedItems.get(p.getUniqueId()).remove(next1);
                                    allitems.remove(next1);
                                    next1.remove();
                                }
                            }, 25);

                        }
                    } else {
                        this.droppedItems.put(event.getPlayer().getUniqueId(), new ArrayList<>());
                    }
                }

                break;
            case PEACETIME:
                if (!this.isSpectator(event.getPlayer())) {
                    if (this.isOnPaths(event.getTo())) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "Trying to escape :( try /spawn");
                        return;
                    }
                    if (event.getTo().getBlockY() > (Walls.gameSpawn.getBlockY() - 4)) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "Trying to escape :( try /spawn");
                        return;
                    }
                }
                break;
            case FIGHTING:
            case FINISHED:
                if (!this.isSpectator(event.getPlayer())) {
                    if (event.getTo().getBlockY() > (Walls.gameSpawn.getBlockY() - 4)) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "Trying to escape :( try /spawn");
                        return;
                    }
                }

                if (this.isInASpawn(event.getTo()) && event.getTo().getBlockY() > 82 && !this.isSpectator(event.getPlayer())) {
                    event.getPlayer().teleport(event.getTo().add(0, -3, 0));
                    Notifier.error(event.getPlayer(), "Aww man its way too dangerous to climb up there.. :(");
                    return;
                }

                break;
            default:
                break;
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onRegainHealthChange(EntityRegainHealthEvent event) {
        switch (getGameState()) {
            case PREGAME:
            case PEACETIME:
            case FIGHTING:
                if (Walls.UHC) {
                    if (event.getRegainReason() != RegainReason.MAGIC_REGEN) {
                        event.setCancelled(true);
                    }
                }
                break;
            case FINISHED:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        switch (getGameState()) {
            case PREGAME:
            case PEACETIME:
            case FINISHED:
                event.setCancelled(true);
                break;
            case FIGHTING:
                if (isSpectator(event.getEntity())) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }


    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().isOp()) {
            final String[] split = event.getMessage().split(" ");
            if (split.length < 1) {
                return;
            }
            final String cmd = split[0].trim().substring(1).toLowerCase();
            if ((cmd.equals("tell") || cmd.equals("msg") || cmd.equals("w")) && this.mutedPlayers.containsKey(event.getPlayer().getUniqueId())) {
                Notifier.error(event.getPlayer(), "You are muted." + ChatColor.BOLD + " Please use chat responsibly.");
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        switch (getGameState()) {
            case PEACETIME:
                if (event.getBucket() == Material.LAVA_BUCKET) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You can't pour lava while the walls are up!");
                }
                break;
            case FINISHED:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }


    @EventHandler
    public void onBucketPlace(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        switch (getGameState()) {
            case PEACETIME:
                if (event.getBucket() == Material.LAVA_BUCKET) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You can't pour lava while the walls are up!");
                }
            case FIGHTING:
                if (event.getBlockClicked().getLocation().getY() > liquidBuildHeight) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place fluids here!");
                    return;
                }
                break;
            case FINISHED:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void preventCrafting(CraftItemEvent event) {
        try {
            final Material m = event.getInventory().getResult().getType();
            if (m == Material.HOPPER || m == Material.ITEM_FRAME || m == Material.BREWING_STAND || m == Material.BREWING_STAND_ITEM) {
                event.setCancelled(true);
                return;
            }
            switch (getGameState()) {
                case PREGAME:
                case PEACETIME:
                    if (m == Material.HOPPER_MINECART || m == Material.EXPLOSIVE_MINECART) {
                        event.setCancelled(true);
                    }
                    break;
                case FIGHTING:
                case FINISHED:
                default:
                    break;
            }
        } catch (NullPointerException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        switch (getGameState()) {
            case PREGAME:
                if (!event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) event.setCancelled(true);
                break;
            case PEACETIME:
            case FIGHTING:
                if (!(event.getWhoClicked() instanceof Player)) return;
                if (!this.isSpectator(event.getWhoClicked())) return;
                if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
                String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                Player player = Bukkit.getPlayer(itemName);
                if (player != null) {
                    event.getWhoClicked().teleport(player.getLocation().add(0, +5, 0));
                    Notifier.success(event.getWhoClicked(), "You have been teleported to " + itemName);
                }
                event.getWhoClicked().closeInventory();
                break;
            case FINISHED:
                if (!(event.getWhoClicked() instanceof Player)) return;
                if (this.isSpectator(event.getWhoClicked())) event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void StopBrew(BrewEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void fireDamageControl2(BlockSpreadEvent event) {
        if (event.getNewState().getType() == Material.FIRE && Walls.random.nextDouble() > 0.7) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onChest(InventoryOpenEvent event) {
        if (!event.getPlayer().isOp() && (event.getPlayer() instanceof Player)) {
            if ((event.getInventory().getType() != InventoryType.PLAYER) && isSpectator(event.getPlayer())) {
                if (!event.getInventory().getTitle().equals("Player Finder :)")) event.setCancelled(true);
            }
        }
        if (event.getInventory().getType() == InventoryType.CHEST && event.getPlayer().getLocation().distance(new Location(event.getPlayer().getWorld(), -10, 53, 129)) < 5) {
            Notifier.broadcast(event.getPlayer().getName() + ChatColor.DARK_PURPLE + " SET OFF THE CENTER TRAP.. 20 SECONDS TO BOOOM!");
            World w = event.getPlayer().getWorld();
            w.getBlockAt(new Location(w, -2, 50, 130)).setType(Material.AIR);
            TNTPrimed tnt = (TNTPrimed) w.spawnEntity(new Location(w, -2, 51, 130), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(400);
        }
    }

    private void checkForProtectedPlacement(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        Material typePlaced = placedBlock.getType();
        if ((typePlaced == Material.CHEST) || (typePlaced == Material.FURNACE) || (typePlaced == Material.BURNING_FURNACE)
                || (typePlaced == Material.ENCHANTMENT_TABLE) || (typePlaced == Material.WORKBENCH)) {
            final Location location = event.getBlock().getLocation();
            Location second = null;
            if (typePlaced == Material.CHEST) {
                final Block north = event.getBlock().getRelative(BlockFace.NORTH);
                final Block south = event.getBlock().getRelative(BlockFace.SOUTH);
                final Block east = event.getBlock().getRelative(BlockFace.EAST);
                final Block west = event.getBlock().getRelative(BlockFace.WEST);
                if (north.getType() == Material.CHEST) {
                    second = north.getLocation();
                } else if (south.getType() == Material.CHEST) {
                    second = south.getLocation();
                } else if (east.getType() == Material.CHEST) {
                    second = east.getLocation();
                } else if (west.getType() == Material.CHEST) {
                    second = west.getLocation();
                }
            }
            int owncount = 0;
            for (ProtectedContainer container : this.protectedContainers) {
                final boolean owned = container.getOwner().equals(event.getPlayer().getName());
                if (owned) {
                    owncount++;
                }
                if (container.matches(location, second)) {
                    if (!owned) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "Adjacent chest is owned by " + container.getOwner());
                        return;
                    } else if (container.getLocation().equals(second)) {
                        Notifier.success(event.getPlayer(), "Protection extended! You have protected " + (owncount + 1) + " containers");
                        return;
                    }
                }
            }
            int max = 2;
            if (getPlayer(event.getPlayer().getUniqueId()).rank.pro()) {
                max = 7;
            } else if (getPlayer(event.getPlayer().getUniqueId()).rank.vip()) {
                max = 5;
            }
            if (owncount < max) {
                this.protectedContainers.add(new ProtectedContainer(location, event.getPlayer().getName()));
                Notifier.success(event.getPlayer(), "Protection extended! You have protected " + owncount + " containers");
            } else {
                Notifier.error(event.getPlayer(), ChatColor.RED + "You have reached your protection limit!");
                Notifier.error(event.getPlayer(), ChatColor.RED + "Unprotect other owned containers");
                if (!getPlayer(event.getPlayer().getUniqueId()).rank.vip()) {
                    Notifier.notify(event.getPlayer(), ChatColor.AQUA + "VIP & PRO get more protected containers! " + Walls.DISCORD);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlock().getLocation().getBlockY() > Walls.buildHeight) {
            event.setCancelled(true);
            if (getGameState().equals(GameState.PREGAME)) return;
            Notifier.error(event.getPlayer(), "You reached the build limit and cannot place blocks here!");
            return;
        }
        if ((event.getBlock().getX() < -142 || event.getBlock().getX() > 137) || (event.getBlock().getZ() < -10 || event.getBlock().getZ() > 269)) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlockPlaced().getType().equals(Material.CARROT)) {
            Notifier.error(event.getPlayer(), "You don't look like a rabbit.. why you need to farm carrots?");
            event.setCancelled(true);
            return;
        }
        switch (getGameState()) {
            case PREGAME:
            case FINISHED:
                event.setCancelled(true);
                break;
            case PEACETIME:
                if (this.isOnPaths(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You are not allowed to place blocks here.");
                    return;
                }
                final Material type = event.getBlock().getType();
                if (type == Material.TNT) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "Cannot place TNT while the walls are up!!");
                    return;
                }
                checkForProtectedPlacement(event);
                final ItemStack stack = event.getPlayer().getItemInHand();
                if (this.loreMatch(stack, "BOOOM")) {
                    this.boom.put(event.getBlock().getLocation(), getPlayer(event.getPlayer().getUniqueId()).playerState);
                }
                break;
            default:
                break;
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().isOp()) return;
        switch (getGameState()) {
            case PREGAME:
                event.setCancelled(true);
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                if (isSpectator(event.getPlayer())) event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTarget(EntityTargetEvent event) {
        if (isSpectator(event.getTarget())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        switch (getGameState()) {
            case PREGAME:
                event.setCancelled(true);
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                if (isSpectator(event.getPlayer())) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }


    public void dropWalls() {
        Bukkit.getWorlds().forEach(world -> world.setGameRuleValue("doFireTick", "true"));
        Notifier.broadcast("Get ready to kill your enemy!");
        setGameState(GameState.FIGHTING);
        final List<Selection> toRemove = new ArrayList<>();
        for (final Selection s : this.selections) {
            if (s.getType() == 1) {
                s.remove(Bukkit.getWorld(Walls.levelName));
                toRemove.add(s);
            }
        }
        this.selections.removeAll(toRemove);
        Notifier.broadcast("The walls are now gone!");
        startHungerChecker();
        this.clock.abort();
        this.kickOffNoWinnerThread();
        this.kickOffCombatThread();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!leprechaunOwners.isEmpty()) {
                leprechaunOwners.clear();
                Notifier.broadcast("Leprechaun kit just lost power :(");
            }
        }, 1800 * 20);
    }

    public Map<UUID, WallsPlayer> getPlayers() {
        return players;
    }

    public WallsPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    public WallsPlayer getPlayer(UUID uuid) {
        return getPlayers().getOrDefault(uuid, null);
    }

    public List<UUID> getStaffList() {
        List<UUID> staffs = new ArrayList<>();
        for (UUID uuid : getPlayers().keySet()) {
            if (getPlayer(uuid).rank.staff()) staffs.add(uuid);
        }
        return staffs;
    }

    public List<UUID> getTeamList(UUID uid) {
        return getTeamList(getPlayer(uid).playerState);
    }

    public List<UUID> getTeamList(PlayerState playerState) {
        List<UUID> teamList = new ArrayList<>();
        for (UUID uuid : getPlayers().keySet()) {
            if (getPlayer(uuid).playerState == playerState) {
                teamList.add(uuid);
            }
        }
        return teamList;
    }

    public void printTeamMates(Player player, PlayerState ps) {
        List<UUID> teamUIDS = this.getTeamList(ps);
        if (!teamUIDS.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (UUID uuid : teamUIDS) names.add(Bukkit.getOfflinePlayer(uuid).getName());
            Notifier.notify(player, Walls.teamChatColors[ps.ordinal()] + String.join("§7, " + Walls.teamChatColors[ps.ordinal()], names));
        } else Notifier.notify(player, "§cTeam " + Walls.teamsNames[ps.ordinal()] + "§c is empty");
    }

    public int getTeamSize(PlayerState state) {
        int teamCounter = 0;
        for (WallsPlayer player : getPlayers().values()) {
            if (player.playerState.equals(state)) teamCounter++;
        }
        return teamCounter;
    }

    public boolean isSpectator(Entity entity) {
        if (entity == null) return false;
        WallsPlayer wallsPlayer = getPlayer(entity.getUniqueId());
        return wallsPlayer != null && wallsPlayer.playerState.equals(PlayerState.SPECTATORS);
    }

    public boolean sameTeam(UUID a, UUID b) {
        return getPlayer(a).playerState.compareTo(getPlayer(b).playerState) == 0;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int calculateTeamsLeft() {
        int tempNumberOfTeams = 0;
        int t1 = 0;
        int t2 = 0;
        int t3 = 0;
        int t4 = 0;
        for (WallsPlayer wp : getPlayers().values()) {
            switch (wp.playerState) {
                case RED:
                    t1++;
                    break;
                case YELLOW:
                    t2++;
                    break;
                case GREEN:
                    t3++;
                    break;
                case BLUE:
                    t4++;
                    break;
                default:
                    break;
            }
        }

        if (t1 > 0) {
            tempNumberOfTeams++;
        }
        if (t2 > 0) {
            tempNumberOfTeams++;
        }
        if (t3 > 0) {
            tempNumberOfTeams++;
        }
        if (t4 > 0) {
            tempNumberOfTeams++;
        }
        if (this.teams != tempNumberOfTeams) {
            this.teams = tempNumberOfTeams;
            if (this.teams > 1) {
                Notifier.broadcast(this.teams + " teams left!");
            } else {
                if (t1 > 0) {
                    winningTeam = 1;
                } else if (t2 > 0) {
                    winningTeam = 2;
                } else if (t3 > 0) {
                    winningTeam = 3;
                } else if (t4 > 0) {
                    winningTeam = 4;
                }
                Notifier.broadcast("---------------------------------------------");
                Notifier.broadcast("  Congratulations to " + Walls.teamsNames[winningTeam] + ChatColor.WHITE + " for winning!");
                Notifier.broadcast("---------------------------------------------");
                FireWorks.spawnFireworksForPlayers(this);
                for (UUID winner : this.getTeamList(PlayerState.values()[winningTeam])) {
                    WallsPlayer wallsWinner = this.getPlayer(winner);
                    wallsWinner.wins = 1;
                    wallsWinner.minutes = (this.clock.getSecondsRemaining() / 60);
                }
                this.myDB.saveAllData();
            }
        }
        return this.teams;
    }

    public int getNumberOfPlayers() {
        return getPlayers().size();
    }

    public int getWinningTeam() {
        return this.winningTeam;
    }

    public final void defineArena() {

        Selection c;
        c = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        c.setPointA(-160, 0, -29);
        c.setPointB(-15, 0, 117);

        c = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        c.setPointA(-160, 0, 285);
        c.setPointB(-15, 0, 142);

        c = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        c.setPointA(155, 0, 287);
        c.setPointB(10, 0, 142);

        c = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        c.setPointA(159, 0, -31);
        c.setPointB(10, 0, 117);

        Selection s;

        // Lobby
        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(-20, 198, 112);// lower left corner
        s.setPointB(15, 204, 147);// upper right corner
        this.selections.add(s);

        // Logo
        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(42, 186, 130);// lower left corner
        s.setPointB(-52, 168, 129);// upper right corner
        this.selections.add(s);

        // walls
        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(-14, 129, -10);// lower left corner
        s.setPointB(-14, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(9, 129, -10);// lower left corner
        s.setPointB(9, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(10, 129, 118);// lower left corner
        s.setPointB(137, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(10, 129, 141);// lower left corner
        s.setPointB(137, 62, 141);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(9, 129, 141);// lower left corner
        s.setPointB(9, 62, 269);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(-14, 129, 141);// lower left corner
        s.setPointB(-14, 62, 269);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(-15, 129, 141);// lower left corner
        s.setPointB(-142, 62, 141);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(-15, 129, 118);// lower left corner
        s.setPointB(-142, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        // Starting pads

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(-156, 77, -26);// lower left corner
        s.setPointB(-135, 62, -3);// upper right corner
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(150, 85, -23);// lower left corner
        s.setPointB(131, 62, -4);// upper right corner
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(152, 77, 284);// lower left corner
        s.setPointB(131, 62, 263);// upper right corner
        this.selections.add(s);

        s = new Selection(Bukkit.getWorld(Walls.levelName).getName());
        s.setPointA(-155, 77, 283);// lower left corner
        s.setPointB(-135, 62, 263);// upper right corner
        this.selections.add(s);

    }

    private boolean isOnPaths(Location loc) {
        if ((loc.getBlockX() > -500 && loc.getBlockX() < 500) && (loc.getBlockZ() > 118 && loc.getBlockZ() < 141)) {
            return true;
        }
        return (loc.getBlockX() > -14 && loc.getBlockX() < 9) && (loc.getBlockZ() > -500 && loc.getBlockZ() < 500);
    }

    private boolean isInASpawn(Location loc) {


//        Team 1 Red (-142, 61 -1) (-165,61,-32)
        if ((loc.getBlockX() < -141 && loc.getBlockX() > -166) && (loc.getBlockZ() < 0 && loc.getBlockZ() > -33)) {
            return true;
        }
//        Team 1 Red (-133, 61 -10) (-165,61,-32)
        if ((loc.getBlockX() < -132 && loc.getBlockX() > -166) && (loc.getBlockZ() < -9 && loc.getBlockZ() > -33)) {
            return true;
        }

//        Team 2 yellow (137, 61, 0) (159,61,-32)
        if ((loc.getBlockX() > 136 && loc.getBlockX() < 160) && (loc.getBlockZ() < 1 && loc.getBlockZ() > -33)) {
            return true;
        }
//        Team 2 yellow (127, 61 -10) (159,61,-32)
        if ((loc.getBlockX() > 127 && loc.getBlockX() < 160) && (loc.getBlockZ() < -9 && loc.getBlockZ() > -33)) {
            return true;
        }

//        Team 3 green (137, 61, 259) (159,61,291)
        if ((loc.getBlockX() > 136 && loc.getBlockX() < 160) && (loc.getBlockZ() > 258 && loc.getBlockZ() < 292)) {
            return true;
        }
//        Team 3 green (127, 61, 269) (159,61,291)
        if ((loc.getBlockX() > 126 && loc.getBlockX() < 160) && (loc.getBlockZ() > 268 && loc.getBlockZ() < 292)) {
            return true;
        }

//        Team 4 blue (-132, 61, 269) (-164,61,291)
        if ((loc.getBlockX() < -131 && loc.getBlockX() > -163) && (loc.getBlockZ() > 268 && loc.getBlockZ() < 292)) {
            return true;
        }

//        Team 4 blue (-142, 61, 259) (-164,61,291)
        return (loc.getBlockX() < -141 && loc.getBlockX() > -163) && (loc.getBlockZ() > 258 && loc.getBlockZ() < 292);
    }

    private boolean loreMatch(ItemStack item, String line) {
        if ((item == null) || !item.hasItemMeta() || (item.getItemMeta().getLore() == null)) {
            return false;
        }
        return item.getItemMeta().getLore().contains(line);
    }

    private void kickOffNoWinnerThread() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (calculateTeamsLeft() < 2 && getGameState() != GameState.FINISHED && getGameState() != GameState.PREGAME)
                System.exit(0);
        }, 2400, 2400);
    }

    public void kickOffCompassThread() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            List<Player> fightingPlayers = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!isSpectator(player)) fightingPlayers.add(player);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (fightingPlayers.size() > 0 && player != null) {
                    try {
                        processCompass(player, fightingPlayers);
                    } catch (Exception e) {
                        getLogger().info("Could not process compass so skipping it..");
                    }
                } else {
                    if (Walls.debugMode) {
                        getLogger().info("Processing compass - fightingPlayersSize = " + fightingPlayers.size());
                        if (player == null) {
                            getLogger().info("Processing compass - player = null");
                        }
                    }
                }
            }
        }, 20L * 2, 20L * 5);

    }

    private void processCompass(Player player, List<Player> playerList) {
        Location target = null;
        if (this.assassinTargets.containsKey(player.getUniqueId())) {

            final Player targetPlayer = Bukkit.getPlayerExact(this.assassinTargets.get(player.getUniqueId()));
            if ((targetPlayer != null) && (this.sameTeam(player.getUniqueId(), player.getUniqueId()) && !this.isSpectator(targetPlayer))) {
                target = targetPlayer.getLocation();
            }
        } else {
            final Location location = player.getLocation();
            Location closestloc = player.getLocation();
            double closestdis = 0xFFFFFF;

            for (final Player potentialEnemy : playerList) {

                if (!potentialEnemy.equals(player)) {

                    if (getPlayers().containsKey(potentialEnemy.getUniqueId())) {

                        if (this.getPlayer(player.getUniqueId()).compassPointsToEnemy) {
                            if (!this.isSpectator(potentialEnemy) && !this.sameTeam(player.getUniqueId(), potentialEnemy.getUniqueId())) {
                                final Location temploc = potentialEnemy.getLocation();
                                final double tempdis = location.distanceSquared(temploc);
                                if (tempdis < closestdis) {
                                    closestdis = tempdis;
                                    closestloc = temploc;
                                }

                            }
                        } else {
                            if (!this.isSpectator(potentialEnemy) && this.sameTeam(player.getUniqueId(), potentialEnemy.getUniqueId())) {

                                final Location temploc = potentialEnemy.getLocation();
                                final double tempdis = location.distanceSquared(temploc);
                                if (tempdis < closestdis) {
                                    closestdis = tempdis;
                                    closestloc = temploc;
                                }

                            }
                        }
                    }
                }
            }

            target = closestloc;
        }
        if (target != null) {
            player.setCompassTarget(target);
            if (Walls.debugMode) {
                getLogger().info("Target found and set for player " + player.getName());
            }
        } else {
            if (Walls.debugMode) {
                getLogger().info("Target for compass was NULL ??!?");
            }

        }

    }

    private void checkLeprechaunDrop(Block block) {
        Material drop = null;
        switch (block.getType()) {
            case IRON_ORE:
                drop = Material.IRON_INGOT;
                break;
            case GOLD_ORE:
                drop = Material.GOLD_INGOT;
                break;
            case DIAMOND_ORE:
                drop = Material.DIAMOND;
                break;
            case EMERALD_ORE:
                drop = Material.EMERALD;
                break;
            default:
                break;
        }

        if (drop != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(drop, 1));
        }

    }

    private void startHungerChecker() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (foodDisabled) return;
            if (++foodTime >= 60) {
                foodDisabled = true;
                foodTime = 0;
                Notifier.broadcast("Food disabled until next kill!");
            }
        }, 0, 200);
    }

    public boolean checkEnoughSpaceInTeam(int teamNumber) {
        if (Walls.tournamentMode) {
            return true;
        }
        int extraTeamAllowance = 2;
        return (this.getTeamSize(PlayerState.values()[teamNumber]) < ((this.getPlayers().size() / 4) + extraTeamAllowance));
    }
}
