package net.nonswag.fvr.populator.populator.structures;

import net.nonswag.fvr.populator.Container;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
 * BlockPopulator that adds trees based on the biome.
 * 
 * @author heldplayer
 */
public class NetherTreePopulator extends BlockPopulator {
    public enum Type {
        FOREST,
        PLAINS,
        RAINFOREST,
        SAVANNA,
        SEASONAL_FOREST,
        SHRUBLAND,
        SWAMPLAND,
        TAIGA,
        TUNDRA,
        OASIS
    }

    Type type;
    Container container;

    public NetherTreePopulator(Type type) {
        this(type, (x, z) -> true);
    }

    public NetherTreePopulator(Type type, Container container) {
        this.type = type;
        this.container = container;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void populate(World world, Random random, Chunk chunk) {
        int centerX;
        int centerZ;

        byte data = 0;
        int chance = 0;
        int height = 4 + random.nextInt(3);
        int multiplier = 1;

        if (random.nextBoolean()) {
            data = 2;
            height = 5 + random.nextInt(3);
        }

        switch (type) {
            case FOREST:
            case RAINFOREST:
                chance = 160;
                multiplier = 10;
                break;
            case PLAINS:
                chance = 40;
                break;
            case SAVANNA:
                chance = 70;
                data = 0;
                multiplier = 8;
                break;
            case SEASONAL_FOREST:
                chance = 140;
                multiplier = 8;
                break;
            case SHRUBLAND:
                chance = 60;
                break;
            case SWAMPLAND:
                chance = 120;
                break;
            case TAIGA:
                chance = 120;
                data = 1;
                height = 8 + random.nextInt(3);
                multiplier = 3;
                break;
            case TUNDRA:
                chance = 5;
                data = 1;
                height = 7 + random.nextInt(3);
                break;
            case OASIS:
                chance = 300;
                multiplier = 22;
                height = 7 + random.nextInt(3);
        }

        for (int i = 0; i < multiplier; i++) {
            centerX = (chunk.getX() << 4) + random.nextInt(16);
            centerZ = (chunk.getZ() << 4) + random.nextInt(16);
            if (random.nextInt(300) < chance) {
                int centerY = world.getHighestBlockYAt(centerX, centerZ) - 1;
                Block sourceBlock = world.getBlockAt(centerX, centerY, centerZ);
                if (!container.contains(sourceBlock.getX(), sourceBlock.getZ()))
                    continue;

                if ((sourceBlock.getType() == Material.GRASS || sourceBlock.getType() == Material.NETHERRACK) && sourceBlock.getRelative(0, 1, 1).getType() != Material.WATER) {
                    world.getBlockAt(centerX, centerY + height + 1, centerZ).setTypeIdAndData(161, data, true);
                    for (int j = 0; j < 4; j++) {
                        world.getBlockAt(centerX, centerY + height + 1 - j, centerZ - 1).setTypeIdAndData(161, data, true);
                        world.getBlockAt(centerX, centerY + height + 1 - j, centerZ + 1).setTypeIdAndData(161, data, true);
                        world.getBlockAt(centerX - 1, centerY + height + 1 - j, centerZ).setTypeIdAndData(161, data, true);
                        world.getBlockAt(centerX + 1, centerY + height + 1 - j, centerZ).setTypeIdAndData(161, data, true);
                    }

                    if (random.nextBoolean()) {
                        world.getBlockAt(centerX + 1, centerY + height, centerZ + 1).setTypeIdAndData(161, data, true);
                    }
                    if (random.nextBoolean()) {
                        world.getBlockAt(centerX + 1, centerY + height, centerZ - 1).setTypeIdAndData(161, data, true);
                    }
                    if (random.nextBoolean()) {
                        world.getBlockAt(centerX - 1, centerY + height, centerZ + 1).setTypeIdAndData(161, data, true);
                    }
                    if (random.nextBoolean()) {
                        world.getBlockAt(centerX - 1, centerY + height, centerZ - 1).setTypeIdAndData(161, data, true);
                    }

                    world.getBlockAt(centerX + 1, centerY + height - 1, centerZ + 1).setTypeIdAndData(161, data, true);
                    world.getBlockAt(centerX + 1, centerY + height - 1, centerZ - 1).setTypeIdAndData(161, data, true);
                    world.getBlockAt(centerX - 1, centerY + height - 1, centerZ + 1).setTypeIdAndData(161, data, true);
                    world.getBlockAt(centerX - 1, centerY + height - 1, centerZ - 1).setTypeIdAndData(161, data, true);
                    world.getBlockAt(centerX + 1, centerY + height - 2, centerZ + 1).setTypeIdAndData(161, data, true);
                    world.getBlockAt(centerX + 1, centerY + height - 2, centerZ - 1).setTypeIdAndData(161, data, true);
                    world.getBlockAt(centerX - 1, centerY + height - 2, centerZ + 1).setTypeIdAndData(161, data, true);
                    world.getBlockAt(centerX - 1, centerY + height - 2, centerZ - 1).setTypeIdAndData(161, data, true);

                    for (int j = 0; j < 2; j++) {
                        for (int k = -2; k <= 2; k++) {
                            for (int l = -2; l <= 2; l++) {
                                world.getBlockAt(centerX + k, centerY + height - 1 - j, centerZ + l).setTypeIdAndData(161, data, true);
                            }
                        }
                    }

                    for (int j = 0; j < 2; j++) {
                        if (random.nextBoolean()) {
                            world.getBlockAt(centerX + 2, centerY + height - 1 - j, centerZ + 2).setTypeIdAndData(0, (byte) 0, true);
                        }
                        if (random.nextBoolean()) {
                            world.getBlockAt(centerX + 2, centerY + height - 1 - j, centerZ - 2).setTypeIdAndData(0, (byte) 0, true);
                        }
                        if (random.nextBoolean()) {
                            world.getBlockAt(centerX - 2, centerY + height - 1 - j, centerZ + 2).setTypeIdAndData(0, (byte) 0, true);
                        }
                        if (random.nextBoolean()) {
                            world.getBlockAt(centerX - 2, centerY + height - 1 - j, centerZ - 2).setTypeIdAndData(0, (byte) 0, true);
                        }
                    }
                    for (int y = 1; y <= height; y++) {
                        world.getBlockAt(centerX, centerY + y, centerZ).setTypeIdAndData(162, data, true);
                    }
                }
            }
        }
    }
}
