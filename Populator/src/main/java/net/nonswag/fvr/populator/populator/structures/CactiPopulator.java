package net.nonswag.fvr.populator.populator.structures;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class CactiPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = world.getHighestBlockYAt((chunk.getX() << 4) + x, (chunk.getZ() << 4) + z);
                Block block = chunk.getBlock(x, y, z).getRelative(BlockFace.DOWN);
                if (!block.getType().equals(Material.SAND)) continue;
                if (random.nextInt(200) != 0) continue;
                generateCactus(block.getRelative(BlockFace.UP), random);
            }
        }
    }

    private void generateCactus(Block block, Random random) {
        if (!block.isEmpty()) return;
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : faces) if (!block.getRelative(face).isEmpty()) return;
        for (int i = 0; i < random.nextInt(4); ++i) block.getRelative(BlockFace.UP, i).setType(Material.CACTUS);
    }
}
