package net.nonswag.fvr.walls.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import org.bukkit.Material;
import org.bukkit.World;
import org.json.simple.JSONObject;

@Getter
@RequiredArgsConstructor
public class Selection {
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;
    private boolean aValid, bValid;
    private final String world;
    private final boolean walls;

    public void setMinPoint(int x, int y, int z) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.aValid = true;
        this.process();
    }

    public void setMaxPoint(int x, int y, int z) {
        this.maxX = x;
        this.maxY = y;
        this.maxZ = z;
        this.bValid = true;
        this.process();
    }

    private void process() {
        if (!isValid()) return;
        if (this.minX > this.maxX) {
            final int min = this.minX;
            this.minX = this.maxX;
            this.maxX = min;
        }
        if (this.minY > this.maxY) {
            int min = this.minY;
            this.minY = this.maxY;
            this.maxY = min;
        }
        if (this.minZ > this.maxZ) {
            final int min = this.minZ;
            this.minZ = this.maxZ;
            this.maxZ = min;
        }
    }

    public boolean isValid() {
        return isAValid() && isBValid();
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean contains(int x, int y, int z, boolean walls) {
        return this.walls == walls && contains(x, y, z);
    }

    public void remove(World world) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public JSONObject getDataJSON() {
        final JSONObject dataJSON = new JSONObject();
        final JSONObject min = new JSONObject();
        final JSONObject max = new JSONObject();
        min.put("x", String.valueOf(this.minX));
        min.put("y", String.valueOf(this.minY));
        min.put("z", String.valueOf(this.minZ));
        max.put("x", String.valueOf(this.maxX));
        max.put("y", String.valueOf(this.maxY));
        max.put("z", String.valueOf(this.maxZ));
        dataJSON.put("min", min);
        dataJSON.put("max", max);
        dataJSON.put(Walls.levelName, this.world);
        return dataJSON;
    }

    @Override
    public String toString() {
        return this.getDataJSON().toJSONString();
    }
}