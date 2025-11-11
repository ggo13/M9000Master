package com.usi.m9000.test;

import java.util.ArrayList;
import java.util.Arrays;

public class ChannelRawData {

	ArrayList<ArrayList<String>> channelsList;
	ArrayList<ArrayList<Double>> binaryChannelsList;
	ArrayList<ArrayList<Integer>> digitalChannelsList;
	private int totalChannels;
	private int totalDigitalChannels;
	
	public ChannelRawData() {
		totalChannels = 0;
		channelsList = new ArrayList<ArrayList<String>>();
		binaryChannelsList = new ArrayList<ArrayList<Double>>();
		digitalChannelsList = new ArrayList<ArrayList<Integer>>();
	}

	public void createChannelsList(int count)
	{
		totalChannels = count;
		for (int i = 0; i < totalChannels; i++) {
			channelsList.add(new ArrayList<String>());
		}
	}

	// Test
	public void createChannelWithData(String[] data)
	{
		totalChannels++;
		channelsList.add(new ArrayList<String>(Arrays.asList(data)));
	}
	public void removeChannels()
	{
		totalChannels = 0;
		channelsList.clear();
	}
	
	public void createChannelsWithData (int channelsCnt, short[][] channelsData)
	{
		ArrayList<Double>dataArray; 
		totalChannels = channelsCnt;
		for (int i = 0; i < totalChannels; i++) {
			dataArray = new ArrayList<Double>();
			for (int j = 0; j < channelsData[i].length; j++) {
				dataArray.add(new Double(channelsData[i][j]));
			}
			binaryChannelsList.add(dataArray);
		}
	}
	
	public void createDigitalChannelsWithData (int channelsCnt, int[][] channelsData)
	{
		ArrayList<Integer>dataArray; 
		totalDigitalChannels = channelsCnt;
		for (int i = 0; i < totalDigitalChannels; i++) {
			dataArray = new ArrayList<Integer>();
			for (int j = 0; j < channelsData[i].length; j++) {
				dataArray.add(new Integer(channelsData[i][j]));
			}
			digitalChannelsList.add(dataArray);
		}
	}

	public void removeBinaryChannels()
	{
		totalChannels = 0;
		binaryChannelsList.clear();
	}
	
	public ArrayList<ArrayList<Double>> getBinaryChannelsData()
	{
		return binaryChannelsList;
	}
	public void clearBinaryChannelsData()
	{
		for (int i = 0; i < totalChannels; i++) {
			((ArrayList<Double>)binaryChannelsList.get(i)).clear();
		}
	}
	public void removeDigitalChannels()
	{
		totalDigitalChannels = 0;
		digitalChannelsList.clear();
	}
	
	public ArrayList<ArrayList<Integer>> getDigitalChannelsData()
	{
		return digitalChannelsList;
	}
	public void clearDigitalChannelsData()
	{
		for (int i = 0; i < totalDigitalChannels; i++) {
			((ArrayList<Integer>)digitalChannelsList.get(i)).clear();
		}
	}

	// Test

	public void addChannelsData(String[] data)
	{
		for (int i = 0; i < data.length; i++) {
			((ArrayList<String>)channelsList.get(i)).add(data[i]);
		}
	}
	
	public void clearChannelsData()
	{
		for (int i = 0; i < totalChannels; i++) {
			((ArrayList<String>)channelsList.get(i)).clear();
		}
	}
	public ArrayList<ArrayList<String>> getChannelsData()
	{
		return channelsList;
	}
	
	public int getChannelsCount()
	{
		return totalChannels;
	}
	
	public int getDigitalChannelsCount()
	{
		return totalDigitalChannels;
	}
	public void setChannelsCount(int count)
	{
		totalChannels = count; 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
