package net.nonswag.fvr.populator.populator.blocks;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class Populator_Water_Lily extends BlockPopulator {

    @Override
    public void populate(World w, Random rnd, Chunk c) {
        if(rnd.nextInt(3) != 0)
            return;
        for (int x = c.getX() * 16; x < c.getX() * 16 + 16; x++) {
            for (int z = c.getZ() * 16; z < c.getZ() * 16 + 16; z++) {
                Block over_water = getHighestBlock(c, x, z);
                if (over_water != null) {
                    if (over_water.getType() == Material.AIR) {
                        int depth = waterDepth(over_water.getRelative(0, -1, 0));
                        if (depth <= 5) {
                            if (rnd.nextInt(100) < (8 * (6 - depth))) {
                                over_water.setType(Material.WATER_LILY);
                            }
                        }
                    }
                }
            }
        }
    }

    private Block getHighestBlock(Chunk chunk, int x, int z) {
        Block block = null;
        // Return the highest block
        for (int i = chunk.getWorld().getMaxHeight(); i >= 0; i--)
            if ((block = chunk.getBlock(x, i, z)).getType() == Material.WATER || (block = chunk.getBlock(x, i, z)).getType() == Material.STATIONARY_WATER)
                return block.getRelative(0, 1, 0);
        // And as a matter of completeness, return the lowest point
        return block;
    }

    private int waterDepth(Block surface) {
        int depth = 0;
        while (surface.getType() == Material.STATIONARY_WATER || surface.getType() == Material.WATER) {
            depth++;
            surface = surface.getRelative(0, -1, 0);
            if (depth > 5) {
                break;
            }
        }
        return depth;
    }
}
