package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.structures.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class DesertBiome extends WorldFiller {

    public DesertBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.DESERT, minX, minZ, maxX, maxZ, startY, groundLevel);

        OasisPopulator oasis = new OasisPopulator(this);
        BlockPopulator treepop = new TreePopulator(TreePopulator.Type.SAVANNA);
        BlockPopulator wildgrass = new WildGrassPopulator((byte) 0);
        BlockPopulator flowers = new FlowerPopulator();
        BlockPopulator gravel = new GravelStack();
        BlockPopulator desert = new DesertPopulator();

        this.addPopulator(treepop);
        this.addPopulator(wildgrass);
        this.addPopulator(flowers);
        this.addPopulator(gravel);
        this.addPopulator(desert);
        this.addPopulator(oasis);
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(this.random, 8);
        g.setScale(1 / 48d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                abs = abs < 20 ? 0 : abs - 20;
                double noise = g.noise(x, z, 0.35D, 0.65D) * 4.5D;
                int highest = (int) (groundLevel + noise);
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y <= highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.SAND);
                }
                Block b = world.getBlockAt(x, highest - 4, z);
                b.setType(Material.SANDSTONE);
            }
        }
    }
}
