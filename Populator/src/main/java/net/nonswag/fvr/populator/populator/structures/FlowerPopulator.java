package net.nonswag.fvr.populator.populator.structures;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class FlowerPopulator extends BlockPopulator {

    private final int flowerPatchChance, plantFlowerChance, numSteps;
    private final Material[] flowers;

    public FlowerPopulator() {
        this.flowerPatchChance = 25;
        this.plantFlowerChance = 10;
        this.flowers = new Material[]{Material.RED_ROSE, Material.YELLOW_FLOWER};
        this.numSteps = 7;
    }

    @Override
    public void populate(World world, Random random, Chunk source) {
        if (random.nextInt(100) < flowerPatchChance) {
            int x = (source.getX() << 4) + random.nextInt(16);
            int z = (source.getZ() << 4) + random.nextInt(16);
            Material flowerType = flowers[random.nextInt(flowers.length)];
            for (int i = 0; i < numSteps; i++) {
                x += random.nextInt(3) - 1;
                z += random.nextInt(3) - 1;
                int y = world.getHighestBlockYAt(x, z);
                if (world.getBlockAt(x, y - 1, z).getType() == Material.GRASS && random.nextInt(100) < plantFlowerChance) {
                    world.getBlockAt(x, y, z).setType(flowerType);
                }
            }
        }
    }
}
