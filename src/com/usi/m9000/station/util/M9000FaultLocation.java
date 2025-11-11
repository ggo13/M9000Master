package com.usi.m9000.station.util;

public class M9000FaultLocation {
public native String getFaultLocation(String filename, String lgDetails);
//Load the library
static {
	System.out.println(System.getProperty("sun.arch.data.model") );
  System.loadLibrary("M9000FaultLoc");
  System.out.println("Successfully loaded");
}

public static void main(String[] aargs)
{
	M9000FaultLocation m9000FaultLocation = new M9000FaultLocation();
	System.out.println("Returned from dll "+m9000FaultLocation.getFaultLocation("FileNameToRead", "LineGroupDetails"));
}
}
