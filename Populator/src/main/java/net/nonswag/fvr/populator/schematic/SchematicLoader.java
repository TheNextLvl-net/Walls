package net.nonswag.fvr.populator.schematic;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SchematicLoader {

    private final JavaPlugin plugin;
    private final EditSession session;

    @SuppressWarnings("deprecation")
    public SchematicLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        session = new EditSession(new BukkitWorld(plugin.getServer().getWorlds().get(0)), Short.MAX_VALUE);
        session.enableQueue();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public List<String> getSchematics(String key) {
        File f = new File(plugin.getDataFolder().getPath() + File.separator + "schematics" + File.separator);
        if (!f.exists()) f.mkdirs();
        File[] files = f.listFiles();
        List<String> filesNames = new ArrayList<>();
        if (files == null) return filesNames;
        for (File fi : files) {
            if (fi.getName().endsWith(".schematic") && fi.getName().contains(key)) {
                filesNames.add(fi.getName().replace(".schematic", ""));
            }
        }
        if (filesNames.isEmpty()) System.err.println("there are files missing: " + key);
        return filesNames;
    }

    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
    public void paste(String name, Location origin) {
        SchematicFormat format = SchematicFormat.MCEDIT;
        try {
            File f = new File(plugin.getDataFolder().getPath() + File.separator + "schematics" + File.separator);
            if (!f.exists()) f.mkdirs();
            File file = new File(f, name + ".schematic");
            if (!file.exists()) {
                throw new FileNotFoundException("Could not find " + file.getAbsolutePath());
            } else {
                CuboidClipboard loaded = format.load(file);
                Vector start = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                loaded.place(session, start, false);
                session.flushQueue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}