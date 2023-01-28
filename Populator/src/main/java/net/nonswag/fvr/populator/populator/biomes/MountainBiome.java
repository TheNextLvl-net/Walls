package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.blocks.Populator_Lake_And_Creek;
import net.nonswag.fvr.populator.populator.blocks.Populator_Mushrooms;
import net.nonswag.fvr.populator.populator.structures.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.MushroomCow;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;

public class MountainBiome extends WorldFiller {

    public Block highest = null;

    public MountainBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.EXTREME_HILLS, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator treepop = new TreePopulator(TreePopulator.Type.FOREST);
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

    @Override
    public void generate() {

        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator gg = new PerlinOctaveGenerator(seed, 8);
        g.setScale(0.0022D);
        gg.setScale(0.01625D);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 20.0) {
                    abs = 15.0;
                } else {
                    abs = abs - 5.0;
                }

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
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.DIRT);
                }

                Block b = world.getBlockAt(x, highest, z);//
                b.setType(Material.GRASS);
                if (this.highest == null || b.getY() > this.highest.getY()) {
                    this.highest = b;

                }
            }
        }
    }
}
