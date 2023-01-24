package me.glennEboy.Walls.utils;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class ViewUtils {


	/**
	    * Gets entities inside a cone.
	    * @see Utilities#getPlayersInCone(List, Location, int, int, int)
	    *
	    * @param entities - {@code List<Entity>}, list of nearby entities
	    * @param startpoint - {@code Location}, center point
	    * @param radius - {@code int}, radius of the circle
	    * @param degrees - {@code int}, angle of the cone
	    * @param direction - {@code int}, direction of the cone
	    * @return {@code List<Entity>} - entities in the cone
	    */
	    public static List<Entity> getEntitiesInCone(List<Entity> entities, Location startpoint, int radius, int degrees, int direction)
	    {
	        List<Entity> newEntities = new ArrayList<Entity>();
	     
	        int[] startPos = new int[] { (int)startpoint.getX(), (int)startpoint.getZ() };
	     
	        int[] endA = new int[] { (int)(radius * Math.cos(direction - (degrees / 2))), (int)(radius * Math.sin(direction - (degrees / 2))) };
	   
	        for(Entity e : entities)
	        {
	            Location l = e.getLocation();      
	            int[] entityVector = getVectorForPoints(startPos[0], startPos[1], l.getBlockX(), l.getBlockY());
	 
	            double angle = getAngleBetweenVectors(endA, entityVector);
	            if(Math.toDegrees(angle) < degrees && Math.toDegrees(angle) > 0)
	                newEntities.add(e);
	        }
	        return newEntities;
	    }
	    /**
	    * Created an integer vector in 2d between two points
	    *
	    * @param x1 - {@code int}, X pos 1
	    * @param y1 - {@code int}, Y pos 1
	    * @param x2 - {@code int}, X pos 2
	    * @param y2 - {@code int}, Y pos 2
	    * @return {@code int[]} - vector
	    */
	    public static int[] getVectorForPoints(int x1, int y1, int x2, int y2)
	    {
	        return new int[] { x2 - x1, y2 - y1 };
	    }
	    /**
	    * Get the angle between two vectors.
	    *
	    * @param vector1 - {@code int[]}, vector 1
	    * @param vector2 - {@code int[]}, vector 2
	    * @return {@code double} - angle
	    */
	    public static double getAngleBetweenVectors(int[] vector1, int[] vector2)
	    {
	        return Math.atan2(vector2[1], vector2[0]) - Math.atan2(vector1[1], vector1[0]);
	    }
}
