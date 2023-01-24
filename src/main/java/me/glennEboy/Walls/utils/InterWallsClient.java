package me.glennEboy.Walls.utils;

import java.io.PrintWriter;
import java.net.Socket;

import org.bukkit.Bukkit;

import me.glennEboy.Walls.TheWalls;


public class InterWallsClient extends Thread{


	private static String myMessage = null;
	
    private final TheWalls myWalls;

	
    public InterWallsClient(TheWalls plugin) {
        this.myWalls = plugin;
        this.start();
    }

	
    @Override
    public void run() {
    	int timer=500;
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(timer);
            } catch (final InterruptedException e) {
                break;
            }
            if (InterWallsClient.myMessage!=null){
            	InterWallsClient.sendMessageToAll(InterWallsClient.myMessage);
            }
        }
    }

    /**
     * Connects to the other servers to send them messages
     */
    protected static void sendMessageToAll(String message){
        String serverAddress = "72.20.37.190";
        Socket s = null;
        for (int i=1; i<8 ;i++){
	        try{
	        		if (i!=TheWalls.serverNumber){
	        			s = new Socket(serverAddress, 9000+i);
	        			s.setSoTimeout(600);
	        			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
	        			out.println(message);
	        			s.close();
	                    Bukkit.getServer().getLogger().info("INTER SERVER MESSAGE SENT: {"+message+"}");
	        	}
	        	InterWallsClient.myMessage=null;
	        	
	        }catch (Exception e){
	        	try{
	        		s.close();
	        	}catch(Exception e2){
	                Bukkit.getServer().getLogger().info("INTER SERVER MESSAGE FAILED: {"+message+"}");        		
	        	}
	        }
        }
    }
    
    public static void sendMessage(String message){
    	InterWallsClient.myMessage = message;   	
    }
    
}