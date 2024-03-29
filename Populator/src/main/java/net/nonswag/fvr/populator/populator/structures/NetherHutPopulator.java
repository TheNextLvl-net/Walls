package net.nonswag.fvr.populator.populator.structures;

import net.nonswag.fvr.populator.Utils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.Random;

public class NetherHutPopulator extends BlockPopulator {

    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(16) != 0) return;
        int x = random.nextInt(5) + 5;
        int z = random.nextInt(5) + 5;
        generateStructure(random, chunk, x, Utils.getHighestBlock(chunk, x, z).getY(), z);
    }

    @SuppressWarnings("deprecation")
    private void generateStructure(Random random, Chunk chunk, int x, int y, int z) {
        if ((x <= 10) && (z <= 10) &&
                (x >= 5) && (z >= 5) &&
                (y >= 14)) {
            for (int x2 = 0; x2 < 3; x2++) {
                for (int z2 = 0; z2 < 3; z2++) {
                    for (int y2 = 0; y2 >= -3; y2--) {
                        chunk.getBlock(x + x2, y + 1 + y2, z + z2).setType(Material.NETHER_BRICK);
                    }
                }
            }
            for (int z2 = 0; z2 < 3; z2++) {
                for (int y2 = 1; y2 < 4; y2++) {
                    chunk.getBlock(x, y + 1 + y2, z + z2).setType(Material.NETHER_BRICK);
                    if ((z2 == 1) && (y2 == 2)) chunk.getBlock(x, y + 1 + y2, z + z2).setType(Material.NETHER_FENCE);
                    if (y2 != 3) continue;
                    chunk.getBlock(x - 1, y + 1 + y2, z + z2).setTypeIdAndData(Material.NETHER_BRICK_STAIRS.getId(), (byte) 0, false);
                }
            }
            for (int z2 = 0; z2 < 3; z2++) {
                for (int y2 = 1; y2 < 4; y2++) {
                    chunk.getBlock(x + 2, y + 1 + y2, z + z2).setType(Material.NETHER_BRICK);
                    if ((z2 == 1) && (y2 == 2))
                        chunk.getBlock(x + 2, y + 1 + y2, z + z2).setType(Material.NETHER_FENCE);
                    if (y2 != 3) continue;
                    chunk.getBlock(x + 3, y + 1 + y2, z + z2).setTypeIdAndData(Material.NETHER_BRICK_STAIRS.getId(), (byte) 1, false);
                }
            }
            for (int x2 = 0; x2 < 3; x2++) {
                for (int y2 = 1; y2 < 4; y2++) {
                    chunk.getBlock(x + 1, y + 1 + y2, z).setType(y2 == 2 ? Material.NETHER_FENCE : Material.NETHER_BRICK);
                    if (y2 != 3) continue;
                    chunk.getBlock(x + x2, y + 1 + y2, z - 1).setTypeIdAndData(Material.NETHER_BRICK_STAIRS.getId(), (byte) 2, false);
                }
            }
            chunk.getBlock(x + 1, y + 4, z + 2).setType(Material.NETHER_BRICK);
            chunk.getBlock(x + 1, y + 2, z + 2).setTypeIdAndData(Material.NETHER_FENCE.getId(), (byte) 3, false);
            for (int x2 = 0; x2 < 3; x2++) {
                for (int y2 = 1; y2 < 4; y2++) {
                    if (y2 != 3) continue;
                    chunk.getBlock(x + x2, y + 1 + y2, z + 3).setTypeIdAndData(Material.NETHER_BRICK_STAIRS.getId(), (byte) 3, false);
                }
            }
            chunk.getBlock(x + 1, y + 4, z + 1).setType(Material.GLOWSTONE);
            for (int x2 = 0; x2 < 3; x2++)
                for (int z2 = 0; z2 < 3; z2++)
                    chunk.getBlock(x + x2, y + 5, z + z2).setTypeIdAndData(Material.NETHER_FENCE.getId(), (byte) 2, false);
            chunk.load();
            if (random.nextInt(3) == 0) {
                chunk.getBlock(x + 1, y + 1, z + 1).setType(Material.CHEST);
            } else if (random.nextBoolean()) {
                chunk.getBlock(x + 1, y + 1, z + 1).setType(Material.TRAPPED_CHEST);
                if (random.nextBoolean())
                    chunk.getBlock(x + 1, y, z + 1).setType(Material.TNT);
                else {
                    Block block = chunk.getBlock(x + 1, y, z + 1);
                    block.setType(Material.DISPENSER);
                    Dispenser dispenser = (Dispenser) block.getState();
                    dispenser.getInventory().addItem((random.nextBoolean() ? new Potion(PotionType.INSTANT_DAMAGE, 2).splash().toItemStack(1) :
                            random.nextBoolean() ? new Potion(PotionType.POISON, 2).extend().splash().toItemStack(1) :
                                    new Potion(PotionType.SPEED, 1).extend().extend().extend().splash().toItemStack(1)));
                }
                if (random.nextInt(3) == 0)
                    chunk.getBlock(x + 1, y - 2, z + 1).setType(Material.CHEST);
            }
        }
    }
}
