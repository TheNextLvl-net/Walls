package me.simplex.tropic.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import de.bananaco.laggy.Utils;

public class Populator_Glowshroom extends BlockPopulator {

    @Override
    public void populate(World world, Random rnd, Chunk source) {
        if (rnd.nextInt(100) >= 7) {
            return;
        }

        Block base = Utils.getHighestGrassBlock(source, rnd.nextInt(16), rnd.nextInt(16));
        if (base != null) {
            createGlowShroom(base.getRelative(0, 1, 0));
        }
    }

    private void createGlowShroom(Block base) {

        // Stem
        changeBlock(base, 0, 0, 0, 100, (byte) 10);
        changeBlock(base, 0, 1, 0, 100, (byte) 10);
        changeBlock(base, 0, 2, 0, 100, (byte) 10);

        // Glowstone
        changeBlock(base, -1, 2, -1, 89, (byte) 0);
        changeBlock(base, -1, 2, 0, 89, (byte) 0);
        changeBlock(base, -1, 2, 1, 89, (byte) 0);

        changeBlock(base, 1, 2, -1, 89, (byte) 0);
        changeBlock(base, 1, 2, 0, 89, (byte) 0);
        changeBlock(base, 1, 2, 1, 89, (byte) 0);

        changeBlock(base, 0, 2, 1, 89, (byte) 0);
        changeBlock(base, 0, 2, -1, 89, (byte) 0);

        // Inner
        changeBlock(base, -1, 3, -1, 100, (byte) 0);
        changeBlock(base, -1, 3, 0, 100, (byte) 0);
        changeBlock(base, -1, 3, 1, 100, (byte) 0);

        changeBlock(base, 1, 3, -1, 100, (byte) 0);
        changeBlock(base, 1, 3, 0, 100, (byte) 0);
        changeBlock(base, 1, 3, 1, 100, (byte) 0);

        changeBlock(base, 0, 3, 1, 100, (byte) 0);
        changeBlock(base, 0, 3, 0, 100, (byte) 0);
        changeBlock(base, 0, 3, -1, 100, (byte) 0);

        //Top
        changeBlock(base, -1, 4, -1, 100, (byte) 1);
        changeBlock(base, -1, 4, 0, 100, (byte) 4);
        changeBlock(base, -1, 4, 1, 100, (byte) 7);

        changeBlock(base, 0, 4, 1, 100, (byte) 8);
        changeBlock(base, 0, 4, 0, 100, (byte) 5);
        changeBlock(base, 0, 4, -1, 100, (byte) 2);

        changeBlock(base, 1, 4, -1, 100, (byte) 3);
        changeBlock(base, 1, 4, 0, 100, (byte) 6);
        changeBlock(base, 1, 4, 1, 100, (byte) 9);

        //North
        changeBlock(base, -1, 2, -2, 100, (byte) 1);
        changeBlock(base, 0, 2, -2, 100, (byte) 2);
        changeBlock(base, 1, 2, -2, 100, (byte) 3);

        changeBlock(base, -1, 3, -2, 100, (byte) 1);
        changeBlock(base, 0, 3, -2, 100, (byte) 2);
        changeBlock(base, 1, 3, -2, 100, (byte) 3);

        //East
        changeBlock(base, 2, 2, -1, 100, (byte) 3);
        changeBlock(base, 2, 2, 0, 100, (byte) 6);
        changeBlock(base, 2, 2, 1, 100, (byte) 9);

        changeBlock(base, 2, 3, -1, 100, (byte) 3);
        changeBlock(base, 2, 3, 0, 100, (byte) 6);
        changeBlock(base, 2, 3, 1, 100, (byte) 9);

        //South
        changeBlock(base, -1, 2, 2, 100, (byte) 7);
        changeBlock(base, 0, 2, 2, 100, (byte) 8);
        changeBlock(base, 1, 2, 2, 100, (byte) 9);

        changeBlock(base, -1, 3, 2, 100, (byte) 7);
        changeBlock(base, 0, 3, 2, 100, (byte) 8);
        changeBlock(base, 1, 3, 2, 100, (byte) 9);

        //West
        changeBlock(base, -2, 2, -1, 100, (byte) 1);
        changeBlock(base, -2, 2, 0, 100, (byte) 4);
        changeBlock(base, -2, 2, 1, 100, (byte) 7);

        changeBlock(base, -2, 3, -1, 100, (byte) 1);
        changeBlock(base, -2, 3, 0, 100, (byte) 4);
        changeBlock(base, -2, 3, 1, 100, (byte) 7);
    }

    private void changeBlock(Block base, int x, int y, int z, int mat, byte data) {
        base.getRelative(x, y, z).setTypeIdAndData(mat, data, false);
    }
}
