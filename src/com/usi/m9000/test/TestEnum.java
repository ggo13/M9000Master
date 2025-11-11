package com.usi.m9000.test;

import com.usi.m9000.station.util.LedName;
import com.usi.m9000.station.util.RelayName;


public class TestEnum {

	public void createDefaultAlarmSettings()
	{
//		Alarms alarmsRoot = substation.addNewAlarms();
//		for (RelayName relayName : RelayName.values()) {
		for (int i=0; i < 7;i++)
		{
			System.out.println(RelayName.values()[i]+" and  "+LedName.values()[i]);
		}
		System.out.println(RelayName.values()[4] +" and "+LedName.values()[7]);
	}	
	public static void main(String args[])
	{
		TestEnum test = new TestEnum();
		test.createDefaultAlarmSettings();
	}
}
