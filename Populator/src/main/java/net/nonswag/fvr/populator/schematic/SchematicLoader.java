package net.nonswag.fvr.populator.schematic;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SchematicLoader {
    private final EditSession session;
    private final File file = new File("plugins/WallsPopulator/schematics");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public SchematicLoader() {
        World world = new BukkitWorld(Bukkit.getWorlds().get(0));
        (session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, Integer.MAX_VALUE)).enableQueue();
        if (!file.exists()) file.mkdirs();
    }

    public List<String> getSchematics(String key) {
        File[] files = file.listFiles((dir, name) -> name.endsWith(".schematic") && name.contains(key));
        List<String> names = new ArrayList<>();
        if (files == null) return names;
        for (File file : files) names.add(file.getName().replace(".schematic", ""));
        if (names.isEmpty()) System.err.println("there are files missing: " + key);
        return names;
    }

    @SuppressWarnings("deprecation")
    public void paste(String name, Location origin) {
        SchematicFormat format = SchematicFormat.MCEDIT;
        try {
            File file = new File(this.file, name + ".schematic");
            if (!file.exists()) throw new FileNotFoundException("Could not find " + file.getAbsolutePath());
            CuboidClipboard loaded = format.load(file);
            Vector start = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
            loaded.place(session, start, false);
            session.flushQueue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}