package net.nonswag.fvr.walls;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.nonswag.core.api.file.formats.PropertiesFile;
import net.nonswag.core.api.math.MathUtil;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Walls extends JavaPlugin implements Listener {

    public static class WallsPlayer {
        public String clan = null;
        public String username = null;
        public String uid = null;
        public String paidKits = null;
        public boolean vip = false;
        public boolean pro = false;
        public boolean nMVP = false;
        public boolean dMVP = false;
        public boolean gm = false;
        public boolean mgm = false;
        public boolean legendary = false;
        public boolean admin = false;
        public boolean owner = false;
        public boolean clanLeader = false;
        public boolean compassPointsToEnemy = true;
        public int statsKills = 0;
        public int statsDeaths = 0;
        public int statsWins = 0;
        public int kills = 0;
        public int deaths = 0;
        public int minutes = 0;
        public int wins = 0;
        public int coins = 0;
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


    SpecPlayerKit specPlayerType;
    public PlayerScoreBoard playerScoreBoard;

    private final Map<Location, PlayerState> boom = new HashMap<>();
    public Map<UUID, String> assassinTargets = new HashMap<>();
    public Map<UUID, Integer> leprechaunOwners = new HashMap<>();
    public Map<UUID, Integer> thorOwners = new HashMap<>();


    public enum GameState {PREGAME, PEACETIME, FIGHTING, FINISHED}

    public enum PlayerState {SPECTATORS, RED, YELLOW, GREEN, BLUE}

    public enum PlayerJoinType {ANYONE, VIP, PRO, LEGENDARY, STAFF}

    private final List<Material> allowedFoods = new ArrayList<>();
    private final List<EntityType> allowedMobs = new ArrayList<>();
    private final List<Selection> selections = new ArrayList<>();
    private final List<Selection> cuboids = new ArrayList<>();
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
    public static String clans = "";
    public static int combatLogTimeInSeconds = 7;
    private final int restartTimer = 15;
    public boolean starting = false;
    private final Set<ProtectedContainer> protectedContainers = new HashSet<>();
    private final Map<UUID, Integer> quitters = new HashMap<>();
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
    public static final int coinsKillReward = 5;
    public static final int coinsWinReward = 25;
    private static final int proCoinMultiplier = 3;
    private static final int vipCoinMultiplier = 2;

    public HashMap<UUID, ArrayList<Entity>> droppedItems = new HashMap<>();
    public ArrayList<Entity> allitems = new ArrayList<>();


    @Override
    public void onEnable() {

        Bukkit.getWorlds().forEach(world -> {
            world.setGameRuleValue("doFireTick", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
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

            if (!this.getConfig().getString("clan1").equals("NN")) {
                Walls.teamsNames[1] = Walls.teamChatColors[1] + this.getConfig().getString("clan1");
            }
            if (!this.getConfig().getString("clan2").equals("NN")) {
                Walls.teamsNames[2] = Walls.teamChatColors[2] + this.getConfig().getString("clan2");
            }
            if (!this.getConfig().getString("clan3").equals("NN")) {
                Walls.teamsNames[3] = Walls.teamChatColors[3] + this.getConfig().getString("clan3");
            }
            if (!this.getConfig().getString("clan4").equals("NN")) {
                Walls.teamsNames[4] = Walls.teamChatColors[4] + this.getConfig().getString("clan4");
            }


            Walls.clans = this.getConfig().getString("clan1") + this.getConfig().getString("clan2") + this.getConfig().getString("clan3") + this.getConfig().getString("clan4");

        }

        World world = this.getServer().getWorlds().get(0);
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

        this.allowedFoods.add(Material.CARROT);
        this.allowedFoods.add(Material.CARROT_ITEM);

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

        specPlayerType = new SpecPlayerKit(this);

        playerScoreBoard = new PlayerScoreBoard(this);

        DeathMessages.initializeDeathMessages();

        this.getServer().getPluginManager().registerEvents(this, this);

        getCommand("shout").setExecutor(new ShoutCmd(this));
        getCommand("tp").setExecutor(new TPCmd(this));
        getCommand("walls").setExecutor(new WallsCmd(this));
        getCommand("giveall").setExecutor(new FullDiamondCmd(this));
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

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.clock = new Clock(this);

        this.myDB = new DatabaseUtil(this);


        if (Walls.advert != null && !Walls.advert.equals("")) {
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> Bukkit.getServer().getLogger().info(advert), 20L * 40, 20L * 240);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            changeLevelName(nextMap);
            freeUpSpace();
        }));
        nextMap = selectNextMap();
    }

    @Override
    public void onDisable() {
        this.clock.interrupt();
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
        switch (this.gameState) {
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

        if (isVIP(event.getPlayer().getUniqueId())) {
            this.specPlayerType.givePlayerKit(event.getPlayer());
            PlayerVisibility.makeSpecInvis(this, event.getPlayer());
            PlayerVisibility.makeSpecVisToSpecs(this, event.getPlayer());
        } else {
            final Player p = event.getPlayer();

            Notifier.notify(p, "You Died :( RIP. Want to fly spectate /surface /spawn ? Get Walls " + ChatColor.GREEN + "VIP" + ChatColor.WHITE + " at " + Walls.DISCORD);

        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {

        switch (this.gameState) {
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
        if (isStaff(event.getPlayer().getUniqueId())) {
            event.setJoinMessage("");
            Notifier.staff(this, event.getPlayer().getName() + " joined the server.");
            WallsPlayer tWP = this.getWallsPlayer(event.getPlayer().getUniqueId());
            this.getAllPlayers().put(event.getPlayer().getUniqueId(), tWP);
        } else {
            WallsPlayer tWP = this.getWallsPlayer(event.getPlayer().getUniqueId());

            if (Walls.clanBattle) {
                if (tWP != null && tWP.clan == null) {
                    Notifier.error(event.getPlayer(), "Sorry, this game is a Clan Battle!!");
                    return;
                }
                if (tWP != null && !Walls.clans.contains(tWP.clan)) {
                    Notifier.error(event.getPlayer(), "Sorry, this game is a Clan Battle!!");
                    return;
                }
            }

            if (playerJoinRestriction != PlayerJoinType.ANYONE) {

                boolean kick = false;

                switch (playerJoinRestriction) {
                    case VIP:
                        if (!this.isVIP(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                    case PRO:
                        if (!this.isPRO(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                    case LEGENDARY:
                        if (tWP != null && !tWP.legendary && !event.getPlayer().isOp() && !this.isStaff(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                    case STAFF:
                        if (!this.isStaff(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                }
                if (kick) {
                    Notifier.error(event.getPlayer(), "Sorry, this game is " + Walls.playerJoinRestriction.toString() + " only just now.");
                    return;
                }


            }

            if (this.gameState != GameState.PREGAME) {
                if (isSpec(event.getPlayer().getUniqueId()) && !this.isVIP(event.getPlayer().getUniqueId())) {
                    Notifier.error(event.getPlayer(), "Sorry, the game is already in progress, you need VIP and up to spectate - " + Walls.DISCORD + " !");
                    return;
                }
            }
            Notifier.broadcast(ChatColor.GRAY + event.getPlayer().getName() + " joined the server.");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.setJoinMessage("");

        this.getServer().getScheduler().runTaskLater(this, () -> Walls.this.wallsJoinMessage(event), 2 * 20L);

        WallsPlayer wallsPlayer = new WallsPlayer();

        switch (this.gameState) {
            case PREGAME:


                if (!players.containsKey(event.getPlayer().getUniqueId())) {
                    wallsPlayer.playerState = PlayerState.SPECTATORS;
                    this.players.put(event.getPlayer().getUniqueId(), wallsPlayer);
                }
                this.specPlayerType.givePlayerKit(event.getPlayer());
                event.getPlayer().setHealth(20);
                event.getPlayer().setFoodLevel(20);
                playerScoreBoard.setScoreBoard(event.getPlayer().getUniqueId());
                playerScoreBoard.updateScoreboardScores();

                if (!starting) {

                    if (this.players.size() > Walls.preGameAutoStartPlayers && !Walls.clanBattle && !Walls.tournamentMode) {
                        Notifier.broadcast("Game starts in " + ChatColor.LIGHT_PURPLE + preGameAutoStartSeconds + ChatColor.WHITE + " seconds!!");

                        this.clock.setClock(preGameAutoStartSeconds, () -> GameStarter.startGame(Walls.this.players, Walls.this));
                        this.starting = true;
                    }
                }
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:

                if (!this.quitters.containsKey(event.getPlayer().getUniqueId())) {
                    this.specPlayerType.givePlayerKit(event.getPlayer());

                    playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), PlayerState.SPECTATORS);
                    event.getPlayer().setHealth(20);
                    event.getPlayer().setFoodLevel(20);
                    PlayerVisibility.makeSpecInvis(this, event.getPlayer());
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

        if (!this.players.containsKey(event.getUniqueId())) {
            this.myDB.loadPlayer(event.getName(), event.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrop(PlayerDropItemEvent event) {
        if (this.gameState == GameState.PREGAME) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getLocation().getY() > buildHeight) event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        switch (this.gameState) {
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

                WallsPlayer deadWallsPlayer = this.players.get(event.getEntity().getUniqueId());
                deadWallsPlayer.deaths = 1;
                deadWallsPlayer.minutes = (this.clock.getSecondsRemaining() / 60);
                this.players.put(event.getEntity().getUniqueId(), deadWallsPlayer);

                if (deadWallsPlayer.vip) {
                    player.setAllowFlight(true);
                }

                if (Walls.clanBattle || Walls.tournamentMode) {
                    Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "whitelist remove " + player.getName());
                    Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "whitelist reload");
                    event.getEntity().kickPlayer("§cGG. No Specs in this game I'm afraid.");
                }


                this.foodTime = 0;
                if (this.foodDisabled) {
                    this.foodDisabled = false;
                    Notifier.broadcast("You can now eat again!");

                }

                WallsPlayer twp = this.players.get(player.getUniqueId());
                twp.playerState = PlayerState.SPECTATORS;
                player.closeInventory();
                player.getInventory().clear();
                this.players.put(player.getUniqueId(), twp);

                if (calculateTeamsLeft() < 2) {

                    this.gameState = GameState.FINISHED;

                    Notifier.broadcast("Server restarting in " + restartTimer + " seconds!");
                    Walls.this.getServer().getScheduler().scheduleSyncDelayedTask(Walls.this, () -> Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "stop Game restarting"), 20L * this.restartTimer);


                }
                playerScoreBoard.updateScoreboardScores();

                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {

        final Player player = event.getPlayer();
        final WallsPlayer twp = this.getWallsPlayer(player.getUniqueId());

        switch (this.gameState) {
            case PREGAME:
                event.getPlayer().getInventory().clear();
                this.players.remove(event.getPlayer().getUniqueId());
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:

                event.setQuitMessage("");

                if (!this.isSpec(event.getPlayer().getUniqueId())) {

                    if (this.inCombat.containsKey(event.getPlayer().getUniqueId())) {
                        Notifier.broadcast(event.getPlayer().getDisplayName() + " just left while in combat. (Grab their stuff! x:"
                                + event.getPlayer().getLocation().getBlockX()
                                + " y:" + event.getPlayer().getLocation().getBlockY()
                                + " z:" + event.getPlayer().getLocation().getBlockZ() + ")");
                        for (final ItemStack item : event.getPlayer().getInventory()) {
                            if (item != null) {
                                Walls.this.getServer().getWorld(Walls.levelName).dropItemNaturally(event.getPlayer().getLocation(), item);
                            }
                        }

                        HolographicDisplaysAPI.get(this).createHologram(player.getLocation().add(0, 2.5, 0)).getLines().appendItem(new ItemStack(Material.RAW_CHICKEN));
                        Hologram tempHologram = HolographicDisplaysAPI.get(this).createHologram(player.getLocation().add(0, 2, 0));
                        tempHologram.getLines().appendText(Walls.teamChatColors[twp.playerState.ordinal()] + player.getDisplayName());
                        tempHologram.getLines().appendText(ChatColor.WHITE + " COMBAT LOGGED HERE");
                        tempHologram.getLines().appendText("Chicken.");


                        this.inCombat.remove(event.getPlayer().getUniqueId());


                        Walls.this.foodTime = 0;
                        if (Walls.this.foodDisabled) {
                            Walls.this.foodDisabled = false;
                            Notifier.broadcast("You can now eat again!");

                        }

                        twp.playerState = PlayerState.SPECTATORS;
                        twp.deaths = twp.deaths + 5;
                        twp.kills = 0;
                        twp.wins = 0;
                        player.closeInventory();
                        player.getInventory().clear();
                        Walls.this.players.put(player.getUniqueId(), twp);

                        playerScoreBoard.removePlayerFromTeam(event.getPlayer().getUniqueId());
                        playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), PlayerState.SPECTATORS);
                        playerScoreBoard.updateScoreboardScores();

                        if (Walls.clanBattle || Walls.tournamentMode) {
                            Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "whitelist remove " + player.getName());
                            Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "whitelist reload");
                        }


                        if (calculateTeamsLeft() < 2) {

                            Walls.this.gameState = GameState.FINISHED;

                            Notifier.broadcast("Server restarting in " + Walls.this.restartTimer + " seconds!");

                            Walls.this.getServer().getScheduler().scheduleSyncDelayedTask(Walls.this, () -> Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "stop Game restarting"), 20L * Walls.this.restartTimer);


                        }


                    } else {

                        Notifier.broadcast(event.getPlayer().getDisplayName() + " may have just left the game..");

                        if (!this.inventory.containsKey(player.getUniqueId())) {
                            this.inventory.put(player.getUniqueId(), player.getInventory());
                        }
                        this.quitters.put(event.getPlayer().getUniqueId(), 0);


                        BukkitTask newQuitterTask = new BukkitRunnable() {
                            @Override
                            public void run() {

                                if (Walls.this.quitters.containsKey(player.getUniqueId())) {

                                    Walls.this.playerScoreBoard.removePlayerFromTeam(event.getPlayer().getUniqueId());
                                    Walls.this.playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), PlayerState.SPECTATORS);

                                    Walls.this.quitters.remove(player.getUniqueId());


                                    if (!Walls.this.getServer().getOfflinePlayer(event.getPlayer().getUniqueId()).isOnline()) {
                                        for (final ItemStack item : Walls.this.inventory.get(event.getPlayer().getUniqueId())) {
                                            if (item != null) {
                                                Walls.this.getServer().getWorld(Walls.levelName).dropItemNaturally(event.getPlayer().getLocation(), item);
                                            }
                                        }
                                        Notifier.broadcast("Yup " + event.getPlayer().getDisplayName()
                                                + " left the game.. (Grab their stuff! x:"
                                                + event.getPlayer().getLocation().getBlockX()
                                                + " y:" + event.getPlayer().getLocation().getBlockY()
                                                + " z:" + event.getPlayer().getLocation().getBlockZ() + ")");
                                    }
                                    Walls.this.foodTime = 0;
                                    if (Walls.this.foodDisabled) {
                                        Walls.this.foodDisabled = false;
                                        Notifier.broadcast("You can now eat again!");

                                    }

                                    WallsPlayer twp = Walls.this.players.get(player.getUniqueId());
                                    twp.playerState = PlayerState.SPECTATORS;
                                    player.closeInventory();
                                    player.getInventory().clear();
                                    Walls.this.players.put(player.getUniqueId(), twp);

                                    if (Walls.clanBattle || Walls.tournamentMode) {
                                        Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "whitelist remove " + player.getName());
                                        Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "whitelist reload");
                                    }


                                    if (calculateTeamsLeft() < 2) {

                                        Walls.this.gameState = GameState.FINISHED;

                                        Notifier.broadcast("Server restarting in " + Walls.this.restartTimer + " seconds!");
                                        Walls.this.getServer().getScheduler().scheduleSyncDelayedTask(Walls.this, () -> Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "stop Game restarting"), 20L * Walls.this.restartTimer);


                                    }


                                }

                            }
                        }.runTaskLater(this, relogTime * 20L);
                        this.quitterTasks.put(event.getPlayer().getUniqueId(), newQuitterTask);
                    }


                } else {
                    event.getPlayer().getInventory().clear();
                }

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

        switch (this.gameState) {
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
                if (isSpec(event.getEntity().getUniqueId())) {
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
                    if (this.isSpec(event.getEntity().getUniqueId())) {
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
                    if (this.isSpec(event.getEntity().getUniqueId())) {
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
                    if (this.isSpec(event.getEntity().getUniqueId())) {
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


        switch (this.gameState) {
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

                if (entityDamager instanceof Player && this.isSpec(entityDamager.getUniqueId())) {
                    event.setCancelled(true);
                    break;
                }

                if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                    if (isSpec(event.getEntity().getUniqueId()) || isSpec(event.getDamager().getUniqueId())) {
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
                        if (this.isSpec(entity.getUniqueId())) {
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
                WallsPlayer twp = this.getWallsPlayer(event.getPlayer().getUniqueId());
                twp.compassPointsToEnemy = !twp.compassPointsToEnemy;
                this.players.put(event.getPlayer().getUniqueId(), twp);

                ItemMeta compass = Bukkit.getServer().getItemFactory().getItemMeta(Material.COMPASS);

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

                final List<Player> fightingPlayers = new ArrayList<>();
                for (final Player player : Walls.this.getServer().getOnlinePlayers()) {
                    if (!Walls.this.isSpec(player.getUniqueId())) {
                        fightingPlayers.add(player);
                    }
                }

                try {
                    Walls.this.processCompass(event.getPlayer(), fightingPlayers);
                } catch (Exception e) {
                    Walls.this.getLogger().info("Could not process compass so skipping it..");
                }


            }
        }
    }


    public void kickOffCombatThread() {

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            List<UUID> markForRemoval = new ArrayList<>();

            for (final UUID pUID : Walls.this.inCombat.keySet()) {

                if ((System.currentTimeMillis() - Walls.this.inCombat.get(pUID)) > (Walls.combatLogTimeInSeconds * 1000L)) {
                    markForRemoval.add(pUID);
                }

            }
            for (final UUID pUID : markForRemoval) {
                Walls.this.inCombat.remove(pUID);
                Player p = Bukkit.getPlayer(pUID);
                if (p != null) {
                    Notifier.success(Bukkit.getPlayer(pUID), "You are now no longer in combat !");
                }
            }
        }, 0L, 20L);
    }

    private void checkForContainerProtection(PlayerInteractEvent event) {


        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            final Material clickedMaterial = event.getClickedBlock().getType();
            if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)
                    || (clickedMaterial == Material.ENCHANTMENT_TABLE) || (clickedMaterial == Material.WORKBENCH)) {

                final Location location = event.getClickedBlock().getLocation();

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
        switch (this.gameState) {
            case PEACETIME:
            case FIGHTING:
                if ((event.getAction() == Action.LEFT_CLICK_BLOCK) || (event.getAction() == Action.RIGHT_CLICK_BLOCK) && !this.isSpec(event.getPlayer().getUniqueId())) {
                    final Block block = event.getClickedBlock();
                    if ((block != null) && (block.getType() == Material.WOOD_BUTTON || block.getType() == Material.STONE_BUTTON) && !this.isSpec(event.getPlayer().getUniqueId())) {
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
        if (isSpec(event.getPlayer().getUniqueId())) {
            if (event.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) {
                Inventory chest = Bukkit.getServer().createInventory(null, 6 * 9, "Player Finder :)");
                for (UUID pUID : this.players.keySet()) {
                    if (!this.isSpec(pUID)) {
                        ItemStack newSkull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                        chest.addItem(ItemStackTools.changeItemName(newSkull, Bukkit.getOfflinePlayer(pUID).getName()));
                    }
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
        switch (this.gameState) {
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

                        WallsPlayer twp = getWallsPlayer(event.getPlayer().getUniqueId());

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

                if (event.getAction() == Action.PHYSICAL && this.isSpec(event.getPlayer().getUniqueId())) {
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

                if (event.getAction() == Action.PHYSICAL && this.isSpec(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }


                if (event.getAction() == Action.PHYSICAL) {
                    if (isSpec(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        return;
                    }
                    if (event.getClickedBlock().getType() == Material.TRIPWIRE && event.getClickedBlock().getLocation().distance(new Location(event.getClickedBlock().getWorld(), -3, 51, 129)) < 10 && !this.isSpec(event.getPlayer().getUniqueId())) {
                        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(event.getPlayer().getDisplayName() + ChatColor.DARK_PURPLE + " is at THE CENTER TRAP trying to take stuff!!"));
                    }
                }

            case FINISHED:
                checkSpecPlayerFinder(event);

                handleKitInteraction(event);
                if (isSpec(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
                if (this.foodDisabled) {
                    final Player player = event.getPlayer();
                    final Material item = player.getItemInHand().getType();
                    if (!this.allowedFoods.contains(item) && item.isEdible()) {
                        player.sendMessage(ChatColor.RED + "Food is currently disabled!");
                        event.setCancelled(true);
                        return;
                    }
                }
                break;
            default:
                break;
        }

    }

    private void handleKitInteraction(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final ItemStack stack = player.getItemInHand();
        if (event.getAction() == Action.PHYSICAL) {
            if ((block != null) && (block.getType() == Material.STONE_PLATE)
                    && (block.getRelative(0, -1, 0).getType() == Material.GRAVEL)) {
                final PlayerState team = this.boom.get(block.getLocation());
                if ((team != null) && team != this.players.get(player.getUniqueId()).playerState) {
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

        switch (this.gameState) {
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
                            this.getServer().getScheduler().runTaskLater(this, () -> {

                                if (!(Walls.this.droppedItems.get(p.getUniqueId())).isEmpty()) {
                                    Iterator<Entity> iterator = Walls.this.droppedItems.get(p.getUniqueId()).iterator();
                                    Entity next1 = iterator.next();
                                    Walls.this.droppedItems.get(p.getUniqueId()).remove(next1);
                                    Walls.this.allitems.remove(next1);
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
                if (!this.isSpec(event.getPlayer().getUniqueId())) {
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
                if (!this.isSpec(event.getPlayer().getUniqueId())) {
                    if (event.getTo().getBlockY() > (Walls.gameSpawn.getBlockY() - 4)) {
                        event.setCancelled(true);
                        Notifier.error(event.getPlayer(), "Trying to escape :( try /spawn");
                        return;
                    }
                }

                if (this.isInASpawn(event.getTo()) && event.getTo().getBlockY() > 82 && !this.isSpec(event.getPlayer().getUniqueId())) {
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
        switch (this.gameState) {
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
        switch (this.gameState) {
            case PREGAME:
            case PEACETIME:
            case FINISHED:
                event.setCancelled(true);
                break;
            case FIGHTING:
                if (isSpec(event.getEntity().getUniqueId())) {
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
        switch (this.gameState) {
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

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (isSpec(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        switch (this.gameState) {
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
            switch (this.gameState) {
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
        switch (this.gameState) {
            case PREGAME:
                if (!event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) event.setCancelled(true);
                break;
            case PEACETIME:
            case FIGHTING:
                if (!(event.getWhoClicked() instanceof Player)) return;
                if (!this.isSpec((event.getWhoClicked()).getUniqueId())) return;
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
                if (this.isSpec((event.getWhoClicked()).getUniqueId())) event.setCancelled(true);
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
        event.setCancelled(event.toWeatherState());
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
            if ((event.getInventory().getType() != InventoryType.PLAYER) && isSpec((event.getPlayer().getUniqueId()))) {
                if (!event.getInventory().getTitle().equals("Player Finder :)")) {
                    event.setCancelled(true);
                }
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
            if (this.isPRO(event.getPlayer().getUniqueId())) {
                max = 7;
            } else if (this.isVIP(event.getPlayer().getUniqueId())) {
                max = 5;
            }

            if (owncount < max) {
                this.protectedContainers.add(new ProtectedContainer(location, event.getPlayer().getName()));
                Notifier.success(event.getPlayer(), "Protection extended! You have protected " + owncount + " containers");
            } else {
                Notifier.error(event.getPlayer(), ChatColor.RED + "You have reached your protection limit!");
                Notifier.error(event.getPlayer(), ChatColor.RED + "Unprotect other owned containers");
                if (!this.isVIP(event.getPlayer().getUniqueId())) {
                    Notifier.notify(event.getPlayer(), ChatColor.AQUA + "VIP & PRO get more protected containers! " + Walls.DISCORD);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (isSpec(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlock().getLocation().getBlockY() > Walls.buildHeight) {
            event.setCancelled(true);
            if (gameState.equals(GameState.PREGAME)) return;
            Notifier.error(event.getPlayer(), "You reached the build limit and cannot place blocks here!");
            return;
        }
        if ((event.getBlock().getX() < -142 || event.getBlock().getX() > 137) || (event.getBlock().getZ() < -10 || event.getBlock().getZ() > 269)) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlockPlaced().getType() == Material.CARROT) {
            Notifier.error(event.getPlayer(), "You don't look like a rabbit.. why you need to farm carrots?");
            event.setCancelled(true);
            return;
        }
        switch (this.gameState) {
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
                    this.boom.put(event.getBlock().getLocation(), this.players.get(event.getPlayer().getUniqueId()).playerState);
                }


                break;
            default:
                break;
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        switch (this.gameState) {
            case PREGAME:
                event.setCancelled(true);
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                if (isSpec(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void NoTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            if (isSpec(event.getTarget().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        switch (this.gameState) {
            case PREGAME:
                event.setCancelled(true);
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                if (isSpec(event.getPlayer().getUniqueId())) {
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
        this.gameState = GameState.FIGHTING;


        final List<Selection> toRemove = new ArrayList<>();
        for (final Selection s : this.selections) {
            if (s.getType() == 1) {
                s.remove(this.getServer().getWorld(Walls.levelName));
                toRemove.add(s);
            }
        }
        this.selections.removeAll(toRemove);

        Notifier.broadcast("The walls are now gone!");
        this.checkForHunger();
        this.clock.abort();
        this.kickOffNoWinnerThread();
        this.kickOffCombatThread();
        this.getServer().getScheduler().runTaskLater(this, () -> {
            if (!Walls.this.leprechaunOwners.isEmpty()) {
                Walls.this.leprechaunOwners.clear();
                Notifier.broadcast("Leprechaun kit just lost power :(");
            }
        }, 1800 * 20);
    }

    public WallsPlayer getWallsPlayer(UUID uid) {
        return this.players.getOrDefault(uid, null);
    }

    public List<UUID> getStaffList() {
        List<UUID> wp = new ArrayList<>();
        for (UUID u : this.players.keySet()) {
            if (this.isStaff(u)) wp.add(u);
        }
        return wp;
    }

    public List<UUID> getTeamList(UUID uid) {
        return getTeamList(this.players.get(uid).playerState);
    }

    public List<UUID> getTeamList(PlayerState playerState) {
        List<UUID> teamList = new ArrayList<>();
        for (UUID u : this.players.keySet()) {
            if (this.players.get(u).playerState == playerState) {
                teamList.add(u);
            }
        }
        return teamList;
    }

    public void printTeamMates(Player p, PlayerState ps) {
        List<UUID> teamUIDS = this.getTeamList(ps);
        if (!teamUIDS.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (UUID pUID : teamUIDS) names.add(Bukkit.getOfflinePlayer(pUID).getName());
            Notifier.notify(p, Walls.teamChatColors[ps.ordinal()] + String.join("§7, " + Walls.teamChatColors[ps.ordinal()], names));
        } else {
            Notifier.notify(p, "§cTeam " + Walls.teamsNames[ps.ordinal()] + "§c is empty");
        }

    }

    public int getTeamSize(PlayerState ps) {

        int teamCounter = 0;

        for (WallsPlayer wp : this.players.values()) {
            if (wp.playerState.compareTo(ps) == 0) {
                teamCounter++;
            }
        }
        return teamCounter;
    }

    public boolean isSpec(UUID a) {
        if (players.containsKey(a)) return (players.get(a).playerState.compareTo(PlayerState.SPECTATORS) == 0);
        return false;
    }

    public boolean isStaff(UUID a) {
        return players.containsKey(a) && (players.get(a).gm || players.get(a).mgm || players.get(a).admin || players.get(a).owner);
    }

    public boolean isMGM(UUID a) {
        return players.containsKey(a) && (players.get(a).mgm || players.get(a).admin || players.get(a).owner);
    }

    public boolean isVIP(UUID a) {
        WallsPlayer twp = players.get(a);
        return twp.vip || isPRO(a);
    }

    public boolean isPRO(UUID a) {
        WallsPlayer twp = players.get(a);
        return twp.pro || isLEGENDARY(a);
    }

    public boolean isLEGENDARY(UUID a) {
        WallsPlayer twp = players.get(a);
        return twp.legendary || isStaff(a);
    }

    public boolean sameTeam(UUID a, UUID b) {
        return this.players.get(a).playerState.compareTo(this.players.get(b).playerState) == 0;
    }

    public void setGameState(GameState gs) {
        this.gameState = gs;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public int calculateTeamsLeft() {
        int tempNumberOfTeams = 0;
        int t1 = 0;
        int t2 = 0;
        int t3 = 0;
        int t4 = 0;
        for (WallsPlayer wp : this.players.values()) {
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
                    WallsPlayer wallsWinner = this.getWallsPlayer(winner);
                    wallsWinner.wins = 1;
                    wallsWinner.minutes = (this.clock.getSecondsRemaining() / 60);
                    wallsWinner.coins = wallsWinner.coins + Walls.coinsWinReward;
                }
                this.myDB.saveAllData();
            }
        }
        return this.teams;
    }

    public Map<UUID, WallsPlayer> getAllPlayers() {
        return this.players;
    }

    public int getNumberOfPlayers() {
        return this.players.size();
    }

    public int getWinningTeam() {
        return this.winningTeam;
    }

    public final void defineArena() {

        Selection c;
        c = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        c.setPointA(-160, 0, -29);
        c.setPointB(-15, 0, 117);
        this.cuboids.add(c);

        c = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        c.setPointA(-160, 0, 285);
        c.setPointB(-15, 0, 142);
        this.cuboids.add(c);

        c = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        c.setPointA(155, 0, 287);
        c.setPointB(10, 0, 142);
        this.cuboids.add(c);

        c = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        c.setPointA(159, 0, -31);
        c.setPointB(10, 0, 117);
        this.cuboids.add(c);

//        // Ice center
//        this.center = new Selection(this.getServer().getWorld(Walls.levelName).getName());
//        this.center.setPointA(0, 65, 127);
//        this.center.setPointB(-5, 82, 132);
//
//        // Arena
//        this.arena = new Selection(this.getServer().getWorld(Walls.levelName).getName());
//        this.arena.setPointA(-141, 0, -9);// lower left corner
//        this.arena.setPointB(136, buildHeight, 268);// upper right corner

        Selection s;

        // Lobby
        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(-20, 198, 112);// lower left corner
        s.setPointB(15, 204, 147);// upper right corner
        this.selections.add(s);

        // Logo
        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(42, 186, 130);// lower left corner
        s.setPointB(-52, 168, 129);// upper right corner
        this.selections.add(s);

        // walls
        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(-14, 129, -10);// lower left corner
        s.setPointB(-14, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(9, 129, -10);// lower left corner
        s.setPointB(9, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(10, 129, 118);// lower left corner
        s.setPointB(137, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(10, 129, 141);// lower left corner
        s.setPointB(137, 62, 141);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(9, 129, 141);// lower left corner
        s.setPointB(9, 62, 269);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(-14, 129, 141);// lower left corner
        s.setPointB(-14, 62, 269);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(-15, 129, 141);// lower left corner
        s.setPointB(-142, 62, 141);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(-15, 129, 118);// lower left corner
        s.setPointB(-142, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        // Starting pads

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(-156, 77, -26);// lower left corner
        s.setPointB(-135, 62, -3);// upper right corner
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(150, 85, -23);// lower left corner
        s.setPointB(131, 62, -4);// upper right corner
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
        s.setPointA(152, 77, 284);// lower left corner
        s.setPointB(131, 62, 263);// upper right corner
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld(Walls.levelName).getName());
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

    public int getCoinKillMultiplier(UUID pUID) {
        if (this.players.get(pUID).pro) {
            return proCoinMultiplier;
        } else if (this.players.get(pUID).vip) {
            return vipCoinMultiplier;
        }
        return 1; // standard multiplier is just 1
    }

    private boolean loreMatch(ItemStack item, String line) {
        if ((item == null) || !item.hasItemMeta() || (item.getItemMeta().getLore() == null)) {
            return false;
        }
        return item.getItemMeta().getLore().contains(line);
    }

    private void kickOffNoWinnerThread() {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

            if (Walls.this.calculateTeamsLeft() < 2 && Walls.this.gameState != GameState.FINISHED && Walls.this.gameState != GameState.PREGAME) {
                Walls.this.getServer().dispatchCommand(Walls.this.getServer().getConsoleSender(), "stop Game restarting");
            }


        }, 20L * 120, 20L * 120);

    }

    public void kickOffCompassThread() {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            final List<Player> fightingPlayers = new ArrayList<>();
            for (final Player player : Walls.this.getServer().getOnlinePlayers()) {
                if (!Walls.this.isSpec(player.getUniqueId())) {
                    fightingPlayers.add(player);
                }
            }
            for (final Player player : Walls.this.getServer().getOnlinePlayers()) {
                if (fightingPlayers.size() > 0 && player != null) {
                    try {
                        Walls.this.processCompass(player, fightingPlayers);
                    } catch (Exception e) {
                        Walls.this.getLogger().info("Could not process compass so skipping it..");
                    }
                } else {

                    if (Walls.debugMode) {
                        Walls.this.getLogger().info("Processing compass - fightingPlayersSize = " + fightingPlayers.size());
                        if (player == null) {
                            Walls.this.getLogger().info("Processing compass - player = null");
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
            if ((targetPlayer != null) && (this.sameTeam(player.getUniqueId(), player.getUniqueId())
                    && !this.isSpec(targetPlayer.getUniqueId()))) {
                target = targetPlayer.getLocation();
            }
        } else {
            final Location location = player.getLocation();
            Location closestloc = player.getLocation();
            double closestdis = 0xFFFFFF;

            for (final Player potentialEnemy : playerList) {

                if (!potentialEnemy.equals(player)) {

                    if (this.players.containsKey(potentialEnemy.getUniqueId())) {

                        if (this.getWallsPlayer(player.getUniqueId()).compassPointsToEnemy) {
                            if (!this.isSpec(potentialEnemy.getUniqueId()) && !this.sameTeam(player.getUniqueId(), potentialEnemy.getUniqueId())) {

                                final Location temploc = potentialEnemy.getLocation();
                                final double tempdis = location.distanceSquared(temploc);
                                if (tempdis < closestdis) {
                                    closestdis = tempdis;
                                    closestloc = temploc;
                                }

                            }
                        } else {
                            if (!this.isSpec(potentialEnemy.getUniqueId()) && this.sameTeam(player.getUniqueId(), potentialEnemy.getUniqueId())) {

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
                Walls.this.getLogger().info("Target found and set for player " + player.getName());
            }
        } else {
            if (Walls.debugMode) {
                Walls.this.getLogger().info("Target for compass was NULL ??!?");
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

    private void checkForHunger() {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (!Walls.this.foodDisabled) {
                Walls.this.foodTime = Walls.this.foodTime + 5;
                if (Walls.this.foodTime == 300) {
                    Walls.this.foodDisabled = true;
                    Walls.this.foodTime = 0;
                    Notifier.broadcast("Food disabled until next kill!");
                }
            }
        }, 20L * 30, 20L * 5);
    }

    public boolean checkEnoughSpaceInTeam(int teamNumber) {
        if (Walls.tournamentMode) {
            return true;
        }
        int extraTeamAllowance = 2;
        return (this.getTeamSize(PlayerState.values()[teamNumber]) < ((this.getAllPlayers().size() / 4) + extraTeamAllowance));
    }
}
