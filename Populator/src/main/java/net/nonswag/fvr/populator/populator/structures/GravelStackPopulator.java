package net.nonswag.fvr.populator.populator.structures;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.Utils;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

@RequiredArgsConstructor
public class GravelStackPopulator extends BlockPopulator {
    private final WorldFiller filler;

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(1000) > 7) return;
        for (int i = 0; i < random.nextInt(15) + 5; i++) {
            Block block = Utils.getHighestBlock(chunk, random.nextInt(16), random.nextInt(16));
            if (filler.contains(block)) stack(block, random);
        }
    }

    public void stack(Block block, Random random) {
        if (!block.getType().equals(Material.OBSIDIAN) || !block.getRelative(BlockFace.UP).isEmpty()) return;
        for (int y = 0; y < random.nextInt(4) + 3; y++) {
            Block relative = block.getRelative(0, y, 0);
            if (relative.getType() == Material.AIR) relative.setType(Material.GRAVEL);
        }
    }
}
