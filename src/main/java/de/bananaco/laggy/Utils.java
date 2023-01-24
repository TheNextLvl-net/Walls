package de.bananaco.laggy;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Utils {
    /**
     * Iteratively determines the highest non-air block
     * 
     * @param chunk
     * @param x
     * @param z
     * @return Block highest non-air
     */
    public static Block getHighestBlock(Chunk chunk, int x, int z) {
        Block block = null;
        // Return the highest block
        for (int i = 127; i >= 0; i--)
            if ((block = chunk.getBlock(x, i, z)).getTypeId() != 0)
                return block;
        // And as a matter of completeness, return the lowest point
        return block;
    }
    
    /**
     * Iteratively determines the highest grass/mycel block
     * 
     * @param world
     * @param x
     * @param z
     * @return Block highest non-air
     */
    public static Block getHighestGrassBlock(World world, int x, int z) {
        Block block = null;
        // Return the highest block
        for (int i = world.getMaxHeight(); i >= 0; i--)
        {
        	int id = (block = world.getBlockAt(x, i, z)).getTypeId();
            if((id == Material.GRASS.getId() || id == Material.MYCEL.getId()))
                return block;
        }
        // And as a matter of completeness, return the lowest point
        return block;
    }
    
    /**
     * Iteratively determines the highest grass/mycel block
     * 
     * @param chunk
     * @param x
     * @param z
     * @return Block highest non-air
     */
    public static Block getHighestGrassBlock(Chunk chunk, int x, int z) {
		Block block = null;
		for (int i = chunk.getWorld().getMaxHeight(); i >= 0; i--) {
			int id = (block = chunk.getBlock(x, i, z)).getTypeId();
			if ((id == Material.GRASS.getId() || id == Material.MYCEL.getId())) {
				return block;
			}
		}
		return block;
	}
    
    /**
     * Iteratively determines the highest solid block
     * 
     * @param chunk
     * @param x
     * @param z
     * @return Block highest non-air
     */
    public static Block getHighestSolidBlock(Chunk chunk, int x, int z) {
        Block block = null;
        // Return the highest block
        for (int i = 127; i >= 0; i--)
            if (!(block = chunk.getBlock(x, i, z)).isLiquid() && block.getTypeId() != 0)
                return block;
        // And as a matter of completeness, return the lowest point
        return block;
    }
}
