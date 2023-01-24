package de.bananaco.laggy;

import java.util.Random;

import me.simplex.tropic.populators.Populator_Bush;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import de.bananaco.laggy.populator.BarrenTreePopulator;
import de.bananaco.laggy.populator.HutPopulator;
import de.bananaco.laggy.populator.OrePopulator;
import de.bananaco.laggy.populator.ShrubberyPopulator;
import de.bananaco.laggy.populator.WildGrassPopulator;

public class WastelandBiome extends WorldFiller {
	byte bedrock = (byte) Material.BEDROCK.getId();
    byte stone = (byte) Material.STONE.getId();
    byte dirt = (byte) Material.DIRT.getId();
    byte grass = (byte) Material.GRASS.getId();
    byte water = (byte) Material.STATIONARY_WATER.getId();
    byte sand = (byte) Material.SAND.getId();

    public WastelandBiome(World world, int minX, int minZ, int maxX, int maxZ, int startY, int groundLevel) {
        super(world, Biome.DESERT, minX, minZ, maxX, maxZ, startY, groundLevel);

        BlockPopulator hut = new HutPopulator();
        BlockPopulator bush = new Populator_Bush(3);
        BlockPopulator grass = new WildGrassPopulator((byte) 0);
        BlockPopulator barren = new BarrenTreePopulator();
        BlockPopulator shrubbery = new ShrubberyPopulator();
        this.addPopulator(hut, true);
        this.addPopulator(bush);
        this.addPopulator(bush);
        this.addPopulator(grass);
        this.addPopulator(grass);
        this.addPopulator(shrubbery);
        this.addPopulator(barren);
        //Replace default ore populator with wasteland specific populator
        this.removePopulator(OrePopulator.class);
        this.addPopulator(new OrePopulator(this, true));
    }

    @Override
    public void generate() {
    	Random seed = this.rand;
        SimplexOctaveGenerator g = new SimplexOctaveGenerator(seed, 8);
        PerlinOctaveGenerator g2 = new PerlinOctaveGenerator(seed, 8);
        g.setScale(1/32d);
        g2.setScale(1/32d);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                // current distance between the two
                double abs = (Math.abs(centerX - x) + Math.abs(centerZ - z)) / 2;
                if(abs < 20)
                	abs = 0;
                else
                	abs -= 20;
                

                //double diff = tDis-abs;
                double noise = g.noise(x, z, 0.35D, 0.65D) * 2D;

                int highest = (int) (groundLevel + noise);
                for (int i = 0; i < abs; i++) {
                    highest = (highest * 9 + groundLevel) / 10;
                }
                double n2 = g2.noise(x, z, 0.5D, 0.5D);
                if(n2 > 0 && n2 < .05)
                	highest += rand.nextInt(2) + 2;
                else if(n2 >= .05 && n2 < .1)
                	highest += rand.nextInt(2) + 5;
                else if(n2 >= .1 && n2 < .15)
                	highest += rand.nextInt(2) + 8;
                else if(n2 >= .15 && n2 < .2)
                	highest += rand.nextInt(2) + 4;
                else if(n2 >= .2 && n2 < .25)
                	highest += rand.nextInt(2) + 3;
                
                for (int y = startY; y < highest - 3 && y < world.getMaxHeight(); y++) {
                    world.getBlockAt(x, y, z).setTypeId(stone);
                }
                for (int y = highest - 3; y < highest && y < world.getMaxHeight(); y++) {
                	if(g2.noise(x, z, 0.5D, 0.5D) > 0)
                		world.getBlockAt(x, y, z).setTypeId(stone);
                	else
                		world.getBlockAt(x, y, z).setTypeId(dirt);
                }

                Block b = world.getBlockAt(x, highest, z);
                if(g2.noise(x, z, 0.5D, 0.5D) > 0)
                	b.setTypeId(stone);
                else
                	b.setTypeId(grass);
            }
        }
    }
}