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

import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Populator_Delayed extends BlockPopulator {
    private List<BlockPopulator> toProcess;
    private JavaPlugin p;
    private BukkitScheduler s;

    public Populator_Delayed(List<BlockPopulator> toProcess, JavaPlugin p, BukkitScheduler s) {
        this.toProcess = toProcess;
        this.p = p;
        this.s = s;
    }

    @Override
    public void populate(final World world, final Random random, final Chunk source) {
        s.scheduleSyncDelayedTask(p, new Runnable() {

            @Override
            public void run() {
                for (BlockPopulator p : toProcess) {
                    p.populate(world, random, source);
                }

            }
        });

    }

}
