package me.glennEboy.Walls.utils;

import me.glennEboy.Walls.TheWalls;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * simple TCP server that runs on port 900x to receive messages from other walls servers
 */

public class InterWallsServer extends Thread{


    
    private final TheWalls myWalls;

    
    public InterWallsServer(TheWalls plugin) {
        this.myWalls = plugin;
        this.start();
        myWalls.getLogger().info("|============================================================");
        myWalls.getLogger().info("|            "+ChatColor.stripColor(TheWalls.chatPrefix)+"LISTNEING ON PORT {"+(9000+TheWalls.serverNumber)+"}");
        myWalls.getLogger().info("|============================================================");

    }


    @Override
    public void run() {
        ServerSocket listener=null;
        try{
             listener = new ServerSocket(9000+TheWalls.serverNumber);
            while (true) {
                Socket socket = listener.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String messageReceived = input.readLine();
                myWalls.getLogger().info("|============================================================");
                myWalls.getLogger().info("|            "+ChatColor.stripColor(TheWalls.chatPrefix)+"INTER SERVER MESSAGE RECEIVED: {"+messageReceived+"}");
                myWalls.getLogger().info("|============================================================");

                if (messageReceived.indexOf(TheWalls.scCode)==0){
                    GameNotifications.staffNotification(myWalls, messageReceived.substring(4));
                }else{

                    GameNotifications.broadcastMessage(messageReceived);
                }
            }
        }catch(Exception e){
            
        }
        try{                
            listener.close();
        }catch (Exception e1){
            
        }

    }
}