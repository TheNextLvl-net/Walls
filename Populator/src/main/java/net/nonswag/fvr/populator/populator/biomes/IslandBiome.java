package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.WaterLilyPopulator;
import net.nonswag.fvr.populator.populator.structures.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;

public class IslandBiome extends WorldFiller {

    private Block highest = null;

    public IslandBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.BEACH, minX, minZ, maxX, maxZ, startY, groundLevel);
        addPopulator(new SandPopulator());
        addPopulator(new WildGrassPopulator((byte) 1));
        addPopulator(new FlowerPopulator());
        addPopulator(new MelonPopulator());
        addPopulator(new TreePopulator(TreePopulator.Type.FOREST));
        addPopulator(new PumpkinPopulator());
        addPopulator(new SugarcanePopulator());
        addPopulator(new WaterLilyPopulator());
        addPopulator(new LapisRingPopulator());
    }

    public void doWater() {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y <= groundLevel; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR) block.setType(Material.STATIONARY_WATER);
                }
            }
        }
    }

    public void createHole(Block start) {
        Block block = start;
        int d = 10;
        createSphere(block, d);
        Random random = Populator.RANDOM;
        for (int i = d; i > 0; i--) {
            block = block.getRelative(d - random.nextInt(d * 2), -random.nextInt(d), d - random.nextInt(d * 2));
            createSphere(block, d);
        }
    }

    public void createSphere(Block center, int diameter) {
        Vector vector = new Vector(0, 0, 0);
        for (int x = -diameter; x < diameter; x++) {
            for (int z = -diameter; z < diameter; z++) {
                for (int y = -diameter; y < diameter; y++) {
                    double d = new Vector(x, y, z).distance(vector);
                    if (d < diameter) center.getRelative(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 9);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(0.0122D);
        perlin.setScale(0.053625D);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 15.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 30.0) abs = 15.0;
                else abs = abs - 15.0;
                double n1 = simplex.noise(x, z, 0.45D, 0.7D) * 2.3D;
                double n2 = perlin.noise(x, z, 0.75D, 0.6D) * 3.5D;
                double noise = (Math.abs(n1 - n2 / 2) * groundLevel);
                int highest = (int) (groundLevel / 2 + noise) - 5;
                for (int i = 0; i < abs; i++) highest = (highest * 9 + groundLevel) / 10;
                for (int y = startY; y < highest - 3; y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest; y++) {
                    world.getBlockAt(x, y, z).setType(Material.DIRT);
                }
                world.getBlockAt(x, highest, z).setType(Material.GRASS);
                if (this.highest == null || highest > this.highest.getY()) {
                    this.highest = world.getBlockAt(x, highest, z);
                }
            }
        }
        this.doWater();
        this.createHole(this.highest);
    }
}
