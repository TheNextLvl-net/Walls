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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static net.nonswag.fvr.populator.Utils.*;

@RequiredArgsConstructor
public class SmallTreePopulator extends BlockPopulator {
    private final WorldFiller filler;

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int runs = random.nextInt(5) + 2;
        for (int i = 0; i <= runs; i++) {
            int x_tree = random.nextInt(16) + chunk.getX() * 16;
            int z_tree = random.nextInt(16) + chunk.getZ() * 16;
            int y_tree = world.getHighestBlockYAt(x_tree, z_tree);
            Block start = world.getBlockAt(x_tree, y_tree, z_tree);
            if (y_tree >= 62 && start.getRelative(BlockFace.DOWN).getType() == Material.GRASS) {
                createSmallTree(start.getLocation(), random);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void createSmallTree(Location location, Random random) {
        Set<Block> log_blocks = new HashSet<>();
        Set<Block> leaves_blocks = new HashSet<>();
        int height = random.nextInt(3) + 5;
        Block toHandle = location.getBlock();
        for (int y = 0; y <= height; y++) {
            toHandle = location.getBlock().getRelative(0, y, 0);
            toHandle.setType(Material.LOG);
            toHandle.setData((byte) 3);
            if (y >= height * 0.25 && y <= height - 2 && random.nextInt(100) < 15) {
                createBranch(toHandle, log_blocks, random, leaves_blocks);
            }
        }
        createLeaves(toHandle, random, leaves_blocks);
        createLeavesOnChance(log_blocks, random, leaves_blocks);
        createVine(filler, leaves_blocks, random, true);
        createVine(filler, log_blocks, random, false);
    }
}
