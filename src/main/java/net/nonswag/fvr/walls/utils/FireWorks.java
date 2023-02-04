package net.nonswag.fvr.walls.utils;

import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.List;
import java.util.Random;
import java.util.UUID;


public class FireWorks {


    public static void spawnFireworksForPlayers(final Walls myWalls) {
        myWalls.getServer().getScheduler().scheduleSyncDelayedTask(myWalls, () -> {
            List<UUID> winningTeam = myWalls.getTeamList(PlayerState.values()[myWalls.getWinningTeam()]);

            // Spawn the Fireworks for winners

            for (UUID pUID : winningTeam) {

                Player player = Bukkit.getPlayer(pUID);
                if (player == null) continue;
                Firework fw = (Firework) player.getWorld().spawnEntity(new Location(player.getWorld(),
                        player.getLocation().getBlockX(), player.getLocation().getBlockY(),
                        player.getLocation().getBlockZ()), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                Random r = new Random();
                Type type = Type.STAR;

                Color c1 = Color.AQUA;
                Color c2 = Color.WHITE;

                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                fwm.addEffect(effect);

                int rp = r.nextInt(2) + 1;
                fwm.setPower(rp);

                fw.setFireworkMeta(fwm);
            }
        }, 20L * 2);

    }
}
