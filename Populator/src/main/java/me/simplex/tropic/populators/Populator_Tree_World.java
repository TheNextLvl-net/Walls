package me.simplex.tropic.populators;

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
import org.bukkit.util.Vector;

import de.bananaco.laggy.WorldFiller;

public class Populator_Tree_World extends BlockPopulator {
    private Set<Block> trunkblocks = new HashSet<Block>();
    private Set<Block> rootblocks = new HashSet<Block>();
    private Set<Block> branchblocks = new HashSet<Block>();

    private int chance = 15;

    private static boolean populating = false;

    public WorldFiller filler;

    @Override
    public void populate(World world, Random rand, Chunk chunk) {

        if (rand.nextInt(100) > chance || populating) {
            return;
        }
        populating = true;

        trunkblocks.clear();
        rootblocks.clear();
        branchblocks.clear();

        int num = rand.nextInt(5) + 5;
        Block start = this.getHighestBlock(chunk, rand.nextInt(8), rand.nextInt(8));

        if (start.getY() < 42 || start.getY() > 126) {
            populating = false;
            return;
        }

        //System.out.println("WorldTree! "+start.getX()+","+start.getZ());
        //chance to have "air" trees
        if (rand.nextInt(100) < 10) {
            start = start.getRelative(0, 7, 0);
        }

        createRoots(num, start, rand, rootblocks);
        Block branch_start = createTrunk(num, start, rand, trunkblocks);

        Set<Block> force_leaves;
        force_leaves = createBranches(num, branch_start, rand, branchblocks);
        force_leaves.addAll(createBranches(num + 2, branch_start.getRelative(0, -10, 0), rand, branchblocks));

        Set<Block> leavesblocks;
        leavesblocks = createLeaves(branchblocks, rand, true);
        leavesblocks.addAll(createLeaves(force_leaves, rand, false));

        buildBlocks(rootblocks, Material.LOG, (byte) 3, false);
        buildBlocks(trunkblocks, Material.LOG, (byte) 3, false);
        buildBlocks(branchblocks, Material.LOG, (byte) 3, false);
        buildBlocks(leavesblocks, Material.LEAVES, (byte) 3, true);

        createVine(leavesblocks, rand, true);
        createVine(trunkblocks, rand, false);
        createVine(rootblocks, rand, false);

        populating = false;

        System.gc();
    }

    /**
     * @param num
     * @param start
     * @param rand
     * @param blocks
     */
    public void createRoots(int num, Block start, Random rand, Set<Block> blocks) {
        int radius = 3;
        for (int j = 0; j <= num + 2; j++) {

            int length = 15 + rand.nextInt(35);

            Vector direction = new Vector(getRandom(rand), rand.nextDouble() * 0.33, getRandom(rand));

            Location loc = start.getLocation();
            generateSphere(loc, blocks, radius, false, true);
            // What does this even DO?
            for (int i = 0; i < length; i++) {
                loc.add(direction);
                if (loc.getY() < 50)
                    break;
                direction.subtract(new Vector(0.0, 0.05, 0.0));
                generateSphere(loc, blocks, radius, false, true);
                if (rand.nextInt(100) < 10 && radius >= 2) {
                    radius--;
                }
            }
        }
    }

    /**
     * @param num
     * @param start
     * @param rand
     * @param blocks
     * @return
     */
    public Set<Block> createBranches(int num, Block start, Random rand, Set<Block> blocks) {
        HashSet<Block> endblocks = new HashSet<Block>();
        int radius = 3;
        for (int j = 0; j <= num + 3; j++) {

            int length = 15 + rand.nextInt(15);

            Vector direction = new Vector(getRandom(rand), rand.nextDouble() * 0.025, getRandom(rand));

            Location loc = start.getLocation();
            generateSphere(loc, blocks, radius, true, true);
            for (int i = 0; i <= length; i++) {
                loc.add(direction);
                direction.add(new Vector(0.0, (rand.nextDouble() - 0.5) * 0.5, 0.0));
                generateSphere(loc, blocks, radius, true, true);

                if (rand.nextInt(100) < 15 && radius > 2) {
                    radius--;
                }
            }
            endblocks.add(loc.getBlock());
        }
        return endblocks;
    }

    /**
     * @param num
     * @param block
     * @param rand
     * @param blocks
     * @return
     */
    public Block createTrunk(int num, Block block, Random rand, Set<Block> blocks) {
        int start_y = block.getY();
        int radius = rand.nextBoolean() ? 4 : 5;
        for (int y = 0; y < num * 3 + radius * 4; y = y + 3) {
            block = block.getLocation().add(rand.nextInt(2) - 1, radius / 2 + 2, rand.nextInt(2) - 1).getBlock();
            generateSphere(block.getLocation(), blocks, radius, false, rand.nextBoolean());
            if (rand.nextInt(100) < 3 && radius >= 3) {
                radius--;
            }
            if (block.getY() - start_y >= 70 - rand.nextInt(35)) {
                return block;
            }
        }
        return block;
    }

    /**
     * @param rand
     * @return
     */
    public double getRandom(Random rand) {
        double r = rand.nextDouble() * 2 - 1;
        while (Math.abs(r) < 0.3) {
            r = rand.nextDouble() * 2 - 1;
        }
        return r;
    }

    /**
     * @param blocks
     * @param rand
     * @param random
     * @return
     */
    private Set<Block> createLeaves(Set<Block> blocks, Random rand, boolean random) {
        Set<Block> leaves = new HashSet<Block>();
        for (Block block : blocks) {
            if (!random || rand.nextInt(200) < 3) {
                int radius = rand.nextBoolean() ? 4 : 5;
                int radius_squared = radius * radius;
                Location center = block.getLocation();
                Vector c = new Vector(0, 0, 0);
                for (int x = -radius; x <= radius; x++)
                    for (int z = -radius; z <= radius; z++)
                        for (int y = 0; y <= radius - 1; y++) {
                            // Calculate 3 dimensional distance
                            Vector v = new Vector(x, y, z);
                            // If it's within this radius gen the sphere
                            if (c.distanceSquared(v) <= radius_squared) {
                                Block b = center.getBlock().getRelative(x, y, z);
                                leaves.add(b);
                            }
                        }
            }
        }
        return leaves;
    }

    /**
     * @param center
     * @param blocks
     * @param radius
     * @param ignore_height
     * @param allow_same
     */
    private void generateSphere(Location center, Set<Block> blocks, int radius, boolean ignore_height, boolean allow_same) {
        if (!ignore_height) {
            if (center.getBlock().getY() < 50) {
                return;
            }
        }
        int radius_squared = radius * radius;
        Vector c = new Vector(0, 0, 0);
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
                for (int y = -radius; y <= radius; y++) {
                    // Calculate 3 dimensional distance
                    Vector v = new Vector(x, y, z);
                    // If it's within this radius gen the sphere
                    if (c.distanceSquared(v) < radius_squared) {
                        Block b = center.getBlock().getRelative(x, y, z);
                        // Check if the block is already MOSSY_COBBLESTONE
                        if (checkMaterialIsModdable(b)) {
                            blocks.add(b);
                        } else {
                            return;
                        }
                    } else if (allow_same && (c.distanceSquared(v) == radius_squared)) {
                        Block b = center.getBlock().getRelative(x, y, z);
                        if (checkMaterialIsModdable(b)) {
                            blocks.add(b);
                        } else {
                            return;
                        }
                    }
                }
    }

    /**
     * Iteratively determines the highest grass block
     * 
     * @param chunk
     * @param x
     * @param z
     * @return Block highest non-air
     */
    private Block getHighestBlock(Chunk chunk, int x, int z) {
        Block block = null;
        // Return the highest block
        for (int i = 127; i >= 0; i--)
            if ((block = chunk.getBlock(x, i, z)).getTypeId() == 2)
                return block;
        // And as a matter of completeness, return the lowest point
        return block;
    }

    /**
     * @param blocks
     * @param rnd
     * @param leaves
     */
    private void createVine(Set<Block> blocks, Random rnd, boolean leaves) {
        HashMap<Block, BlockFace> toHandle = getOutsideBlocks(blocks);
        for (Block key : toHandle.keySet()) {
            if (rnd.nextInt(100) < (leaves ? 25 : 10)) {
                Block handle = key.getRelative(toHandle.get(key));
                for (int y = 0; y > -1 * (rnd.nextInt(45) + 10); y--) {
                    if (handle.getType() == Material.AIR && filler.contains(handle.getX(), handle.getZ())) {
                        handle.setTypeIdAndData(Material.VINE.getId(), BlockFaceToVineData(toHandle.get(key)), false);
                        handle = handle.getRelative(0, -1, 0);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param leaves
     * @return
     */
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

    /**
     * @param block
     * @return
     */
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

    /**
     * @param face
     * @return
     */
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

    /**
     * @param block
     * @return
     */
    private boolean checkMaterialIsModdable(Block block) {
        return block.getType() == Material.AIR || block.getType() == Material.DIRT || block.getType() == Material.GRASS || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.SAND;
    }

    /**
     * @param blocks
     * @param material
     * @param data
     * @param checkIsAir
     */
    private void buildBlocks(Set<Block> blocks, Material material, byte data, boolean checkIsAir) {
        if (checkIsAir) {
            for (Block b : blocks) {
                if (b.getType() == Material.AIR && filler.contains(b.getX(), b.getZ())) {
                    b.setType(material);
                    b.setData(data);
                }
            }
        } else {
            for (Block b : blocks) {
                if (filler.contains(b.getX(), b.getZ())) {
                    b.setType(material);
                    b.setData(data);
                }
            }
        }
    }
}
