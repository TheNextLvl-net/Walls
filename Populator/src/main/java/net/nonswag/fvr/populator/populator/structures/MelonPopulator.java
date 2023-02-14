package net.nonswag.fvr.populator.populator.structures;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class MelonPopulator extends BlockPopulator {
    @Override
    public void populate(World world, Random random, Chunk source) {
        if (random.nextInt(1000) >= 20) return;
        int x = (source.getX() << 4) + random.nextInt(16);
        int z = (source.getZ() << 4) + random.nextInt(16);
        for (int i = 0; i < random.nextInt(7); i++) {
            int cx = x + random.nextInt(20) - 10;
            int cz = z + random.nextInt(20) - 10;
            int y = world.getHighestBlockYAt(cx, cz);
            if (world.getBlockAt(cx, y - 1, cz).getType() != Material.GRASS) continue;
            world.getBlockAt(cx, y, cz).setType(Material.MELON_BLOCK);
        }
    }
}
