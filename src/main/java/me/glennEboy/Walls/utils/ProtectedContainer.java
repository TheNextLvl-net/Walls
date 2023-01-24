package me.glennEboy.Walls.utils;

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
        if ((a != null) && a.equals(this.location)) {
            return true;
        }
        if ((b != null) && b.equals(this.location)) {
            return true;
        }
        return false;
    }
}
