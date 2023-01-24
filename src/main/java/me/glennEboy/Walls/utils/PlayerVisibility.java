package me.glennEboy.Walls.utils;

import java.util.UUID;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.TheWalls.PlayerState;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class PlayerVisibility {

    
    
    
    public static void makeSpecInvis(TheWalls myWalls, Player spec) {
        for (int i = 1; i< 5; i++){
            PlayerState ps = PlayerState.values()[i];
            for (UUID fighter : myWalls.getTeamList(ps)) {
                try{
                    Bukkit.getPlayer(fighter).hidePlayer(spec);
                }catch(Exception e){
                    
                }
            }
        }
    }

//    public void makeSpecVis(TheWalls myWalls, Player spec) {
//        // make any specs visible again..
//        for (int i = 0; i < this.plugin.getServer().getOnlinePlayers().size(); i++) {
//            Player pl = (Player) this.plugin.getServer().getOnlinePlayers().toArray()[i];
//            pl.showPlayer(spec);
//        }
//    }

    public static void makeSpecVisToSpecs(TheWalls myWalls, Player spec) {
        // make any specs visible again..
        for (UUID specTeam : myWalls.getTeamList(PlayerState.SPEC)) {
            try{
                Bukkit.getPlayer(specTeam).showPlayer(spec);
                spec.showPlayer(Bukkit.getPlayer(specTeam));
            }catch(Exception e){
                
            }
        }
    }

    public static void hideAllSpecs(TheWalls myWalls, Player player) {
        for (UUID specTeam : myWalls.getTeamList(PlayerState.SPEC)) {
            try{
                player.hidePlayer(Bukkit.getPlayer(specTeam));
            }catch(Exception e){
                
            }
        }
    }
    
    public static void makeInVisPlayerNowVisible(TheWalls myWalls, Player wasInvis) {
        // make all players visible again..
        for (Player allPlayers : myWalls.getServer().getOnlinePlayers()) {
            try{
                allPlayers.showPlayer(wasInvis);
            }catch(Exception e){
                
            }
        }
    }

}
