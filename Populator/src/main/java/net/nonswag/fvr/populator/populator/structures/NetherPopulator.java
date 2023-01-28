package net.nonswag.fvr.populator.populator.structures;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class NetherPopulator extends BlockPopulator {

    private final int minSteps, maxSteps, chance;

    public NetherPopulator() {
        this.minSteps = 7;
        this.maxSteps = 20;
        this.chance = 100;
    }

    @Override
    public void populate(World world, Random random, Chunk source) {

        if (random.nextInt(100) > chance)
            return;

        int x = (source.getX() << 4);
        int z = (source.getZ() << 4);
        int y;

        int numSteps = random.nextInt(maxSteps - minSteps + 1) + minSteps;

        for (int i = 0; i < numSteps; i++) {
            x += random.nextInt(3) - 1;
            z += random.nextInt(3) - 1;
            y = world.getHighestBlockYAt(x, z);
            Block b = world.getBlockAt(x, y, z);

            if ((b.getRelative(0, -1, 0).getType() == Material.NETHER_BRICK || b.getRelative(0, -1, 0).getType() == Material.SOUL_SAND || b.getRelative(0, -1, 0).getType() == Material.NETHERRACK) && b.getType().equals(Material.AIR)) {
                
                 if (random.nextBoolean()){
                     b.setType(Material.SOUL_SAND);
                 }
            }
        }
    }

}
