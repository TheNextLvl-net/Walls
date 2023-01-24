package de.bananaco.laggy;

import java.util.Random;

import me.simplex.tropic.populators.Populator_Bush;
import me.simplex.tropic.populators.Populator_Water_Lily;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.MushroomCow;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import de.bananaco.laggy.populator.FungiTreePopulator;
import de.bananaco.laggy.populator.HugeMushroomPopulator;
import de.bananaco.laggy.populator.MushroomPopulator;
import de.bananaco.laggy.populator.ShallowLakePopulator;
import de.bananaco.laggy.populator.WildGrassPopulator;

public class FungiForestBiome extends WorldFiller {
    byte bedrock = (byte) Material.BEDROCK.getId();
    byte stone = (byte) Material.STONE.getId();
    byte dirt = (byte) Material.DIRT.getId();
    byte grass = (byte) Material.GRASS.getId();
    byte water = (byte) Material.STATIONARY_WATER.getId();
    byte mycel = (byte) Material.MYCEL.getId();

    public FungiForestBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.SWAMPLAND, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator tree1 = new FungiTreePopulator();
        BlockPopulator mushroom = new MushroomPopulator();
        BlockPopulator bush = new Populator_Bush(5);
        BlockPopulator hugeMushroom = new HugeMushroomPopulator();
        BlockPopulator shallowLake = new ShallowLakePopulator(this);
        BlockPopulator lily = new Populator_Water_Lily();
        mobs.mobs.add(MushroomCow.class);

        BlockPopulator grass1 = new WildGrassPopulator((byte) 1);
        BlockPopulator grass2 = new WildGrassPopulator((byte) 2);

        this.addPopulator(shallowLake, true);
        this.addPopulator(lily);
        this.addPopulator(mushroom);
        this.addPopulator(hugeMushroom);
        this.addPopulator(bush);
        this.addPopulator(bush);
        this.addPopulator(tree1);
        this.addPopulator(tree1);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass2);
    }

    @Override
    public void generate() {
        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator g2 = new PerlinOctaveGenerator(seed, 8);
        g.setScale(1 / 24d);
        g2.setScale(1 / 24d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2;
                if (abs < 20)
                    abs = 0;
                else
                    abs -= 20;

                // double diff = tDis-abs;
                double noise = g.noise(x, z, 0.35D, 0.65D) * 2D;

                int highest = (int) (groundLevel + noise);
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }

                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setTypeId(stone);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setTypeId(dirt);
                }

                Block b = world.getBlockAt(x, highest, z);
                if (g2.noise(x, z, 0.5D, 0.5D) > .5)
                    b.setTypeId(mycel);
                else
                    b.setTypeId(grass);
            }
        }
    }
}
