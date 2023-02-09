package net.nonswag.fvr.walls.utils;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class Fireworks {

    public static void spawnFireworksForPlayers(Walls walls) {
        Random random = ThreadLocalRandom.current();
        Bukkit.getScheduler().scheduleSyncDelayedTask(walls, () -> {
            for (UUID uuid : walls.getTeamList(PlayerState.values()[walls.getWinningTeam()])) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                Firework firework = (Firework) player.getWorld().spawnEntity(new Location(player.getWorld(),
                        player.getLocation().getBlockX(), player.getLocation().getBlockY(),
                        player.getLocation().getBlockZ()), EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();
                FireworkEffect effect = FireworkEffect.builder().flicker(random.nextBoolean()).with(FireworkEffect.Type.STAR).
                        withColor(Color.AQUA).withFade(Color.WHITE).trail(random.nextBoolean()).build();
                meta.addEffect(effect);
                meta.setPower(random.nextInt(2) + 1);
                firework.setFireworkMeta(meta);
            }
        }, 20L * 2);
    }
}
