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
