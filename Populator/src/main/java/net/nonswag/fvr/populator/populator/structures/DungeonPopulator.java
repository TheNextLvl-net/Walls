package net.nonswag.fvr.populator.populator.structures;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.Random;

public class DungeonPopulator extends BlockPopulator {

    private Random random;
    private World world;

    @Override
    public void populate(World w, Random rnd, Chunk chunk) {
        SimplexNoiseGenerator simplex = new SimplexNoiseGenerator(rnd);
        random = rnd;
        world = w;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int cx = (chunk.getX() << 4) + x;
                int cz = (chunk.getZ() << 4) + z;
                int y = world.getHighestBlockYAt(cx, cz);
                Block block = chunk.getBlock(x, y - 1, z);

                if (block.getType() == Material.STONE && random.nextInt(1024) == 0) {
                    placeChest(block);
                }
            }
        }
        double density = simplex.noise(chunk.getX() * 16, chunk.getZ() * 16);
        if (density > 0.8) {
            int roomCount = (int) (density * 10) - 3;

            for (int i = 0; i < roomCount; i++) {
                if (random.nextBoolean()) {
                    int x = (chunk.getX() << 4) + random.nextInt(16);
                    int z = (chunk.getZ() << 4) + random.nextInt(16);
                    int y = 12 + random.nextInt(22);

                    int sizeX = random.nextInt(12) + 5;
                    int sizeY = random.nextInt(6) + 4;
                    int sizeZ = random.nextInt(12) + 5;

                    generateRoom(x, y, z, sizeX, sizeY, sizeZ);
                }
            }
        }
    }

    private void generateRoom(int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ) {
        for (int x = posX; x < posX + sizeX; x++) {
            for (int y = posY; y < posY + sizeY; y++) {
                for (int z = posZ; z < posZ + sizeZ; z++) {
                    placeBlock(x, y, z, Material.AIR);
                }
            }
        }

        int numSpawners = 1 + random.nextInt(2);
        for (int i = 0; i < numSpawners; ++i) {
            int x = posX + random.nextInt(sizeX);
            int z = posZ + random.nextInt(sizeZ);
            placeSpawner(world.getBlockAt(x, posY, z));
        }

        int numChests = numSpawners + random.nextInt(2);
        for (int i = 0; i < numChests; ++i) {
            int x = posX + random.nextInt(sizeX);
            int z = posZ + random.nextInt(sizeZ);
            placeChest(world.getBlockAt(x, posY, z));
        }

        for (int x = posX - 1; x <= posX + sizeX; x++) {
            for (int z = posZ - 1; z <= posZ + sizeZ; z++) {
                placeBlock(x, posY - 1, z, pickStone());
                placeBlock(x, posY + sizeY, z, pickStone());
            }
        }

        for (int y = posY - 1; y <= posY + sizeX; y++) {
            for (int z = posZ - 1; z <= posZ + sizeZ; z++) {
                placeBlock(posX - 1, y, z, pickStone());
                placeBlock(posX + sizeX, y, z, pickStone());
            }
        }

        for (int x = posX - 1; x <= posX + sizeX; x++) {
            for (int y = posY - 1; y <= posY + sizeY; y++) {
                placeBlock(x, y, posZ - 1, pickStone());
                placeBlock(x, y, posZ + sizeZ, pickStone());
            }
        }
    }

    private Material pickStone() {
        if (random.nextInt(6) == 0) {
            return Material.MOSSY_COBBLESTONE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private void placeSpawner(Block block) {
        EntityType[] types = new EntityType[] { EntityType.SKELETON, EntityType.ZOMBIE, EntityType.CREEPER, EntityType.SPIDER };
        block.setType(Material.MOB_SPAWNER);
        if (block.getState() instanceof CreatureSpawner)
            ((CreatureSpawner) block.getState()).setSpawnedType(types[random.nextInt(types.length)]);
        else block.setType(Material.MOSSY_COBBLESTONE);
    }

    private void placeChest(Block block) {
        block.setType(Material.CHEST);
    }

    private void placeBlock(int x, int y, int z, Material mat) {
        if (canPlaceBlock(x, y, z)) {
            world.getBlockAt(x, y, z).setType(mat);
        }
    }

    private boolean canPlaceBlock(int x, int y, int z) {
        switch (world.getBlockAt(x, y, z).getType()) {
        case AIR:
        case MOB_SPAWNER:
        case CHEST:
        case WATER:
        case STATIONARY_WATER:
        case LAVA:
        case STATIONARY_LAVA:
            return false;
        default:
            return true;
        }
    }

}
