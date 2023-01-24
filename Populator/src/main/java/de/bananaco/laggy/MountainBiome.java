package de.bananaco.laggy;

import java.util.Random;

import me.simplex.tropic.populators.Populator_Lake_And_Creek;
import me.simplex.tropic.populators.Populator_Mushrooms;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.MushroomCow;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import de.bananaco.laggy.populator.FlowerPopulator;
import de.bananaco.laggy.populator.GravelStack;
import de.bananaco.laggy.populator.MelonPopulator;
import de.bananaco.laggy.populator.TreePopulator;
import de.bananaco.laggy.populator.TreePopulator.Type;
import de.bananaco.laggy.populator.WildGrassPopulator;

public class MountainBiome extends WorldFiller {

    public Block highest = null;
    byte bedrock = (byte) Material.BEDROCK.getId();
    byte stone = (byte) Material.STONE.getId();
    byte dirt = (byte) Material.DIRT.getId();
    byte grass = (byte) Material.GRASS.getId();
    byte water = (byte) Material.STATIONARY_WATER.getId();
    byte sand = (byte) Material.SAND.getId();

    public MountainBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.EXTREME_HILLS, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator treepop = new TreePopulator(Type.FOREST);
        BlockPopulator wildgrass = new WildGrassPopulator((byte) 2);
        BlockPopulator flowers = new FlowerPopulator();
        BlockPopulator gravel = new GravelStack();
        BlockPopulator lake = new Populator_Lake_And_Creek();
        BlockPopulator shroom = new Populator_Mushrooms();
        BlockPopulator melon = new MelonPopulator();
        mobs.mobs.add(MushroomCow.class);

        this.addPopulator(treepop);
        this.addPopulator(wildgrass);
        this.addPopulator(flowers);
        this.addPopulator(gravel);
        this.addPopulator(lake);
        this.addPopulator(shroom);
        this.addPopulator(melon);

    }

    public void doWater() {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y < groundLevel; y++) {
                    int id = world.getBlockTypeIdAt(x, y, z);// world.getBlockAt(x, y, z);
                    if (id == 0) {
                        world.getBlockAt(x, y, z).setTypeId(Material.STATIONARY_WATER.getId());
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
        // super.generate();

        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator gg = new PerlinOctaveGenerator(seed, 8);
        g.setScale(0.0022D);
        gg.setScale(0.01625D);
        // max distance between the two
        // double tDis = Math.abs(((centerX-minX)+(centerZ-minZ))/2);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2;
                if (abs < 20.0) {
                    abs = 15.0;
                } else {
                    abs = abs - 5.0;
                }

                // double diff = tDis-abs;
                double n1 = g.noise(x, z, 0.45D, 0.7D) * 7.3D;
                double n2 = gg.noise(x, z, 0.75D, 0.6D) * 9.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel) + 2;

                int highest = (int) (groundLevel / 2 + noise) - 3;
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }
                if (highest >= world.getMaxHeight()) {
                    highest = world.getMaxHeight() - 1;
                }
                // System.out.println(abs);
                // all other land
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setTypeId(stone);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setTypeId(dirt);
                }

                Block b = world.getBlockAt(x, highest, z);//
                b.setTypeId(grass);
                if (this.highest == null || b.getY() > this.highest.getY()) {
                    this.highest = b;

                }
                // cw.getHandle().setRawTypeId(x, highest, z, grass);
            }
        }
        // sign.getRelative(BlockFace.DOWN).setType(Material.TNT);
    }
}
