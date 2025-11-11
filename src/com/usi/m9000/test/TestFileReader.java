package com.usi.m9000.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestFileReader {

	public TestFileReader()
	{
		String str = "@Channels(Channels)/Analogs(Analogs)/AnalogInput(Analog0)/_Value";
		System.out.println(str.substring(str.indexOf("AnalogInput(")+12,str.lastIndexOf(")")));
	}
	public void createFile()
	{
		String str = "//maginst-dc1/USI Files/R&D Projects/New DFR/Comtrade/M9kShare/100812,114333524,-5t,Central Station,Utility Systems M9000 S_N 1,USI.cfg";
		String path = getClass().getProtectionDomain().getCodeSource().
		   getLocation().toString().substring(6);
		System.out.println("Path..."+path);
	}
	private List<String> getAvailableStations()
	{
		List<String> availableStations = new ArrayList<String>();
		String stationsListFileName = "E:/M9kConfig/StationNames.txt";
		BufferedReader brStationNames; 
		String stationName;
		File stationConfigFile;
		try {
			brStationNames = new BufferedReader(new FileReader(stationsListFileName));
			while(brStationNames.ready())
			{
				stationName = brStationNames.readLine();
				stationConfigFile =new File("e:/M9kConfig/"+stationName+".xml");
				if (stationConfigFile.exists() && stationConfigFile.canRead())
				{
					availableStations.add(stationName);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return availableStations;
	}

	public static void main(String[] args)
	{
		TestFileReader tfr =  new TestFileReader();
		tfr.createFile();
//		System.out.println("Available stations..."+tfr.getAvailableStations());
	}
}
