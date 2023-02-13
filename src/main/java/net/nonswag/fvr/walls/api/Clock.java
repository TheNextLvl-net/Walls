package net.nonswag.fvr.walls.api;


import net.nonswag.fvr.walls.Walls;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Clock extends Thread {


    private int seconds;
    private Runnable runner;
    private final Walls plugin;

    public Clock(Walls plugin) {
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
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                break;
            }
            switch (plugin.getGameState()) {
                case PREGAME:
                    this.seconds--;
                    if ((this.seconds <= 0) && (this.runner != null)) {
                        Bukkit.getScheduler().runTask(this.plugin, this.runner);
                        this.abort();
                    }
                    this.plugin.getPlayerScoreBoard().updateClock(seconds);
                    break;
                case PEACETIME:
                    this.seconds--;
                    if ((this.seconds % 60) == 0) {
                        final int min = this.seconds / 60;
                        switch (min) {
                            case 7:
                            case 5:
                            case 1:
                                Notifier.broadcast(ChatColor.GOLD + "" + min + " minute" + (min != 1 ? "s" : "") + " left until the wall drops!");
                        }
                    }
                    if ((this.seconds <= 0) && (this.runner != null)) {
                        Bukkit.getScheduler().runTask(this.plugin, this.runner);
                        this.abort();
                    }
                    this.plugin.getPlayerScoreBoard().updateClock(seconds);
                    break;
                case FIGHTING:
                    this.seconds++;
                    this.plugin.getPlayerScoreBoard().updateClock(seconds);
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