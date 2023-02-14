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
public class MediumTreePopulator extends BlockPopulator {
    private final WorldFiller filler;

    @Override
    public void populate(World world, Random rnd, Chunk source) {
        if (rnd.nextInt(100) < 60) {
            int x_tree = rnd.nextInt(16) + source.getX() * 16;
            int z_tree = rnd.nextInt(16) + source.getZ() * 16;
            int y_tree = world.getHighestBlockYAt(x_tree, z_tree);
            Block start = world.getBlockAt(x_tree, y_tree, z_tree);
            if (y_tree >= 64 && start.getRelative(BlockFace.DOWN).getType() == Material.GRASS) {
                createMediumTree(start.getLocation(), rnd);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void createMediumTree(Location loc, Random random) {
        Set<Block> log_blocks = new HashSet<>();
        Set<Block> leaves_blocks = new HashSet<>();
        int height = random.nextInt(20) + 10;
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                Block toHandle = loc.getBlock();
                for (int y = -1; y <= height; y++) {
                    toHandle = loc.getBlock().getRelative(x, y, z);
                    toHandle.setType(Material.LOG);
                    toHandle.setData((byte) 3);
                    if (y >= height * 0.33 && y <= height - 5 && random.nextInt(100) < 15) {
                        createBranch(toHandle, log_blocks, random, leaves_blocks);
                    }
                }
                createLeaves(toHandle, random, leaves_blocks);
            }
        }
        createLeavesOnChance(log_blocks, random, leaves_blocks);
        createVine(filler, leaves_blocks, random, true);
        createVine(filler, log_blocks, random, false);
    }
}
