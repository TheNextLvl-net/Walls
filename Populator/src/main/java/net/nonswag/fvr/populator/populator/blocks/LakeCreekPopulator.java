package net.nonswag.fvr.populator.populator.blocks;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;

import java.util.*;

@RequiredArgsConstructor
public class LakeCreekPopulator extends BlockPopulator {
    private final WorldFiller filler;

    private static final int MIN_BLOCK_COUNT = 300;
    private static final int MAX_BLOCK_COUNT = 850;
    private static final int LAKE_CHANCE = 7;
    private static final int CREEK_CHANCE = 75;
    private static final int MAX_CREEK_LENGTH = 125;

    @Override
    public void populate(World world, Random random, Chunk source) {
        if (random.nextInt(100) >= LAKE_CHANCE) return;
        int start_x = random.nextInt(16);
        int start_z = random.nextInt(16);
        Block block = world.getHighestBlockAt(source.getX() * 16 + start_x, source.getZ() * 16 + start_z);
        if (block.getY() - 1 <= 60 || block.getY() > 95) return;
        Set<Block> lake_form = collectLakeLayout(world, block, random);
        Set<Block>[] form_result = startLakeBuildProcess(world, lake_form);
        if (form_result == null) return;
        Block creek_start = buildLake(form_result[0], random);
        buildAirAndWaterfall(form_result[0], form_result[1], random);
        if (creek_start == null || random.nextInt(100) >= CREEK_CHANCE) return;
        List<Block> creekblocks = collectCreekBlocks(world, creek_start, random);
        if (creekblocks != null) buildCreek(world, creekblocks);
    }

    private List<Block> collectCreekBlocks(World world, Block creekStart, Random random) {
        int check_radius = 7;
        Vector main_dir = null;
        int highest_diff = 0;
        for (int mod_x = -check_radius; mod_x <= check_radius; mod_x++) {
            for (int mod_z = -check_radius; mod_z <= check_radius; mod_z++) {
                Block toCheck = world.getHighestBlockAt(mod_x + creekStart.getX(), mod_z + creekStart.getZ());
                int diff = creekStart.getY() - toCheck.getY();
                if (diff > highest_diff) {
                    highest_diff = diff;
                    main_dir = new Vector(toCheck.getX() - creekStart.getX(), 0, toCheck.getZ() - creekStart.getZ());
                }
            }
        }
        if (main_dir != null) {
            List<Block> creekblocks = new ArrayList<>();
            Location creek_current_center = creekStart.getRelative(0, 10, 0).getLocation();
            main_dir = main_dir.normalize().multiply(2);
            int steps = 0;
            while (!world.getHighestBlockAt(creek_current_center).getRelative(0, -1, 0).isLiquid() && steps < MAX_CREEK_LENGTH) {
                creekblocks.add(creek_current_center.getBlock());
                creek_current_center = creek_current_center.add(main_dir);
                main_dir = rotateVector(main_dir, random.nextDouble() * 0.5 - 0.25);
                steps++;
            }
            if (steps < MAX_CREEK_LENGTH) {
                return creekblocks;
            }
        }
        return null;
    }

    private void buildCreek(World world, List<Block> center_blocks) {
        Set<Block> collected_blocks_air = new HashSet<>();
        Set<Block> collected_blocks_water = new HashSet<>();
        int radius = 3;
        int radius_squared = 9;
        int last_y = world.getMaxHeight();
        Set<Block> circle = new HashSet<>();
        for (Block center : center_blocks) {
            circle.clear();
            for (int x_mod = -radius; x_mod <= radius; x_mod++) {
                for (int z_mod = -radius; z_mod <= radius; z_mod++) {
                    if ((x_mod * x_mod + z_mod * z_mod) < radius_squared) {
                        Block block = center.getRelative(x_mod, 0, z_mod);
                        if (filler.contains(x_mod, z_mod)) circle.add(block);
                    }
                }
            }
            int lowest = world.getMaxHeight();
            int highest = 0;
            for (Block block : circle) {
                int x = block.getX();
                int z = block.getZ();
                int compare = world.getHighestBlockYAt(x, z);
                if (compare < lowest) lowest = compare;
                if (compare > highest) highest = compare;
            }
            if (lowest > last_y) lowest = last_y;
            else {
                last_y = lowest;
                if (last_y < 60) {
                    last_y = 60;
                    lowest = 60;
                }
            }
            for (Block block : circle) {
                collected_blocks_water.add(world.getBlockAt(block.getX(), lowest - 2, block.getZ()));
                for (int y = lowest - 1; y <= highest; y++) {
                    collected_blocks_air.add(world.getBlockAt(block.getX(), y, block.getZ()));
                }
            }
        }
        for (Block toWater : collected_blocks_water) {
            toWater.setType(Material.WATER);
        }
        for (Block toAir : collected_blocks_air) {
            if (toAir.getY() <= 60) {
                toAir.setType(Material.WATER);
            } else if (toAir.getType() != Material.LOG && toAir.getType() != Material.LEAVES && toAir.getType() != Material.RED_MUSHROOM && toAir.getType() != Material.VINE && toAir.getType() != Material.GLOWSTONE) {
                toAir.setType(Material.AIR);
            }
        }
    }

    private Vector rotateVector(Vector dir, double angle) {
        double new_x = dir.getX() * Math.cos(angle) - dir.getZ() * Math.sin(angle);
        double new_z = dir.getX() * Math.sin(angle) + dir.getZ() * Math.cos(angle);
        return new Vector(new_x, 0, new_z);
    }

    private Set<Block> collectLakeLayout(World world, Block start, Random random) {
        Set<Block> result = new HashSet<>();
        int sizelimit = MIN_BLOCK_COUNT + random.nextInt(MAX_BLOCK_COUNT - MIN_BLOCK_COUNT);
        int blockX = start.getX();
        int blockY = start.getY();
        int blockZ = start.getZ();
        int count = 0;
        while (result.size() < sizelimit) {
            int size = result.size();
            int radius = random.nextInt(5) + 1;
            int radius_squared = radius * radius + 1;
            for (int x_mod = -radius; x_mod <= radius; x_mod++) {
                for (int z_mod = -radius; z_mod <= radius; z_mod++) {
                    if ((x_mod * x_mod + z_mod * z_mod) <= radius_squared) {
                        Block block = world.getBlockAt(blockX + x_mod, blockY, blockZ + z_mod);
                        if (filler.contains(block)) result.add(block);
                    }
                }
            }
            if (random.nextBoolean()) {
                if (random.nextBoolean()) blockX++;
                else blockZ++;
            } else {
                if (random.nextBoolean()) blockX--;
                else blockZ--;
            }
            if (size == result.size() && count++ >= 5) return result;
        }
        return result;
    }

    private void buildAirAndWaterfall(Set<Block> ground, Set<Block> blocks, Random random) {
        List<Block> candidates = new ArrayList<>();
        if (ground.isEmpty()) return;
        int ground_height = ground.iterator().next().getY();
        for (Block block : blocks) {
            if (block.getType() != Material.LOG && block.getType() != Material.LEAVES && block.getType() != Material.RED_MUSHROOM && block.getType() != Material.VINE && block.getType() != Material.GLOWSTONE) {
                block.setType(Material.AIR);
                if (checkBlockIsOnBorderOfSlice(block, blocks) && isWaterfallQualified(block) && block.getY() >= ground_height + 3) {
                    candidates.add(block);
                }
            }
        }
        if (!candidates.isEmpty()) {
            buildWaterfall(candidates.get(random.nextInt(candidates.size())));
            if (random.nextInt(100) < 20) {
                buildWaterfall(candidates.get(random.nextInt(candidates.size())));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Block>[] startLakeBuildProcess(World world, Set<Block> blocks) {
        int lowest = world.getMaxHeight();
        int highest = 0;
        for (Block block : blocks) {
            int x = block.getX();
            int z = block.getZ();
            int compare = world.getHighestBlockYAt(x, z);
            if (compare < lowest) lowest = compare;
            if (compare > highest) highest = compare;
            if (compare < 60) return null;
        }
        if (lowest < 60 || highest - lowest > 25) return null;
        Set<Block>[] result = new Set[2];
        result[0] = new HashSet<>();
        result[1] = new HashSet<>();
        for (Block block : blocks) {
            result[0].add(world.getBlockAt(block.getX(), lowest - 1, block.getZ()));
            for (int y = lowest; y <= highest; y++) {
                result[1].add(world.getBlockAt(block.getX(), y, block.getZ()));
            }
        }
        return result;
    }

    private Block buildLake(Set<Block> top_layer, Random random) {
        int max_lake_depth = random.nextInt(2) + 3;
        Set<Block> to_air = new HashSet<>();
        int lowering = 0;
        while (!sliceHasBorder(top_layer) && lowering <= 3) {
            to_air.addAll(top_layer);
            top_layer = lower_layer(top_layer);
            lowering++;
        }
        for (Block block : to_air) {
            block.setType(Material.AIR);
        }

        //Build the First water layer
        for (Block block : top_layer) {
            block.setType(Material.STATIONARY_WATER);
        }

        // "Stepped" Ground
        Set<Block> working_layer = lower_layer(top_layer);

        for (int mod_y = 0; mod_y > -max_lake_depth; mod_y--) {
            Set<Block> next_layer = new HashSet<>();
            for (Block block : working_layer) {
                if (checkBlockIsOnBorderOfSlice(block, working_layer)) {
                    if (!block.isLiquid()) {
                        block.setType(Material.DIRT);
                    }
                } else {
                    next_layer.add(block.getRelative(0, -1, 0));
                    block.setType(Material.STATIONARY_WATER);
                }
            }
            working_layer = next_layer;
        }

        //Ground of the lake
        for (Block block : working_layer) {
            if (!block.isLiquid()) {
                block.setType(Material.DIRT);
            }
        }

        //Return the start point for a possible creek, null if this lake doesn't have a suitable block
        for (Block block : top_layer) {
            if (checkBlockIsOnBorderOfSlice(block, top_layer)) {
                Block candidate = block.getRelative(getUncontainedBlockFace(block, top_layer));
                if (candidate.getRelative(BlockFace.UP).isEmpty()) {
                    //                    System.out.println("startpoint found");
                    //                    candidate.setType(Material.GLOWSTONE);
                    return candidate;
                }
            }
        }
        return null;
    }

    private Set<Block> lower_layer(Set<Block> waterLayer) {
        Set<Block> result = new HashSet<>();
        for (Block block : waterLayer) {
            result.add(block.getRelative(0, -1, 0));
        }
        return result;
    }

    private boolean checkBlockIsOnBorderOfSlice(Block block, Set<Block> slice) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        return !slice.contains(block.getRelative(faces[0])) || !slice.contains(block.getRelative(faces[1])) || !slice.contains(block.getRelative(faces[2])) || !slice.contains(block.getRelative(faces[3]));
    }

    private BlockFace getUncontainedBlockFace(Block block, Set<Block> slice) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : faces) {
            if (!slice.contains(block.getRelative(face))) {
                return face;
            }
        }

        return null;
    }

    private boolean sliceHasBorder(Set<Block> slice) {
        for (Block block : slice) if (!hasNeighbors(block)) return false;
        return true;
    }

    private boolean hasNeighbors(Block block) {
        return !block.getRelative(BlockFace.WEST).isEmpty() && !block.getRelative(BlockFace.EAST).isEmpty() && !block.getRelative(BlockFace.NORTH).isEmpty() && !block.getRelative(BlockFace.SOUTH).isEmpty();
    }

    private boolean isWaterfallQualified(Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace f : faces) {
            Block r = block.getRelative(f);
            if (!r.isEmpty() && !r.getRelative(BlockFace.UP).isEmpty()) {
                if (r.getType().equals(Material.DIRT) || r.getType().equals(Material.STONE)) return true;
            }
        }
        return false;
    }

    private void buildWaterfall(Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace f : faces) {
            Block r = block.getRelative(f);
            if (!r.isEmpty()) {
                r.setType(Material.WATER);
                return;
            }
        }
    }
}
