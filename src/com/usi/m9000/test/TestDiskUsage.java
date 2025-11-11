/**
 * 
 */
package com.usi.m9000.test;

import java.io.File;

/**
 * @author sramasamy
 *
 */
public class TestDiskUsage
{
	private final static long BYTES_IN_GB = 1073741824L; // 1024*1024*1024

        public static void main(String[] args)
        {
        	System.out.println((int)Math.floor((95.0/100)*49));
//                getPercentageOfDiskUsed();
        }
        public static void getPercentageOfDiskUsed()
        {
                File root = new File("/");
                File home = new File("/home");
                File data = new File("/data");
                File dataDb = new File("/data-db");
                int highestPercentage;
                int percentage;

                float totalSpace = Math.round((float)((root.getTotalSpace()/ BYTES_IN_GB)+0.5));
//              logger.debug("DISK: Total space "+totalSpace);
//              logger.debug("DISK: Free space "+file.getFreeSpace());
                float usedSpace = Math.round((float)(((root.getTotalSpace()-root.getFreeSpace())/ BYTES_IN_GB)+0.5));
//              logger.debug("DISK: Used space "+usedSpace);
                highestPercentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
                System.out.println("root total space in bytes "+root.getTotalSpace());
                System.out.println("root totalSpace "+totalSpace);
                System.out.println("root free space in bytes "+root.getFreeSpace());
                System.out.println("root usedSpace "+usedSpace);
                System.out.println("root usage "+highestPercentage);
                totalSpace = Math.round((float)((home.getTotalSpace()/ BYTES_IN_GB)+0.5));
                usedSpace = Math.round((float)(((home.getTotalSpace()-home.getFreeSpace())/ BYTES_IN_GB)+0.5));
                percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
                System.out.println("\nhome total space in bytes "+home.getTotalSpace());
                System.out.println("home totalSpace "+totalSpace);
                System.out.println("home free space in bytes "+home.getFreeSpace());
                System.out.println("home usedSpace "+usedSpace);  
                System.out.println("home usage "+percentage);  
                if (percentage > highestPercentage)
                {
                        highestPercentage = percentage;
                }

                totalSpace = Math.round((float)((data.getTotalSpace()/ BYTES_IN_GB)+0.5));
                usedSpace = Math.round((float)(((data.getTotalSpace()-data.getFreeSpace())/ BYTES_IN_GB)+0.5));
                percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
                System.out.println("\ndata total space in bytes "+data.getTotalSpace());
                System.out.println("data totalSpace "+totalSpace);
                System.out.println("data free space in bytes "+data.getFreeSpace());
                System.out.println("data usedSpace "+usedSpace);  
                System.out.println("data usage "+percentage);
                if (percentage > highestPercentage)
                {
                        highestPercentage = percentage;
                }

                if (dataDb.exists())
                {
	                totalSpace = Math.round((float)((dataDb.getTotalSpace()/ BYTES_IN_GB)));
	                usedSpace = Math.round((float)(((dataDb.getTotalSpace()-dataDb.getFreeSpace())/ BYTES_IN_GB)+0.5));
	                percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
	                System.out.println("\nDatadb totalSpace "+totalSpace);
	                System.out.println("Datadb usedSpace "+usedSpace);  
	                System.out.println("Datadb usage "+percentage);
	                if (percentage > highestPercentage)
	                {
	                        highestPercentage = percentage;
	                }
                }
                
                
        }
}
