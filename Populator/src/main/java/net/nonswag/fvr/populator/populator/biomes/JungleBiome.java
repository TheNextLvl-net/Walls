package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.MediumTreePopulator;
import net.nonswag.fvr.populator.populator.blocks.SmallTreePopulator;
import net.nonswag.fvr.populator.populator.blocks.TreePopulator;
import net.nonswag.fvr.populator.populator.structures.WildGrassPopulator;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Wolf;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class JungleBiome extends WorldFiller {

    public JungleBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.JUNGLE, minX, minZ, maxX, maxZ, startY, groundLevel);
        getMobPopulator().getMobs().add(Wolf.class);
        addPopulator(new SmallTreePopulator(this));
        addPopulator(new MediumTreePopulator(this));
        addPopulator(new TreePopulator(this));
        addPopulator(new WildGrassPopulator(this, GrassSpecies.NORMAL));
        addPopulator(new WildGrassPopulator(this, GrassSpecies.FERN_LIKE));
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(0.0022D);
        perlin.setScale(0.013625D);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                double n1 = simplex.noise(x, z, 0.45D, 0.7D) * 3.3D;
                double n2 = perlin.noise(x, z, 0.75D, 0.6D) * 2.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel) - 1.75;
                int highest = (int) (groundLevel / 2 + noise) - 3;
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }
                for (int y = startY; y < highest - 3; y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest; y++) {
                    world.getBlockAt(x, y, z).setType(Material.DIRT);
                }
                world.getBlockAt(x, highest, z).setType(Material.GRASS);
            }
        }
    }
}
