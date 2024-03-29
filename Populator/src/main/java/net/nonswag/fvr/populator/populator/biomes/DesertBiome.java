package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.structures.CactiPopulator;
import net.nonswag.fvr.populator.populator.structures.OasisPopulator;
import net.nonswag.fvr.populator.populator.structures.TreePopulator;
import net.nonswag.fvr.populator.populator.structures.WildGrassPopulator;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class DesertBiome extends WorldFiller {

    public DesertBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.DESERT, minX, minZ, maxX, maxZ, startY, groundLevel);
        addPopulator(new WildGrassPopulator(this, GrassSpecies.DEAD, Material.SAND));
        addPopulator(new OasisPopulator(this));
        addPopulator(new CactiPopulator());
        addPopulator(new TreePopulator(TreePopulator.Type.SAVANNA));
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(1 / 48d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                abs = abs < 20 ? 0 : abs - 20;
                double noise = simplex.noise(x, z, 0.35D, 0.65D) * 4.5D;
                int highest = (int) (groundLevel + noise);
                for (int i = 0; i < abs; i++) highest = (highest * 9 + groundLevel) / 10;
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y <= highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.SAND);
                }
                world.getBlockAt(x, highest - 4, z).setType(Material.SANDSTONE);
            }
        }
    }
}
