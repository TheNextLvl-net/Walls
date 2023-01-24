package me.glennEboy.Walls.utils;

public class Math {

    
    public static float mean(float[] listOfValues){
        float sum = 0;  // sum of all the elements
            for (int i=0; i<listOfValues.length; i++) {
                sum += listOfValues[i];
            }
        return sum / listOfValues.length;
    }

    public static float mean(int[] listOfValues){
        float sum = 0;  // sum of all the elements
            for (int i=0; i<listOfValues.length; i++) {
                sum += (float)listOfValues[i];
            }
        return sum / listOfValues.length;
    }

}
