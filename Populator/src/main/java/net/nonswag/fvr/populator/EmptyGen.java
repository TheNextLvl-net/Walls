package net.nonswag.fvr.populator;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class EmptyGen extends ChunkGenerator {

    @Override
    @SuppressWarnings("deprecation")
    public byte[] generate(World world, Random random, int x, int z) {
        return new byte[world.getMaxHeight() * 16 * 16];
    }
}
