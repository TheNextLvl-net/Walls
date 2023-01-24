/*
 * Copyright 2012 s1mpl3x
 * 
 * This file is part of Tropic.
 * 
 * Tropic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Tropic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Tropic If not, see <http://www.gnu.org/licenses/>.
 */
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