package de.bananaco.laggy;

import java.util.Random;

import me.simplex.tropic.populators.Populator_Lake_And_Creek;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import de.bananaco.laggy.populator.FlowerPopulator;
import de.bananaco.laggy.populator.GravelStack;
import de.bananaco.laggy.populator.TreePopulator;
import de.bananaco.laggy.populator.TreePopulator.Type;
import de.bananaco.laggy.populator.WildGrassPopulator;

public class CanyonBiome extends WorldFiller {

    public Block highest = null;
    Material bedrock = Material.BEDROCK;
    Material stone = Material.STONE;
    Material dirt = Material.DIRT;
    Material grass = Material.GRASS;
    Material water = Material.STATIONARY_WATER;
    Material sand = Material.SAND;

    public CanyonBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.SMALL_MOUNTAINS, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator treepop = new TreePopulator(Type.SEASONAL_FOREST);
        BlockPopulator wildgrass = new WildGrassPopulator((byte) 1);
        BlockPopulator flowers = new FlowerPopulator();
        BlockPopulator gravel = new GravelStack();
        BlockPopulator lake = new Populator_Lake_And_Creek();

        this.addPopulator(treepop);
        this.addPopulator(wildgrass);
        this.addPopulator(flowers);
        this.addPopulator(gravel);
        this.addPopulator(lake);
    }

    @Override
    public void generate() {
        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator gg = new PerlinOctaveGenerator(seed, 8);
        g.setScale(1 / 48d);
        gg.setScale(1 / 24d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2;
                if (abs < 20)
                    abs -= 10;
                else if (abs < 30)
                    abs = 20;
                else
                    abs -= 10;
                if (abs < 5)
                    abs = 5;

                double n1 = g.noise(x, z, 0.45D, 0.7D) * 3.3D;
                double n2 = gg.noise(x, z, 0.75D, 0.6D) * 6.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel / 4);

                int highest = (int) (groundLevel / 2 + noise);
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }

                if (n1 > 2)
                    highest -= 5;
                if (n2 > 3)
                    highest -= 3;

                if (highest >= world.getMaxHeight()) {
                    highest = world.getMaxHeight() - 1;
                }
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(stone);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(dirt);
                }

                Block b = world.getBlockAt(x, highest, z);//
                b.setType   (grass);
                if (this.highest == null || b.getY() > this.highest.getY()) {
                    this.highest = b;

                }
            }
        }
    }
}
