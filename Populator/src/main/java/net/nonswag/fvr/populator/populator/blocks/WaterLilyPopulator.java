package net.nonswag.fvr.populator.populator.blocks;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

@RequiredArgsConstructor
public class WaterLilyPopulator extends BlockPopulator {
    private final WorldFiller filler;

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(3) != 0) return;
        for (int x = chunk.getX() * 16; x < chunk.getX() * 16 + 16; x++) {
            for (int z = chunk.getZ() * 16; z < chunk.getZ() * 16 + 16; z++) {
                Block block = getHighestBlock(chunk, x, z);
                if (block == null || !block.isEmpty() || !filler.contains(block)) continue;
                int depth = waterDepth(block.getRelative(0, -1, 0));
                if (depth > 5) continue;
                if (random.nextInt(100) >= (8 * (6 - depth))) continue;
                block.setType(Material.WATER_LILY);
            }
        }
    }

    private Block getHighestBlock(Chunk chunk, int x, int z) {
        Block block = null;
        for (int i = chunk.getWorld().getMaxHeight(); i >= 0; i--)
            if ((block = chunk.getBlock(x, i, z)).getType() == Material.WATER || (block = chunk.getBlock(x, i, z)).getType() == Material.STATIONARY_WATER)
                return block.getRelative(0, 1, 0);
        return block;
    }

    private int waterDepth(Block surface) {
        int depth = 0;
        while (surface.getType() == Material.STATIONARY_WATER || surface.getType() == Material.WATER) {
            surface = surface.getRelative(0, -1, 0);
            if (++depth > 5) break;
        }
        return depth;
    }
}
