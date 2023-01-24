package me.glennEboy.Walls.utils;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.PlayerState;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;


public class FireWorks {

	
	public static void spawnFireworksForPlayers(final TheWalls myWalls){
        myWalls.getServer().getScheduler().scheduleSyncDelayedTask(myWalls, new Runnable() {

            @Override
            public void run() {
            	List<UUID> winningTeam = myWalls.getTeamList(PlayerState.values()[myWalls.getWinningTeam()]);

            	// Spawn the Fireworks for winners

            	for (UUID pUID : winningTeam){
            		
            		try{
	                    Firework fw = (Firework) Bukkit.getPlayer(pUID).getWorld().spawnEntity(new Location(Bukkit.getPlayer(pUID).getWorld(), 
	                    		Bukkit.getPlayer(pUID).getLocation().getBlockX(), Bukkit.getPlayer(pUID).getLocation().getBlockY(), 
	                    		Bukkit.getPlayer(pUID).getLocation().getBlockZ()), EntityType.FIREWORK);
	                    FireworkMeta fwm = fw.getFireworkMeta();
	
	                    Random r = new Random();
	                    Type type = Type.STAR;
	
	                    Color c1 = Color.AQUA;
	                    Color c2 = Color.WHITE;
	
	                    FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();
	
	                    fwm.addEffect(effect);
	
	                    // Generate some random power and set it
	                    int rp = r.nextInt(2) + 1;
	                    fwm.setPower(rp);
	
	                    fw.setFireworkMeta(fwm);
            		}catch (Exception e){
            			
            		}
            	}

            }
        }, 20L * 2);

	}
}
