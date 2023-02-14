package net.nonswag.fvr.populator.populator.structures;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;

import java.util.Random;

public class LapisRingPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        if (rand.nextInt(100) > 13) return;
        Block block = chunk.getBlock(rand.nextInt(16), 32 + rand.nextInt(64), rand.nextInt(16));
        if (block.getType() == Material.AIR || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
            return;
        createRing(block, 5 + rand.nextInt(5));
    }

    public void createRing(Block block, int radius) {
        Vector v = new Vector(0, 0, 0);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 3; y++) {
                    Vector d = new Vector(x, 0, z);
                    double distance = d.distance(v);
                    Block relative = block.getRelative(x, y, z);
                    if (relative.getType() != Material.OBSIDIAN && relative.getType() != Material.CLAY && relative.getType() != Material.STONE)
                        return;
                    if (distance < radius - 3) {
                        if (y == 0) relative.setType(Material.LAVA);
                        else relative.setType(Material.AIR);
                    } else if (distance < radius - 2) relative.setType(Material.LAPIS_BLOCK);
                    else if (distance < radius - 1) relative.setType(Material.LAPIS_ORE);
                    else if (distance < radius) relative.setType(Material.OBSIDIAN);
                }
            }
        }
    }
}
