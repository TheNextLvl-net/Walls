package net.nonswag.fvr.walls.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

@RequiredArgsConstructor
public class Clock extends Thread {
    private final Walls walls;
    @Getter
    private int seconds;
    private Runnable runner;

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            switch (walls.getGameState()) {
                case PREGAME:
                    if (--seconds <= 0 && runner != null) {
                        Bukkit.getScheduler().runTask(walls, runner);
                        abort();
                    }
                    walls.getPlayerScoreBoard().updateClock(seconds);
                    break;
                case PEACETIME:
                    if (--seconds % 60 == 0) {
                        int min = seconds / 60;
                        switch (min) {
                            case 7:
                            case 5:
                            case 1:
                                Notifier.broadcast(ChatColor.GOLD + "" + min + " minute" + (min != 1 ? "s" : "") + " left until the wall drops!");
                        }
                    }
                    if (seconds <= 0 && runner != null) {
                        Bukkit.getScheduler().runTask(walls, runner);
                        abort();
                    }
                    walls.getPlayerScoreBoard().updateClock(seconds);
                    break;
                case FIGHTING:
                    walls.getPlayerScoreBoard().updateClock(++seconds);
                    break;
                default:
                    break;
            }
        }
    }

    public void abort() {
        runner = null;
        seconds = 0;
    }
    
    public void setClock(int seconds, Runnable runner) {
        this.seconds = seconds;
        this.runner = runner;
    }
}