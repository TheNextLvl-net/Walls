package net.nonswag.fvr.populator.populator.structures;

import net.nonswag.fvr.populator.Utils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class SandPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Block b = Utils.getHighestBlock(chunk, x, z);
                if (b.getType() != Material.WATER && b.getType() != Material.STATIONARY_WATER) continue;
                for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                    Block f = b.getRelative(face);
                    if (f.getType() == Material.GRASS) {
                        f.setType(Material.SAND);
                    }
                    Block f2 = f.getRelative(face);
                    if (f2.getType() == Material.GRASS) {
                        f2.setType(Material.SAND);
                    }
                }
            }
        }
    }
}
