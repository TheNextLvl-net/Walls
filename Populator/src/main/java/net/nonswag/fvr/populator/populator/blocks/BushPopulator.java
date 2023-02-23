package net.nonswag.fvr.populator.populator.blocks;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.Utils;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

import static net.nonswag.fvr.populator.Utils.createLeaves;

@RequiredArgsConstructor
public class BushPopulator extends BlockPopulator {
    private final WorldFiller filler;
    private final int density;

    @Override
    public void populate(World world, Random random, Chunk source) {
        if (random.nextInt(100) >= 30) return;
        int runs = random.nextInt(density) + density / 2;
        for (int i = 0; i <= runs; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            if (!filler.contains(x, z)) continue;
            Block start = Utils.getHighestGrassBlock(source, x, z);
            if (start != null) createBush(start, random);
        }
    }

    private void createBush(Block block, Random random) {
        block.setType(Material.LOG);
        createLeaves(filler, block.getLocation(), random);
    }
}
