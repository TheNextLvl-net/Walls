package net.nonswag.fvr.walls.listeners;

import lombok.RequiredArgsConstructor;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import net.nonswag.fvr.walls.api.Position;
import net.nonswag.fvr.walls.api.signs.BiomeSign;
import net.nonswag.fvr.walls.api.signs.StatSign;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class SignListener implements Listener {
    private final Walls walls;

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().isOp()) return;
        if (event.getLine(0).equalsIgnoreCase("[stats]")) statSign(event);
        else if (event.getLine(0).equalsIgnoreCase("[biome]")) biomeSign(event);
    }

    private void statSign(SignChangeEvent event) {
        String statName = event.getLine(1).replace(" ", "_");
        try {
            Walls.Sort stat = Walls.Sort.valueOf(statName.toUpperCase());
            Position position = Position.of(event.getBlock().getLocation());
            Walls.STAT_SIGNS.getRoot().getSigns().removeIf(sign -> sign.getPosition().equals(position));
            Walls.STAT_SIGNS.getRoot().getSigns().add(new StatSign(position, stat));
            Notifier.success(event.getPlayer(), "Added a new stat sign for §6" + stat.name().replace("_", " ").toLowerCase());
            Bukkit.getScheduler().runTaskLater(walls, Walls::updateStatSigns, 1);
        } catch (IllegalArgumentException e) {
            if (!statName.isEmpty()) {
                Notifier.error(event.getPlayer(), String.format("§4%s§c is not a valid option", statName));
                List<String> stats = Arrays.stream(Walls.Sort.values()).map(stat -> stat.name().replace("_", " ").toLowerCase()).collect(Collectors.toList());
                Notifier.notify(event.getPlayer(), "§7Options§8: §a" + String.join("§8, §a", stats));
            } else Notifier.error(event.getPlayer(), "Please enter the corresponding stat in line 2");
        }
    }

    private void biomeSign(SignChangeEvent event) {
        String teamName = event.getLine(1).trim();
        try {
            Walls.Team team = Walls.Team.valueOf(teamName.toUpperCase());
            if (team.equals(Walls.Team.SPECTATORS)) throw new IllegalArgumentException();
            Position position = Position.of(event.getBlock().getLocation());
            Walls.BIOME_SIGNS.getRoot().getSigns().removeIf(sign -> sign.getPosition().equals(position));
            Walls.BIOME_SIGNS.getRoot().getSigns().add(new BiomeSign(position, team));
            Notifier.success(event.getPlayer(), "Added a new biome sign for team §6" + team.name().toLowerCase());
            Bukkit.getScheduler().runTaskLater(walls, Walls::updateBiomeSigns, 1);
        } catch (IllegalArgumentException e) {
            if (!teamName.isEmpty()) {
                Notifier.error(event.getPlayer(), String.format("§4%s§c is not a valid option", teamName));
                List<String> teams = Arrays.stream(Walls.Team.values()).filter(team -> !team.equals(Walls.Team.SPECTATORS)).
                        map(team -> team.name().toLowerCase()).collect(Collectors.toList());
                Notifier.notify(event.getPlayer(), "§7Options§8: §a" + String.join("§8, §a", teams));
            } else Notifier.error(event.getPlayer(), "Please enter the corresponding team in line 2");
        }
    }
}
