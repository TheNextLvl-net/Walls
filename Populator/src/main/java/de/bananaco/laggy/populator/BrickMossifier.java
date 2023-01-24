package de.bananaco.laggy.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import de.bananaco.laggy.Utils;

public class BrickMossifier extends BlockPopulator {

    int checks = 4;

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        /*    // Is this even on a continent we want to use it on?
            int conx = Continent.getContinentFromChunk(chunk.getX());
            int conz = Continent.getContinentFromChunk(chunk.getZ());
            
            Continent continent = ContinentManager.getInstance(world.getName()).getContinent(world, conx, conz);
            
            // Don't do anything if it's not one we want to use
            if(continent.getType() != ContinentType.NETHER)
                return;*/

        for (int i = 0; i < 4; i++) {
            Block b = Utils.getHighestBlock(chunk, rand.nextInt(16), rand.nextInt(16));
            if (b.getType() == Material.SMOOTH_BRICK) {
                b.setData((byte) rand.nextInt(3), true);
            }
        }
    }
}
