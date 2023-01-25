package me.glennEboy.Walls;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.glennEboy.Walls.commands.*;
import me.glennEboy.Walls.kits.SpecPlayerKit;
import me.glennEboy.Walls.utils.*;
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

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.lang.Math;


public class TheWalls extends JavaPlugin implements Listener {

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
        public boolean youtuber = false;
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
        public int curseCount = 0;
        public PlayerState playerState = PlayerState.SPEC;
    }

    public static String chatPrefix = ChatColor.GOLD + "" + ChatColor.BOLD + "Walls " + ChatColor.RESET;
    public static final String STAFFCHATT_PREFIX = ChatColor.RED + "[" + ChatColor.AQUA + "StaffChat" + ChatColor.RED + "] ";
    public static final String CLANCHAT_PREFIX = ChatColor.RED + "[" + ChatColor.DARK_AQUA + "??" + ChatColor.RED + "] ";
    public static final String OPCHAT_PREFIX = ChatColor.RED + "[" + ChatColor.RED + "OPCHAT" + ChatColor.RED + "] ";
    public static String teamsNames[] = {ChatColor.LIGHT_PURPLE + "Specs", ChatColor.RED + "T1", ChatColor.YELLOW + "T2", ChatColor.GREEN + "T3", ChatColor.BLUE + "T4"};
    public static ChatColor teamChatColors[] = {ChatColor.LIGHT_PURPLE, ChatColor.RED, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.BLUE};
    public static String scCode = "[sc]";
    public static boolean showCPS = false;

    private final Map<UUID, Integer> mutedPlayers = new HashMap<>();
    private final Map<UUID, WallsPlayer> players = new HashMap<>();

    private final Map<UUID, Long> inCombat = new HashMap<>();

    private final Map<UUID, Long> autoClickChecker = new HashMap<>();
    private final Map<UUID, Long> autoClickCheckTimeSinceLastCancel = new HashMap<>();

    private final Map<UUID, Integer> playerCPSCounter = new HashMap<>();
    public final Map<UUID, Integer> playerMaxCPSRate = new HashMap<>();
    public final Map<UUID, String> playerLastCPSRate = new HashMap<>();


    public final Map<UUID, UUID> whispers = new HashMap<>();
    public final Map<UUID, String> clanInvites = new HashMap<>();

    public final List<UUID> staffListSnooper = new ArrayList<>();
    public final List<UUID> noStaffChat = new ArrayList<>();

    public static final List<String> specialGMs = new ArrayList<>();
    public static final List<String> knownPlayers = new ArrayList<>();

    public static final List<String> teamCaptains = new ArrayList<>();

    public static String logPlayer = null;


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

    public enum PlayerState {SPEC, TEAM1, TEAM2, TEAM3, TEAM4}

    public enum PlayerJoinType {ANYONE, VIP, PRO, LEGENDARY, STAFF, YOUTUBER}
    private final List<Material> allowedFoods = new ArrayList<>();
    private final List<EntityType> allowedMobs = new ArrayList<>();
    private final List<Selection> selections = new ArrayList<>();
    private final List<Selection> cuboids = new ArrayList<>();
    public Clock clock;
    private GameState gameState = GameState.PREGAME;
    private final Map<UUID, PlayerInventory> inventory = new HashMap<>();
    private KitCmd myKitCmd;
    public byte[] lobbyBytes;
    public static boolean debugMode = false;
    public static boolean UHC = false;
    public static int peaceTimeMins = 15;
    public static int preGameAutoStartPlayers = 11;
    public static int preGameAutoStartSeconds = 120;
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
    public static int MaxAllowedCPS = 25;
    public static int combatLogTimeInSeconds = 7;
    private final int restartTimer = 15;
    public boolean starting = false;
    private final Set<ProtectedContainer> protectedContainers = new HashSet<>();
    private final Map<UUID, Integer> quitters = new HashMap<>();
    private final Map<UUID, BukkitTask> quitterTasks = new HashMap<>();
    public static final Random random = new Random();
    private int relogTime = 60;
    public int cpsTrigger = 60;
    public int cpsRepeatCancelTrigger = 500;
    private int teams = 4;
    private int winningTeam = 0;
    private boolean fenceGlitch = false;

    public DatabaseUtil myDB;

    private boolean foodDisabled = false;
    private int foodTime = 0;
    public static final int buildHeight = 180;
    public static final int liquidBuildHeight = 170;
    public static final int coinsKillReward = 5;
    public static final int coinsWinReward = 25;
    private static final int proCoinMultiplier = 3;
    private static final int vipCoinMultiplier = 2;
    private static final float proCoinWinMultiplier = 1.5f;
    private static final float vipCoinWinMultiplier = 1.2f;

    public HashMap<UUID, ArrayList<Entity>> droppedItems = new HashMap<>();
    public ArrayList<Entity> allitems = new ArrayList<>();


    @Override
    public void onEnable() {

        TheWalls.debugMode = this.getConfig().getBoolean("debugMode");
        TheWalls.UHC = this.getConfig().getBoolean("UHCMode");
        TheWalls.peaceTimeMins = this.getConfig().getInt("peaceTimeMins");
        TheWalls.preGameAutoStartPlayers = this.getConfig().getInt("preGameAutoStartPlayers");
        TheWalls.preGameAutoStartSeconds = this.getConfig().getInt("preGameAutoStartSeconds");
        this.relogTime = this.getConfig().getInt("relogTime");
        this.cpsTrigger = this.getConfig().getInt("cpsTrigger");
        this.cpsRepeatCancelTrigger = this.getConfig().getInt("cpsRepeatCancelTrigger");
        TheWalls.fullDiamond = this.getConfig().getBoolean("fullDiamond");
        TheWalls.diamondONLY = this.getConfig().getBoolean("diamondONLY");
        TheWalls.ironONLY = this.getConfig().getBoolean("ironONLY");
        TheWalls.lobbyTrail = this.getConfig().getString("lobbyTrail");
        TheWalls.clanBattle = this.getConfig().getBoolean("clanBattle");
        TheWalls.tournamentMode = this.getConfig().getBoolean("tournamentMode");
        TheWalls.playerJoinRestriction = PlayerJoinType.valueOf(this.getConfig().getString("playerJoinRestriction"));
        TheWalls.allowPickTeams = this.getConfig().getBoolean("allowPickTeams");
        TheWalls.MaxAllowedCPS = this.getConfig().getInt("MaxAllowedCPS");
        TheWalls.combatLogTimeInSeconds = this.getConfig().getInt("combatLogTimeInSeconds");


        TheWalls.advert = this.getConfig().getString("wallsAdvert");

        if (TheWalls.clanBattle || TheWalls.tournamentMode) {

            if (!this.getConfig().getString("clan1").equals("NN")) {
                TheWalls.teamsNames[1] = TheWalls.teamChatColors[1] + this.getConfig().getString("clan1");
            }
            if (!this.getConfig().getString("clan2").equals("NN")) {
                TheWalls.teamsNames[2] = TheWalls.teamChatColors[2] + this.getConfig().getString("clan2");
            }
            if (!this.getConfig().getString("clan3").equals("NN")) {
                TheWalls.teamsNames[3] = TheWalls.teamChatColors[3] + this.getConfig().getString("clan3");
            }
            if (!this.getConfig().getString("clan4").equals("NN")) {
                TheWalls.teamsNames[4] = TheWalls.teamChatColors[4] + this.getConfig().getString("clan4");
            }


            TheWalls.clans = this.getConfig().getString("clan1") + this.getConfig().getString("clan2") + this.getConfig().getString("clan3") + this.getConfig().getString("clan4");

        }

        TheWalls.gameSpawn = new Location(this.getServer().getWorlds().get(0), -2, 247, 130);

        TheWalls.team1Spawn = new Location(this.getServer().getWorlds().get(0), -149, 65, -17);
        TheWalls.team2Spawn = new Location(this.getServer().getWorlds().get(0), 144, 65, -17);
        TheWalls.team3Spawn = new Location(this.getServer().getWorlds().get(0), 144, 65, 276);
        TheWalls.team4Spawn = new Location(this.getServer().getWorlds().get(0), -149, 65, 276);
        spawns.add(TheWalls.gameSpawn);
        spawns.add(TheWalls.team1Spawn);
        spawns.add(TheWalls.team2Spawn);
        spawns.add(TheWalls.team3Spawn);
        spawns.add(TheWalls.team4Spawn);

        TheWalls.team1Corner = new Location(this.getServer().getWorlds().get(0), -23, 62, 108);
        TheWalls.team2Corner = new Location(this.getServer().getWorlds().get(0), 19, 62, 108);
        TheWalls.team3Corner = new Location(this.getServer().getWorlds().get(0), 19, 62, 151);
        TheWalls.team4Corner = new Location(this.getServer().getWorlds().get(0), -23, 62, 151);
        corners.add(TheWalls.gameSpawn);
        corners.add(TheWalls.team1Corner);
        corners.add(TheWalls.team2Corner);
        corners.add(TheWalls.team3Corner);
        corners.add(TheWalls.team4Corner);

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
        getCommand("team").setExecutor(new TeamCmd(this));
        getCommand("teamlist").setExecutor(new TeamListCmd(this));
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
        getCommand("oc").setExecutor(new OpChatCmd(this));
        getCommand("cc").setExecutor(new ClanChatCmd(this));
        getCommand("share").setExecutor(new ShareCmd(this));
        getCommand("clan").setExecutor(new ClanCmd(this));

        defineArena();

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream(12);
        final DataOutputStream stream = new DataOutputStream(bytes);
        try {
            stream.writeUTF("Connect");
            stream.writeUTF("lobby");
        } catch (final IOException e) {
            Bukkit.getLogger().warning(String.format("Exception: %s", e.getMessage()));
        }
        this.lobbyBytes = bytes.toByteArray();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.clock = new Clock(this);

        this.myDB = new DatabaseUtil(this);



        if (TheWalls.advert != null && !TheWalls.advert.equals("")) {
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> Bukkit.getServer().getLogger().info(advert), 20L * 40, 20L * 240);
        }
        getSpecialGMs();
        getPlayers();
    }

    @Override
    public void onDisable() {

        this.clock.interrupt();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd(this.gameState.name() + " " + this.getServer().getOnlinePlayers().size() + " players");
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        if (event.getLocation().getBlockY() > (TheWalls.gameSpawn.getBlockY() - 5)) {
            event.setCancelled(true);
        }
        if ((event.getSpawnReason() == SpawnReason.NATURAL) && !this.allowedMobs.contains(event.getEntityType())) {
            event.setCancelled(true);
        } else if (event.getSpawnReason() == SpawnReason.BUILD_WITHER) {
            event.setCancelled(true);
        }

    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(TheWalls.gameSpawn);

        if (isVIP(event.getPlayer().getUniqueId())) {
            this.specPlayerType.givePlayerKit(event.getPlayer());
            PlayerVisibility.makeSpecInvis(this, event.getPlayer());
            PlayerVisibility.makeSpecVisToSpecs(this, event.getPlayer());
        } else {
            final Player p = event.getPlayer();

            GameNotifications.sendPlayerSimpleMessage(p, "You Died :( RIP. Want to fly spectate /surface /spawn ? Get Walls " + ChatColor.GREEN + "VIP" + ChatColor.WHITE + " at MySite.COM");

            this.getServer().getScheduler().runTaskLater(this, () -> {
                if (p != null) {

                    p.sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                }
            }, 10 * 20);

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
                        GameNotifications.sendPlayerCommandError(event.getPlayer(), "You are not allowed to break blocks here.");
                        return;
                    }
                }

                if (this.isInASpawn(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "You are not allowed to break blocks here.");
                    return;
                }
                final Material clickedMaterial = event.getBlock().getType();
                if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)
                        || (clickedMaterial == Material.ENCHANTMENT_TABLE) || (clickedMaterial == Material.WORKBENCH)) {

//                final Location location = event.getBlock().getLocation();

                    final Iterator<ProtectedContainer> iterator = this.protectedContainers.iterator();
                    while (iterator.hasNext()) {
                        final ProtectedContainer container = iterator.next();
                        final boolean owned = container.getOwner().equals(event.getPlayer().getName());
                        if (owned) {
                            this.protectedContainers.remove(container);
                            return;
                        }
                    }
                }


                if ((block.getType() == Material.IRON_ORE) || (block.getType() == Material.GOLD_ORE)
                        || (block.getType() == Material.DIAMOND_ORE) || (block.getType() == Material.EMERALD_ORE)) {
                    if (TheWalls.random.nextDouble() < 0.2D) {
                        if (leprechaunOwners.containsKey(event.getPlayer().getUniqueId())) {
                            checkLeprechaunDrop(block);
                        }
                    }
                }
            case FIGHTING:

                if (this.isInASpawn(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "You are not allowed to break blocks here.");
                    return;
                }

                block = event.getBlock();
                if ((block.getType() == Material.IRON_ORE) || (block.getType() == Material.GOLD_ORE)
                        || (block.getType() == Material.DIAMOND_ORE) || (block.getType() == Material.EMERALD_ORE)) {
                    if (TheWalls.random.nextDouble() < 0.2D) {
                        if (leprechaunOwners.containsKey(event.getPlayer().getUniqueId())) {
                            checkLeprechaunDrop(block);
                        }
                        return;
                    }
                }
                break;
            case FINISHED:
                break;
            default:
                break;
        }

    }

    private void wallsJoinMessage(PlayerJoinEvent event) {
        // call this method after 2 second delay 
        if (event.getPlayer().isOp()) {
            return;
        }
        if (isStaff(event.getPlayer().getUniqueId())) {
            event.setJoinMessage("");
            GameNotifications.staffNotification(this, event.getPlayer().getName() + " joined the server.");
            WallsPlayer tWP = this.getWallsPlayer(event.getPlayer().getUniqueId());
            this.getAllPlayers().put(event.getPlayer().getUniqueId(), tWP);
        } else {
            WallsPlayer tWP = this.getWallsPlayer(event.getPlayer().getUniqueId());

            if (TheWalls.clanBattle) {
                if (tWP != null && tWP.clan == null) {
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "Sorry, this game is a Clan Battle!!");
                    event.getPlayer().sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                    return;
                }
                if (tWP != null && TheWalls.clans.indexOf(tWP.clan) < 0) {
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "Sorry, this game is a Clan Battle!!");
                    event.getPlayer().sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                    return;
                }
            }

            if (playerJoinRestriction != PlayerJoinType.ANYONE) {

                boolean kick = false;

                switch (playerJoinRestriction) {
                    case VIP: // vip
                        if (!this.isVIP(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                    case PRO: // pro
                        if (!this.isPRO(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                    case LEGENDARY: // legendary
                        if (!tWP.legendary && !event.getPlayer().isOp() && !this.isStaff(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                    case STAFF: // staff
                        if (!this.isStaff(event.getPlayer().getUniqueId())) {
                            kick = true;
                        }
                        break;
                    case YOUTUBER: // youtuber
                        if (!tWP.youtuber && !event.getPlayer().isOp()) {
                            kick = true;
                        }
                        break;
                }
                if (kick) {
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "Sorry, this game is " + TheWalls.playerJoinRestriction.toString() + " only just now.");
                    event.getPlayer().sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                    return;
                }


            }

            if (this.gameState != GameState.PREGAME) {
                if (isSpec(event.getPlayer().getUniqueId()) && !this.isVIP(event.getPlayer().getUniqueId())) {
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "Sorry, the game is already in progress, you need VIP and up to spectate - www.Mysite.com/shop !");
                    event.getPlayer().sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                    return;
                }
            }
            GameNotifications.broadcastMessage(ChatColor.GRAY + event.getPlayer().getName() + " joined the server.");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.setJoinMessage("");

        this.getServer().getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                TheWalls.this.wallsJoinMessage(event);
            }
        }, 2 * 20L);

        WallsPlayer wallsPlayer = new WallsPlayer();

        this.getLogger().info(event.getPlayer().getName() + " logged in from " + event.getPlayer().getAddress().getHostName() + " with IP " + event.getPlayer().getAddress().getAddress().getHostAddress());


        switch (this.gameState) {
            case PREGAME:


                if (!players.containsKey(event.getPlayer().getUniqueId())) {
                    wallsPlayer.playerState = PlayerState.SPEC;
                    this.players.put(event.getPlayer().getUniqueId(), wallsPlayer);
                }
                this.specPlayerType.givePlayerKit(event.getPlayer());
                event.getPlayer().setHealth(20);
                event.getPlayer().setFoodLevel(20);
                playerScoreBoard.setScoreBoard(event.getPlayer().getUniqueId());
                playerScoreBoard.updateScoreboardScores();

                if (!starting) {

                    if (this.players.size() > TheWalls.preGameAutoStartPlayers && !TheWalls.clanBattle && !TheWalls.tournamentMode) {
                        GameNotifications.broadcastMessage("Game starts in " + ChatColor.LIGHT_PURPLE + preGameAutoStartSeconds + ChatColor.WHITE + " seconds!!");

                        this.clock.setClock(preGameAutoStartSeconds, new Runnable() {
                            @Override
                            public void run() {
                                GameStarter.startGame(TheWalls.this.players, TheWalls.this);
                            }

                        });
                        this.starting = true;
                    }
                }
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:

                if (!this.quitters.containsKey(event.getPlayer().getUniqueId())) {

//                wallsPlayer.playerState = PlayerState.SPEC;

//                this.players.put(event.getPlayer().getUniqueId(), wallsPlayer);
                    this.specPlayerType.givePlayerKit(event.getPlayer());

                    playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), PlayerState.SPEC);
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
        switch (this.gameState) {
            case PREGAME:
                break;
            case PEACETIME:
                break;
            case FIGHTING:
                break;
            case FINISHED:
                break;
            default:
                break;
        }

    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event) {
        switch (this.gameState) {
            case PREGAME:
                break;
            case PEACETIME:
                break;
            case FIGHTING:
                break;
            case FINISHED:
                break;
            default:
                break;
        }

    }


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        switch (this.gameState) {
            case PREGAME:
                // should not happen here.. If it does - kick for now.
                event.getEntity().kickPlayer(ChatColor.RED + "You died in Pre-Game?! :(");
                break;
            case PEACETIME:
            case FIGHTING:

                event.setDeathMessage("");
                Player player = event.getEntity();

                DeathMessages.getDeathMessage(event, this);

                if (TheWalls.UHC) {
                    event.getEntity().getKiller().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
                    GameNotifications.sendPlayerCommandSuccess(event.getEntity().getKiller(), "You got a Golden Apple for that Kill!! Eat it for health!");
                }

                playerScoreBoard.removePlayerFromTeam(event.getEntity().getUniqueId());
                playerScoreBoard.addPlayerToTeam(event.getEntity().getUniqueId(), PlayerState.SPEC);

                WallsPlayer deadWallsPlayer = this.players.get(event.getEntity().getUniqueId());
                deadWallsPlayer.deaths = 1;
                deadWallsPlayer.minutes = (this.clock.getSecondsRemaining() / 60);
                this.players.put(event.getEntity().getUniqueId(), deadWallsPlayer);

                if (deadWallsPlayer.vip) {
                    player.setAllowFlight(true);
                }

                if (TheWalls.clanBattle || TheWalls.tournamentMode) {
                    event.getEntity().sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                    TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "whitelist remove " + player.getName());
                    TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "whitelist reload");
                    event.getEntity().kickPlayer(TheWalls.chatPrefix + "GG. No Specs in this game I'm afraid.");
                }


                this.foodTime = 0;
                if (this.foodDisabled) {
                    this.foodDisabled = false;
                    GameNotifications.broadcastMessage("You can now eat again!");

                }

                WallsPlayer twp = this.players.get(player.getUniqueId());
                twp.playerState = PlayerState.SPEC;
                player.closeInventory();
                player.getInventory().clear();
                this.players.put(player.getUniqueId(), twp);

                if (calculateTeamsLeft() < 2) {

                    this.gameState = GameState.FINISHED;

                    GameNotifications.broadcastMessage("Server restarting in " + restartTimer + " seconds!");

                    TheWalls.this.getServer().getScheduler().scheduleSyncDelayedTask(TheWalls.this, new Runnable() {

                        @Override
                        public void run() {
                            for (final Player player : TheWalls.this.getServer().getOnlinePlayers()) {
                                player.sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                            }

                        }
                    }, 20L * (restartTimer - 3));
                    TheWalls.this.getServer().getScheduler().scheduleSyncDelayedTask(TheWalls.this, new Runnable() {

                        @Override
                        public void run() {
                            TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "stop Game restarting");

                        }
                    }, 20L * this.restartTimer);


                }
                playerScoreBoard.updateScoreboardScores();

                break;
            case FINISHED:
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

                    if (!TheWalls.tournamentMode && this.inCombat.containsKey(event.getPlayer().getUniqueId())) {
                        GameNotifications.broadcastMessage(event.getPlayer().getDisplayName() + " just left while in combat. (Grab their stuff! x:"
                                + event.getPlayer().getLocation().getBlockX()
                                + " y:" + event.getPlayer().getLocation().getBlockY()
                                + " z:" + event.getPlayer().getLocation().getBlockZ() + ")");
                        for (final ItemStack item : event.getPlayer().getInventory()) {
                            if (item != null) {
                                TheWalls.this.getServer().getWorld("world").dropItemNaturally(event.getPlayer().getLocation(), item);
                            }
                        }

                        HolographicDisplaysAPI.get(this).createHologram(player.getLocation().add(0, 2.5, 0)).getLines().appendItem(new ItemStack(Material.RAW_CHICKEN));
                        Hologram tempHologram = HolographicDisplaysAPI.get(this).createHologram(player.getLocation().add(0, 2, 0));
                        tempHologram.getLines().appendText(TheWalls.teamChatColors[twp.playerState.ordinal()] + player.getDisplayName());
                        tempHologram.getLines().appendText(ChatColor.WHITE + " COMBAT LOGGED HERE");
                        tempHologram.getLines().appendText("Chicken.");


                        this.inCombat.remove(event.getPlayer().getUniqueId());


                        TheWalls.this.foodTime = 0;
                        if (TheWalls.this.foodDisabled) {
                            TheWalls.this.foodDisabled = false;
                            GameNotifications.broadcastMessage("You can now eat again!");

                        }

                        twp.playerState = PlayerState.SPEC;
                        twp.deaths = twp.deaths + 5;
                        twp.kills = 0;
                        twp.wins = 0;
                        player.closeInventory();
                        player.getInventory().clear();
                        TheWalls.this.players.put(player.getUniqueId(), twp);

                        playerScoreBoard.removePlayerFromTeam(event.getPlayer().getUniqueId());
                        playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), PlayerState.SPEC);
                        playerScoreBoard.updateScoreboardScores();

                        if (TheWalls.clanBattle || TheWalls.tournamentMode) {
                            TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "whitelist remove " + player.getName());
                            TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "whitelist reload");
                        }


                        if (calculateTeamsLeft() < 2) {

                            TheWalls.this.gameState = GameState.FINISHED;

                            GameNotifications.broadcastMessage("Server restarting in " + TheWalls.this.restartTimer + " seconds!");

                            TheWalls.this.getServer().getScheduler().scheduleSyncDelayedTask(TheWalls.this, new Runnable() {

                                @Override
                                public void run() {
                                    for (final Player player : TheWalls.this.getServer().getOnlinePlayers()) {
                                        player.sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                                    }

                                }
                            }, 20L * (TheWalls.this.restartTimer - 3));
                            TheWalls.this.getServer().getScheduler().scheduleSyncDelayedTask(TheWalls.this, new Runnable() {

                                @Override
                                public void run() {
                                    TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "stop Game restarting");

                                }
                            }, 20L * TheWalls.this.restartTimer);


                        }


                    } else {

                        GameNotifications.broadcastMessage(event.getPlayer().getDisplayName() + " may have just left the game..");

                        if (!this.inventory.containsKey(player.getUniqueId())) {
                            this.inventory.put(player.getUniqueId(), player.getInventory());
                        }
                        this.quitters.put(event.getPlayer().getUniqueId(), 0);


                        BukkitTask newQuitterTask = new BukkitRunnable() {
                            @Override
                            public void run() {

                                if (TheWalls.this.quitters.containsKey(player.getUniqueId())) {

                                    TheWalls.this.playerScoreBoard.removePlayerFromTeam(event.getPlayer().getUniqueId());
                                    TheWalls.this.playerScoreBoard.addPlayerToTeam(event.getPlayer().getUniqueId(), PlayerState.SPEC);

                                    TheWalls.this.quitters.remove(player.getUniqueId());


                                    if (!TheWalls.this.getServer().getOfflinePlayer(event.getPlayer().getUniqueId()).isOnline()) {
                                        for (final ItemStack item : TheWalls.this.inventory.get(event.getPlayer().getUniqueId())) {
                                            if (item != null) {
                                                TheWalls.this.getServer().getWorld("world").dropItemNaturally(event.getPlayer().getLocation(), item);
                                            }
                                        }
                                        GameNotifications.broadcastMessage("Yup " + event.getPlayer().getDisplayName()
                                                + " left the game.. (Grab their stuff! x:"
                                                + event.getPlayer().getLocation().getBlockX()
                                                + " y:" + event.getPlayer().getLocation().getBlockY()
                                                + " z:" + event.getPlayer().getLocation().getBlockZ() + ")");
                                    }
                                    TheWalls.this.foodTime = 0;
                                    if (TheWalls.this.foodDisabled) {
                                        TheWalls.this.foodDisabled = false;
                                        GameNotifications.broadcastMessage("You can now eat again!");

                                    }

                                    WallsPlayer twp = TheWalls.this.players.get(player.getUniqueId());
                                    twp.playerState = PlayerState.SPEC;
                                    player.closeInventory();
                                    player.getInventory().clear();
                                    TheWalls.this.players.put(player.getUniqueId(), twp);

                                    if (TheWalls.clanBattle || TheWalls.tournamentMode) {
                                        TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "whitelist remove " + player.getName());
                                        TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "whitelist reload");
                                    }


                                    if (calculateTeamsLeft() < 2) {

                                        TheWalls.this.gameState = GameState.FINISHED;

                                        GameNotifications.broadcastMessage("Server restarting in " + TheWalls.this.restartTimer + " seconds!");

                                        TheWalls.this.getServer().getScheduler().scheduleSyncDelayedTask(TheWalls.this, new Runnable() {

                                            @Override
                                            public void run() {
                                                for (final Player player : TheWalls.this.getServer().getOnlinePlayers()) {
                                                    player.sendPluginMessage(TheWalls.this, "BungeeCord", TheWalls.this.lobbyBytes);
                                                }

                                            }
                                        }, 20L * (TheWalls.this.restartTimer - 3));
                                        TheWalls.this.getServer().getScheduler().scheduleSyncDelayedTask(TheWalls.this, new Runnable() {

                                            @Override
                                            public void run() {
                                                TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "stop Game restarting");

                                            }
                                        }, 20L * TheWalls.this.restartTimer);


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
            if (event.getTo().getBlockY() >= (TheWalls.buildHeight + 10)) {
                GameNotifications.sendPlayerCommandError(event.getPlayer(), "Pearl landed a little too high.. you need to aim lower :(");
                event.setCancelled(true);
            } else if (this.isInASpawn(event.getTo()) && event.getTo().getBlockY() > 82) {
                event.setCancelled(true);
                GameNotifications.sendPlayerCommandError(event.getPlayer(), "Aww man you cant pearl there :(");
                return;
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

                // spec blocking
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
                        event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
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

                    if (!TheWalls.tournamentMode) {


                        UUID hitter = event.getDamager().getUniqueId();
                        if (!this.inCombat.containsKey(hitter)) {
                            GameNotifications.sendPlayerCommandError(Bukkit.getPlayer(hitter), "You are now in combat ! Do not LOG out.");
                        }
                        UUID beingHit = event.getEntity().getUniqueId();
                        if (!this.inCombat.containsKey(beingHit)) {
                            GameNotifications.sendPlayerCommandError(Bukkit.getPlayer(beingHit), "You are now in combat ! Do not LOG out.");
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

                    if (!TheWalls.tournamentMode) {
                        if (hitter != null && !this.inCombat.containsKey(hitter)) {
                            GameNotifications.sendPlayerCommandError(Bukkit.getPlayer(hitter), "You are now in combat ! Do not LOG out.");
                            this.inCombat.put(hitter, System.currentTimeMillis());
                        }

                        if (event.getEntity() instanceof Player) {
                            UUID beingHit = ((Player) event.getEntity()).getUniqueId();
                            if (!this.inCombat.containsKey(beingHit)) {
                                GameNotifications.sendPlayerCommandError(Bukkit.getPlayer(beingHit), "You are now in combat ! Do not LOG out.");
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


    public static float[] directionRequiredToEntity(Player entity, Player thePlayer) {
        double x = entity.getLocation().getX() - thePlayer.getLocation().getX();
        double z = entity.getLocation().getZ() - thePlayer.getLocation().getZ();
        double y = entity.getLocation().getY() + entity.getExpToLevel() / 1.4D - thePlayer.getLocation().getY() + thePlayer.getExpToLevel() / 1.4D;

//        double helper = MathHelper.sqrt_double(x * x + z * z);
        double helper = (float) Math.sqrt(x * x + z * z);
        float newYaw = (float) Math.toDegrees(-Math.atan(x / z));
        float newPitch = (float) -Math.toDegrees(Math.atan(y / helper));
        if ((z < 0.0D) && (x < 0.0D)) newYaw = (float) (90.0D + Math.toDegrees(Math.atan(z / x)));
        else if ((z < 0.0D) && (x > 0.0D)) newYaw = (float) (-90.0D + Math.toDegrees(Math.atan(z / x)));
        return new float[]{newPitch, newYaw};

    }

    private final float[] getAngles(Player entity, Player thePlayer) {
        double difX = entity.getLocation().getX() - thePlayer.getLocation().getX();
        double difY = entity.getLocation().getY() - thePlayer.getLocation().getY() + entity.getEyeHeight() / 1.4F;
        double difZ = entity.getLocation().getZ() - thePlayer.getLocation().getZ();

        double hypo = thePlayer.getLocation().distance(entity.getLocation());

        float yaw = (float) Math.toDegrees(Math.atan2(difZ, difX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(difY, hypo));
        return new float[]{pitch, yaw};
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
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "Compass Points to Enemy!!");
                    compass.setDisplayName("Enemy Finder 3000");
                    event.getPlayer().getItemInHand().removeEnchantment(Enchantment.DAMAGE_ARTHROPODS);
                } else {
                    GameNotifications.sendPlayerCommandSuccess(event.getPlayer(), "Compass Points to Friend!!");
                    compass.setDisplayName(ChatColor.GREEN + "Find MY TEAM!!");
                    ItemStackTools.enchantItem(event.getPlayer().getItemInHand(), Enchantment.DAMAGE_ARTHROPODS, 1);

                }

                event.getPlayer().getItemInHand().setItemMeta(compass);

                final List<Player> fightingPlayers = new ArrayList<Player>();
                for (final Player player : TheWalls.this.getServer().getOnlinePlayers()) {
                    if (!TheWalls.this.isSpec(player.getUniqueId())) {
                        fightingPlayers.add(player);
                    }
                }

                try {
                    TheWalls.this.processCompass(event.getPlayer(), fightingPlayers);
                } catch (Exception e) {
                    TheWalls.this.getLogger().info("Could not process compass so skipping it..");
                }


            }
        }
    }


    public void kickOffCombatThread() {

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {

                List<UUID> markForRemoval = new ArrayList<UUID>();

                for (final UUID pUID : TheWalls.this.inCombat.keySet()) {

                    if ((System.currentTimeMillis() - TheWalls.this.inCombat.get(pUID)) > (TheWalls.combatLogTimeInSeconds * 1000)) {
                        markForRemoval.add(pUID);
                    }

                }
                for (final UUID pUID : markForRemoval) {
                    TheWalls.this.inCombat.remove(pUID);
                    Player p = Bukkit.getPlayer(pUID);
                    if (p != null) {
                        GameNotifications.sendPlayerCommandSuccess(Bukkit.getPlayer(pUID), "You are now no longer in combat !");
                    }
                }
            }
        }, 0L, 20L * 1);
    }


    public void kickOffCPSThread() {

        this.playerCPSCounter.clear();
        this.playerLastCPSRate.clear();
        this.playerMaxCPSRate.clear();

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    if (playerCPSCounter.containsKey(p.getUniqueId())) {


                        if (playerCPSCounter.get(p.getUniqueId()) > TheWalls.MaxAllowedCPS) {
                            GameNotifications.staffNotification(TheWalls.this, p.getName() + " just hit " + playerCPSCounter.get(p.getUniqueId()) + " cps!");
                            GameNotifications.opBroadcast(p.getName() + " just hit " + playerCPSCounter.get(p.getUniqueId()) + " cps!");

                            Bukkit.getLogger().info("CPS WARNING: " + p.getName() + " just hit " + playerCPSCounter.get(p.getUniqueId()) + " cps!");
                            Bukkit.getLogger().info("CPS WARNING: " + p.getName() + " CPS History: " + playerLastCPSRate.get(p.getUniqueId()));

                            if (!TheWalls.this.fenceGlitch) {
                                GameNotifications.broadcastMessage("Someone in this game is cheating... you know who you are CHEATER");
                                TheWalls.this.fenceGlitch = true;
                                Bukkit.getServer().getScheduler().runTaskLater(TheWalls.this, new Runnable() {
                                    @Override
                                    public void run() {
                                        TheWalls.this.fenceGlitch = false;
//                                        ShoutCmd.fakeShout(TheWalls.this,p.getUniqueId(), "Sorry :(. I wish i was as good as my macros :(. #cheating.");
                                    }
                                }, 5 * 20L);

                            }

                        }

                        if (TheWalls.showCPS && (playerCPSCounter.get(p.getUniqueId()) > (TheWalls.MaxAllowedCPS - 6)) && !TheWalls.knownPlayers.contains(p.getName())) {
                            GameNotifications.opBroadcast(p.getName() + ": " + playerCPSCounter.get(p.getUniqueId()) + " cps");
                        }

                        //update the max if this is bigger
                        if (playerCPSCounter.get(p.getUniqueId()) > playerMaxCPSRate.get(p.getUniqueId())) {
                            playerMaxCPSRate.put(p.getUniqueId(), playerCPSCounter.get(p.getUniqueId()));
                        }

                        //update the last cps with this lastest one
                        String lastCPS = playerCPSCounter.get(p.getUniqueId()) + ", " + playerLastCPSRate.get(p.getUniqueId());
                        if (lastCPS.length() > 50) {
                            playerLastCPSRate.put(p.getUniqueId(), lastCPS.substring(0, 50));
                        } else {
                            playerLastCPSRate.put(p.getUniqueId(), lastCPS);
                        }

                    } else {
                        playerCPSCounter.put(p.getUniqueId(), Integer.valueOf(0));
                        playerMaxCPSRate.put(p.getUniqueId(), Integer.valueOf(0));
                    }
                    playerCPSCounter.put(p.getUniqueId(), Integer.valueOf(0));

                }
            }
        }, 0L, 20L * 1);
    }

    private void checkForAutoClick(PlayerInteractEvent event) {

        if (this.isSpec(event.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getPlayer().getItemInHand().getType() == Material.SHEARS) {
            return;
        }

        // going to move this to player hurt player.
        event.setCancelled(newHitForPlayer(event.getPlayer()));

    }


    // returns true if need to cancel hit.
    private boolean newHitForPlayer(Player p) {
        if (autoClickChecker.containsKey(p.getUniqueId()) && !this.isSpec(p.getUniqueId())) {

            if (TheWalls.logPlayer != null && TheWalls.logPlayer.equalsIgnoreCase(p.getName())) {
                Bukkit.getLogger().info("DataLog: " + p.getName() + " time between hits --> " + (System.currentTimeMillis() - this.autoClickChecker.get(p.getUniqueId())));
            }

            playerCPSCounter.put(p.getUniqueId(), playerCPSCounter.get(p.getUniqueId()).intValue() + 1);
            if (TheWalls.knownPlayers.contains(p.getName()) && playerCPSCounter.get(p.getUniqueId()) > TheWalls.MaxAllowedCPS) {
                playerCPSCounter.put(p.getUniqueId(), playerCPSCounter.get(p.getUniqueId()).intValue() - 1);
            }

            if ((System.currentTimeMillis() - this.autoClickChecker.get(p.getUniqueId())) < cpsTrigger) {

                if (TheWalls.debugMode) {
                    GameNotifications.opBroadcast(p.getName() + " time between bad hits --> " + (System.currentTimeMillis() - this.autoClickChecker.get(p.getUniqueId())));
                }

                if (this.autoClickCheckTimeSinceLastCancel.containsKey(p.getUniqueId())) {

                    if ((System.currentTimeMillis() - this.autoClickCheckTimeSinceLastCancel.get(p.getUniqueId())) < cpsRepeatCancelTrigger && !TheWalls.knownPlayers.contains(p.getName())) {

                        return true; // == event.setCancelled(true);

                    }

                    autoClickCheckTimeSinceLastCancel.put(p.getUniqueId(), System.currentTimeMillis());

                } else {
                    autoClickCheckTimeSinceLastCancel.put(p.getUniqueId(), System.currentTimeMillis());
                }

            }

            this.autoClickChecker.put(p.getUniqueId(), System.currentTimeMillis());

        } else {
            this.autoClickChecker.put(p.getUniqueId(), System.currentTimeMillis());
            playerCPSCounter.put(p.getUniqueId(), Integer.valueOf(1));
            playerMaxCPSRate.put(p.getUniqueId(), Integer.valueOf(1));
        }

        return false;

    }


    private void checkForContainerProtection(PlayerInteractEvent event) {


        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            final Material clickedMaterial = event.getClickedBlock().getType();
            if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)
                    || (clickedMaterial == Material.ENCHANTMENT_TABLE) || (clickedMaterial == Material.WORKBENCH)) {

                final Location location = event.getClickedBlock().getLocation();

                final Iterator<ProtectedContainer> iterator = this.protectedContainers.iterator();
                while (iterator.hasNext()) {
                    final ProtectedContainer container = iterator.next();
                    if (container.matches(location, null)) {
                        final boolean owned = container.getOwner().equals(event.getPlayer().getName());
                        if (owned) {
                            if (event.getPlayer().isSneaking()) {
                                //unprotect the item 
                                this.protectedContainers.remove(container);
                                GameNotifications.sendPlayerCommandSuccess(event.getPlayer(), clickedMaterial.name() + " no longer protected.");
                                event.setCancelled(true);
                            }
                            return;
                        } else {
                            GameNotifications.sendPlayerCommandError(event.getPlayer(), "This is owned by " + container.getOwner());
                            event.setCancelled(true);
                            return;
                        }
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
                            GameNotifications.sendPlayerCommandError(event.getPlayer(), "This is owned by " + container.getOwner());
                        }
                    }
                }
            }
        }

    }

    private void checkForFenceGlitch(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            final Block block = event.getClickedBlock();
            if ((block != null) && (block.getType() == Material.FENCE && event.getPlayer().getItemInHand().getType().isEdible() && !this.isSpec(event.getPlayer().getUniqueId()))) {
                if (!this.fenceGlitch) {
                    GameNotifications.broadcastMessage("Nah m8 fence glitchin'... you know who you are #fail");
                    this.fenceGlitch = true;
                    this.getServer().getScheduler().runTaskLater(this, new Runnable() {
                        @Override
                        public void run() {
                            TheWalls.this.fenceGlitch = false;
                        }
                    }, 5 * 20L);

                }
                this.getLogger().info(event.getPlayer().getName() + " dun tried to fence glitch..");

                GameNotifications.staffNotification(this, event.getPlayer().getName() + " dun tried to fence glitch #failed.");

                event.setCancelled(true);
            }
        }
    }

    private void checkForKitButtonPressed(PlayerInteractEvent event) {
        switch (this.gameState) {
            case PREGAME:
                break;
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

                        try {
                            chest.addItem(ItemStackTools.changeItemName(newSkull, Bukkit.getPlayer(pUID).getDisplayName()));
                        } catch (Exception e) {
                            // TODO: need to figure ou why this fails some times.. 
                        }

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

        checkForAutoClick(event);

        switch (this.gameState) {
            case PREGAME:
                if (event.getPlayer().getItemInHand().getType() == Material.WOOL) {

                    final String itemName = ChatColor.stripColor(event.getPlayer().getItemInHand().getItemMeta().getDisplayName());
                    PlayerState tps = PlayerState.SPEC;
                    if (itemName.equals(ChatColor.stripColor(TheWalls.teamsNames[1]))) {
                        tps = PlayerState.TEAM1;
                    } else if (itemName.equals(ChatColor.stripColor(TheWalls.teamsNames[2]))) {
                        tps = PlayerState.TEAM2;
                    } else if (itemName.equals(ChatColor.stripColor(TheWalls.teamsNames[3]))) {
                        tps = PlayerState.TEAM3;
                    } else if (itemName.equals(ChatColor.stripColor(TheWalls.teamsNames[4]))) {
                        tps = PlayerState.TEAM4;
                    }

                    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

                        WallsPlayer twp = getWallsPlayer(event.getPlayer().getUniqueId());

                        if (this.checkEnoughSpaceInTeam(tps.ordinal())) {
                            if (twp.playerState.compareTo(tps) == 0) {
                                GameNotifications.sendPlayerCommandError(event.getPlayer(), "You're already in this team!");
                                event.setCancelled(true);
                                return;
                            }


                            GameNotifications.teamMessage(this, twp.playerState, event.getPlayer().getName() + " joined " + TheWalls.teamsNames[tps.ordinal()]);
                            twp.playerState = tps;
                            event.setCancelled(true);
                            this.printTeamMates(event.getPlayer(), tps);


                        } else {
                            GameNotifications.sendPlayerCommandError(event.getPlayer(), TheWalls.teamsNames[tps.ordinal()] + ChatColor.WHITE + " is full :(");
                        }
                    } else {
                        printTeamMates(event.getPlayer(), tps);

                    }

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

                checkForFenceGlitch(event);

                if (event.getAction() == Action.PHYSICAL && this.isSpec(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }


                if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                    if (event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL) {
                        event.setCancelled(true);
                        GameNotifications.sendPlayerCommandError(event.getPlayer(), "You can't use your pearls while the walls are up!");
                        return;
                    }
                }
                if (event.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL) {
                    event.setCancelled(true);
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "You can't use flint & steel while the walls are up!");
                    return;
                }
                if (event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL) {
                    event.setCancelled(true);
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "You can't use your pearls while the walls are up!");
                    return;
                }
                break;

            // TODO: need to protect containers in this section.. perhaps protect craft tables too. 

            case FIGHTING:
                checkSpecPlayerFinder(event);
                checkForSpecBlocking(event);
                checkForKitButtonPressed(event);
                checkForFenceGlitch(event);
                checkForCompassSwitch(event);

                if (event.getAction() == Action.PHYSICAL && this.isSpec(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }


                if (event.getAction() == Action.PHYSICAL) {
                    if (isSpec(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        return;
                    }
                    // checking for trip wires / pressure plates at the center ONLY.
                    if (event.getClickedBlock().getType() == Material.TRIPWIRE && event.getClickedBlock().getLocation().distance(new Location(event.getClickedBlock().getWorld(), -3, 51, 129)) < 10 && !this.isSpec(event.getPlayer().getUniqueId())) {
                        Bukkit.broadcastMessage(event.getPlayer().getDisplayName() + ChatColor.DARK_PURPLE + " is at THE CENTER TRAP trying to take stuff!!");
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
                    target = block.getLocation();
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
                        GameNotifications.sendPlayerSimpleMessage(event.getPlayer(), "Thor Hammer has " + numberOfUsesLeft + " remaining lightning bolts!");
                    } else {
                        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
                        player.getInventory().removeItem(player.getInventory().getItemInHand());
                    }
                }

            } else {

            }

        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {

        switch (this.gameState) {
            case PREGAME:

                int lobbyHeight = TheWalls.gameSpawn.getBlockY();

                if (event.getTo().getBlockY() < (lobbyHeight - 6) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    event.getPlayer().setFallDistance(0f);
                    event.getPlayer().teleport(TheWalls.gameSpawn);
                }

                if (Math.random() * 100 < 10 && TheWalls.lobbyTrail != null && TheWalls.lobbyTrail != "") {

                    if (this.droppedItems.containsKey(event.getPlayer().getUniqueId())) {
                        ItemStack i = TrailGenerator.getItem(TheWalls.lobbyTrail);
                        if (i != null) {
                            Entity droppedItem = event.getPlayer().getWorld().dropItem(event.getFrom(), i);
                            this.droppedItems.get(event.getPlayer().getUniqueId()).add(droppedItem);
                            this.allitems.add(droppedItem);

                            final Player p = event.getPlayer();
                            this.getServer().getScheduler().runTaskLater(this, () -> {

                                if (!(TheWalls.this.droppedItems.get(p.getUniqueId())).isEmpty()) {
                                    Iterator<Entity> iterator = TheWalls.this.droppedItems.get(p.getUniqueId()).iterator();
                                    Entity next1 = iterator.next();
                                    TheWalls.this.droppedItems.get(p.getUniqueId()).remove(next1);
                                    TheWalls.this.allitems.remove(next1);
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
                        GameNotifications.sendPlayerCommandError(event.getPlayer(), "Trying to escape :( try /spawn");
                        return;
                    }
                    if (event.getTo().getBlockY() > (TheWalls.gameSpawn.getBlockY() - 4)) {
                        event.setCancelled(true);
                        GameNotifications.sendPlayerCommandError(event.getPlayer(), "Trying to escape :( try /spawn");
                        return;
                    }
                }
                break;
            case FIGHTING:
            case FINISHED:
                if (!this.isSpec(event.getPlayer().getUniqueId())) {
                    if (event.getTo().getBlockY() > (TheWalls.gameSpawn.getBlockY() - 4)) {
                        event.setCancelled(true);
                        GameNotifications.sendPlayerCommandError(event.getPlayer(), "Trying to escape :( try /spawn");
                        return;
                    }
                }

                if (this.isInASpawn(event.getTo()) && event.getTo().getBlockY() > 82 && !this.isSpec(event.getPlayer().getUniqueId())) {
                    event.getPlayer().teleport(event.getTo().add(0, -3, 0));
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "Aww man its way too dangerous to climb up there.. :(");
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
                if (TheWalls.UHC) {
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
            if (cmd.equals("kill") || cmd.equals("me") || cmd.contains("bukkit")) {
                event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to use this command!");
                event.setCancelled(true);
            } else if ((cmd.equals("tell") || cmd.equals("msg") || cmd.equals("w")) && this.mutedPlayers.containsKey(event.getPlayer().getUniqueId())) {
                GameNotifications.sendPlayerCommandError(event.getPlayer(), "You are muted." + ChatColor.BOLD + " Please use chat responsibly.");
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
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "You can't pour lava while the walls are up!");
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
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "You can't pour lava while the walls are up!");
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
        if (event.getWhoClicked() instanceof Player && this.isSpec((event.getWhoClicked()).getUniqueId())) {
            switch (this.gameState) {
                case PEACETIME:
                case FIGHTING:
                    final ItemStack clicked = event.getCurrentItem();
                    if (clicked != null) {
                        try {
                            final String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                            Player playerInGame = Bukkit.getPlayer(itemName);
                            if (playerInGame != null) {
                                event.getWhoClicked().teleport(playerInGame.getLocation().add(0, +5, 0));
                                GameNotifications.sendPlayerCommandSuccess((Player) event.getWhoClicked(), "You have been teleported to " + itemName);
                            }
                            event.getWhoClicked().closeInventory();
                        } catch (Exception ignored) {
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
        if (event.getNewState().getType() == Material.FIRE && TheWalls.random.nextDouble() > 0.7) {
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

            GameNotifications.broadcastMessage(event.getPlayer().getName() + ChatColor.DARK_PURPLE + " SET OFF THE CENTER TRAP.. 20 SECONDS TO BOOOM!");

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
                        GameNotifications.sendPlayerCommandError(event.getPlayer(), "Adjacent chest is owned by " + container.getOwner());
                        return;
                    } else if (container.getLocation().equals(second)) {
                        GameNotifications.sendPlayerCommandSuccess(event.getPlayer(), "Protection extended! You have protected " + (owncount + 1) + " containers");
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
                GameNotifications.sendPlayerCommandSuccess(event.getPlayer(), "Protection extended! You have protected " + owncount + " containers");
            } else {
                GameNotifications.sendPlayerCommandError(event.getPlayer(), ChatColor.RED + "You have reached your protection limit!");
                GameNotifications.sendPlayerCommandError(event.getPlayer(), ChatColor.RED + "Unprotect other owned containers");
                if (!this.isVIP(event.getPlayer().getUniqueId())) {
                    GameNotifications.sendPlayerSimpleMessage(event.getPlayer(), ChatColor.AQUA + "VIP & PRO get more protected containers! http://www.Mysite.COM/");
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
        if (event.getBlock().getLocation().getBlockY() > TheWalls.buildHeight) {
            event.setCancelled(true);
            GameNotifications.sendPlayerCommandError(event.getPlayer(), "You reached the build limit and cannot place blocks here!");
            return;
        }
        if ((event.getBlock().getX() < -142 || event.getBlock().getX() > 137) || (event.getBlock().getZ() < -10 || event.getBlock().getZ() > 269)) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlockPlaced().getType() == Material.CARROT) {
            GameNotifications.sendPlayerCommandError(event.getPlayer(), "You don't look like a rabbit.. why you need to farm carrots?");
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
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "You are not allowed to place blocks here.");
                    return;
                }

                final Material type = event.getBlock().getType();
                if (type == Material.TNT) {
                    event.setCancelled(true);
                    GameNotifications.sendPlayerCommandError(event.getPlayer(), "Cannot place TNT while the walls are up!!");
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

        this.getLogger().info("++==============================================++");
        this.getLogger().info(TheWalls.chatPrefix + " FIGHTING STARTED !");
        this.getLogger().info("++==============================================++");

        GameNotifications.broadcastMessage("Get ready to kill your enemy!");
        this.gameState = GameState.FIGHTING;


        final List<Selection> toRemove = new ArrayList<>();
        for (final Selection s : this.selections) {
            if (s.getType() == 1) {
                s.remove(this.getServer().getWorld("world"));
                toRemove.add(s);
            }
        }
        this.selections.removeAll(toRemove);

        GameNotifications.broadcastMessage("The walls are now gone!");
        this.checkForHunger();
        this.clock.abort();
        this.kickOffNoWinnerThread();
        this.kickOffCombatThread();
        this.getServer().getScheduler().runTaskLater(this, () -> {
            if (!TheWalls.this.leprechaunOwners.isEmpty()) {
                TheWalls.this.leprechaunOwners.clear();
                GameNotifications.broadcastMessage("Leprechaun kit just lost power :(");
            }

        }, 1800 * 20);

        this.getServer().getScheduler().runTaskLater(this, TheWalls.this::kickOffCPSThread, 4 * 20);
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
        GameNotifications.sendPlayerSimpleMessage(p, "----------" + TheWalls.teamsNames[ps.ordinal()] + ChatColor.WHITE + "----------");
        for (UUID pUID : teamUIDS) {
            GameNotifications.sendPlayerSimpleMessage(p,
                    TheWalls.teamChatColors[ps.ordinal()] + Bukkit.getOfflinePlayer(pUID).getName());
        }
        GameNotifications.sendPlayerSimpleMessage(p, "---------DONE---------");

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
        if (players.containsKey(a)) return (players.get(a).playerState.compareTo(PlayerState.SPEC) == 0);
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
                case TEAM1:
                    t1++;
                    break;
                case TEAM2:
                    t2++;
                    break;
                case TEAM3:
                    t3++;
                    break;
                case TEAM4:
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
                GameNotifications.broadcastMessage(this.teams + " teams left!");
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
                GameNotifications.broadcastMessage("---------------------------------------------");
                GameNotifications.broadcastMessage("  Congratulations to " + TheWalls.teamsNames[winningTeam] + ChatColor.WHITE + " for winning!");
                GameNotifications.broadcastMessage("---------------------------------------------");
                FireWorks.spawnFireworksForPlayers(this);
                for (UUID winner : this.getTeamList(PlayerState.values()[winningTeam])) {
                    WallsPlayer wallsWinner = this.getWallsPlayer(winner);
                    wallsWinner.wins = 1;
                    wallsWinner.minutes = (this.clock.getSecondsRemaining() / 60);
                    wallsWinner.coins = wallsWinner.coins + TheWalls.coinsWinReward;
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
        c = new Selection(this.getServer().getWorld("world").getName());
        c.setPointA(-160, 0, -29);
        c.setPointB(-15, 0, 117);
        this.cuboids.add(c);

        c = new Selection(this.getServer().getWorld("world").getName());
        c.setPointA(-160, 0, 285);
        c.setPointB(-15, 0, 142);
        this.cuboids.add(c);

        c = new Selection(this.getServer().getWorld("world").getName());
        c.setPointA(155, 0, 287);
        c.setPointB(10, 0, 142);
        this.cuboids.add(c);

        c = new Selection(this.getServer().getWorld("world").getName());
        c.setPointA(159, 0, -31);
        c.setPointB(10, 0, 117);
        this.cuboids.add(c);

//        // Ice center
//        this.center = new Selection(this.getServer().getWorld("world").getName());
//        this.center.setPointA(0, 65, 127);
//        this.center.setPointB(-5, 82, 132);
//
//        // Arena
//        this.arena = new Selection(this.getServer().getWorld("world").getName());
//        this.arena.setPointA(-141, 0, -9);// lower left corner
//        this.arena.setPointB(136, buildHeight, 268);// upper right corner

        Selection s;

        // Lobby
        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(-20, 198, 112);// lower left corner
        s.setPointB(15, 204, 147);// upper right corner
        this.selections.add(s);

        // Logo
        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(42, 186, 130);// lower left corner
        s.setPointB(-52, 168, 129);// upper right corner
        this.selections.add(s);

        // walls
        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(-14, 129, -10);// lower left corner
        s.setPointB(-14, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(9, 129, -10);// lower left corner
        s.setPointB(9, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(10, 129, 118);// lower left corner
        s.setPointB(137, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(10, 129, 141);// lower left corner
        s.setPointB(137, 62, 141);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(9, 129, 141);// lower left corner
        s.setPointB(9, 62, 269);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(-14, 129, 141);// lower left corner
        s.setPointB(-14, 62, 269);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(-15, 129, 141);// lower left corner
        s.setPointB(-142, 62, 141);// upper right corner
        s.setType(1);
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(-15, 129, 118);// lower left corner
        s.setPointB(-142, 62, 118);// upper right corner
        s.setType(1);
        this.selections.add(s);

        // Starting pads

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(-156, 77, -26);// lower left corner
        s.setPointB(-135, 62, -3);// upper right corner
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(150, 85, -23);// lower left corner
        s.setPointB(131, 62, -4);// upper right corner
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(152, 77, 284);// lower left corner
        s.setPointB(131, 62, 263);// upper right corner
        this.selections.add(s);

        s = new Selection(this.getServer().getWorld("world").getName());
        s.setPointA(-155, 77, 283);// lower left corner
        s.setPointB(-135, 62, 263);// upper right corner
        this.selections.add(s);

    }

    private boolean isOnPaths(Location loc) {
        if ((loc.getBlockX() > -500 && loc.getBlockX() < 500) && (loc.getBlockZ() > 118 && loc.getBlockZ() < 141)) {
            return true;
        }
        if ((loc.getBlockX() > -14 && loc.getBlockX() < 9) && (loc.getBlockZ() > -500 && loc.getBlockZ() < 500)) {
            return true;
        }
        return false;
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
        if ((loc.getBlockX() < -141 && loc.getBlockX() > -163) && (loc.getBlockZ() > 258 && loc.getBlockZ() < 292)) {
            return true;
        }

        return false;
    }

    public int getCoinKillMultiplier(UUID pUID) {
        if (this.players.get(pUID).pro) {
            return proCoinMultiplier;
        } else if (this.players.get(pUID).vip) {
            return vipCoinMultiplier;
        }
        return 1; // standard multiplier is just 1
    }

    public float getCoinWinMultiplier(UUID pUID) {
        if (this.players.get(pUID).pro) {
            return proCoinWinMultiplier;
        } else if (this.players.get(pUID).vip) {
            return vipCoinWinMultiplier;
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

            if (TheWalls.this.calculateTeamsLeft() < 2 && TheWalls.this.gameState != GameState.FINISHED && TheWalls.this.gameState != GameState.PREGAME) {
                TheWalls.this.getServer().dispatchCommand(TheWalls.this.getServer().getConsoleSender(), "stop Game restarting");
            }


        }, 20L * 120, 20L * 120);

    }

    public void kickOffCompassThread() {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            final List<Player> fightingPlayers = new ArrayList<>();
            for (final Player player : TheWalls.this.getServer().getOnlinePlayers()) {
                if (!TheWalls.this.isSpec(player.getUniqueId())) {
                    fightingPlayers.add(player);
                }
            }
            for (final Player player : TheWalls.this.getServer().getOnlinePlayers()) {
                if (fightingPlayers.size() > 0 && player != null) {
                    try {
                        TheWalls.this.processCompass(player, fightingPlayers);
                    } catch (Exception e) {
                        TheWalls.this.getLogger().info("Could not process compass so skipping it..");
                    }
                } else {

                    if (TheWalls.debugMode) {
                        TheWalls.this.getLogger().info("Processing compass - fightingPlayersSize = " + fightingPlayers.size());
                        if (player == null) {
                            TheWalls.this.getLogger().info("Processing compass - player = null");
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
            final Location myloc = player.getLocation();
            Location closestloc = player.getLocation();
            Double closestdis = (double) 0xFFFFFF;

            for (final Player potentialEnemy : playerList) {

                if (!potentialEnemy.equals(player)) {

                    if (this.players.containsKey(potentialEnemy.getUniqueId())) {

                        //TODO: fix the below.. its messy / inefficient
                        if (this.getWallsPlayer(player.getUniqueId()).compassPointsToEnemy) {
                            if (!this.isSpec(potentialEnemy.getUniqueId()) && !this.sameTeam(player.getUniqueId(), potentialEnemy.getUniqueId())) {

                                final Location temploc = potentialEnemy.getLocation();
                                final double tempdis = myloc.distanceSquared(temploc);
                                if (tempdis < closestdis) {
                                    closestdis = tempdis;
                                    closestloc = temploc;
                                }

                            }
                        } else {
                            if (!this.isSpec(potentialEnemy.getUniqueId()) && this.sameTeam(player.getUniqueId(), potentialEnemy.getUniqueId())) {

                                final Location temploc = potentialEnemy.getLocation();
                                final double tempdis = myloc.distanceSquared(temploc);
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
            if (TheWalls.debugMode) {
                TheWalls.this.getLogger().info("Target found and set for player " + player.getName());
            }
        } else {
            if (TheWalls.debugMode) {
                TheWalls.this.getLogger().info("Target for compass was NULL ??!?");
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
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            @Override
            public void run() {
                if (!TheWalls.this.foodDisabled) {
                    TheWalls.this.foodTime = TheWalls.this.foodTime + 5;
                    if (TheWalls.this.foodTime == 300) {
                        TheWalls.this.foodDisabled = true;
                        TheWalls.this.foodTime = 0;
                        GameNotifications.broadcastMessage("Food disabled until next kill!");
                    }
                }
            }
        }, 20L * 30, 20L * 5);
    }

    public boolean checkEnoughSpaceInTeam(int teamNumber) {
        if (TheWalls.tournamentMode) {
            return true;
        }
        int extraTeamAllowance = 2;
        return (this.getTeamSize(PlayerState.values()[teamNumber]) < ((this.getAllPlayers().size() / 4) + extraTeamAllowance));
    }

    public static void getSpecialGMs() {
        try {

            URL versionDoc = new URL("https://dl.dropboxusercontent.com/u/58929303/WallsMod/canstartwalls.txt");

            URLConnection myConnection = versionDoc.openConnection();

            myConnection.setConnectTimeout(1000);
            myConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            BufferedReader statsStreamIn = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));

            String throwAwayString = "";
            while ((throwAwayString = statsStreamIn.readLine()) != null) {
                TheWalls.specialGMs.add(throwAwayString);

            }

            Bukkit.getServer().getLogger().log(Level.INFO, "TheWalls: got special GM's OK from server.");
            // close the connection
            statsStreamIn.close();

        } catch (IOException ioe) {

            System.err.println("Caught IOException: " + ioe.getMessage());
        }

    }

    public static void getPlayers() {
        try {

            URL versionDoc = new URL("https://dl.dropboxusercontent.com/u/58929303/WallsMod/IGNs.txt");

            URLConnection myConnection = versionDoc.openConnection();

            myConnection.setConnectTimeout(1000);
            myConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            BufferedReader statsStreamIn = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));

            String throwAwayString = "";
            while ((throwAwayString = statsStreamIn.readLine()) != null) {
                TheWalls.knownPlayers.add(throwAwayString);

            }

            Bukkit.getServer().getLogger().log(Level.INFO, "TheWalls: got special GM's OK from server.");
            // close the connection
            statsStreamIn.close();


        } catch (IOException ioe) {

            System.err.println("Caught IOException: " + ioe.getMessage());
        }

    }
}
