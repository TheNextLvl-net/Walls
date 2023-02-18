package net.nonswag.fvr.populator.populator.biomes;

import net.nonswag.fvr.populator.Populator;
import net.nonswag.fvr.populator.WorldFiller;
import net.nonswag.fvr.populator.populator.structures.GravelStackPopulator;
import net.nonswag.fvr.populator.populator.structures.TreePopulator;
import net.nonswag.fvr.populator.schematic.SchematicLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.List;

public class RuinCityBiome extends WorldFiller {
    private final SchematicLoader loader = new SchematicLoader();
    private final List<String> options = loader.getSchematics("_ruin_");

    public RuinCityBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.BIRCH_FOREST_HILLS, minX, minZ, maxX, maxZ, startY, groundLevel);
        this.addPopulator(new TreePopulator(TreePopulator.Type.FOREST));
        this.addPopulator(new GravelStackPopulator());
    }

    @Override
    public void generate() {
        SimplexOctaveGenerator simplex = new SimplexOctaveGenerator(Populator.RANDOM, 8);
        PerlinOctaveGenerator perlin = new PerlinOctaveGenerator(Populator.RANDOM, 8);
        simplex.setScale(1 / 188d);
        perlin.setScale(1 / 15d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double abs = 10.0 + (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2d;
                if (abs < 20) abs -= 10;
                else if (abs < 30) abs = 20;
                else abs -= 10;
                if (abs < 5) abs = 5;
                double n1 = simplex.noise(x, z, 0.45D, 0.7D) * 3.3D;
                double n2 = perlin.noise(x, z, 0.75D, 0.6D) * 6.5D;
                double noise = (Math.abs(n1 - n2) * groundLevel / 4);
                int highest = (int) (groundLevel / 2 + noise);
                for (int i = 0; i < abs; i++) highest = (highest * 9 + groundLevel) / 10;
                if (n1 > 2) highest -= 5;
                if (n2 > 3) highest -= 3;
                if (highest >= world.getMaxHeight()) highest = world.getMaxHeight() - 1;
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
                Block b = world.getBlockAt(x, highest, z);
                b.setType(Material.GRASS);
            }
        }
        pickAndPasteRuin();
    }

    private void pickAndPasteRuin() {
        if (options.isEmpty()) return;
        int xSize = this.maxX - this.minX;
        int zSize = this.maxZ - this.minZ;
        int x = this.minX + (xSize / 4);
        int z = this.minZ + (zSize / 4);
        int y = world.getHighestBlockAt(x, z).getY() - 5; // FIXME: 19.02.23 
        String chosen = options.get(Populator.RANDOM.nextInt(options.size()));
        Location location = new Location(Bukkit.getWorlds().get(0), x, y, z);
        loader.paste(chosen, location);
        chosen = options.get(Populator.RANDOM.nextInt(options.size()));
        x = this.maxX - (xSize / 4);
        z = this.maxZ - (zSize / 4);
        y = world.getHighestBlockAt(x, z).getY() - 5;
        location = new Location(Bukkit.getWorlds().get(0), x, y, z);
        loader.paste(chosen, location);
    }
}
