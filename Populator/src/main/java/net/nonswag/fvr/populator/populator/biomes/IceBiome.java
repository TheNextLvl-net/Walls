package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.LavaLakePopulator;
import net.nonswag.fvr.populator.populator.structures.IceTreePopulator;
import net.nonswag.fvr.populator.populator.structures.PumpkinPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Snowman;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class IceBiome extends WorldFiller {

    public IceBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.ICE_PLAINS, minX, minZ, maxX, maxZ, startY, groundLevel);
        getMobPopulator().getMobs().add(Snowman.class);
        addPopulator(new IceTreePopulator());
        addPopulator(new PumpkinPopulator());
        addPopulator(new LavaLakePopulator());
    }

    public void doWater() {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y < groundLevel; y++) {
                    Material type = world.getBlockAt(x, y, z).getType();
                    if (type.equals(Material.AIR) || type.equals(Material.SNOW) || type.equals(Material.GRASS)) {
                        world.getBlockAt(x, y, z).setType(Material.STATIONARY_WATER);
                    }
                }
                Material type = world.getBlockAt(x, groundLevel, z).getType();
                if (type.equals(Material.AIR) || type.equals(Material.SNOW) || type.equals(Material.GRASS)) {
                    world.getBlockAt(x, groundLevel, z).setType(Material.ICE);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(0.0062D);
        perlin.setScale(0.11625D);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 25.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 35.0) abs = 25.0;
                else abs = abs - 10.0;
                double n1 = simplex.noise(x, z, 0.45D, 0.7D) * 8.3D;
                double n2 = perlin.noise(x, z, 0.75D, 0.6D) * 4.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel);
                int highest = (int) (groundLevel / 2 + noise) - 2;
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
                if (world.getBlockTypeIdAt(x, highest + 1, z) == 0) {
                    world.getBlockAt(x, highest + 1, z).setType(Material.SNOW);
                }
            }
        }
        this.doWater();
    }
}
