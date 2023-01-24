package de.bananaco.laggy.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;

import de.bananaco.laggy.Utils;

public class StoneTentacles extends BlockPopulator {

    int radius = 3;
    int chance = 9;

    private static boolean populating = false;

    @Override
    public void populate(World world, Random rand, Chunk chunk) {

        if (rand.nextInt(100) > chance)
            return;

        int num = rand.nextInt(4);
        Block start = Utils.getHighestBlock(chunk, rand.nextInt(8), rand.nextInt(8));

        if (populating)
            return;

        populating = true;
        System.out.println("Tentacles! " + start.getX() + "," + start.getZ());
        createTentacle(num, start, rand);

        populating = false;
    }

    public void createTentacle(int num, Block start, Random rand) {
        for (int j = 0; j < num; j++) {
            // Not below the water, thanks
            if (start.getY() < 67 || start.getY() > 115)
                return;

            int length = 15 + rand.nextInt(35);

            Vector direction = new Vector(getRandom(rand), 1.0 + rand.nextDouble() * 2.0, getRandom(rand));

            Location loc = start.getLocation();
            generateSphere(loc, Material.MOSSY_COBBLESTONE);
            // What does this even DO?
            for (int i = 0; i < length; i++) {
                loc.add(direction);
                if (loc.getY() > 123 || loc.getY() < 30)
                    break;
                direction.subtract(new Vector(0.0, 0.1, 0.0));
                generateSphere(loc, Material.MOSSY_COBBLESTONE);
            }
            // And finally do a lava one
            loc.add(direction);
            generateSphere(loc, Material.LAVA);
        }
    }

    public double getRandom(Random rand) {
        double r = rand.nextDouble() * 2 - 1;
        while (Math.abs(r) < 0.3) {
            r = rand.nextDouble() * 2 - 1;
        }
        return r;
    }

    public void generateSphere(Location center, Material material) {
    	Vector c = new Vector(0, 0, 0);

    	for (int x = -radius; x <= radius; x++)
    		for (int z = -radius; z <= radius; z++)
    			for (int y = -radius; y <= radius; y++) {
    				// Calculate 3 dimensional distance
    				Vector v = new Vector(x, y, z);
    				// If it's within this radius gen the sphere
    				if (c.distance(v) < radius && center.getBlock().getY() + y < 120 && center.getBlock().getY() + y > 20) {
    					Block b = center.getBlock().getRelative(x, y, z);
    					// Check if the block is already MOSSY_COBBLESTONE
    					if (b.getType() != Material.MOSSY_COBBLESTONE)
    						b.setType(material);
    				}
    			}
    }
}
