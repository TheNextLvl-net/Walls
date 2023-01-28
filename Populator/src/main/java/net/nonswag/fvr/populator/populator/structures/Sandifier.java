package net.nonswag.fvr.populator.populator.structures;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import net.nonswag.fvr.populator.Container;
import net.nonswag.fvr.populator.Utils;

public class Sandifier extends BlockPopulator {

    public Container filler;

    public BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

    public Sandifier(Container filler) {
        this.filler = filler;
    }

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Block b = Utils.getHighestBlock(chunk, x, z);
                if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
                    for (BlockFace face : faces) {
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
}
