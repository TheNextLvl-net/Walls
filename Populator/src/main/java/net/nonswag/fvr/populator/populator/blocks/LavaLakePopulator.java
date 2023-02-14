package net.nonswag.fvr.populator.populator.blocks;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LavaLakePopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk source) {
        if (!(random.nextInt(100) < 5)) return;
        ChunkSnapshot snapshot = source.getChunkSnapshot();
        int rx16 = random.nextInt(16);
        int rx = (source.getX() << 4) + rx16;
        int rz16 = random.nextInt(16);
        int rz = (source.getZ() << 4) + rz16;
        if (snapshot.getHighestBlockYAt(rx16, rz16) < 4) return;
        int ry = random.nextInt(40) + 20;
        int radius = 2 + random.nextInt(4);
        Material solidMaterial = Material.STATIONARY_LAVA;
        List<Block> lakeBlocks = new ArrayList<>();
        for (int i = -1; i < 4; i++) {
            Vector center = new BlockVector(rx, ry - i, rz);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Vector position = center.clone().add(new Vector(x, 0, z));
                    if (center.distance(position) <= radius + 0.5 - i) {
                        lakeBlocks.add(world.getBlockAt(position.toLocation(world)));
                    }
                }
            }
        }
        for (Block block : lakeBlocks) {
            if (!block.isEmpty() && !block.isLiquid()) {
                if (block.getY() >= ry) block.setType(Material.AIR);
                else block.setType(solidMaterial);
            }
        }
    }
}
