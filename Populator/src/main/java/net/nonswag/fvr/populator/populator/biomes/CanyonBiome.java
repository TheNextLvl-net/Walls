package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.LakeCreekPopulator;
import net.nonswag.fvr.populator.populator.structures.FlowerPopulator;
import net.nonswag.fvr.populator.populator.structures.GravelStackPopulator;
import net.nonswag.fvr.populator.populator.structures.TreePopulator;
import net.nonswag.fvr.populator.populator.structures.WildGrassPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class CanyonBiome extends WorldFiller {

    public Block highest = null;
    Material stone = Material.STONE;
    Material dirt = Material.DIRT;
    Material grass = Material.GRASS;

    public CanyonBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.SMALL_MOUNTAINS, minX, minZ, maxX, maxZ, startY, groundLevel);
        addPopulator(new TreePopulator(TreePopulator.Type.SEASONAL_FOREST));
        addPopulator(new WildGrassPopulator((byte) 1));
        addPopulator(new FlowerPopulator());
        addPopulator(new GravelStackPopulator());
        addPopulator(new LakeCreekPopulator());
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(1 / 48d);
        perlin.setScale(1 / 24d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 20) abs -= 10;
                else if (abs < 30) abs = 20;
                else abs -= 10;
                if (abs < 5) abs = 5;
                double n1 = simplex.noise(x, z, 0.45D, 0.7D) * 3.3D;
                double n2 = perlin.noise(x, z, 0.75D, 0.6D) * 6.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel / 4);
                int highest = (int) (groundLevel / 2 + noise);
                for (int i = 0; i < abs; i++) highest = (highest * 9 + groundLevel) / 10;
                if (n1 > 2) highest -= 5;
                if (n2 > 3) highest -= 3;
                if (highest >= world.getMaxHeight()) {
                    highest = world.getMaxHeight() - 1;
                }
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(stone);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(dirt);
                }
                Block b = world.getBlockAt(x, highest, z);
                b.setType(grass);
                if (this.highest == null || b.getY() > this.highest.getY()) {
                    this.highest = b;
                }
            }
        }
    }
}
