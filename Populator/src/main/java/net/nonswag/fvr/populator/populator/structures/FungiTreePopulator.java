package net.nonswag.fvr.populator.populator.structures;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static net.nonswag.fvr.populator.Utils.createVine;

@RequiredArgsConstructor
public class FungiTreePopulator extends BlockPopulator {
    private final WorldFiller filler;

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int centerX = (chunk.getX() << 4) + random.nextInt(16);
        int centerZ = (chunk.getZ() << 4) + random.nextInt(16);
        int centerY = world.getHighestBlockYAt(centerX, centerZ) - 1;
        Block source = world.getBlockAt(centerX, centerY, centerZ);
        if (source.getType() != Material.GRASS) return;
        if (random.nextFloat() > .8f) createLargeTree(source.getRelative(BlockFace.UP).getLocation(), random);
        else createTree(source.getRelative(BlockFace.UP).getLocation(), random);
    }

    @SuppressWarnings("deprecation")
    private void createLargeTree(Location start, Random random) {
        int height = 12 + random.nextInt(6);
        Set<Block> logs = new HashSet<>();
        Set<Block> leaves = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 2; k++) {
                    Block block = start.clone().add(i, j, k).getBlock();
                    block.setType(Material.LOG);
                    block.setData((byte) 3);
                    logs.add(block);
                }
            }
        }
        float radius = 2;
        for (int j = height; j > height / 2 - 4; j -= 2) {
            for (int i = (int) -(radius - 1); i <= radius; i++) {
                for (int k = (int) -(radius - 1); k <= radius; k++) {
                    int checkCorner = 0;
                    if (i == -(radius - 1)) checkCorner++;
                    if (i == radius) checkCorner++;
                    if (k == -(radius - 1)) checkCorner++;
                    if (k == radius) checkCorner++;
                    if (checkCorner == 2) continue;
                    Block block = start.clone().add(i, j, k).getBlock();
                    if (!block.getType().equals(Material.LOG)) {
                        block.setType(Material.LEAVES);
                        block.setData((byte) 3);
                        leaves.add(block);
                    }
                }
            }
            if (radius < 4) radius += .5f;
        }
        createVine(filler, logs, random, false);
        createVine(filler, leaves, random, true);
    }

    @SuppressWarnings("deprecation")
    private void createTree(Location start, Random random) {
        int height = 8 + random.nextInt(4);
        Set<Block> logs = new HashSet<>();
        Set<Block> leaves = new HashSet<>();
        for (int j = 0; j < height; j++) {
            Block block = start.clone().add(0, j, 0).getBlock();
            block.setType(Material.LOG);
            block.setData((byte) 3);
            logs.add(block);
        }
        float radius = 1;
        for (int j = height; j > height / 2 - 2; j -= 2) {
            for (int i = (int) -radius; i <= radius; i++) {
                for (int k = (int) -radius; k <= radius; k++) {
                    int checkCorner = 0;
                    if (i == -radius) checkCorner++;
                    if (i == radius) checkCorner++;
                    if (k == -radius) checkCorner++;
                    if (k == radius) checkCorner++;
                    if (checkCorner == 2) continue;
                    Block block = start.clone().add(i, j, k).getBlock();
                    if (!block.getType().equals(Material.LOG)) {
                        block.setType(Material.LEAVES);
                        block.setData((byte) 3);
                        leaves.add(block);
                    }
                }
            }
            if (radius < 3) radius += .5f;
        }
        createVine(filler, logs, random, false);
        createVine(filler, leaves, random, true);
    }
}