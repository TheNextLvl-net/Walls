package me.glennEboy.Walls.utils;

import java.util.Random;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.PlayerState;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class VictoryFireworks {

	public static void setOffVictorFireworks(final TheWalls plugin){
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                for (final Player player : plugin.getServer().getOnlinePlayers()) {
                    // Spawn the Fireworks for winners
                    if (plugin.getWallsPlayer(player.getUniqueId()).playerState != PlayerState.SPEC) {

                        Firework fw = (Firework) player.getWorld().spawnEntity(new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()), EntityType.FIREWORK);
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

                    }

                }
            }
        }, 20L * 2);

	}
}
