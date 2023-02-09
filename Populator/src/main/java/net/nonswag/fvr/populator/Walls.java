package net.nonswag.fvr.populator;

import net.nonswag.core.api.math.MathUtil;
import net.nonswag.core.utils.StringUtil;
import net.nonswag.fvr.populator.populator.biomes.*;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.*;

public class Walls extends JavaPlugin {

    public static final Random GLOBAL_RANDOM = new Random();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                long now = System.currentTimeMillis();
                World world = Bukkit.getWorlds().get(0);
                world.setSpawnLocation(0, 64, 0);
                List<Class<? extends WorldFiller>> locations = new ArrayList<>(Arrays.asList(
                        CanyonBiome.class, DesertBiome.class, FungiForestBiome.class, IceBiome.class, IslandBiome.class,
                        JungleBiome.class, MountainBiome.class, WastelandBiome.class, NetherBiome.class, RuinCityBiome.class
                ));
                Collections.shuffle(locations);
                System.out.println("Starting biome generation");
                for (int i = 0; i < 4; i++) {
                    int minX = (i % 2 == 0) ? -141 : 11;
                    int minZ = (i < 2) ? -9 : 143;
                    int maxX = (i % 2 == 0) ? -15 : 137;
                    int maxZ = (i < 2) ? 117 : 269;
                    Constructor<?> constructor = locations.remove(0).getConstructors()[0];
                    WorldFiller filler = (WorldFiller) constructor.newInstance(world, minX, minZ, maxX, maxZ, 1, 61);
                    filler.generate();
                    filler.populate();
                    System.out.println("Generated biome " + (i + 1) + ": " + filler.getClass().getSimpleName());
                }
                String time = StringUtil.format("#,##0.000", (System.currentTimeMillis() - now) / 1000d);
                System.out.println("Generated biomes in: " + time + "s");
                world.loadChunk(new Location(world, -145, 66, -12).getChunk());
                world.loadChunk(new Location(world, 140, 66, -12).getChunk());
                world.loadChunk(new Location(world, 140, 66, 272).getChunk());
                world.loadChunk(new Location(world, 147, 66, 277).getChunk());
                world.loadChunk(new Location(world, 145, 66, 280).getChunk());
                world.loadChunk(new Location(world, -145, 65, 272).getChunk());
                for (Chunk chunk : world.getLoadedChunks()) {
                    for (BlockState e : chunk.getTileEntities()) {
                        if (e instanceof Chest) fillChest((Chest) e);
                        if (!(e instanceof Dispenser)) continue;
                        Dispenser dispenser = (Dispenser) e;
                        dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 56));
                        dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 61));
                        dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 56));
                        dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 61));
                        dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 56));
                        dispenser.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 61));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new EmptyGen();
    }

    public void fillChest(Chest c) {
        boolean filled = false;
        while (!filled) {
            filled = fill(c, new ItemStack(Material.PORK, 5), 0.15, false);
            filled = fill(c, new ItemStack(Material.BOW, 1), 0.5, filled);
            filled = fill(c, new ItemStack(Material.ARROW, 8), 0.5, filled);
            filled = fill(c, new ItemStack(Material.IRON_CHESTPLATE, 1), 0.025, filled);
            filled = fill(c, new ItemStack(Material.IRON_HELMET, 1), 0.025, filled);
            filled = fill(c, new ItemStack(Material.LEATHER_LEGGINGS, 1), 0.05, filled);
            filled = fill(c, new ItemStack(Material.BONE, 8), 0.15, filled);
            filled = fill(c, new ItemStack(Material.COAL, 16), 0.05, filled);
            filled = fill(c, new ItemStack(Material.TORCH, 16), 0.10, filled);
            filled = fill(c, new ItemStack(Material.LOG, 16), 0.10, filled);
            filled = fill(c, new ItemStack(Material.COOKED_FISH, 8), 0.05, filled);
            filled = fill(c, new ItemStack(Material.TNT, 1), 0.05, filled);
            filled = fill(c, new ItemStack(Material.EMERALD, 3), 0.5, filled);
            filled = fill(c, new ItemStack(Material.GOLD_SWORD, 1), 0.025, filled);
            filled = fill(c, new ItemStack(Material.SNOW_BALL, 1), 0.025, filled);
            filled = fill(c, new ItemStack(Material.MONSTER_EGG, 1, (short) 120), 0.05, filled); // Villager
            filled = fill(c, new ItemStack(Material.MONSTER_EGG, 1, (short) 90), 0.05, filled); // Pig
            filled = fill(c, new ItemStack(Material.EXP_BOTTLE, 4), 0.05, filled);
            filled = fill(c, new ItemStack(Material.BOOKSHELF, 3), 0.05, filled);
            filled = fill(c, new ItemStack(Material.WEB, 2), 0.05, filled);
            filled = fill(c, new ItemStack(Material.DIAMOND, 2), 0.005, filled);
            filled = fill(c, new ItemStack(Material.SIGN, 1), 0.05, filled);
        }
    }

    private boolean fill(Chest c, ItemStack item, double probability, boolean filled) {
        if (MathUtil.chance(probability * 100)) {
            c.getInventory().addItem(item);
            return true;
        }
        return filled;
    }
}