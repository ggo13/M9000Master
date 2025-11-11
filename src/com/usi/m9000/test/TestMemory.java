package com.usi.m9000.test;

public class TestMemory {
    
    public static void main(String [] args) {
         
        int mb = 1024*1024;
         
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        System.out.println("##### Heap utilization statistics [MB] #####");
         
        //Print used memory
        System.out.println("Used Memory:"
            + (runtime.totalMemory() - runtime.freeMemory()) / mb);
 
        //Print free memory
        System.out.println("Free Memory:"
            + runtime.freeMemory() / mb);
         
        //Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);
 
        //Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
        
        Float f = Float.MAX_VALUE;
        System.out.println("Max Value "+f.MAX_VALUE);
        System.out.println("Min Value "+f.MIN_VALUE);
        System.out.println("Float "+f.floatValue());
        Short s = f.shortValue();
        System.out.println("Short value "+s.shortValue());
        System.out.println("Back to float value "+s.floatValue());
        
        float scaleFactor;
        
        scaleFactor = (32767.0f / 120000);
        
        System.out.println("Scale factor "+scaleFactor);
        
        
    }
}
