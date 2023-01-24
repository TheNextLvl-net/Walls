package me.simplex.tropic.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import de.bananaco.laggy.Utils;

public class Populator_Pumpkin extends BlockPopulator {

    int chancePer100, numMelons, depositRadius;

    public Populator_Pumpkin() {
        this.chancePer100 = 3;
        this.numMelons = 5;
        this.depositRadius = 20;
    }

    @Override
    public void populate(World world, Random random, Chunk source) {
        // Check if we should place a melon patch on this chunk
        if (random.nextInt(100) < chancePer100) {
            int x = (source.getX() << 4) + random.nextInt(16);
            int z = (source.getZ() << 4) + random.nextInt(16);

            for (int i = 0; i < random.nextInt(numMelons); i++) {
                // Pick a random spot within the radius
                int cx = x + random.nextInt(depositRadius * 2) - depositRadius;
                int cz = z + random.nextInt(depositRadius * 2) - depositRadius;

                Block base = Utils.getHighestGrassBlock(world, cx, cz);
                if (base != null) {
                    Block pumpkin = base.getRelative(0, 1, 0);
                    pumpkin.setType(Material.PUMPKIN);
                    pumpkin.setData((byte) random.nextInt(4));
                }
            }
        }
    }
}