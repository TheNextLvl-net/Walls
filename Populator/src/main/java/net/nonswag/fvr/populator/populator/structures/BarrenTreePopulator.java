package net.nonswag.fvr.populator.populator.structures;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class BarrenTreePopulator extends BlockPopulator
{
    @Override
    public void populate(World world, Random rnd, Chunk chunk)
    {
        if(rnd.nextInt(3) >= 1)
            return;
        int centerX;
        int centerZ;

        int multiplier = 1;

        for (int i = 0; i < multiplier; i++) {
            centerX = (chunk.getX() << 4) + rnd.nextInt(16);
            centerZ = (chunk.getZ() << 4) + rnd.nextInt(16);
            int centerY = world.getHighestBlockYAt(centerX, centerZ) - 1;
            Block sourceBlock = world.getBlockAt(centerX, centerY, centerZ);

            if (sourceBlock.getType() == Material.GRASS) {
                createTree(sourceBlock.getRelative(BlockFace.UP).getLocation(), rnd);
            }
        }
    }
    
    private void createTree(Location start, Random rnd)
    {
        int height = 8 + rnd.nextInt(4);
        for(int j = 0; j < height; j++)
            start.clone().add(0, j, 0).getBlock().setType(Material.LOG);
    }
}
