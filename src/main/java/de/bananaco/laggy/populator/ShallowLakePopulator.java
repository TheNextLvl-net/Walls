package de.bananaco.laggy.populator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import de.bananaco.laggy.Container;

public class ShallowLakePopulator extends BlockPopulator {
	Container filler;
	public ShallowLakePopulator(Container filler)
	{
		this.filler = filler;
	}
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(5) > 1) {
            return;
        }

        int rx = (chunk.getX() << 4) + random.nextInt(16);
        int rz = (chunk.getZ() << 4) + random.nextInt(16);
        if (world.getHighestBlockYAt(rx, rz) <= 4)
            return;

        int radius = 8 + random.nextInt(4);
        int ry = world.getHighestBlockYAt(rx, rz) - 1;

        Material liquidMaterial = Material.WATER;
        Material solidMaterial = Material.STONE;
        
        int[] radiusMods = new int[4];
        for(int i = 0; i < 4; i++) radiusMods[i] = random.nextInt(3) - 1;

        List<Location> lakeBlocks = new ArrayList<Location>();
        Vector center = new BlockVector(rx, ry, rz);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
            	int mod = (x < 0 && z < 0) ? radiusMods[0] : (x < 0 && z > 0) ? radiusMods[1] : (x > 0 && z < 0) ? radiusMods[2] : radiusMods[3];
                Vector position = center.clone().add(new Vector(x, 0, z));
                if (center.distance(position) <= radius + 0.5 + mod) {
                	Location loc = world.getHighestBlockAt(position.toLocation(world)).getLocation().add(0, -1, 0);
                    lakeBlocks.add(loc);
                }
            }
        }
        
        int lowest = 128;
        for(Location loc : lakeBlocks)
        {
        	if(loc.getY() < lowest && loc.getY() >= 4) lowest = (int) loc.getY();
        }
        
        for(Location loc : lakeBlocks) loc.setY(lowest);

        for (Location loc : lakeBlocks) {
        	Block block = loc.getBlock();
        	if(filler.contains(block.getX(), block.getZ()))
        	{
        		block.setType(liquidMaterial);
        		block.getRelative(0, -1, 0).setType(solidMaterial);
        		block.getRelative(0, 1, 0).setTypeId(0);
        	}
        }
    }

}
