package net.nonswag.fvr.populator.populator.structures;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@RequiredArgsConstructor
public class FlowerPopulator extends BlockPopulator {
    private final int plantChance, patchChance;
    private final List<Material> flowers = new ArrayList<>();
    private final WorldFiller filler;

    public FlowerPopulator(WorldFiller filler) {
        plantChance = 10;
        patchChance = 25;
        this.filler = filler;
    }

    {
        getFlowers().add(Material.RED_ROSE);
        getFlowers().add(Material.YELLOW_FLOWER);
    }

    @Override
    public void populate(World world, Random random, Chunk source) {
        if (random.nextInt(100) >= patchChance) return;
        int x = (source.getX() << 4) + random.nextInt(16);
        int z = (source.getZ() << 4) + random.nextInt(16);
        Material flowerType = flowers.get(random.nextInt(flowers.size()));
        for (int i = 0; i < 7; i++) {
            x += random.nextInt(3) - 1;
            z += random.nextInt(3) - 1;
            if (!filler.contains(x, z)) continue;
            int y = world.getHighestBlockYAt(x, z);
            Block block = world.getBlockAt(x, y, z);
            if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS)) continue;
            if (!block.isEmpty() || random.nextInt(100) >= plantChance) continue;
            block.setType(flowerType);
        }
    }
}
