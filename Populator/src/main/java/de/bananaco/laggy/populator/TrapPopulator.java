package de.bananaco.laggy.populator;

import de.bananaco.laggy.Utils;
import de.bananaco.laggy.WorldFiller;
import de.bananaco.laggy.schematic.SchematicLoader;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class TrapPopulator extends BlockPopulator {

    public JavaPlugin plugin;

    public int chance = 27;

    public WorldFiller filler;

    public TrapPopulator(WorldFiller filler) {
        this.plugin = (JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin("WallsPopulator");
        this.filler = filler;
    }

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        if (rand.nextInt(100) < chance) {
            return;
        }
        if (!contains(chunk)) {
            return;
        }

        int x = 6 + rand.nextInt(4);
        int z = 6 + rand.nextInt(4);
        int depth = 20 + rand.nextInt(30);
        Block highest = Utils.getHighestBlock(chunk, x, z);
        if (highest.getY() > 40) {
            // prevent crazy pasting
            if (highest.getY() > filler.groundLevel) {
                highest = chunk.getBlock(x, filler.groundLevel - 3, z);
            }

            Block start = highest.getRelative(0, -depth, 0);
            if (start.getType() != Material.AIR && start.getType() != Material.BEDROCK) {
                paste(start.getLocation(), rand);
            }
        }
    }

    public void paste(Location loc, Random rand) {
        SchematicLoader loader = new SchematicLoader(plugin);
        // choose a random schematic
        List<String> options = loader.getSchematics("trap");
        if (options.isEmpty()) return;
        String chosen = options.get(rand.nextInt(options.size()));
        loader.paste(chosen, loc);
    }

    // stricter conditions
    public boolean contains(Chunk chunk) {
        if (filler.contains(chunk.getX() * 16, chunk.getZ() * 16)) {
            if (filler.contains(chunk.getX() * 16 + 32, chunk.getZ() * 16 + 32)) {
                return filler.contains(chunk.getX() * 16 - 32, chunk.getZ() * 16 - 32);
            }
        }
        return false;
    }
}
