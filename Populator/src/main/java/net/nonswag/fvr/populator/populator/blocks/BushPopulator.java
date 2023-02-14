package net.nonswag.fvr.populator.populator.blocks;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.Utils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

import static net.nonswag.fvr.populator.Utils.createLeaves;

@RequiredArgsConstructor
public class BushPopulator extends BlockPopulator {
    private final int density;

    @Override
    public void populate(World world, Random rnd, Chunk source) {
        if(rnd.nextInt(11) >= 1)
            return;
        int runs = rnd.nextInt(density) + density / 2;
        for (int i = 0; i <= runs; i++) {
            int x_bush = rnd.nextInt(16);
            int z_bush = rnd.nextInt(16);

            Block start = Utils.getHighestGrassBlock(source, x_bush, z_bush);

            if (start != null) {
                //System.out.println("Bush! " + x_tree + ", " + z_tree);
                createBush(start.getLocation(), rnd);
            }
        }
    }

    private void createBush(Location loc, Random random) {
        Block toHandle = loc.getBlock();
        toHandle.setType(Material.LOG);
        createLeaves(toHandle, random);
    }
}
