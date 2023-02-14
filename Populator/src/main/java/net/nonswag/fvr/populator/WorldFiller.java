package net.nonswag.fvr.populator;

import lombok.Getter;
import net.nonswag.fvr.populator.populator.biomes.WastelandBiome;
import net.nonswag.fvr.populator.populator.structures.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import java.util.ArrayList;
import java.util.List;

public abstract class WorldFiller implements Container {

    protected final World world;
    protected final Biome biome;
    protected final int minX;
    protected final int minZ;
    protected final int maxX;
    protected final int maxZ;
    protected final int startY;
    public final int groundLevel;

    protected final int centerX;
    protected final int centerZ;

    private final List<BlockPopulator> populators = new ArrayList<>();

    @Getter
    private final MobPopulator mobPopulator = new MobPopulator(this);

    public WorldFiller(World world, Biome biome, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        this.world = world;
        this.biome = biome;
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.startY = startY;
        this.groundLevel = groundLevel;

        this.centerX = (minX + maxX) / 2;
        this.centerZ = (minZ + maxZ) / 2;

        addPopulator(new OrePopulator(this, getClass().equals(WastelandBiome.class)));
        addPopulator(new CavePopulator(this));
        addPopulator(new MineShaftPopulator(this));
        addPopulator(new DungeonPopulator());
        addPopulator(new TrapPopulator(this));
        addPopulator(getMobPopulator());
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public void addPopulator(BlockPopulator pop) {
        addPopulator(pop, false);
    }

    public void addPopulator(BlockPopulator pop, boolean front) {
        if (populators.contains(pop)) new Exception().printStackTrace();
        if (front) populators.add(0, pop);
        else populators.add(pop);
    }

    public Chunk[] getChunks() {
        Location bottomLeft = new Location(world, minX, 1, minZ);
        Chunk[] chunks = new Chunk[9 * 9];
        int count = 0;
        World world = bottomLeft.getWorld();
        int cx = world.getChunkAt(bottomLeft).getX();
        int cz = world.getChunkAt(bottomLeft).getZ();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                chunks[count] = world.getChunkAt(cx + x, cz + z);
                count++;
            }
        }
        return chunks;
    }

    public void populate() {
        Chunk[] chunks = getChunks();
        populators.forEach(populator -> {
            for (Chunk chunk : chunks) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        world.setBiome(chunk.getX() * 16 + x, chunk.getZ() * 16 + z, biome);
                    }
                }
                populator.populate(world, Populator.RANDOM, chunk);
            }
        });
    }

    public abstract void generate();
}
