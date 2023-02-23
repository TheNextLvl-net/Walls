package net.nonswag.fvr.populator.populator.structures;

import net.nonswag.fvr.populator.Utils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;

import java.util.Random;

public class ShrubberyPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int chance = 18;
        if (random.nextInt(100) > chance) return;
        Block block = Utils.getHighestBlock(chunk, random.nextInt(16), random.nextInt(16));
        if (block.getType() != Material.STONE) return;
        if (!block.getRelative(BlockFace.UP).isEmpty()) return;
        createShrubbery(block, random);
    }

    public void createShrubbery(Block block, Random rand) {
        Vector v = new Vector(0, 0, 0);
        Block rel;
        for (int x = -4; x <= 4; x++)
            for (int z = -4; z <= 4; z++)
                for (int y = -rand.nextInt(4); y <= 0; y++) {
                    rel = block.getRelative(x, y, z);
                    Vector vi = new Vector(x, y - 1, z);
                    if (rel.getType() == Material.STONE && vi.distance(v) < 4) {
                        rel.setType(Material.DIRT);
                    }
                }
        for (int x = -3; x <= 3; x++)
            for (int z = -3; z <= 3; z++)
                for (int y = 2; y <= 5; y++) {
                    rel = block.getRelative(x, y, z);
                    Vector vi = new Vector(x, y - 2, z);
                    if (vi.distance(v) <= 2)
                        rel.setType(Material.LEAVES);
                }
        rel = block.getRelative(0, 1, 0);
        rel.setType(Material.LOG);
        rel = block.getRelative(0, 2, 0);
        rel.setType(Material.LOG);
    }
}
