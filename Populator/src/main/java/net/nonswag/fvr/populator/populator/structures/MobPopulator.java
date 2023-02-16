package net.nonswag.fvr.populator.populator.structures;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.Container;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@RequiredArgsConstructor
public class MobPopulator extends BlockPopulator {
    private final List<Class<? extends LivingEntity>> mobs = new ArrayList<>();
    private final Container filler;

    {
        mobs.add(Chicken.class);
        mobs.add(Cow.class);
        mobs.add(Pig.class);
        mobs.add(Sheep.class);
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(100) > 15) return;
        for (int i = 0; i < random.nextInt(3); i++) {
            int x = chunk.getX() * 16 + random.nextInt(16);
            int z = chunk.getZ() * 16 + random.nextInt(16);
            Location location = chunk.getWorld().getHighestBlockAt(x, z).getLocation();
            if (!filler.contains(location.getBlockX(), location.getBlockZ())) return;
            world.spawn(location.clone().add(0, 1, 0), mobs.get(random.nextInt(mobs.size())));
        }
    }
}