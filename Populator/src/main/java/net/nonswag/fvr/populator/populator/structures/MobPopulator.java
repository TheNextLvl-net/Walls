package net.nonswag.fvr.populator.populator.structures;

import net.nonswag.fvr.populator.Container;
import net.nonswag.fvr.populator.Utils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobPopulator extends BlockPopulator {

    public List<Class<? extends LivingEntity>> mobs = new ArrayList<>();
    public Container filler;

    public MobPopulator(Container filler) {
        this.filler = filler;
        mobs.add(Chicken.class);
        mobs.add(Chicken.class);
        mobs.add(Chicken.class);
        mobs.add(Cow.class);
        mobs.add(Cow.class);
        mobs.add(Pig.class);
        mobs.add(Pig.class);
        mobs.add(Pig.class);
        mobs.add(Sheep.class);
        mobs.add(Sheep.class);
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int number = 1;
        Random rand = new Random();

        for (int i = 0; i < number; i++) {
            int x = rand.nextInt(16);
            int z = rand.nextInt(16);

            Location loc = Utils.getHighestBlock(chunk, x, z).getLocation();
            loc.setY(loc.getY() + 2);

            if (filler.contains(loc.getBlockX(), loc.getBlockZ())) {
                world.spawn(loc, mobs.get(rand.nextInt(mobs.size())));
            }

        }
    }
}