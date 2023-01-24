package de.bananaco.laggy;

import java.util.Random;

import me.simplex.tropic.populators.Populator_Water_Lily;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import de.bananaco.laggy.populator.FlowerPopulator;
import de.bananaco.laggy.populator.LapisRing;
import de.bananaco.laggy.populator.MelonPopulator;
import de.bananaco.laggy.populator.PumpkinPopulator;
import de.bananaco.laggy.populator.Sandifier;
import de.bananaco.laggy.populator.SugarcanePopulator;
import de.bananaco.laggy.populator.TreePopulator;
import de.bananaco.laggy.populator.WildGrassPopulator;
import de.bananaco.laggy.populator.TreePopulator.Type;

public class IslandBiome extends WorldFiller {

    byte bedrock = (byte) Material.BEDROCK.getId();
    byte stone = (byte) Material.STONE.getId();
    byte dirt = (byte) Material.DIRT.getId();
    byte grass = (byte) Material.GRASS.getId();
    byte water = (byte) Material.STATIONARY_WATER.getId();
    byte sand = (byte) Material.SAND.getId();
    Block highest = null;

    public IslandBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.BEACH, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator treepop = new TreePopulator(Type.FOREST);
        BlockPopulator sandy = new Sandifier(this);
        BlockPopulator wildgrass = new WildGrassPopulator((byte) 1);
        BlockPopulator flowers = new FlowerPopulator();
        BlockPopulator melon = new MelonPopulator();
        BlockPopulator pumpkin = new PumpkinPopulator();
        BlockPopulator sugarcane = new SugarcanePopulator();
        BlockPopulator lily = new Populator_Water_Lily();
        BlockPopulator ring = new LapisRing();
        
        this.addPopulator(sandy);
        this.addPopulator(wildgrass);
        this.addPopulator(flowers);
        this.addPopulator(melon);
        this.addPopulator(treepop);
        this.addPopulator(pumpkin);
        this.addPopulator(sugarcane);
        this.addPopulator(lily);
        this.addPopulator(ring);

    }

    public void doWater() {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y <= groundLevel; y++) {
                    int id = world.getBlockTypeIdAt(x, y, z);//world.getBlockAt(x, y, z);
                    if (id == 0) {
                        world.getBlockAt(x, y, z).setType(Material.STATIONARY_WATER);
                        //block.setRawTypeId(Material.STATIONARY_WATER.getId());
                    } else {
                        //System.out.println(block.getTypeId());
                    }
                }
            }
        }
    }

    public void createHole(Block start) {
        Block block = start;
        int d = 10;
        createSphere(block, d);
        for (int i = d; i > 0; i--) {
            block = block.getRelative(d - rand.nextInt(d * 2), -rand.nextInt(d), d - rand.nextInt(d * 2));
            createSphere(block, d);
        }
    }

    public void createSphere(Block center, int diameter) {
        Vector v = new Vector(0, 0, 0);
        for (int x = -diameter; x < diameter; x++) {
            for (int z = -diameter; z < diameter; z++) {
                for (int y = -diameter; y < diameter; y++) {
                    Vector v2 = new Vector(x, y, z);
                    double d = v2.distance(v);
                    if (d < diameter) {
                        center.getRelative(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }

    @Override
    public void generate() {
        //super.generate();

        Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 9);
        PerlinOctaveGenerator gg = new PerlinOctaveGenerator(seed, 8);
        //PerlinOctaveGenerator ggg = new PerlinOctaveGenerator(seed, 8);
        g.setScale(0.0122D);
        gg.setScale(0.053625D);
        //ggg.setScale(0.005);
        // max distance between the two
        //double tDis = Math.abs(((centerX-minX)+(centerZ-minZ))/2);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = 15.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2;
                if (abs < 30.0) {
                    abs = 15.0;
                } else {
                    abs = abs - 15.0;
                }
                //double diff = tDis-abs;
                double n1 = g.noise(x, z, 0.45D, 0.7D) * 2.3D;
                double n2 = gg.noise(x, z, 0.75D, 0.6D) * 3.5D;
                //double n3 = ggg.noise(x, z, 0.85D, 0.6D)*1.5D;
                double noise = (Math.abs(n1 - n2 / 2) * groundLevel);

                int highest = (int) (groundLevel / 2 + noise) - 5;
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
                if (this.highest == null || highest > this.highest.getY()) {
                    this.highest = world.getBlockAt(x, highest, z);
                }
            }
        }

        this.doWater();
        this.createHole(this.highest);
    }
}
