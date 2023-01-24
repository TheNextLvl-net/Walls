package de.bananaco.laggy.populator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.Vector2D;

import de.bananaco.laggy.Container;
import de.bananaco.laggy.populator.TreePopulator.Type;

public class OasisPopulator extends BlockPopulator {
	Container filler;
	public OasisPopulator(Container filler) {
		this.filler = filler;
	}
	@Override
	public void populate(World world, Random random, Chunk chunk) {
		if (random.nextInt(36) > 1) {
            return;
        }

        int rx = (chunk.getX() << 4) + random.nextInt(16);
        int rz = (chunk.getZ() << 4) + random.nextInt(16);
        if (world.getHighestBlockYAt(rx, rz) <= 4)
            return;

        int radius = 6 + random.nextInt(3);
        int oasisRadius = radius + 6;

        Material liquidMaterial = Material.WATER;
        Material solidMaterial = Material.WATER;

        int ry = world.getHighestBlockYAt(rx, rz) - 1;

        ArrayList<Block> lakeBlocks = new ArrayList<Block>();
        ArrayList<Block> oasisBlocks = new ArrayList<Block>();
        if(new Location(world, rx, ry, rz).getBlock().getType() != Material.SAND)
        	return;
        for (int i = -1; i < 4; i++) {
            Vector center = new BlockVector(rx, ry - i, rz);
            for (int x = -oasisRadius; x <= oasisRadius; x++) {
                for (int z = -oasisRadius; z <= oasisRadius; z++) {
                    Vector position = center.clone().add(new Vector(x, 0, z));
                    if (center.distance(position) <= radius + 0.5 - i) {
                        lakeBlocks.add(world.getBlockAt(position.toLocation(world)));
                    } else if(center.distance(position) <= oasisRadius + 0.5 - i) {
                    	oasisBlocks.add(world.getBlockAt(position.toLocation(world)));
                    }
                }
            }
        }

        for (Block block : lakeBlocks) {
            // Ensure it's not air or liquid already
            if (block.getTypeId() != 0 && (block.getTypeId() < 8 || block.getTypeId() > 11)) {
                if (block.getY() == ry + 1) {
                    if (random.nextBoolean()) {
                        block.setType(Material.AIR);
                    }
                } else if (block.getY() == ry) {
                    block.setType(Material.AIR);
                } else if (random.nextInt(10) > 1) {
                    block.setType(liquidMaterial);
                } else {
                    block.setType(solidMaterial);
                }
            }
        }
        Set<Chunk> chunks = new HashSet<Chunk>();
        final Set<Vector2D> validXZCoords = new HashSet<Vector2D>();
        for(Block block : oasisBlocks) {
        	chunks.add(block.getChunk());
        	validXZCoords.add(new Vector2D(block.getX(), block.getZ()));
        	block.setType(Material.GRASS);
        	block.getRelative(0, -1, 0).setType(Material.GRASS);
        }
        Container container = new Container() {
			@Override
			public boolean contains(int x, int z) {
				return validXZCoords.contains(new Vector2D(x, z));
			}
        };
        TreePopulator trees = new TreePopulator(Type.OASIS, container);
        for(Chunk c : chunks) {
        	trees.populate(world, random, c);
        }
	}
}
