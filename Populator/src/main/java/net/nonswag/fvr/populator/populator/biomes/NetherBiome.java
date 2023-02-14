package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.structures.LapisRingPopulator;
import net.nonswag.fvr.populator.populator.structures.NetherHutPopulator;
import net.nonswag.fvr.populator.populator.structures.NetherPopulator;
import net.nonswag.fvr.populator.populator.structures.NetherTreePopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;

public class NetherBiome extends WorldFiller {

    Block highest = null;
    
    public NetherBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.HELL, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator treepop = new NetherTreePopulator(NetherTreePopulator.Type.SAVANNA);
        BlockPopulator netherBrick = new NetherPopulator();
        BlockPopulator ring = new LapisRingPopulator();
        BlockPopulator hut = new NetherHutPopulator();

        
        this.addPopulator(hut, true);
        this.addPopulator(netherBrick);
        this.addPopulator(treepop);
        this.addPopulator(ring);
        

    }

    public void doLava() {
        Random random = Populator.RANDOM;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = groundLevel - 15; y <= groundLevel; y++) {
                    if (world.getBlockAt(x, y, z).getType() == Material.AIR && random.nextInt(10) > 7) {
                        world.getBlockAt(x, y, z).setType(Material.STATIONARY_LAVA);
                    }else{
                        world.getBlockAt(x, y, z).setType(Material.NETHERRACK);
                    }
                }
            }
        }
    }

    public void createHole(Block start) {
        Block block = start;
        int d = 10;
        createSphere(block, d);
        Random random = Populator.RANDOM;
        for (int i = d; i > 0; i--) {
            block = block.getRelative(d - random.nextInt(d * 2), -random.nextInt(d), d - random.nextInt(d * 2));
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
                        if (center.getRelative(x, y, z).getLocation().getBlockY() < 50){

                            center.getRelative(x, y, z).setType(Material.STATIONARY_WATER);
                            
                        }else{
                            center.getRelative(x, y, z).setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generate() {
        //super.generate();

        SimplexOctaveGenerator g = new SimplexOctaveGenerator(Populator.RANDOM, 9);
        PerlinOctaveGenerator gg = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        //PerlinOctaveGenerator ggg = new PerlinOctaveGenerator(seed, 8);
        g.setScale(0.0122D);
        gg.setScale(0.053625D);
        //ggg.setScale(0.005);
        // max distance between the two
        //double tDis = Math.abs(((centerX-minX)+(centerZ-minZ))/2);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = 15.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
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
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest; y++) {
                    world.getBlockAt(x, y, z).setType(Material.DIRT);
                }
                world.getBlockAt(x, highest, z).setType(Material.NETHER_BRICK);
                if (this.highest == null || highest > this.highest.getY()) {
                    this.highest = world.getBlockAt(x, highest, z);
                }
            }
        }

        this.doLava();
        this.createHole(this.highest);
    }
}
