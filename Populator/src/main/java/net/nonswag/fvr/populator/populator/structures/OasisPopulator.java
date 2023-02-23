package net.nonswag.fvr.populator.populator.structures;

import com.sk89q.worldedit.Vector2D;
import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.Container;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.structures.TreePopulator.Type;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;

@RequiredArgsConstructor
public class OasisPopulator extends BlockPopulator {
    private final WorldFiller filler;

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(36) > 1) return;
        int rx = (chunk.getX() << 4) + random.nextInt(16);
        int rz = (chunk.getZ() << 4) + random.nextInt(16);
        if (world.getHighestBlockYAt(rx, rz) <= 4) return;
        int radius = 6 + random.nextInt(3);
        int oasisRadius = radius + 6;
        Material liquidMaterial = Material.WATER;
        Material solidMaterial = Material.WATER;
        int ry = world.getHighestBlockYAt(rx, rz) - 1;
        List<Block> lakeBlocks = new ArrayList<>();
        List<Block> oasisBlocks = new ArrayList<>();
        if (new Location(world, rx, ry, rz).getBlock().getType() != Material.SAND) return;
        for (int i = -1; i < 4; i++) {
            Vector center = new BlockVector(rx, ry - i, rz);
            for (int x = -oasisRadius; x <= oasisRadius; x++) {
                for (int z = -oasisRadius; z <= oasisRadius; z++) {
                    Vector position = center.clone().add(new Vector(x, 0, z));
                    if (center.distance(position) <= radius + 0.5 - i) {
                        lakeBlocks.add(world.getBlockAt(position.toLocation(world)));
                    } else if (center.distance(position) <= oasisRadius + 0.5 - i) {
                        oasisBlocks.add(world.getBlockAt(position.toLocation(world)));
                    }
                }
            }
        }
        lakeBlocks.forEach(block -> {
            if (block.isEmpty() || block.isLiquid() || !filler.contains(block)) return;
            if (block.getY() == ry + 1) {
                if (random.nextBoolean()) block.setType(Material.AIR);
            } else if (block.getY() == ry) block.setType(Material.AIR);
            else if (random.nextInt(10) > 1) block.setType(liquidMaterial);
            else block.setType(solidMaterial);
        });
        Set<Chunk> chunks = new HashSet<>();
        Set<Vector2D> coordinates = new HashSet<>();
        oasisBlocks.forEach(block -> {
            if (!filler.contains(block)) return;
            chunks.add(block.getChunk());
            coordinates.add(new Vector2D(block.getX(), block.getZ()));
            block.getRelative(BlockFace.DOWN).setType(Material.DIRT);
            block.setType(Material.GRASS);
        });
        Container container = (x, z) -> coordinates.contains(new Vector2D(x, z));
        TreePopulator trees = new TreePopulator(Type.OASIS, container);
        FlowerPopulator flowers = new FlowerPopulator(75, 75, filler);
        WildGrassPopulator grass = new WildGrassPopulator(filler, GrassSpecies.NORMAL);
        chunks.forEach(target -> {
            flowers.populate(world, random, target);
            trees.populate(world, random, target);
            grass.populate(world, random, target);
        });
    }
}
