package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.LakeCreekPopulator;
import net.nonswag.fvr.populator.populator.blocks.MushroomPopulator;
import net.nonswag.fvr.populator.populator.structures.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.MushroomCow;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class MountainBiome extends WorldFiller {

    public MountainBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.EXTREME_HILLS, minX, minZ, maxX, maxZ, startY, groundLevel);
        getMobPopulator().getMobs().add(MushroomCow.class);
        this.addPopulator(new TreePopulator(TreePopulator.Type.FOREST));
        this.addPopulator(new WildGrassPopulator((byte) 2));
        this.addPopulator(new FlowerPopulator());
        this.addPopulator(new GravelStackPopulator());
        this.addPopulator(new LakeCreekPopulator());
        this.addPopulator(new MushroomPopulator());
        this.addPopulator(new MelonPopulator());
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(0.0022D);
        perlin.setScale(0.01625D);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                abs = abs < 20 ? 15 : abs - 5;
                double n1 = simplex.noise(x, z, 0.45D, 0.7D) * 7.3D;
                double n2 = perlin.noise(x, z, 0.75D, 0.6D) * 9.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel) + 2;
                int highest = (int) (groundLevel / 2 + noise) - 3;
                for (int i = 0; i < abs; i++) highest = (highest * 9 + groundLevel) / 10;
                if (highest >= world.getMaxHeight()) highest = world.getMaxHeight() - 1;
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.DIRT);
                }
                Block block = world.getBlockAt(x, highest, z);
                block.setType(Material.GRASS);
            }
        }
    }
}
