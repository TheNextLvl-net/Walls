package de.bananaco.laggy;

import java.util.Random;

import me.simplex.tropic.populators.Populator_Tree_Medium;
import me.simplex.tropic.populators.Populator_Tree_Small;
import me.simplex.tropic.populators.Populator_Tree_World;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Wolf;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import de.bananaco.laggy.populator.WildGrassPopulator;

public class JungleBiome extends WorldFiller {

    byte bedrock = (byte) Material.BEDROCK.getId();
    byte stone = (byte) Material.STONE.getId();
    byte dirt = (byte) Material.DIRT.getId();
    byte grass = (byte) Material.GRASS.getId();
    byte water = (byte) Material.STATIONARY_WATER.getId();
    byte sand = (byte) Material.SAND.getId();

    public JungleBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.JUNGLE, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator tree1 = new Populator_Tree_Small();
        BlockPopulator tree2 = new Populator_Tree_Medium();
        Populator_Tree_World tree3 = new Populator_Tree_World();
        mobs.mobs.add(Wolf.class);
        tree3.filler = this;
        BlockPopulator grass1 = new WildGrassPopulator((byte) 1);
        BlockPopulator grass2 = new WildGrassPopulator((byte) 2);
        
        this.addPopulator(tree1);
        this.addPopulator(tree2);
        this.addPopulator(tree3);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass1);
        this.addPopulator(grass2);
        this.addPopulator(grass2);
    }

    public void doWater() {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y < groundLevel; y++) {
                    int id = world.getBlockTypeIdAt(x, y, z);//world.getBlockAt(x, y, z);
                    if (id == 0) {
                        world.getBlockAt(x, y, z).setTypeId(Material.STATIONARY_WATER.getId());
                        //block.setRawTypeId(Material.STATIONARY_WATER.getId());
                    } else {
                        //System.out.println(block.getTypeId());
                    }
                }
            }
        }
    }

    @Override
    public void generate() {
        //super.generate();

        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator gg = new PerlinOctaveGenerator(seed, 8);
        g.setScale(0.0022D);
        gg.setScale(0.013625D);
        // max distance between the two
        //double tDis = Math.abs(((centerX-minX)+(centerZ-minZ))/2);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2;
                //double diff = tDis-abs;
                double n1 = g.noise(x, z, 0.45D, 0.7D) * 3.3D;
                double n2 = gg.noise(x, z, 0.75D, 0.6D) * 2.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel) - 1.75;

                int highest = (int) (groundLevel / 2 + noise) - 3;
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }
                //System.out.println(abs);
                //all other land
                for (int y = startY; y < highest - 3; y++) {
                    world.getBlockAt(x, y, z).setTypeId(stone);
                }
                for (int y = highest - 3; y < highest; y++) {
                    world.getBlockAt(x, y, z).setTypeId(dirt);
                }
                world.getBlockAt(x, highest, z).setTypeId(grass);
                //cw.getHandle().setRawTypeId(x, highest, z, grass);
            }
        }

        this.checkSurface();
        this.checkSurface();
    }

    public void checkSurface() {

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                int y = groundLevel - 30;
                int id = world.getBlockTypeIdAt(x, y, z);//world.getBlockAt(x, y, z);
                if (id == 0) {
                    for (int sy = startY; sy < world.getMaxHeight(); sy++) {
                        boolean update = world.getBlockTypeIdAt(x - 1, sy, z) == world.getBlockTypeIdAt(x + 1, sy, z);
                        if (update) {
                            id = world.getBlockTypeIdAt(x - 1, sy, z);
                            world.getBlockAt(x, sy, z).setTypeId(id);
                        }
                    }
                }
            }
        }
    }
}
