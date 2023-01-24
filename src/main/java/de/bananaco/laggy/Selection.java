package de.bananaco.laggy;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Selection {

    int minX, minY, minZ;
    int maxX, maxY, maxZ;

    boolean a = false;
    boolean b = false;

    private final String world;

    public Selection(String world) {
        this.world = world;
    }

    public void setPointA(int x, int y, int z) {
        minX = x;
        minY = y;
        minZ = z;
        a = true;
        process();
    }

    public void setPointB(int x, int y, int z) {
        maxX = x;
        maxY = y;
        maxZ = z;
        b = true;
        process();
    }

    private void process() {
        if (isValid()) {
            if (minX > maxX) {
                int min = minX;
                int max = maxX;
                minX = max;
                maxX = min;
            }
            if (minY > maxY) {
                int min = minY;
                int max = maxY;
                minY = max;
                maxY = min;
            }
            if (minZ > maxZ) {
                int min = minZ;
                int max = maxZ;
                minZ = max;
                maxZ = min;
            }
        }
    }

    public Selection getCopy() {
        Selection s = new Selection(world);
        s.minX = minX;
        s.minY = minY;
        s.minZ = minZ;

        s.maxX = maxX;
        s.maxY = maxY;
        s.maxZ = maxZ;

        s.a = a;
        s.b = b;
        return s;
    }

    public boolean isValid() {
        return (a == true && b == true);
    }

    public boolean contains(Player player) {
        return contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x < maxX + 1 && y >= minY && y < maxY + 1 && z >= minZ && z < maxZ + 1;
    }

    public String getWorld() {
        return world;
    }

    public int[] getPointA() {
        return new int[] { minX, minY, minZ };
    }

    public int[] getPointB() {
        return new int[] { maxX, maxY, maxZ };
    }

    public static Selection getFromString(String data) throws ParseException {
        JSONObject dataJSON = (JSONObject) new JSONParser().parse(data);
        JSONObject min = (JSONObject) dataJSON.get("min");
        JSONObject max = (JSONObject) dataJSON.get("max");
        String world = (String) dataJSON.get("world");
        int minX = Integer.parseInt((String) min.get("x"));
        int minY = Integer.parseInt((String) min.get("y"));
        int minZ = Integer.parseInt((String) min.get("z"));
        int maxX = Integer.parseInt((String) max.get("x"));
        int maxY = Integer.parseInt((String) max.get("y"));
        int maxZ = Integer.parseInt((String) max.get("z"));
        Selection selection = new Selection(world);
        selection.setPointA(minX, minY, minZ);
        selection.setPointB(maxX, maxY, maxZ);
        return selection;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getDataJSON() {
        JSONObject dataJSON = new JSONObject();
        JSONObject min = new JSONObject();
        JSONObject max = new JSONObject();
        // write the data in
        min.put("x", String.valueOf(minX));
        min.put("y", String.valueOf(minY));
        min.put("z", String.valueOf(minZ));
        max.put("x", String.valueOf(maxX));
        max.put("y", String.valueOf(maxY));
        max.put("z", String.valueOf(maxZ));
        dataJSON.put("min", min);
        dataJSON.put("max", max);
        dataJSON.put("world", world);
        return dataJSON;
    }

    @Override
    public String toString() {
        return getDataJSON().toJSONString();
    }

}