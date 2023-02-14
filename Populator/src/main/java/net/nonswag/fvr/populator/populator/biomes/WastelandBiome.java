package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.BushPopulator;
import net.nonswag.fvr.populator.populator.structures.BarrenTreePopulator;
import net.nonswag.fvr.populator.populator.structures.HutPopulator;
import net.nonswag.fvr.populator.populator.structures.ShrubberyPopulator;
import net.nonswag.fvr.populator.populator.structures.WildGrassPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class WastelandBiome extends WorldFiller {

    public WastelandBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.DESERT, minX, minZ, maxX, maxZ, startY, groundLevel);
        this.addPopulator(new HutPopulator(), true);
        this.addPopulator(new BushPopulator(3));
        this.addPopulator(new WildGrassPopulator((byte) 0));
        this.addPopulator(new ShrubberyPopulator());
        this.addPopulator(new BarrenTreePopulator());
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(1 / 32d);
        perlin.setScale(1 / 32d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 20) abs = 0;
                else abs -= 20;
                double noise = simplex.noise(x, z, 0.35D, 0.65D) * 2D;

                int highest = (int) (groundLevel + noise);
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }
                double n2 = perlin.noise(x, z, 0.5D, 0.5D);
                if (n2 > 0 && n2 < .05)
                    highest += Populator.RANDOM.nextInt(2) + 2;
                else if (n2 >= .05 && n2 < .1)
                    highest += Populator.RANDOM.nextInt(2) + 5;
                else if (n2 >= .1 && n2 < .15)
                    highest += Populator.RANDOM.nextInt(2) + 8;
                else if (n2 >= .15 && n2 < .2)
                    highest += Populator.RANDOM.nextInt(2) + 4;
                else if (n2 >= .2 && n2 < .25)
                    highest += Populator.RANDOM.nextInt(2) + 3;

                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    if (perlin.noise(x, z, 0.5D, 0.5D) > 0)
                        world.getBlockAt(x, y, z).setType(Material.STONE);
                    else
                        world.getBlockAt(x, y, z).setType(Material.DIRT);
                }

                Block b = world.getBlockAt(x, highest, z);
                if (perlin.noise(x, z, 0.5D, 0.5D) > 0) b.setType(Material.STONE);
                else b.setType(Material.GRASS);
            }
        }
    }
}