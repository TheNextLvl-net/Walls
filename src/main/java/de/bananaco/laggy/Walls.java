package de.bananaco.laggy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


import de.bananaco.laggy.schematic.SchematicLoader;

public class Walls extends JavaPlugin implements Runnable {

    public static final Random GLOBAL_RANDOM = new Random();

    @Override
    public void onEnable() {
        // UpdateServerState(1, 0, "Generating world");
        getServer().getScheduler().scheduleSyncDelayedTask(this, this, 1);
    }

    public static void UpdateServerState(int state, int players, String motd) {
        try {
            ProcessUpdateStatement(state, players, motd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ProcessUpdateStatement(int state, int players, String motd) throws Exception {
        Connection handle = null;
        PreparedStatement PreparedStatement = null;
        ResultSet resultSet = null;
        int ServerNumber = Bukkit.getPluginManager().getPlugin("MCTheWalls").getConfig().getInt("ServerNumber");
        FileConfiguration config = Bukkit.getServer().getPluginManager().getPlugin("MCTheWalls").getConfig();

        try {
            Class.forName("com.mysql.jdbc.Driver");

            handle = DriverManager.getConnection("jdbc:mysql://" + config.getString("host") + ":" + config.getString("port") + "/" + config.getString("database"), config.getString("username"), config.getString("password"));

            PreparedStatement = handle.prepareStatement("UPDATE `servers` SET `state` = ?, `players` = ?, `motd` = ? WHERE `server` = ?");
            PreparedStatement.setInt(1, state);
            PreparedStatement.setInt(2, players);
            PreparedStatement.setString(3, motd);
            PreparedStatement.setInt(4, ServerNumber);
            PreparedStatement.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }

            if (handle != null) {
                handle.close();
            }
        }
    }

    // empty gen - we do it all programmatically

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new EmptyGen();
    }

    public Chunk[] getChunks(Location bottomLeft) {
        Chunk[] chunks = new Chunk[8 * 8];
        int count = 0;
        World world = bottomLeft.getWorld();

        int cx = world.getChunkAt(bottomLeft).getX();
        int cz = world.getChunkAt(bottomLeft).getZ();

        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                chunks[count] = world.getChunkAt(cx + x, cz + z);
                count++;
            }
        }
        return chunks;
    }

    public void fillChest(Chest c) {
        boolean x = false;

        while (!x) {
            x = calcRandom(c, new ItemStack(Material.PORK, 5), 0.15, x);
            x = calcRandom(c, new ItemStack(Material.BOW, 1), 0.5, x);
            x = calcRandom(c, new ItemStack(Material.ARROW, 8), 0.5, x);
            x = calcRandom(c, new ItemStack(Material.IRON_CHESTPLATE, 1), 0.025, x);
            x = calcRandom(c, new ItemStack(Material.IRON_HELMET, 1), 0.025, x);
            x = calcRandom(c, new ItemStack(Material.LEATHER_LEGGINGS, 1), 0.05, x);
            x = calcRandom(c, new ItemStack(Material.BONE, 8), 0.15, x);
            /*
             * enchanted stuff
             * 
             * ItemStack enchantedItem = new ItemStack(Material.LEATHER_LEGGINGS, 1); enchantedItem.addUnsafeEnchantment(Enchantment.DURABILITY, 2);
             * 
             * calcRandom(c, enchantedItem, 0.05);
             */
            x = calcRandom(c, new ItemStack(Material.COAL, 16), 0.05, x);
            x = calcRandom(c, new ItemStack(Material.TORCH, 16), 0.10, x);
            x = calcRandom(c, new ItemStack(Material.LOG, 16), 0.10, x);
            x = calcRandom(c, new ItemStack(Material.COOKED_FISH, 8), 0.05, x);
            x = calcRandom(c, new ItemStack(Material.TNT, 1), 0.05, x);
            // x = calcRandom(c, new ItemStack(Material.EMERALD, 1), 0.10, x);
            x = calcRandom(c, new ItemStack(Material.EMERALD, 3), 0.5, x);
            x = calcRandom(c, new ItemStack(Material.GOLD_SWORD, 1), 0.025, x);
            x = calcRandom(c, new ItemStack(Material.SNOW_BALL, 1), 0.025, x);
            x = calcRandom(c, new ItemStack(Material.MONSTER_EGG, 1, (short) 120), 0.05, x); // Villager
            x = calcRandom(c, new ItemStack(Material.MONSTER_EGG, 1, (short) 90), 0.05, x); // Pig
            x = calcRandom(c, new ItemStack(Material.EXP_BOTTLE, 4), 0.05, x);
            x = calcRandom(c, new ItemStack(Material.BOOKSHELF, 3), 0.05, x);
            x = calcRandom(c, new ItemStack(Material.WEB, 2), 0.05, x);
            x = calcRandom(c, new ItemStack(Material.DIAMOND, 2), 0.005, x);
            x = calcRandom(c, new ItemStack(Material.SIGN, 1), 0.05, x);

        }
    }

    private boolean calcRandom(Chest c, ItemStack item, double probability, boolean found) {
        if ((new Random()).nextInt(100) < probability * 100) {
            c.getInventory().addItem(item);
            return true;
        }
        return found;
    }

    private int clan1 = -1;
    private int clan2 = -1;
    private int clan3 = -1;
    private int clan4 = -1;
    private boolean fixedGeneration = false;
    
    @SuppressWarnings("deprecation")
	@Override
    public void run() {
		fixedGeneration = this.getConfig().getBoolean("fixedgeneration");
		clan1 = this.getConfig().getInt("clan1");
		clan2 = this.getConfig().getInt("clan2");
		clan3 = this.getConfig().getInt("clan3");
		clan4 = this.getConfig().getInt("clan4");

		this.getLogger().info("Starting arena building...");
        World world = Bukkit.getWorlds().get(0);
        world.setSpawnLocation(0, 64, 0);

        this.getLogger().info("Picking biomes...");
        List<Class<? extends WorldFiller>> locations = new ArrayList<Class<? extends WorldFiller>>();
        locations.add(CanyonBiome.class);
        locations.add(DesertBiome.class);
        locations.add(FungiForestBiome.class);
        locations.add(IceBiome.class);
        locations.add(IslandBiome.class);
        locations.add(JungleBiome.class);
        locations.add(MountainBiome.class);
        locations.add(WastelandBiome.class);
        locations.add(NetherBiome.class);
        locations.add(RuinCityBiome.class);
        
        if (!fixedGeneration){
        	
        	Collections.shuffle(locations);
        }

        int numberBiomes = 4;

        int minX1 = -141;
        int minX2 = 11;
        int minZ1 = -9;
        int minZ2 = 143;
        int maxX1 = -15;
        int maxX2 = 137;
        int maxZ1 = 117;
        int maxZ2 = 269;
        int startY = 1;
        int groundLevel = 61;
        WorldFiller[] fillers = new WorldFiller[numberBiomes];
        for (int i = 0; i < numberBiomes; i++) {
            int minX = (i % 2 == 0) ? minX1 : minX2;
            int minZ = (i < 2) ? minZ1 : minZ2;
            int maxX = (i % 2 == 0) ? maxX1 : maxX2;
            int maxZ = (i < 2) ? maxZ1 : maxZ2;
            if (fixedGeneration && clan1 >-1 && clan2 >-1 &&  clan3>-1 && clan4>-1){
            	if (i==0){            		
            		try {
            			fillers[i] = (WorldFiller) locations.get(clan1).getConstructors()[0].newInstance(world, minX, minZ, maxX, maxZ, startY, groundLevel);
            		} catch (Exception e) {
            			e.printStackTrace();
            		}            	
            	}else if (i==1){            		
            		try {
            			fillers[i] = (WorldFiller) locations.get(clan2).getConstructors()[0].newInstance(world, minX, minZ, maxX, maxZ, startY, groundLevel);
            		} catch (Exception e) {
            			e.printStackTrace();
            		}            	
            	}else if (i==2){            		
            		try {
            			fillers[i] = (WorldFiller) locations.get(clan3).getConstructors()[0].newInstance(world, minX, minZ, maxX, maxZ, startY, groundLevel);
            		} catch (Exception e) {
            			e.printStackTrace();
            		}            	
            	}else if (i==3){            		
            		try {
            			fillers[i] = (WorldFiller) locations.get(clan4).getConstructors()[0].newInstance(world, minX, minZ, maxX, maxZ, startY, groundLevel);
            		} catch (Exception e) {
            			e.printStackTrace();
            		}            	
            	}
            }else{            	
            	try {
            		fillers[i] = (WorldFiller) locations.remove(0).getConstructors()[0].newInstance(world, minX, minZ, maxX, maxZ, startY, groundLevel);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
        }
        
        this.getLogger().info("Biomes picked!");
       

        this.getLogger().info("Generating areas...");
        for (int i = 0; i < numberBiomes; i++) {
            WorldFiller filler = fillers[i];
            filler.generate();
            getLogger().info(String.format("Area %d (%s) generated", i + 1, filler.getClass().getSimpleName()));        
        }

        this.getLogger().info("Areas generated!");
        this.getLogger().info("Populating areas");
        for (int i = 0; i < numberBiomes; i++) {
            WorldFiller filler = fillers[i];
            filler.populate();
            getLogger().info(String.format("Area %d (%s) populated", i + 1, filler.getClass().getSimpleName()));
        }
        this.getLogger().info("Areas populated!");

//        pickAndPasteCenter();
//        getLogger().info("Center Picked and Pasted!");

        this.getLogger().info("Filling chests...");
        world.loadChunk((new Location(world, -145, 66, -12)).getChunk());
        world.loadChunk((new Location(world, 140, 66, -12)).getChunk());
        world.loadChunk((new Location(world, 140, 66, 272)).getChunk());
        world.loadChunk((new Location(world, 147, 66, 277)).getChunk());
        world.loadChunk((new Location(world, 145, 66, 280)).getChunk());
        world.loadChunk((new Location(world, -145, 65, 272)).getChunk());

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState e : chunk.getTileEntities()) {
                if (e instanceof Chest) {
                    Chest chest = (Chest) e;
                    fillChest(chest);
                }
                if ((e instanceof Dispenser)) {
                    Dispenser dispenser = (Dispenser)e;
                    dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 56));
                    dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 61));
                    dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 56));
                    dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 61));
                    dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 56));
                    dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 61));
               }
           }
        }
        getLogger().info("Chests & dispensers filled!");
        //      
        this.getLogger().info("Arena building complete!");
    }
    
    
    private void pickAndPasteCenter() {
        SchematicLoader loader = new SchematicLoader(this);
        // choose a random schematic
        
        List<String> options = loader.getSchematics("_center_");
        
        String chosen = options.get(Walls.GLOBAL_RANDOM.nextInt(options.size()));
        loader.paste(chosen, new Location(Bukkit.getWorlds().get(0), -13, 40, 119));
    }

}