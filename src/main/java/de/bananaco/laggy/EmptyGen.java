package de.bananaco.laggy;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyGen extends ChunkGenerator {

    @Override
    public byte[] generate(World world, Random random, int x, int z) {
        return new byte[world.getMaxHeight() * 16 * 16];
    }

}
