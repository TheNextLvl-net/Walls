package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.BushPopulator;
import net.nonswag.fvr.populator.populator.blocks.WaterLilyPopulator;
import net.nonswag.fvr.populator.populator.structures.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.MushroomCow;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class FungiForestBiome extends WorldFiller {

    public FungiForestBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.SWAMPLAND, minX, minZ, maxX, maxZ, startY, groundLevel);
        getMobPopulator().getMobs().remove(Cow.class);
        getMobPopulator().getMobs().add(MushroomCow.class);
        addPopulator(new ShallowLakePopulator(this), true);
        addPopulator(new WaterLilyPopulator());
        addPopulator(new MushroomPopulator());
        addPopulator(new HugeMushroomPopulator());
        addPopulator(new BushPopulator(5));
        addPopulator(new FungiTreePopulator(this));
        addPopulator(new WildGrassPopulator((byte) 1));
        addPopulator(new WildGrassPopulator((byte) 2));
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(1 / 24d);
        perlin.setScale(1 / 24d);
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
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.DIRT);
                }
                Block b = world.getBlockAt(x, highest, z);
                if (perlin.noise(x, z, 0.5D, 0.5D) > .5) b.setType(Material.MYCEL);
                else b.setType(Material.GRASS);
            }
        }
    }
}
