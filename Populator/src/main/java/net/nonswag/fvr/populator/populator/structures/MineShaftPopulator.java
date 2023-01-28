package net.nonswag.fvr.populator.populator.structures;

import net.nonswag.fvr.populator.Container;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MineShaftPopulator extends BlockPopulator {

    public Container filler;
    private final boolean climb;

    public MineShaftPopulator(Container filler) {
        this(filler, true);
    }

    public MineShaftPopulator(Container filler, boolean climb) {
        this.filler = filler;
        this.climb = climb;
    }

    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        int chance = 20;
        if (rand.nextInt(100) > chance)
            return;
        int length = 8 * rand.nextInt(8);
        int minY = 8;
        int maxY = 40;
        int y = rand.nextInt(maxY - minY) + minY;
        Block start = chunk.getBlock(8, y, 8);
        Block chest = start.getRelative(BlockFace.UP);
        if (!canPlace(start))
            return;
        int up = climb ? 1 : 0;
        Set<Block> wood = new HashSet<>();
        Set<Block> fence = new HashSet<>();
        Set<Block> rails = new HashSet<>();
        Set<Block> air = new HashSet<>();
        for (int i = 0; i < 8; i++) {
            generateSection(start, rand, wood, fence, rails, air);
            start = start.getRelative(1, 0, 0);
        }
        for (int i = 0; i < length; i++) {
            int climb = rand.nextBoolean() ? up : 0;
            generateSection(start, rand, wood, fence, rails, air);
            start = start.getRelative(1, climb, 0);
        }
        for (Block b : air) {
            if (canPlace(b)) {
                b.setType(Material.AIR);
            }
        }
        for (Block b : wood) {
            if (canPlace(b)) {
                b.setType(Material.WOOD);
            }
        }
        for (Block b : fence) {
            if (canPlace(b)) {
                b.setType(Material.FENCE);
            }
        }
        for (Block b : rails) {
            if (canPlace(b)) {
                b.setType(Material.RAILS);
                if (rand.nextInt(8) == 1) {
                    // 1 in 8 chance of placing a torch
                    b.getRelative(0, 0, 1).setType(Material.TORCH);
                }
                /*
                 * if (rand.nextInt(10) == 1) { Block s = b.getRelative(0, -2, 0); s.setType(Material.MOB_SPAWNER); CreatureSpawner cs = (CreatureSpawner) s.getState();
                 * cs.setSpawnedType(EntityType.ZOMBIE); }
                 */
            }
        }
        // if(canPlace(chest))
        chest.setType(Material.CHEST);
    }

    public void generateSection(Block center, Random rand, Set<Block> wood, Set<Block> fence, Set<Block> rails, Set<Block> air) {
        // floor
        wood.add(center.getRelative(0, -1, 0));
        wood.add(center.getRelative(0, -1, -1));
        wood.add(center.getRelative(0, -1, 1));
        // rails
        rails.add(center);
        // air
        air.add(center.getRelative(0, 1, 0));
        air.add(center.getRelative(0, 2, 0));

        air.add(center.getRelative(0, 0, 1));
        air.add(center.getRelative(0, 1, 1));
        air.add(center.getRelative(0, 2, 1));

        air.add(center.getRelative(0, 0, -1));
        air.add(center.getRelative(0, 1, -1));
        air.add(center.getRelative(0, 2, -1));
        // ceiling
        wood.add(center.getRelative(0, 3, 0));
        wood.add(center.getRelative(0, 3, 1));
        wood.add(center.getRelative(0, 3, -1));
        // sides
        if (center.getX() % 2 == 0) {
            // floor sides
            wood.add(center.getRelative(0, -1, -2));
            wood.add(center.getRelative(0, -1, 2));
            // ceiling sides
            wood.add(center.getRelative(0, 3, -2));
            wood.add(center.getRelative(0, 3, 2));
            // fence sides
            fence.add(center.getRelative(0, 2, 2));
            fence.add(center.getRelative(0, 2, -2));
            fence.add(center.getRelative(0, 1, 2));
            fence.add(center.getRelative(0, 1, -2));
            // cobweb|wood siding
            Block b;
            b = center.getRelative(0, 0, -2);
            if (canPlace(b) && rand.nextBoolean()) {
                b.setType(Material.WEB);
            } else {
                wood.add(b);
            }
            b = center.getRelative(0, 0, 2);
            if (canPlace(b) && rand.nextBoolean()) {
                b.setType(Material.WEB);
            } else {
                wood.add(b);
            }
        } else {
            air.add(center.getRelative(0, 2, 2));
            air.add(center.getRelative(0, 2, -2));
            air.add(center.getRelative(0, 1, 2));
            air.add(center.getRelative(0, 1, -2));
            air.add(center.getRelative(0, 0, 2));
            air.add(center.getRelative(0, 0, -2));
        }
    }

    public boolean canPlace(Block block) {
        return filler.contains(block.getX(), block.getZ()) && (block.getType() == Material.COBBLESTONE || block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE);
    }

}