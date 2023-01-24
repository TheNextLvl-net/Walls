package de.bananaco.laggy;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import de.bananaco.laggy.populator.DesertPopulator;
import de.bananaco.laggy.populator.FlowerPopulator;
import de.bananaco.laggy.populator.GravelStack;
import de.bananaco.laggy.populator.OasisPopulator;
import de.bananaco.laggy.populator.TreePopulator;
import de.bananaco.laggy.populator.TreePopulator.Type;
import de.bananaco.laggy.populator.WildGrassPopulator;

public class DesertBiome extends WorldFiller {

    byte bedrock = (byte) Material.BEDROCK.getId();
    byte stone = (byte) Material.STONE.getId();
    byte dirt = (byte) Material.DIRT.getId();
    byte grass = (byte) Material.GRASS.getId();
    byte water = (byte) Material.STATIONARY_WATER.getId();
    byte sand = (byte) Material.SAND.getId();

    public DesertBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.DESERT, minX, minZ, maxX, maxZ, startY, groundLevel);

        OasisPopulator oasis = new OasisPopulator(this);
        BlockPopulator treepop = new TreePopulator(Type.SAVANNA);
        BlockPopulator wildgrass = new WildGrassPopulator((byte) 0);
        BlockPopulator flowers = new FlowerPopulator();
        BlockPopulator gravel = new GravelStack();
        BlockPopulator desert = new DesertPopulator();

        this.addPopulator(treepop);
        this.addPopulator(wildgrass);
        this.addPopulator(flowers);
        this.addPopulator(gravel);
        this.addPopulator(desert);
        this.addPopulator(oasis);
    }

    public void doWater() {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y < groundLevel; y++) {
                    int id = world.getBlockTypeIdAt(x, y, z);// world.getBlockAt(x, y, z);
                    if (id == 0) {
                        world.getBlockAt(x, y, z).setTypeId(Material.STATIONARY_LAVA.getId());
                        // world.getBlockAt(x, y, z).setTypeId(Material.STATIONARY_WATER.getId());
                        // block.setRawTypeId(Material.STATIONARY_WATER.getId());
                    } else {
                        // System.out.println(block.getTypeId());
                    }
                }
            }
        }
    }

    @Override
    public void generate() {
        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        g.setScale(1 / 48d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2;
                if (abs < 20)
                    abs = 0;
                else
                    abs -= 20;

                // double diff = tDis-abs;
                double noise = g.noise(x, z, 0.35D, 0.65D) * 4.5D;

                int highest = (int) (groundLevel + noise);
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }

                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setTypeId(stone);
                }
                for (int y = highest - 3; y <= highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setTypeId(sand);
                }

                Block b = world.getBlockAt(x, highest - 4, z);
                b.setTypeId(Material.SANDSTONE.getId());
            }
        }
    }
}