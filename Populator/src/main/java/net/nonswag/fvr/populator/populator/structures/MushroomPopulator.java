package net.nonswag.fvr.populator.populator.structures;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import net.nonswag.fvr.populator.Utils;

public class MushroomPopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk source) {
        int chance = random.nextInt(100);
        if (chance < 50) {
            int flowercount = random.nextInt(5) + 2;
            int type = random.nextInt(100);
            for (int t = 0; t <= flowercount; t++) {
                int flower_x = random.nextInt(15);
                int flower_z = random.nextInt(15);

                Block handle = Utils.getHighestGrassBlock(source, flower_x, flower_z);
                if (handle != null) {
                    if (handle.getType() == Material.AIR) {
                        if (type < 33) {
                            handle.setType(Material.RED_MUSHROOM);
                        } else {
                            handle.setType(Material.BROWN_MUSHROOM);
                        }
                    }
                }
            }
        }
    }
}
