package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.Populator_Lava_Lakes;
import net.nonswag.fvr.populator.populator.structures.IceTreePopulator;
import net.nonswag.fvr.populator.populator.structures.PumpkinPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Snowman;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;

public class IceBiome extends WorldFiller {

    public IceBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.ICE_PLAINS, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator treepop = new IceTreePopulator();
        BlockPopulator pumpkin = new PumpkinPopulator();
        BlockPopulator lava = new Populator_Lava_Lakes();
        mobs.mobs.add(Snowman.class);

        this.addPopulator(treepop);
        this.addPopulator(treepop);
        this.addPopulator(pumpkin);
        this.addPopulator(lava);
        this.addPopulator(lava);
    }

    @SuppressWarnings("deprecation")
    public void doWater() {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y < groundLevel; y++) {
                    int id = world.getBlockTypeIdAt(x, y, z);//world.getBlockAt(x, y, z);
                    if (id == 0 || id == Material.SNOW.getId() || id == Material.GRASS.getId()) {
                        world.getBlockAt(x, y, z).setType(Material.STATIONARY_WATER);
                    }
                }
                int id = world.getBlockTypeIdAt(x, groundLevel, z);
                if (id == 0 || id == Material.SNOW.getId() || id == Material.GRASS.getId()) {
                    world.getBlockAt(x, groundLevel, z).setType(Material.ICE);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void generate() {

        Random seed = this.random;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator gg = new PerlinOctaveGenerator(seed, 8);
        g.setScale(0.0062D);
        gg.setScale(0.11625D);

        // max distance between the two
        //double tDis = Math.abs(((centerX-minX)+(centerZ-minZ))/2);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = 25.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 35.0) {
                    abs = 25.0;
                } else {
                    abs = abs - 10.0;
                }
                //double diff = tDis-abs;
                double n1 = g.noise(x, z, 0.45D, 0.7D) * 8.3D;
                double n2 = gg.noise(x, z, 0.75D, 0.6D) * 4.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel);

                int highest = (int) (groundLevel / 2 + noise) - 2;
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }
                //System.out.println(abs);
                //all other land
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
