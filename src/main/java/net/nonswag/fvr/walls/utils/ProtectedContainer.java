package net.nonswag.fvr.walls.utils;

import org.bukkit.Location;

public class ProtectedContainer {
    private final Location location;
    private final String owner;

    public ProtectedContainer(Location location, String owner) {
        this.location = location;
        this.owner = owner;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getOwner() {
        return this.owner;
    }

    public boolean matches(Location a, Location b) {
        return ((a != null) && a.equals(this.location)) || ((b != null) && b.equals(this.location));
    }
}
