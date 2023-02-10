package net.nonswag.fvr.walls.api;

import net.nonswag.fvr.walls.Walls;
import org.bukkit.Material;
import org.bukkit.World;
import org.json.simple.JSONObject;

public class Selection {

    int minX, minY, minZ;
    int maxX, maxY, maxZ;
    int type = 0;
    boolean a = false;
    boolean b = false;
    private final String world;

    public Selection(String world) {
        this.world = world;
    }

    public void setPointA(int x, int y, int z) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.a = true;
        this.process();
    }

    public void setPointB(int x, int y, int z) {
        this.maxX = x;
        this.maxY = y;
        this.maxZ = z;
        this.b = true;
        this.process();
    }

    public void setType(int the_type) {
        this.type = the_type;
    }

    private void process() {
        if (this.isValid()) {
            if (this.minX > this.maxX) {
                final int min = this.minX;
                this.minX = this.maxX;
                this.maxX = min;
            }
            if (this.minY > this.maxY) {
                final int min = this.minY;
                this.minY = this.maxY;
                this.maxY = min;
            }
            if (this.minZ > this.maxZ) {
                final int min = this.minZ;
                this.minZ = this.maxZ;
                this.maxZ = min;
            }
        }
    }

    public boolean isValid() {
        return this.a && this.b;
    }

    public boolean contains(int x, int y, int z) {
        return (x >= this.minX) && (x < (this.maxX + 1)) && (y >= this.minY) && (y < (this.maxY + 1)) && (z >= this.minZ) && (z < (this.maxZ + 1));
    }

    public boolean containsType(int x, int y, int z, int aType) {
        return (x >= this.minX) && (x < (this.maxX + 1)) && (y >= this.minY) && (y < (this.maxY + 1)) && (z >= this.minZ) && (z < (this.maxZ + 1) && this.type == aType);
    }

    public void remove(World the_world) {
        for (int x_operate = this.minX; x_operate <= this.maxX; x_operate++) {
            for (int y_operate = this.minY; y_operate <= this.maxY; y_operate++) {
                for (int z_operate = this.minZ; z_operate <= this.maxZ; z_operate++) {
                    the_world.getBlockAt(x_operate, y_operate, z_operate).setType(Material.AIR);
                }
            }
        }
    }

    public int getType() {
        return this.type;
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