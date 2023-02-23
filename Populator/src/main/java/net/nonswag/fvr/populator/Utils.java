package net.nonswag.fvr.populator;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Utils {

    public static byte blockFaceToVineData(BlockFace face) {
        switch (face) {
            case SOUTH:
                return 2;
            case WEST:
                return 4;
            case NORTH:
                return 8;
            case EAST:
                return 1;
            default:
                return 0;
        }
    }

    public static BlockFace getAirFacingSide(Block block) {
        if (block.getRelative(BlockFace.NORTH).getType() == Material.AIR) return BlockFace.NORTH;
        if (block.getRelative(BlockFace.EAST).getType() == Material.AIR) return BlockFace.EAST;
        if (block.getRelative(BlockFace.SOUTH).getType() == Material.AIR) return BlockFace.SOUTH;
        if (block.getRelative(BlockFace.WEST).getType() == Material.AIR) return BlockFace.WEST;
        return null;
    }

    public static void createLeavesOnChance(Set<Block> blocks, Random rnd, Set<Block> leaves) {
        for (Block block : blocks) {
            if (rnd.nextInt(100) < 10) {
                createLeaves(block, rnd, leaves);
            }
        }
    }

    public static void generateSphere(Location center, Set<Block> blocks, int radius, boolean ignore_height, boolean allow_same) {
        if (!ignore_height && center.getBlock().getY() < 50) return;
        int radius_squared = radius * radius;
        Vector c = new Vector(0, 0, 0);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius; y <= radius; y++) {
                    Vector v = new Vector(x, y, z);
                    if (c.distanceSquared(v) < radius_squared) {
                        Block b = center.getBlock().getRelative(x, y, z);
                        if (checkMaterialIsModdable(b)) blocks.add(b);
                        else return;
                    } else if (allow_same && (c.distanceSquared(v) == radius_squared)) {
                        Block b = center.getBlock().getRelative(x, y, z);
                        if (checkMaterialIsModdable(b)) blocks.add(b);
                        else return;
                    }
                }
            }
        }
    }

    public static boolean checkMaterialIsModdable(Block block) {
        return block.isEmpty() || block.getType() == Material.DIRT || block.getType() == Material.GRASS || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.SAND;
    }

    @SuppressWarnings("deprecation")
    public static void createLeaves(Block block, Random random, Set<Block> leaves) {
        int radius = random.nextInt(3) + 2;
        int radius_squared = radius * radius;
        Location center = block.getLocation();
        Vector c = new Vector(0, 0, 0);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y < radius - 1; y++) {
                    Vector v = new Vector(x, y, z);
                    if (c.distanceSquared(v) <= radius_squared) {
                        Block b = center.getBlock().getRelative(x, y, z);
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.LEAVES);
                            b.setData((byte) 3);
                            leaves.add(b);
                        }
                    }
                }
            }
        }
    }

    public static void createLeaves(WorldFiller filler, Location center, Random random) {
        int radius = random.nextInt(3) + 2;
        int radius_squared = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= radius - (radius == 4 ? 2 : 1); y++) {
                    Block block = center.getBlock().getRelative(x, y, z);
                    if (!filler.contains(block)) continue;
                    if (center.distanceSquared(block.getLocation()) > radius_squared) continue;
                    if (block.isEmpty() || block.getType().equals(Material.VINE)) block.setType(Material.LEAVES);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void createVine(WorldFiller filler, Set<Block> blocks, Random random, boolean leaves) {
        getOutsideBlocks(blocks).forEach((block, blockFace) -> {
            if (!filler.contains(block.getX(), block.getZ())) return;
            if (random.nextInt(100) >= (leaves ? 25 : 10)) return;
            Block handle = block.getRelative(blockFace);
            if (!filler.contains(handle.getX(), handle.getZ())) return;
            for (int y = 0; y > -1 * (random.nextInt(12) + 3); y--) {
                if (handle.getType() == Material.AIR) {
                    handle.setTypeIdAndData(Material.VINE.getId(), blockFaceToVineData(blockFace), false);
                    handle = handle.getRelative(0, -1, 0);
                }
            }
        });
    }

    public static HashMap<Block, BlockFace> getOutsideBlocks(Set<Block> leaves) {
        HashMap<Block, BlockFace> outside_blocks = new HashMap<>();
        for (Block block : leaves) {
            BlockFace side = getAirFacingSide(block);
            if (side != null) {
                outside_blocks.put(block, side);
            }
        }
        return outside_blocks;
    }

    @SuppressWarnings("deprecation")
    public static void generateSphere(Location center, Set<Block> blocks, int radius) {
        int radius_squared = radius * radius;
        Vector c = new Vector(0, 0, 0);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius; y <= radius; y++) {
                    Vector v = new Vector(x, y, z);
                    if (c.distanceSquared(v) < radius_squared) {
                        Block b = center.getBlock().getRelative(x, y, z);
                        blocks.add(b);
                        b.setType(Material.LOG);
                        b.setData((byte) 3);
                    }
                }
            }
        }
    }

    public static void createBranch(Block toHandle, Set<Block> blocks, Random rnd, Set<Block> leaves) {
        int length = rnd.nextInt(10) + 3;

        Location loc = toHandle.getLocation();
        Vector direction = new Vector(rnd.nextDouble() - 0.5, rnd.nextDouble() * 0.025, rnd.nextDouble() - 0.5);

        for (int i = 0; i <= length; i++) {
            loc.add(direction);
            direction.add(new Vector(0.0, 0.033, 0.0));
            generateSphere(loc, blocks, 1);
        }
        createLeaves(loc.getBlock(), rnd, leaves);
    }
    public static Set<Block> createBranches(int num, Block start, Random rand, Set<Block> blocks) {
        HashSet<Block> endblocks = new HashSet<>();
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

    public static double getRandom(Random random) {
        double v = random.nextDouble() * 2 - 1;
        while (Math.abs(v) < 0.3) {
            v = random.nextDouble() * 2 - 1;
        }
        return v;
    }

    public static Block getHighestBlock(Chunk chunk, int x, int z) {
        Block block = null;
        for (int i = 127; i >= 0; i--)
            if ((block = chunk.getBlock(x, i, z)).getType().equals(Material.GRASS))
                return block;
        return block;
    }

    @SuppressWarnings("deprecation")
    public static void buildBlocks(WorldFiller filler, Set<Block> blocks, Material material, boolean checkIsAir) {
        if (checkIsAir) {
            for (Block b : blocks) {
                if (b.getType() == Material.AIR && filler.contains(b.getX(), b.getZ())) {
                    b.setType(material);
                    b.setData((byte) 3);
                }
            }
        } else {
            for (Block b : blocks) {
                if (filler.contains(b.getX(), b.getZ())) {
                    b.setType(material);
                    b.setData((byte) 3);
                }
            }
        }
    }

    public static void createRoots(int num, Block start, Random random, Set<Block> blocks) {
        int radius = 3;
        for (int j = 0; j <= num + 2; j++) {

            int length = 15 + random.nextInt(35);

            Vector direction = new Vector(getRandom(random), random.nextDouble() * 0.33, getRandom(random));

            Location loc = start.getLocation();
            generateSphere(loc, blocks, radius, false, true);
            for (int i = 0; i < length; i++) {
                loc.add(direction);
                if (loc.getY() < 50)
                    break;
                direction.subtract(new Vector(0.0, 0.05, 0.0));
                generateSphere(loc, blocks, radius, false, true);
                if (random.nextInt(100) < 10 && radius >= 2) {
                    radius--;
                }
            }
        }
    }

    public static Block createTrunk(int num, Block block, Random random, Set<Block> blocks) {
        int start_y = block.getY();
        int radius = random.nextBoolean() ? 4 : 5;
        for (int y = 0; y < num * 3 + radius * 4; y = y + 3) {
            block = block.getLocation().add(random.nextInt(2) - 1, radius / 2d + 2, random.nextInt(2) - 1).getBlock();
            generateSphere(block.getLocation(), blocks, radius, false, random.nextBoolean());
            if (random.nextInt(100) < 3 && radius >= 3) {
                radius--;
            }
            if (block.getY() - start_y >= 70 - random.nextInt(35)) {
                return block;
            }
        }
        return block;
    }

    public static Set<Block> createLeaves(Set<Block> blocks, Random rand, boolean random) {
        Set<Block> leaves = new HashSet<>();
        for (Block block : blocks) {
            if (!random || rand.nextInt(200) < 3) {
                int radius = rand.nextBoolean() ? 4 : 5;
                int radius_squared = radius * radius;
                Location center = block.getLocation();
                Vector c = new Vector(0, 0, 0);
                for (int x = -radius; x <= radius; x++)
                    for (int z = -radius; z <= radius; z++)
                        for (int y = 0; y <= radius - 1; y++) {
                            Vector v = new Vector(x, y, z);
                            if (c.distanceSquared(v) <= radius_squared) {
                                Block b = center.getBlock().getRelative(x, y, z);
                                leaves.add(b);
                            }
                        }
            }
        }
        return leaves;
    }

    public static Block getHighestGrassBlock(Chunk chunk, int x, int z) {
        Block block;
        for (int i = chunk.getWorld().getMaxHeight(); i >= 0; i--) {
            Material material = (block = chunk.getBlock(x, i, z)).getType();
            if ((material == Material.GRASS || material == Material.MYCEL)) return block;
        }
        return null;
    }
}
