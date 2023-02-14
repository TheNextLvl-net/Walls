package net.nonswag.fvr.populator.populator.blocks;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class MushroomPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk source) {
        int chance = random.nextInt(100);
        if (chance < 10) {
            int type = random.nextInt(100);
            Material mushroom = type < 33 ? Material.BROWN_MUSHROOM : Material.RED_MUSHROOM;
            int mushrooms = random.nextInt(3) + 2;
            int placed = 0;
            for (int t = 0; t <= mushrooms; t++) {
                for (int flower_x = 0; flower_x < 16; flower_x++) {
                    for (int flower_z = 0; flower_z < 16; flower_z++) {
                        Block handle = world.getBlockAt(flower_x + source.getX() * 16, getHighestEmptyBlockYAtIgnoreTreesAndFoliage(world, flower_x + source.getX() * 16, flower_z + source.getZ() * 16), flower_z + source.getZ() * 16);
                        if (handle.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS) && isRelativeTo(handle) && handle.isEmpty()) {
                            handle.setType(mushroom);
                            placed++;
                            if (placed >= mushrooms) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isRelativeTo(Block block) {
        for (BlockFace blockFace : BlockFace.values()) {
            if (block.getRelative(blockFace).getType().equals(Material.LOG)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private int getHighestEmptyBlockYAtIgnoreTreesAndFoliage(World w, int x, int z) {
        for (int y = w.getMaxHeight(); y >= 1; y--) {
            Block handle = w.getBlockAt(x, y - 1, z);
            int id = handle.getTypeId();
            if (id != 0 && id != 17 && id != 18 && id != 37 && id != 38) {
                return y;
            }
        }
        return 0;
    }
}
