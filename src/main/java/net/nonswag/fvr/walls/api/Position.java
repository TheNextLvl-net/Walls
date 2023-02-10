package net.nonswag.fvr.walls.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Position {
    private String world;
    private int x, y, z;

    public static Position of(Location location) {
        return new Position(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
