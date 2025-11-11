package com.usi.m9000.test;

import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;


public class TestConfigParser {
	public TestConfigParser() throws ConfigurationException
	{
		HierarchicalINIConfiguration iniConf = new HierarchicalINIConfiguration("C:/M9k/new-development/WorkSpace/M9000Master/resources/station.properties");
		
		Iterator<String> itrSections = iniConf.getSections().iterator();
		while (itrSections.hasNext()) {
			String key = itrSections.next();
			System.out.println("Key "+key);
		}
		System.out.println(iniConf.getProperty("DFR1.ip-address"));
		Iterator<String> itrString = iniConf.getSection("master-notify").getKeys();
		while (itrString.hasNext()) {
			String key = itrString.next();
			System.out.println("Key "+key);
		}
		System.out.println(iniConf.getSection("health").getDouble("v5-low",6.8));
		System.out.println(iniConf.getString("RelayInfo.master-notify-queue", "ERROR"));
	}
	
	public static void main(String args[]) throws ConfigurationException
	{
		new TestConfigParser();
	}
}
