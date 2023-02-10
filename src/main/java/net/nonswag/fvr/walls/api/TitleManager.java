package net.nonswag.fvr.walls.api;


import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

public class TitleManager {

    public static void sendTitle(Player p, String title, String subtitle) {
        p.sendTitle(new Title(title, subtitle));
    }
}
