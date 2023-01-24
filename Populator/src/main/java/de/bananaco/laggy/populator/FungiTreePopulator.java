package de.bananaco.laggy.populator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class FungiTreePopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int centerX = (chunk.getX() << 4) + random.nextInt(16);
        int centerZ = (chunk.getZ() << 4) + random.nextInt(16);

        int multiplier = 1;

        for (int i = 0; i < multiplier; i++) {
            centerX = (chunk.getX() << 4) + random.nextInt(16);
            centerZ = (chunk.getZ() << 4) + random.nextInt(16);
            int centerY = world.getHighestBlockYAt(centerX, centerZ) - 1;
            Block sourceBlock = world.getBlockAt(centerX, centerY, centerZ);

            if (sourceBlock.getType() == Material.GRASS) {
                if (random.nextFloat() > .8f)
                    createLargeTree(sourceBlock.getRelative(BlockFace.UP).getLocation(), random);
                else
                    createTree(sourceBlock.getRelative(BlockFace.UP).getLocation(), random);
            }
        }
    }

    private void createLargeTree(Location start, Random rnd) {
        int height = 12 + rnd.nextInt(6);
        // Store the blocks changed so we can later add vines to them
        Set<Block> logs = new HashSet<Block>();
        Set<Block> leaves = new HashSet<Block>();
        // Create the trunk, which is 2x2
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 2; k++) {
                    Block block = start.clone().add(i, j, k).getBlock();
                    block.setType(Material.LOG);
                    block.setData((byte) 3);
                    logs.add(block);
                }
            }
        }
        // Create the leaves, which are alternating rings of leaves that increase in size from 2 to 4 as you descend in height
        // The odd numbering is to account for the fact that I, quite frustratingly, made the trunk 2x2 in size
        // And also because I suck at making clear code
        float radius = 2;
        for (int j = height; j > height / 2 - 4; j -= 2) {
            for (int i = (int) -(radius - 1); i <= radius; i++) {

                for (int k = (int) -(radius - 1); k <= radius; k++) {
                    // If it's one of the corners
                    int checkCorner = 0;
                    if (i == -(radius - 1))
                        checkCorner++;
                    if (i == radius)
                        checkCorner++;
                    if (k == -(radius - 1))
                        checkCorner++;
                    if (k == radius)
                        checkCorner++;
                    if (checkCorner == 2)
                        continue;

                    Block block = start.clone().add(i, j, k).getBlock();
                    if (!block.getType().equals(Material.LOG)) {
                        block.setType(Material.LEAVES);
                        block.setData((byte) 3);
                        leaves.add(block);
                    }
                }
            }
            if (radius < 4)
                radius += .5f;
        }
        createVine(logs, rnd, false);
        createVine(leaves, rnd, true);
    }

    private void createTree(Location start, Random rnd) {
        int height = 8 + rnd.nextInt(4);
        // Store the blocks changed so we can later add vines to them
        Set<Block> logs = new HashSet<Block>();
        Set<Block> leaves = new HashSet<Block>();
        // Create the trunk, which is 2x2
        for (int j = 0; j < height; j++) {
            Block block = start.clone().add(0, j, 0).getBlock();
            block.setType(Material.LOG);
            block.setData((byte) 3);
            logs.add(block);
        }
        // Create the leaves, which are alternating rings of leaves that increase in size from 2 to 4 as you descend in height
        // The odd numbering is to account for the fact that I, quite frustratingly, made the trunk 2x2 in size
        // And also because I suck at making clear code
        float radius = 1;
        for (int j = height; j > height / 2 - 2; j -= 2) {
            for (int i = (int) -radius; i <= radius; i++) {

                for (int k = (int) -radius; k <= radius; k++) {
                    // If it's one of the corners
                    int checkCorner = 0;
                    if (i == -radius)
                        checkCorner++;
                    if (i == radius)
                        checkCorner++;
                    if (k == -radius)
                        checkCorner++;
                    if (k == radius)
                        checkCorner++;
                    if (checkCorner == 2)
                        continue;

                    Block block = start.clone().add(i, j, k).getBlock();
                    if (!block.getType().equals(Material.LOG)) {
                        block.setType(Material.LEAVES);
                        block.setData((byte) 3);
                        leaves.add(block);
                    }
                }
            }
            if (radius < 3)
                radius += .5f;
        }
        createVine(logs, rnd, false);
        createVine(leaves, rnd, true);
    }

    private void createVine(Set<Block> blocks, Random rnd, boolean leaves) {
        HashMap<Block, BlockFace> toHandle = getOutsideBlocks(blocks);
        for (Block key : toHandle.keySet()) {
            if (rnd.nextInt(100) < (leaves ? 45 : 20)) {
                Block handle = key.getRelative(toHandle.get(key));
                for (int y = 0; y > -1 * (rnd.nextInt(12) + 3); y--) {
                    if (handle.getType() == Material.AIR) {
                        handle.setTypeIdAndData(Material.VINE.getId(), BlockFaceToVineData(toHandle.get(key)), false);
                        handle = handle.getRelative(0, -1, 0);
                    }
                }
            }
        }
    }

    private HashMap<Block, BlockFace> getOutsideBlocks(Set<Block> leaves) {
        HashMap<Block, BlockFace> outside_blocks = new HashMap<Block, BlockFace>();
        for (Block block : leaves) {
            BlockFace side = getAirFacingSide(block);
            if (side != null) {
                outside_blocks.put(block, side);
            }
        }
        return outside_blocks;
    }

    private byte BlockFaceToVineData(BlockFace face) {
        switch (face) {
        case SOUTH:
            return 2; //
        case WEST:
            return 4; //
        case NORTH:
            return 8; //
        case EAST:
            return 1; //
        default:
            return 0;
        }
    }

    private BlockFace getAirFacingSide(Block block) {
        if (block.getRelative(BlockFace.NORTH).getType() == Material.AIR) {
            return BlockFace.NORTH;
        }
        if (block.getRelative(BlockFace.EAST).getType() == Material.AIR) {
            return BlockFace.EAST;
        }
        if (block.getRelative(BlockFace.SOUTH).getType() == Material.AIR) {
            return BlockFace.SOUTH;
        }
        if (block.getRelative(BlockFace.WEST).getType() == Material.AIR) {
            return BlockFace.WEST;
        }
        return null;
    }
}