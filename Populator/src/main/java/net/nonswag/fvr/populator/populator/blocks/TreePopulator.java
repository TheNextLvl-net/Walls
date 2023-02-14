package net.nonswag.fvr.populator.populator.blocks;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static net.nonswag.fvr.populator.Utils.*;

@RequiredArgsConstructor
public class TreePopulator extends BlockPopulator {
    private final Set<Block> trunkblocks = new HashSet<>();
    private final Set<Block> rootblocks = new HashSet<>();
    private final Set<Block> branchblocks = new HashSet<>();

    private static boolean populating = false;

    private final WorldFiller filler;

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int chance = 15;
        if (random.nextInt(100) > chance || populating) return;
        populating = true;
        trunkblocks.clear();
        rootblocks.clear();
        branchblocks.clear();
        int num = random.nextInt(5) + 5;
        Block start = getHighestBlock(chunk, random.nextInt(8), random.nextInt(8));
        if (start.getY() < 42 || start.getY() > 126) {
            populating = false;
            return;
        }
        if (random.nextInt(100) < 10) start = start.getRelative(0, 7, 0);
        createRoots(num, start, random, rootblocks);
        Block branch_start = createTrunk(num, start, random, trunkblocks);
        Set<Block> force_leaves;
        force_leaves = createBranches(num, branch_start, random, branchblocks);
        force_leaves.addAll(createBranches(num + 2, branch_start.getRelative(0, -10, 0), random, branchblocks));
        Set<Block> leavesblocks;
        leavesblocks = createLeaves(branchblocks, random, true);
        leavesblocks.addAll(createLeaves(force_leaves, random, false));
        buildBlocks(filler, rootblocks, Material.LOG, false);
        buildBlocks(filler, trunkblocks, Material.LOG, false);
        buildBlocks(filler, branchblocks, Material.LOG, false);
        buildBlocks(filler, leavesblocks, Material.LEAVES, true);
        createVine(filler, leavesblocks, random, true);
        createVine(filler, trunkblocks, random, false);
        createVine(filler, rootblocks, random, false);
        populating = false;
    }
}
