package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.Populator_Bush;
import net.nonswag.fvr.populator.populator.blocks.Populator_Water_Lily;
import net.nonswag.fvr.populator.populator.structures.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.MushroomCow;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;

public class FungiForestBiome extends WorldFiller {
    public FungiForestBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.SWAMPLAND, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator tree1 = new FungiTreePopulator();
        BlockPopulator mushroom = new MushroomPopulator();
        BlockPopulator bush = new Populator_Bush(5);
        BlockPopulator hugeMushroom = new HugeMushroomPopulator();
        BlockPopulator shallowLake = new ShallowLakePopulator(this);
        BlockPopulator lily = new Populator_Water_Lily();
        mobs.mobs.add(MushroomCow.class);

        BlockPopulator grass1 = new WildGrassPopulator((byte) 1);
        BlockPopulator grass2 = new WildGrassPopulator((byte) 2);

        this.addPopulator(shallowLake, true);
        this.addPopulator(lily);
        this.addPopulator(mushroom);
        this.addPopulator(hugeMushroom);
        this.addPopulator(bush);
        this.addPopulator(bush);
        this.addPopulator(tree1);
        this.addPopulator(tree1);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass2);
    }

    @Override
    public void generate() {
        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator g2 = new PerlinOctaveGenerator(seed, 8);
        g.setScale(1 / 24d);
        g2.setScale(1 / 24d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 20)
                    abs = 0;
                else
                    abs -= 20;

                double noise = g.noise(x, z, 0.35D, 0.65D) * 2D;

                int highest = (int) (groundLevel + noise);
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }

                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.DIRT);
                }

                Block b = world.getBlockAt(x, highest, z);
                if (g2.noise(x, z, 0.5D, 0.5D) > .5) b.setType(Material.MYCEL);
                else b.setType(Material.GRASS);
            }
        }
    }
}
