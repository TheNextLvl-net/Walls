package net.nonswag.fvr.populator.populator.structures;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.Utils;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.schematic.SchematicLoader;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class TrapPopulator extends BlockPopulator {
    private final WorldFiller filler;
    private final SchematicLoader loader = new SchematicLoader();
    private final List<String> options = loader.getSchematics("trap");

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        if (!contains(chunk)) return;
        if (rand.nextInt(100) < 27) return;
        int x = 6 + rand.nextInt(4);
        int z = 6 + rand.nextInt(4);
        int depth = 20 + rand.nextInt(30);
        Block highest = Utils.getHighestBlock(chunk, x, z);
        if (highest.getY() <= 40) return;
        if (highest.getY() > filler.groundLevel) {
            highest = chunk.getBlock(x, filler.groundLevel - 3, z);
        }
        Block start = highest.getRelative(0, -depth, 0);
        if (start.getType() != Material.AIR && start.getType() != Material.BEDROCK) {
            paste(start.getLocation(), rand);
        }
    }

    public void paste(Location loc, Random rand) {
        if (options.isEmpty()) return;
        String chosen = options.get(rand.nextInt(options.size()));
        loader.paste(chosen, loc);
    }

    public boolean contains(Chunk chunk) {
        if (filler.contains(chunk.getX() * 16, chunk.getZ() * 16)) {
            if (filler.contains(chunk.getX() * 16 + 32, chunk.getZ() * 16 + 32)) {
                return filler.contains(chunk.getX() * 16 - 32, chunk.getZ() * 16 - 32);
            }
        }
        return false;
    }
}
