package net.nonswag.fvr.populator;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Utils {

    public static Block getHighestBlock(Chunk chunk, int x, int z) {
        return chunk.getWorld().getHighestBlockAt(x, z);
    }

    public static Block getHighestGrassBlock(Chunk chunk, int x, int z) {
        Block block = null;
        for (int i = chunk.getWorld().getMaxHeight(); i >= 0; i--) {
            Material material = (block = chunk.getBlock(x, i, z)).getType();
            if ((material == Material.GRASS || material == Material.MYCEL)) return block;
        }
        return block;
    }

}
