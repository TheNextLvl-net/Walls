package net.nonswag.fvr.populator.populator.structures;

import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

@RequiredArgsConstructor
public class WildGrassPopulator extends BlockPopulator {
    private final byte data;

    @Override
    @SuppressWarnings("deprecation")
    public void populate(World world, Random random, Chunk source) {
        int x = (source.getX() << 4);
        int z = (source.getZ() << 4);
        for (int i = 0; i < random.nextInt(12) + 7; i++) {
            x += random.nextInt(3) - 1;
            z += random.nextInt(3) - 1;
            int y = world.getHighestBlockYAt(x, z);
            Block b = world.getBlockAt(x, y, z);
            if (b.getRelative(0, -1, 0).getType() == Material.GRASS && b.getType().equals(Material.AIR)) {
                b.setType(Material.LONG_GRASS);
                b.setData(data);
            }
        }
    }
}
