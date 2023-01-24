package me.glennEboy.Walls.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
                final int max = this.maxX;
                this.minX = max;
                this.maxX = min;
            }
            if (this.minY > this.maxY) {
                final int min = this.minY;
                final int max = this.maxY;
                this.minY = max;
                this.maxY = min;
            }
            if (this.minZ > this.maxZ) {
                final int min = this.minZ;
                final int max = this.maxZ;
                this.minZ = max;
                this.maxZ = min;
            }
        }
    }

    public Selection getCopy() {
        final Selection s = new Selection(this.world);
        s.minX = this.minX;
        s.minY = this.minY;
        s.minZ = this.minZ;

        s.maxX = this.maxX;
        s.maxY = this.maxY;
        s.maxZ = this.maxZ;

        s.a = this.a;
        s.b = this.b;
        return s;
    }

    public boolean isValid() {
        return this.a && this.b;
    }
    
    public boolean containsType(Player player, int type){
        return this.containsType(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), type);        
    }

    public boolean contains(Player player) {
        return this.contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
    }

    public boolean contains(int x, int z) {
        return (x >= this.minX) && (x < (this.maxX + 1)) && (z >= this.minZ) && (z < (this.maxZ + 1));
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

    public List<BlockState> getBlockStates(World the_world, int y) {
        final List<BlockState> list = new ArrayList<BlockState>();
        for (int x_operate = this.minX; x_operate <= this.maxX; x_operate++) {
            for (int z_operate = this.minZ; z_operate <= this.maxZ; z_operate++) {
                list.add(the_world.getBlockAt(x_operate, y, z_operate).getState());
            }
        }
        return list;
    }

    public String getWorld() {
        return this.world;
    }

    public int getType() {
        return this.type;
    }

    public int[] getPointA() {
        return new int[] { this.minX, this.minY, this.minZ };
    }

    public int[] getPointB() {
        return new int[] { this.maxX, this.maxY, this.maxZ };
    }

    public static Selection getFromString(String data) throws ParseException {
        final JSONObject dataJSON = (JSONObject) new JSONParser().parse(data);
        final JSONObject min = (JSONObject) dataJSON.get("min");
        final JSONObject max = (JSONObject) dataJSON.get("max");
        final String world = (String) dataJSON.get("world");
        final int minX = Integer.parseInt((String) min.get("x"));
        final int minY = Integer.parseInt((String) min.get("y"));
        final int minZ = Integer.parseInt((String) min.get("z"));
        final int maxX = Integer.parseInt((String) max.get("x"));
        final int maxY = Integer.parseInt((String) max.get("y"));
        final int maxZ = Integer.parseInt((String) max.get("z"));
        final Selection selection = new Selection(world);
        selection.setPointA(minX, minY, minZ);
        selection.setPointB(maxX, maxY, maxZ);
        return selection;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getDataJSON() {
        final JSONObject dataJSON = new JSONObject();
        final JSONObject min = new JSONObject();
        final JSONObject max = new JSONObject();
        // write the data in
        min.put("x", String.valueOf(this.minX));
        min.put("y", String.valueOf(this.minY));
        min.put("z", String.valueOf(this.minZ));
        max.put("x", String.valueOf(this.maxX));
        max.put("y", String.valueOf(this.maxY));
        max.put("z", String.valueOf(this.maxZ));
        dataJSON.put("min", min);
        dataJSON.put("max", max);
        dataJSON.put("world", this.world);
        return dataJSON;
    }

    @Override
    public String toString() {
        return this.getDataJSON().toJSONString();
    }
}