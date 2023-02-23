package net.nonswag.fvr.populator.populator.structures;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.GrassSpecies;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

@AllArgsConstructor
@RequiredArgsConstructor
public class WildGrassPopulator extends BlockPopulator {
    private final WorldFiller filler;
    private final GrassSpecies species;
    private Material ground = Material.GRASS;

    @Override
    @SuppressWarnings("deprecation")
    public void populate(World world, Random random, Chunk source) {
        int x = (source.getX() << 4);
        int z = (source.getZ() << 4);
        for (int i = 0; i < random.nextInt(12) + 7; i++) {
            x += random.nextInt(3) - 1;
            z += random.nextInt(3) - 1;
            if (!filler.contains(x, z)) continue;
            Block block = world.getBlockAt(x, world.getHighestBlockYAt(x, z), z);
            if (!block.isEmpty() || !block.getRelative(BlockFace.DOWN).getType().equals(ground)) continue;
            block.setType(Material.LONG_GRASS);
            block.setData(species.getData());
        }
    }
}
