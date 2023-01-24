package de.bananaco.laggy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import de.bananaco.laggy.populator.CavePopulator;
import de.bananaco.laggy.populator.MineShaftPopulator;
import de.bananaco.laggy.populator.MobPopulator;
import de.bananaco.laggy.populator.OrePopulator;
import de.bananaco.laggy.populator.TrapPopulator;
import de.bananaco.laggy.populator.DungeonPopulator;

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

    protected final Random rand;

    private List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
    
    protected MobPopulator mobs;

    //protected final EditSession session;

    public static final int WATER_LEVEL = 64;

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

        this.rand = Walls.GLOBAL_RANDOM;
        
        addPopulator(new OrePopulator(this));
        addPopulator(new CavePopulator(this));
        addPopulator(new MineShaftPopulator(this));
        addPopulator(new DungeonPopulator());
        addPopulator(new TrapPopulator(this));
        mobs = new MobPopulator(this);
        addPopulator(mobs);
        //session = new EditSession(new BukkitWorld(Bukkit.getServer().getWorlds().get(0)), Short.MAX_VALUE);
        //session.setFastMode(true);
        //session.enableQueue();
        //session.enableQueue();
    }

    public boolean contains(int x, int z) {
        return x >= minX && x < maxX + 1 && z >= minZ && z < maxZ + 1;
    }

    public void addPopulator(BlockPopulator pop) {
        addPopulator(pop, false);
    }
    
    public void removePopulator(Class<? extends BlockPopulator> populatorClass)
    {
        Stack<BlockPopulator> toRemove = new Stack<BlockPopulator>();
        for(BlockPopulator populator : populators)
        {
            if(populator.getClass().equals(populatorClass))
                toRemove.push(populator);
        }
        populators.removeAll(toRemove);
    }
    
    public void addPopulator(BlockPopulator pop, boolean front) {
        if(front)
            populators.add(0, pop);
        else
            populators.add(pop);
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
        for(BlockPopulator pop : populators)
        {
            for(Chunk chunk : chunks)
            {
                for(int x = 0; x < 16; x++)
                {
                    for(int z = 0; z < 16; z++)
                    {
                        world.setBiome(chunk.getX() * 16 + x, chunk.getZ() * 16 + z, biome);
                    }
                }
                pop.populate(world, rand, chunk);
            }
        }
    }

    public abstract void generate();
}
