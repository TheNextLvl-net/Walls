package net.nonswag.fvr.walls;

import com.sk89q.worldedit.WorldEdit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.nonswag.core.api.file.formats.GsonFile;
import net.nonswag.core.api.file.formats.PropertiesFile;
import net.nonswag.core.api.math.MathUtil;
import net.nonswag.core.api.sql.Database;
import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.walls.api.*;
import net.nonswag.fvr.walls.api.signs.BiomeSign;
import net.nonswag.fvr.walls.api.signs.BiomeSigns;
import net.nonswag.fvr.walls.api.signs.StatSign;
import net.nonswag.fvr.walls.api.signs.StatSigns;
import net.nonswag.fvr.walls.commands.*;
import net.nonswag.fvr.walls.kits.SpecPlayerKit;
import net.nonswag.fvr.walls.listeners.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Walls extends JavaPlugin implements Listener {
    public static final GsonFile<StatSigns> STAT_SIGNS = new GsonFile<>("plugins/TheWalls", "stats.json", new StatSigns());
    public static final GsonFile<BiomeSigns> BIOME_SIGNS = new GsonFile<>("plugins/TheWalls", "biomes.json", new BiomeSigns());
    public static final HashMap<Sort, WallsPlayer> STATS = new HashMap<>();
    public static final HashMap<Team, String> BIOMES = new HashMap<>();

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

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class WallsPlayer {
        private final UUID uniqueId;
        private final String name;
        private String clan = null;
        private Rank rank = Rank.NONE;
        private boolean clanLeader = false;
        private boolean compassPointsToEnemy = true;
        private int statsKills = 0;
        private int statsDeaths = 0;
        private int statsWins = 0;
        private double statsKD = 0;
        private int kills = 0;
        private int deaths = 0;
        private int wins = 0;
        private int minutes = 0;
        private Team team = Team.SPECTATORS;

        public WallsPlayer(OfflinePlayer player) {
            this(player.getUniqueId(), player.getName());
        }

        public double getKD() {
            if (getDeaths() + getStatsDeaths() == 0) return 0;
            return (double) (getKills() + getStatsKills()) / (getDeaths() + getStatsDeaths());
        }
    }

    public static final String CLANCHAT_PREFIX = "§c[§3??§c] ";
    public static String[] teamNames = {"§dSpecs", "§cTeam 1", "§eTeam 2", "§aTeam 3", "§9Team 4"};
    public static ChatColor[] teamChatColors = {ChatColor.LIGHT_PURPLE, ChatColor.RED, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.BLUE};

    private final Map<UUID, Integer> mutedPlayers = new HashMap<>(); // TODO: 17.02.23 should i add a mute system or remove the code base?
    @Getter
    private final Map<UUID, WallsPlayer> players = new HashMap<>();

    @Getter
    private final Map<UUID, Long> inCombat = new HashMap<>();

    public final Map<UUID, UUID> whispers = new HashMap<>();
    public final Map<UUID, String> clanInvites = new HashMap<>();

    public final List<UUID> staffListSnooper = new ArrayList<>();
    public final List<UUID> noStaffChat = new ArrayList<>();

    public static final List<UUID> teamCaptains = new ArrayList<>();

    public static String logPlayer = null;
    public static final String levelName;
    private static String nextMap;
    public static final String DISCORD = "https://discord.gg/vpAgZxQ";

    static {
        PropertiesFile properties = new PropertiesFile("server.properties");
        levelName = properties.getRoot().getString("level-name");
        freeUpSpace();
    }

    @Getter
    private Location gameSpawn;
    @Getter
    private Location team1Spawn, team2Spawn, team3Spawn, team4Spawn;
    @Getter
    private Location team1Corner, team2Corner, team3Corner, team4Corner;
    @Getter
    private final List<Location> spawns = new ArrayList<>();
    @Getter
    private final List<Location> corners = new ArrayList<>();

    @Getter
    private SpecPlayerKit spectatorKit;
    @Getter
    private PlayerScoreBoard playerScoreBoard;

    @Getter
    private final Map<Location, Team> boom = new HashMap<>();
    public Map<UUID, String> assassinTargets = new HashMap<>();
    public Map<UUID, Integer> leprechaunOwners = new HashMap<>();
    public Map<UUID, Integer> thorOwners = new HashMap<>();

    public enum GameState {
        PREGAME, PEACETIME, FIGHTING, FINISHED
    }

    public enum Sort {
        KILLS, WINS, KD_RATIO
    }

    @Getter
    @RequiredArgsConstructor
    public enum Team {
        SPECTATORS("Specs"),
        RED("Team 1"),
        YELLOW("Team 2"),
        GREEN("Team 3"),
        BLUE("Team 4");

        private final String name;
    }

    public enum PlayerJoinType {
        ANYONE, VIP, PRO, STAFF
    }

    @Getter
    private final List<EntityType> allowedMobs = Arrays.asList(
            EntityType.SHEEP,
            EntityType.PIG,
            EntityType.CHICKEN,
            EntityType.MUSHROOM_COW,
            EntityType.COW,
            EntityType.GHAST,
            EntityType.BLAZE
    );

    @Getter
    private final List<Selection> selections = new ArrayList<>();
    public Clock clock;
    @Getter
    @Setter
    private GameState gameState = GameState.PREGAME;
    @Getter
    private final Map<UUID, PlayerInventory> inventory = new HashMap<>();
    public static boolean UHC = false;
    public static int preGameAutoStartPlayers = 4;
    public static int preGameAutoStartSeconds = 30;
    public static PlayerJoinType playerJoinRestriction = PlayerJoinType.ANYONE;
    public static boolean diamondWalls = false;
    public static boolean ironWalls = false;
    public static boolean clanBattle = false;
    public static boolean tournamentMode = false;
    public static boolean allowPickTeams = false;
    public static boolean shhhhh = false;
    public static List<String> clans = new ArrayList<>();
    private int peaceTimeMins = 15;
    public final int restartTimer = 15;
    public boolean starting = false;
    @Getter
    private final Set<ProtectedContainer> protectedContainers = new HashSet<>();
    private int combatRelogTime = 10;
    public int relogTime = 90;
    private int teams = 4;
    @Getter
    private int winningTeam = 0;

    public DatabaseUtil database;

    public boolean foodDisabled = false;
    public int foodTime = 0;
    public static final int buildHeight = 180;
    public static final int liquidBuildHeight = 170;

    @Override
    public void onEnable() {
        spectatorKit = new SpecPlayerKit(this);
        playerScoreBoard = new PlayerScoreBoard(this);
        Populator.selectBiomes().forEach((ordinal, biome) -> {
            BIOMES.put(Team.values()[ordinal], biome);
            System.out.println(biome + " is related to team " + Team.values()[ordinal] + " " + ordinal);
        });
        WorldEdit.getInstance().getConfiguration().navigationWand = -1;

        Bukkit.getScheduler().runTaskLater(this, () -> {
            updateBiomeSigns();
            updateStatSigns();
        }, 1);

        Bukkit.getWorlds().forEach(world -> {
            world.setGameRuleValue("doFireTick", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setThundering(false);
            world.setStorm(false);
            world.setTime(1000);
        });

        saveDefaultConfig();
        reloadConfig();

        UHC = getConfig().getBoolean("UHCMode");
        peaceTimeMins = getConfig().getInt("peaceTimeMins");
        preGameAutoStartPlayers = getConfig().getInt("preGameAutoStartPlayers");
        preGameAutoStartSeconds = getConfig().getInt("preGameAutoStartSeconds");
        relogTime = getConfig().getInt("relogTime");
        combatRelogTime = getConfig().getInt("combatRelogTime");
        diamondWalls = getConfig().getBoolean("diamondWalls");
        ironWalls = getConfig().getBoolean("ironWalls");
        clanBattle = getConfig().getBoolean("clanBattle");
        tournamentMode = getConfig().getBoolean("tournamentMode");
        playerJoinRestriction = PlayerJoinType.valueOf(getConfig().getString("playerJoinRestriction"));
        allowPickTeams = getConfig().getBoolean("allowPickTeams");

        if (clanBattle || tournamentMode) {
            teamNames[1] = teamChatColors[1] + getConfig().getString("clan-1");
            teamNames[2] = teamChatColors[2] + getConfig().getString("clan-2");
            teamNames[3] = teamChatColors[3] + getConfig().getString("clan-3");
            teamNames[4] = teamChatColors[4] + getConfig().getString("clan-4");
            List<String> clans = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                String string = getConfig().getString("clan-" + i);
                teamNames[i] = teamChatColors[i] + string;
                clans.add(string);
            }
            Walls.clans = clans;
        }

        World world = Bukkit.getWorlds().get(0);

        spawns.add(team1Spawn = new Location(world, -149, 65, -17));
        spawns.add(team2Spawn = new Location(world, 144, 65, -17));
        spawns.add(team3Spawn = new Location(world, 144, 65, 276));
        spawns.add(team4Spawn = new Location(world, -149, 65, 276));

        corners.add(gameSpawn = new Location(world, -2, world.getHighestBlockYAt(-2, 130), 130));
        corners.add(team1Corner = new Location(world, -23, 62, 108));
        corners.add(team2Corner = new Location(world, 19, 62, 108));
        corners.add(team3Corner = new Location(world, 19, 62, 151));
        corners.add(team4Corner = new Location(world, -23, 62, 151));

        DeathMessages.initializeDeathMessages();

        registerListeners();
        registerCommands();

        defineArena();
        (this.clock = new Clock(this)).start();
        this.database = new DatabaseUtil(this);
        nextMap = selectNextMap();
        loadStats();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new PingListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SpawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RespawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
    }

    private void registerCommands() {
        getCommand("ping").setExecutor(new PingCommand());
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("shout").setExecutor(new ShoutCommand(this));
        getCommand("tp").setExecutor(new TPCommand(this));
        getCommand("walls").setExecutor(new WallsCommand(this));
        getCommand("giveall").setExecutor(new FullKitCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("corner").setExecutor(new CornerCommand(this));
        getCommand("surface").setExecutor(new SurfaceCommand(this));
        getCommand("msg").setExecutor(new MSGCommand(this));
        getCommand("r").setExecutor(new ReplyCommand(this));
        getCommand("kit").setExecutor(new KitCommand(this));
        getCommand("sc").setExecutor(new StaffChatCommand(this));
        getCommand("oc").setExecutor(new OpChatCommand());
        getCommand("cc").setExecutor(new ClanChatCommand(this));
        getCommand("share").setExecutor(new ShareCommand(this));
        getCommand("clan").setExecutor(new ClanCommand(this));
        PluginCommand find = getCommand("find");
        FindCommand findCommand = new FindCommand(this);
        find.setExecutor(findCommand);
        find.setTabCompleter(findCommand);
    }

    @Override
    public void onDisable() {
        Walls.STAT_SIGNS.save();
        Walls.BIOME_SIGNS.save();
        clock.interrupt();
        if (WallsCommand.FIX_DB) fixDatabase();
        Database.disconnect();
        changeLevelName(nextMap);
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

    private static void freeUpSpace() {
        try {
            File[] files = Bukkit.getWorldContainer().listFiles((dir, name) -> !name.equalsIgnoreCase(levelName) && !name.equals("Templates"));
            if (files == null) throw new FileNotFoundException(Bukkit.getWorldContainer().getAbsolutePath());
            for (File file : files) delete(file);
        } catch (IOException e) {
            System.err.println("Failed to delete the old map, You have to do it manually");
            e.printStackTrace();
        }
    }

    private void fixDatabase() {
        try {
            Database.getConnection().executeUpdate("DROP TABLE `profiles`");
            Database.getConnection().executeUpdate("DROP TABLE `accounts`");
            Database.getConnection().executeUpdate("DROP TABLE `guilds`");
            Database.getConnection().executeUpdate("DROP TABLE `stats`");
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

    private static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) for (File all : files) delete(all);
        }
        Files.deleteIfExists(file.toPath());
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
                if (UHC) {
                    event.getEntity().getKiller().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
                    Notifier.success(event.getEntity().getKiller(), "You got a Golden Apple for that Kill!! Eat it for health!");
                }
                getPlayerScoreBoard().removePlayerFromTeam(event.getEntity());
                getPlayerScoreBoard().addPlayerToTeam(event.getEntity(), Team.SPECTATORS);
                WallsPlayer deadWallsPlayer = getPlayer(event.getEntity());
                deadWallsPlayer.setDeaths(1);
                deadWallsPlayer.setMinutes(clock.getSeconds() / 60);
                if (deadWallsPlayer.getRank().vip()) player.setAllowFlight(true);
                if (clanBattle || tournamentMode) {
                    event.getEntity().kickPlayer("§cGG. No Specs in this game I'm afraid.");
                }
                this.foodTime = 0;
                if (this.foodDisabled) {
                    this.foodDisabled = false;
                    Notifier.broadcast("You can now eat again!");
                }
                getPlayer(player).setTeam(Team.SPECTATORS);
                player.closeInventory();
                player.getInventory().clear();
                if (calculateTeamsLeft() < 2) {
                    setGameState(GameState.FINISHED);
                    Notifier.broadcast("Server restarting in " + restartTimer + " seconds!");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> System.exit(0), 20L * this.restartTimer);
                }
                getPlayerScoreBoard().updateScoreboardScores();
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDispense(BlockDispenseEvent event) {
        if (foodDisabled && event.getItem().getType() == Material.POTION) event.setCancelled(true);
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
    public void PlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.ENDER_PEARL) return;
        if (event.getTo().getBlockY() >= buildHeight) {
            Notifier.error(event.getPlayer(), "Pearl landed a little too high.. you need to aim lower :(");
            event.setCancelled(true);
        } else if (this.isInASpawn(event.getTo()) && event.getTo().getBlockY() > 82) {
            Notifier.error(event.getPlayer(), "Aww man you cant pearl there :(");
            event.setCancelled(true);
        }
    }

    private boolean checkSpecBlockingProjectile(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) return false;
        if (!(event.getEntity() instanceof Player)) return false;
        if (!(((Projectile) event.getDamager()).getShooter() instanceof Player)) return false;
        Projectile projectile = (Projectile) event.getDamager();
        Vector velocity = projectile.getVelocity();
        Player shooter = (Player) projectile.getShooter();
        Player damaged = (Player) event.getEntity();
        if (!isSpectator(event.getEntity())) return false;
        damaged.teleport(event.getDamager().getLocation().add(0, 5, 0));
        damaged.setFlying(true);
        Projectile newProjectile = shooter.launchProjectile(projectile.getClass());
        newProjectile.setShooter(shooter);
        newProjectile.setVelocity(velocity);
        newProjectile.setBounce(false);
        event.setCancelled(true);
        projectile.remove();
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHurtPlayer(EntityDamageByEntityEvent event) {
        Entity entityDamager = event.getDamager();
        switch (getGameState()) {
            case PREGAME:
                if (!(entityDamager instanceof Player)) return;
                Player damager = (Player) entityDamager;
                if (damager.getInventory().getItemInHand().getType() == Material.SNOW_BALL) damager.setHealth(20);
                else event.setCancelled(true);
                break;
            case PEACETIME:
            case FIGHTING:
                if (checkSpecBlockingProjectile(event)) return;
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
                    if (tournamentMode) return;
                    UUID hitter = event.getDamager().getUniqueId();
                    if (!getInCombat().containsKey(hitter)) {
                        Notifier.error(Bukkit.getPlayer(hitter), "You are now in combat ! Do not LOG out.");
                    }
                    UUID beingHit = event.getEntity().getUniqueId();
                    if (!getInCombat().containsKey(beingHit)) {
                        Notifier.error(Bukkit.getPlayer(beingHit), "You are now in combat ! Do not LOG out.");
                    }
                    getInCombat().put(hitter, System.currentTimeMillis() + (combatRelogTime * 1000L));
                    getInCombat().put(beingHit, System.currentTimeMillis() + (combatRelogTime * 1000L));
                }
                if (event.getDamager() instanceof Projectile) {
                    ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                    UUID attacker = null;
                    if (shooter instanceof Player && event.getEntity() instanceof Player) {
                        if (sameTeam(event.getEntity().getUniqueId(), ((Entity) shooter).getUniqueId())) {
                            event.setCancelled(true);
                            break;
                        }
                        attacker = ((Player) ((Projectile) event.getDamager()).getShooter()).getUniqueId();
                    }
                    if (tournamentMode) return;
                    if (attacker != null && !getInCombat().containsKey(attacker)) {
                        Notifier.error(Bukkit.getPlayer(attacker), "You are now in combat ! Do not LOG out.");
                        getInCombat().put(attacker, System.currentTimeMillis() + (combatRelogTime * 1000L));
                    }
                    if (!(event.getEntity() instanceof Player)) return;
                    UUID beingHit = event.getEntity().getUniqueId();
                    if (!getInCombat().containsKey(beingHit)) {
                        Notifier.error(Bukkit.getPlayer(beingHit), "You are now in combat ! Do not LOG out.");
                    }
                    getInCombat().put(beingHit, System.currentTimeMillis() + (combatRelogTime * 1000L));
                }
                break;
            case FINISHED:
                if (!(event.getEntity() instanceof Player) && !(event.getDamager() instanceof Player)) return;
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    private void checkForCompassSwitch(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getPlayer().getItemInHand().getType() == Material.COMPASS) {
                WallsPlayer wallsPlayer = this.getPlayer(event.getPlayer().getUniqueId());
                wallsPlayer.setCompassPointsToEnemy(!wallsPlayer.isCompassPointsToEnemy());
                ItemMeta compass = Bukkit.getItemFactory().getItemMeta(Material.COMPASS);
                if (wallsPlayer.isCompassPointsToEnemy()) {
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
            if (!inCombat.isEmpty()) new HashMap<>(inCombat).forEach((uuid, time) -> {
                Player player = Bukkit.getPlayer(uuid);
                if (time > System.currentTimeMillis() && player != null) return;
                inCombat.remove(uuid);
                if (player != null) Notifier.success(player, "You are now no longer in combat !");
            });
        }, 0, 20);
    }

    private void checkForContainerProtection(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            final Material clickedMaterial = event.getClickedBlock().getType();
            if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)
                    || (clickedMaterial == Material.ENCHANTMENT_TABLE) || (clickedMaterial == Material.WORKBENCH)) {
                Location location = event.getClickedBlock().getLocation();
                for (ProtectedContainer container : getProtectedContainers()) {
                    if (!container.matches(location, null)) continue;
                    if (container.getOwner().equals(event.getPlayer().getName())) {
                        if (event.getPlayer().isSneaking()) {
                            getProtectedContainers().remove(container);
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

        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            final Material clickedMaterial = event.getClickedBlock().getType();
            if ((clickedMaterial == Material.CHEST) || (clickedMaterial == Material.FURNACE) || (clickedMaterial == Material.BURNING_FURNACE)) {
                Location location = event.getClickedBlock().getLocation();
                Location second = null;
                if (clickedMaterial == Material.CHEST) {
                    Block north = event.getClickedBlock().getRelative(BlockFace.NORTH);
                    Block south = event.getClickedBlock().getRelative(BlockFace.SOUTH);
                    Block east = event.getClickedBlock().getRelative(BlockFace.EAST);
                    Block west = event.getClickedBlock().getRelative(BlockFace.WEST);
                    if (north.getType() == Material.CHEST) second = north.getLocation();
                    else if (south.getType() == Material.CHEST) second = south.getLocation();
                    else if (east.getType() == Material.CHEST) second = east.getLocation();
                    else if (west.getType() == Material.CHEST) second = west.getLocation();
                }
                for (ProtectedContainer container : getProtectedContainers()) {
                    if (!container.matches(location, second)) continue;
                    if (container.getOwner().equals(event.getPlayer().getName())) continue;
                    Notifier.error(event.getPlayer(), "This is owned by " + container.getOwner());
                    event.setCancelled(true);
                }
            }
        }
    }

    private void checkForKitButtonPressed(PlayerInteractEvent event) {
        switch (getGameState()) {
            case PEACETIME:
            case FIGHTING:
                if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
                    return;
                if (this.isSpectator(event.getPlayer())) return;
                Block block = event.getClickedBlock();
                if ((block != null) && (block.getType() == Material.WOOD_BUTTON || block.getType() == Material.STONE_BUTTON)) {
                    Block above = block.getRelative(BlockFace.DOWN);
                    if (above == null || above.getType() != Material.WALL_SIGN) return;
                    Sign sign = (Sign) above.getState();
                    if (!ChatColor.stripColor(sign.getLine(2)).equalsIgnoreCase("kit")) return;
                    String choice = ChatColor.stripColor(sign.getLine(1)).toLowerCase().trim();
                    KitCommand.playerChoice(this, event.getPlayer(), choice);
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
        if (!isSpectator(event.getPlayer())) return;
        if (event.getPlayer().getItemInHand().getType() == Material.SKULL_ITEM) {
            Inventory chest = Bukkit.createInventory(null, 6 * 9, "Player Finder :)");
            for (UUID uuid : getPlayers().keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || isSpectator(player)) continue;
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                ItemMeta meta = skull.getItemMeta();
                if (meta == null) continue;
                if (meta instanceof SkullMeta) ((SkullMeta) meta).setOwner(player.getName());
                meta.setDisplayName("§7" + player.getName());
                skull.setItemMeta(meta);
                chest.addItem(skull);
            }
            event.getPlayer().openInventory(chest);
            event.setCancelled(true);
        } else event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        switch (getGameState()) {
            case PREGAME:
                if (event.getPlayer().getItemInHand().getType() == Material.WOOL) {
                    final String itemName = ChatColor.stripColor(event.getPlayer().getItemInHand().getItemMeta().getDisplayName());
                    Team team = Team.SPECTATORS;
                    if (itemName.equals(ChatColor.stripColor(teamNames[1]))) team = Team.RED;
                    else if (itemName.equals(ChatColor.stripColor(teamNames[2]))) team = Team.YELLOW;
                    else if (itemName.equals(ChatColor.stripColor(teamNames[3]))) team = Team.GREEN;
                    else if (itemName.equals(ChatColor.stripColor(teamNames[4]))) team = Team.BLUE;
                    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        WallsPlayer player = getPlayer(event.getPlayer().getUniqueId());
                        if (this.checkEnoughSpaceInTeam(team.ordinal())) {
                            if (player.team.compareTo(team) == 0) {
                                event.setCancelled(true);
                                return;
                            }
                            Notifier.team(this, player.team, event.getPlayer().getName() + " joined " + teamNames[team.ordinal()]);
                            getPlayerScoreBoard().addPlayerToTeam(event.getPlayer(), team);
                            player.team = team;
                            event.setCancelled(true);
                        } else {
                            Notifier.error(event.getPlayer(), teamNames[team.ordinal()] + ChatColor.WHITE + " is full :(");
                        }
                    } else printTeamMates(event.getPlayer(), team);
                } else if (event.getPlayer().getItemInHand().getType() == Material.SNOW_BALL) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                        event.getPlayer().updateInventory();
                    }
                }
                break;
            case PEACETIME:
                checkSpecPlayerFinder(event);
                if (isSpectator(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
                checkForContainerProtection(event);
                checkForKitButtonPressed(event);
                checkForCompassSwitch(event);
                if (event.getItem() == null) return;
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType() == Material.FLINT_AND_STEEL) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You can't use flint & steel while the walls are up!");
                } else if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getItem().getType() == Material.ENDER_PEARL) {
                    event.setCancelled(true);
                    Notifier.error(event.getPlayer(), "You can't use your pearls while the walls are up!");
                }
                break;
            case FIGHTING:
                checkSpecPlayerFinder(event);
                if (isSpectator(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
                checkForKitButtonPressed(event);
                checkForCompassSwitch(event);
                handleKitInteraction(event);

                if (event.getAction() != Action.PHYSICAL) return;
                if (event.getClickedBlock().getType() == Material.TRIPWIRE && event.getClickedBlock().getLocation().distance(new Location(event.getClickedBlock().getWorld(), -3, 51, 129)) < 10) {
                    Notifier.broadcast("§d" + event.getPlayer().getName() + "§d is at THE CENTER TRAP trying to take stuff!!");
                }
            case FINISHED:
                checkSpecPlayerFinder(event);
                if (isSpectator(event.getPlayer())) event.setCancelled(true);
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
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack stack = player.getItemInHand();
        if (event.getAction() == Action.PHYSICAL) {
            if ((block != null) && (block.getType() == Material.STONE_PLATE)) {
                Team team = getBoom().get(block.getLocation());
                if (team != null && team != getPlayer(player).getTeam()) {
                    block.getWorld().createExplosion(block.getLocation(), 3);
                    getBoom().remove(block.getLocation());
                }
            }
        }
        if (!this.loreMatch(stack, "Weapon of the Gods!")) return;
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
                    Notifier.notify(event.getPlayer(), "Mjölnir has " + numberOfUsesLeft + " remaining lightning bolts!");
                } else {
                    player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
                    player.getInventory().removeItem(player.getInventory().getItemInHand());
                }
            }
        } else Notifier.error(event.getPlayer(), "§cYou cannot use Mjölnir");
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onRegainHealthChange(EntityRegainHealthEvent event) {
        switch (getGameState()) {
            case PREGAME:
            case PEACETIME:
            case FIGHTING:
                if (UHC && event.getRegainReason() != RegainReason.MAGIC_REGEN) event.setCancelled(true);
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
                event.setCancelled(true);
                break;
            case FIGHTING:
                if (!isSpectator(event.getEntity()) && ThreadLocalRandom.current().nextInt(100) > 30) return;
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }


    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().isOp()) return;
        String[] split = event.getMessage().split(" ");
        if (split.length < 1) return;
        String cmd = split[0].trim().substring(1).toLowerCase();
        if ((cmd.equals("tell") || cmd.equals("msg") || cmd.equals("w")) && this.mutedPlayers.containsKey(event.getPlayer().getUniqueId())) {
            Notifier.error(event.getPlayer(), "You are muted." + ChatColor.BOLD + " Please use chat responsibly.");
            event.setCancelled(true);
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
    public void fireDamageControl(BlockSpreadEvent event) {
        if (event.getNewState().getType() != Material.FIRE || !(ThreadLocalRandom.current().nextDouble() > 0.7)) return;
        event.setCancelled(true);
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
            World world = event.getPlayer().getWorld();
            world.getBlockAt(new Location(world, -2, 50, 130)).setType(Material.AIR);
            world.spawn(new Location(world, -2, 51, 130), TNTPrimed.class).setFuseTicks(400);
        }
    }

    public void checkForProtectedPlacement(BlockPlaceEvent event) {
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
            for (ProtectedContainer container : getProtectedContainers()) {
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
            if (getPlayer(event.getPlayer().getUniqueId()).getRank().pro()) {
                max = 7;
            } else if (getPlayer(event.getPlayer().getUniqueId()).getRank().vip()) {
                max = 5;
            }
            if (owncount < max) {
                getProtectedContainers().add(new ProtectedContainer(location, event.getPlayer().getName()));
                Notifier.success(event.getPlayer(), "Protection extended! You have protected " + owncount + " containers");
            } else {
                Notifier.error(event.getPlayer(), ChatColor.RED + "You have reached your protection limit!");
                Notifier.error(event.getPlayer(), ChatColor.RED + "Unprotect other owned containers");
                if (!getPlayer(event.getPlayer().getUniqueId()).getRank().vip()) {
                    Notifier.notify(event.getPlayer(), ChatColor.AQUA + "VIP & PRO get more protected containers! " + DISCORD);
                }
            }
        }
    }

    public boolean isOnPaths(Location loc) {
        if ((loc.getBlockX() > -500 && loc.getBlockX() < 500) && (loc.getBlockZ() > 118 && loc.getBlockZ() < 141)) {
            return true;
        }
        return (loc.getBlockX() > -14 && loc.getBlockX() < 9) && (loc.getBlockZ() > -500 && loc.getBlockZ() < 500);
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
        World world = Bukkit.getWorld(levelName);
        for (Selection selection : new ArrayList<>(getSelections())) {
            if (!selection.isWalls()) continue;
            selection.remove(world);
            getSelections().remove(selection);
        }
        Notifier.broadcast("The walls are now gone!");
        startHungerChecker();
        clock.abort();
        kickOffNoWinnerThread();
        kickOffCombatThread();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!leprechaunOwners.isEmpty()) {
                leprechaunOwners.clear();
                Notifier.broadcast("Leprechaun kit just lost power :(");
            }
        }, 1800 * 20);
    }

    public WallsPlayer getPlayer(Player player) {
        if (player == null) return null;
        return getPlayer(player.getUniqueId());
    }

    public WallsPlayer getPlayer(UUID uuid) {
        return getPlayers().getOrDefault(uuid, null);
    }

    public List<UUID> getStaffList() {
        List<UUID> staffs = new ArrayList<>();
        for (UUID uuid : getPlayers().keySet()) {
            if (getPlayer(uuid).getRank().staff()) staffs.add(uuid);
        }
        return staffs;
    }

    public List<UUID> getTeamList(UUID uid) {
        return getTeamList(getPlayer(uid).team);
    }

    public List<UUID> getTeamList(Team playerState) {
        List<UUID> teamList = new ArrayList<>();
        for (UUID uuid : getPlayers().keySet()) {
            if (getPlayer(uuid).team == playerState) {
                teamList.add(uuid);
            }
        }
        return teamList;
    }

    public void printTeamMates(Player player, Team ps) {
        List<UUID> teamUIDS = this.getTeamList(ps);
        if (!teamUIDS.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (UUID uuid : teamUIDS) names.add(Bukkit.getOfflinePlayer(uuid).getName());
            Notifier.notify(player, teamChatColors[ps.ordinal()] + String.join("§7, " + teamChatColors[ps.ordinal()], names));
        } else Notifier.notify(player, "§cTeam " + teamNames[ps.ordinal()] + "§c is empty");
    }

    public int getTeamSize(Team state) {
        int teamCounter = 0;
        for (WallsPlayer player : getPlayers().values()) {
            if (player.team.equals(state)) teamCounter++;
        }
        return teamCounter;
    }

    public boolean isSpectator(Entity entity) {
        if (entity == null) return false;
        WallsPlayer wallsPlayer = getPlayer(entity.getUniqueId());
        return wallsPlayer != null && wallsPlayer.team.equals(Team.SPECTATORS);
    }

    public boolean sameTeam(UUID a, UUID b) {
        return getPlayer(a).team.compareTo(getPlayer(b).team) == 0;
    }

    public int calculateTeamsLeft() {
        int tempNumberOfTeams = 0;
        int team1 = 0;
        int team2 = 0;
        int team3 = 0;
        int team4 = 0;
        for (WallsPlayer wp : getPlayers().values()) {
            switch (wp.team) {
                case RED:
                    team1++;
                    break;
                case YELLOW:
                    team2++;
                    break;
                case GREEN:
                    team3++;
                    break;
                case BLUE:
                    team4++;
                    break;
                default:
                    break;
            }
        }

        if (team1 > 0) tempNumberOfTeams++;
        if (team2 > 0) tempNumberOfTeams++;
        if (team3 > 0) tempNumberOfTeams++;
        if (team4 > 0) tempNumberOfTeams++;
        if (this.teams != tempNumberOfTeams) {
            this.teams = tempNumberOfTeams;
            if (this.teams > 1) {
                Notifier.broadcast(this.teams + " teams left!");
            } else {
                if (team1 > 0) winningTeam = 1;
                else if (team2 > 0) winningTeam = 2;
                else if (team3 > 0) winningTeam = 3;
                else if (team4 > 0) winningTeam = 4;
                Notifier.broadcast("---------------------------------------------");
                Notifier.broadcast("  Congratulations to " + teamNames[winningTeam] + ChatColor.WHITE + " for winning!");
                Notifier.broadcast("---------------------------------------------");
                Fireworks.spawnFireworksForPlayers(this);
                for (UUID winner : this.getTeamList(Team.values()[winningTeam])) {
                    WallsPlayer wallsWinner = this.getPlayer(winner);
                    wallsWinner.minutes = (this.clock.getSeconds() / 60);
                    wallsWinner.wins++;
                }
                this.database.saveAllData();
            }
        }
        return this.teams;
    }

    public int getNumberOfPlayers() {
        return getPlayers().size();
    }

    public void defineArena() {
        Selection selection;

        // Lobby
        selection = new Selection(levelName, false);
        selection.setMinPoint(-20, 198, 112);
        selection.setMaxPoint(15, 204, 147);
        getSelections().add(selection);

        // Logo
        selection = new Selection(levelName, false);
        selection.setMinPoint(42, 186, 130);
        selection.setMaxPoint(-52, 168, 129);
        getSelections().add(selection);

        // walls
        selection = new Selection(levelName, true);
        selection.setMinPoint(-14, 129, -10);
        selection.setMaxPoint(-14, 62, 118);
        getSelections().add(selection);

        selection = new Selection(levelName, true);
        selection.setMinPoint(9, 129, -10);
        selection.setMaxPoint(9, 62, 118);
        getSelections().add(selection);

        selection = new Selection(levelName, true);
        selection.setMinPoint(10, 129, 118);
        selection.setMaxPoint(137, 62, 118);
        getSelections().add(selection);

        selection = new Selection(levelName, true);
        selection.setMinPoint(10, 129, 141);
        selection.setMaxPoint(137, 62, 141);
        getSelections().add(selection);

        selection = new Selection(levelName, true);
        selection.setMinPoint(9, 129, 141);
        selection.setMaxPoint(9, 62, 269);
        getSelections().add(selection);

        selection = new Selection(levelName, true);
        selection.setMinPoint(-14, 129, 141);
        selection.setMaxPoint(-14, 62, 269);
        getSelections().add(selection);

        selection = new Selection(levelName, true);
        selection.setMinPoint(-15, 129, 141);
        selection.setMaxPoint(-142, 62, 141);
        getSelections().add(selection);

        selection = new Selection(levelName, true);
        selection.setMinPoint(-15, 129, 118);
        selection.setMaxPoint(-142, 62, 118);
        getSelections().add(selection);

        // Starting pads

        selection = new Selection(levelName, false);
        selection.setMinPoint(-156, 77, -26);
        selection.setMaxPoint(-135, 62, -3);
        getSelections().add(selection);

        selection = new Selection(levelName, false);
        selection.setMinPoint(150, 85, -23);
        selection.setMaxPoint(131, 62, -4);
        getSelections().add(selection);

        selection = new Selection(levelName, false);
        selection.setMinPoint(152, 77, 284);
        selection.setMaxPoint(131, 62, 263);
        getSelections().add(selection);

        selection = new Selection(levelName, false);
        selection.setMinPoint(-155, 77, 283);
        selection.setMaxPoint(-135, 62, 263);
        getSelections().add(selection);
    }

    public boolean isInASpawn(Location location) {
//        Team 1 Red (-142, 61 -1) (-165,61,-32)
        if ((location.getBlockX() < -141 && location.getBlockX() > -166) && (location.getBlockZ() < 0 && location.getBlockZ() > -33)) {
            return true;
        }
//        Team 1 Red (-133, 61 -10) (-165,61,-32)
        if ((location.getBlockX() < -132 && location.getBlockX() > -166) && (location.getBlockZ() < -9 && location.getBlockZ() > -33)) {
            return true;
        }

//        Team 2 yellow (137, 61, 0) (159,61,-32)
        if ((location.getBlockX() > 136 && location.getBlockX() < 160) && (location.getBlockZ() < 1 && location.getBlockZ() > -33)) {
            return true;
        }
//        Team 2 yellow (127, 61 -10) (159,61,-32)
        if ((location.getBlockX() > 127 && location.getBlockX() < 160) && (location.getBlockZ() < -9 && location.getBlockZ() > -33)) {
            return true;
        }

//        Team 3 green (137, 61, 259) (159,61,291)
        if ((location.getBlockX() > 136 && location.getBlockX() < 160) && (location.getBlockZ() > 258 && location.getBlockZ() < 292)) {
            return true;
        }
//        Team 3 green (127, 61, 269) (159,61,291)
        if ((location.getBlockX() > 126 && location.getBlockX() < 160) && (location.getBlockZ() > 268 && location.getBlockZ() < 292)) {
            return true;
        }

//        Team 4 blue (-132, 61, 269) (-164,61,291)
        if ((location.getBlockX() < -131 && location.getBlockX() > -163) && (location.getBlockZ() > 268 && location.getBlockZ() < 292)) {
            return true;
        }

//        Team 4 blue (-142, 61, 259) (-164,61,291)
        return (location.getBlockX() < -141 && location.getBlockX() > -163) && (location.getBlockZ() > 258 && location.getBlockZ() < 292);
    }

    public boolean loreMatch(ItemStack item, String line) {
        if ((item == null) || !item.hasItemMeta() || (item.getItemMeta().getLore() == null)) return false;
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
                if (fightingPlayers.size() == 0 || player == null) continue;
                try {
                    processCompass(player, fightingPlayers);
                } catch (Exception e) {
                    getLogger().info("Could not process compass so skipping it..");
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
                        if (this.getPlayer(player).compassPointsToEnemy) {
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
        if (target != null) player.setCompassTarget(target);
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
        if (tournamentMode) return true;
        int extraTeamAllowance = 2;
        return (this.getTeamSize(Team.values()[teamNumber]) < ((this.getPlayers().size() / 4) + extraTeamAllowance));
    }

    private void loadStats() {
        STATS.put(Sort.KILLS, database.bestKiller());
        STATS.put(Sort.WINS, database.mostWins());
        STATS.put(Sort.KD_RATIO, database.kdRatio());
    }

    public static void updateBiomeSigns() {
        World world = Bukkit.getWorlds().get(0);
        BIOME_SIGNS.getRoot().getSigns().forEach(sign -> {
            Position position = sign.getPosition();
            if (position.getWorld().equals(world.getName())) BIOMES.forEach((team, biome) -> {
                if (!sign.getTeam().equals(team)) return;
                Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());
                updateBiomeSign(block, sign, biome);
            });
        });
    }

    public static void updateStatSigns() {
        World world = Bukkit.getWorlds().get(0);
        STAT_SIGNS.getRoot().getSigns().forEach(sign -> {
            Position position = sign.getPosition();
            if (position.getWorld().equals(world.getName())) STATS.forEach((stat, player) -> {
                if (!sign.getStat().equals(stat)) return;
                Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());
                updateStatSign(block, sign, player);
            });
        });
    }

    public static void updateBiomeSign(Block block, BiomeSign sign, String biome) {
        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;
        ((Sign) state).setLine(0, "");
        ((Sign) state).setLine(1, sign.getTeam().getName());
        ((Sign) state).setLine(2, biome);
        ((Sign) state).setLine(3, "");
        state.update();
    }

    public static void updateStatSign(Block block, StatSign sign, WallsPlayer player) {
        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;
        ((Sign) state).setLine(0, "");
        if (player != null) ((Sign) state).setLine(1, player.getName());
        else ((Sign) state).setLine(1, "???");
        switch (sign.getStat()) {
            case KILLS:
                if (player != null) ((Sign) state).setLine(2, "Kills: " + player.getStatsKills());
                else ((Sign) state).setLine(2, "Kills: ???");
                break;
            case WINS:
                if (player != null) ((Sign) state).setLine(2, "Wins: " + player.getStatsWins());
                else ((Sign) state).setLine(2, "Wins: ???");
                break;
            case KD_RATIO:
                if (player != null) ((Sign) state).setLine(2, "KD: " + player.getStatsKD());
                else ((Sign) state).setLine(2, "KD: ???");
                break;
        }
        ((Sign) state).setLine(3, "");
        state.update();
    }

    public int getPeaceTimeMins() {
        return ironWalls || diamondWalls ? 3 : peaceTimeMins;
    }
}
