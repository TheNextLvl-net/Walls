package net.nonswag.fvr.populator.populator.structures;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import net.nonswag.fvr.populator.Utils;

public class GravelStack extends BlockPopulator {

    int min = 5;
    int max = 20;
    int minh = 3;
    int maxh = 7;
    int chance = 7;

    @Override
    public void populate(World world, Random rand, Chunk chunk) {

        if (rand.nextInt(1000) > chance)
            return;

        // Total # of stacks
        int num = min + rand.nextInt(max - min);

        for (int i = 0; i < num; i++) {
            // Choose a different x, z every time (sometimes the same but it'll be fine)
            Block b = Utils.getHighestBlock(chunk, rand.nextInt(16), rand.nextInt(16));
            if (b.getType() == Material.OBSIDIAN && b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                stack(b, rand);
            }
        }
    }

    public void stack(Block block, Random rand) {
        int h = minh + rand.nextInt(maxh - minh);
        Block rel;
        for (int y = 0; y < h; y++) {
            rel = block.getRelative(0, y, 0);
            if (rel.getType() == Material.AIR)
                rel.setType(Material.GRAVEL);
        }
    }
}
