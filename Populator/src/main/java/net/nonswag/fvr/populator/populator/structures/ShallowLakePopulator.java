package net.nonswag.fvr.populator.populator.structures;

import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.populator.WorldFiller;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class ShallowLakePopulator extends BlockPopulator {
    private final WorldFiller filler;

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextInt(5) > 1) return;
        int rx = (chunk.getX() << 4) + random.nextInt(16);
        int rz = (chunk.getZ() << 4) + random.nextInt(16);
        if (world.getHighestBlockYAt(rx, rz) <= 4) return;
        int radius = random.nextInt(4) + 8;
        int y = world.getHighestBlockYAt(rx, rz) - 1;
        int[] radiusMods = new int[4];
        for (int i = 0; i < 4; i++) radiusMods[i] = random.nextInt(3) - 1;
        List<Location> lakeBlocks = new ArrayList<>();
        Vector center = new BlockVector(rx, y, rz);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int mod = (x < 0 && z < 0) ? radiusMods[0] : (x < 0 && z > 0) ? radiusMods[1] : (x > 0 && z < 0) ? radiusMods[2] : radiusMods[3];
                Vector position = center.clone().add(new Vector(x, 0, z));
                if (!(center.distance(position) <= radius + 0.5 + mod)) continue;
                lakeBlocks.add(world.getHighestBlockAt(position.toLocation(world)).getLocation().subtract(0, 1, 0));
            }
        }

        int lowest = 128;
        for (Location location : lakeBlocks) {
            if (location.getY() < lowest && location.getY() >= 4) lowest = (int) location.getY();
        }
        for (Location location : lakeBlocks) location.setY(lowest);
        for (Location loc : lakeBlocks) {
            Block block = loc.getBlock();
            if (!filler.contains(block.getX(), block.getZ())) continue;
            block.setType(Material.WATER);
            block.getRelative(0, -1, 0).setType(Material.STONE);
            block.getRelative(0, 1, 0).setType(Material.AIR);
        }
    }
}
