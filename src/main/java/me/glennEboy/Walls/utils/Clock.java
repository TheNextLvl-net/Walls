package me.glennEboy.Walls.utils;


import me.glennEboy.Walls.TheWalls;

import org.bukkit.ChatColor;

public class Clock extends Thread {


    private int seconds;
    private Runnable runner;
    private final TheWalls plugin;

    public Clock(TheWalls plugin) {
        this.plugin = plugin;
        this.start();
    }

    public int getSecondsRemaining() {
        return this.seconds;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                break;
            }
            
//            PREGAME, PEACETIME, FIGHTING, FINISHED
            switch (plugin.getGameState()){
            case PREGAME:
                this.seconds--;
                if ((this.seconds <= 0) && (this.runner != null)) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, this.runner);
                    this.abort();
                }
                this.plugin.playerScoreBoard.updateClock(seconds);
                break;
            case PEACETIME:
                this.seconds--;
                if ((this.seconds % 5) == 0) {                    
                    if ((this.seconds % 60) == 0) {
                        final int min = this.seconds / 60;
                        switch (min) {
                        case 7:
                        case 5:
                        case 1:
                            GameNotifications.broadcastMessage(ChatColor.GOLD + "" + min + " minute" + (min != 1 ? "s" : "") + " left until the wall drops!");
                        }
                    }
                    if ((this.seconds <= 0) && (this.runner != null)) {
                        this.plugin.getServer().getScheduler().runTask(this.plugin, this.runner);
                        this.abort();
                    }
                    
                    this.plugin.playerScoreBoard.updateClock(seconds);
                }


                break;
            case FIGHTING:
                this.seconds++;
                if ((this.seconds % 5) == 0) {                                        
                    final int s = seconds % 60;
                    String ss = String.valueOf(s);
                    if (ss.length() == 1) {
                        ss = "0" + ss;
                    }
                    
                    final int m = (seconds - s) / 60;
//                    final String clockString = m + ":" + ss;
                    this.plugin.playerScoreBoard.updateClock(seconds);
                }                
                break;
                default:
                break;
            }
            
        }
    }

    public void abort() {
        this.runner = null;
        this.seconds = 0;
    }
    
    public void setClock(int seconds, Runnable runner) {
        this.seconds = seconds;
        this.runner = runner;
    }


}