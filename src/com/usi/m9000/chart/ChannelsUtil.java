package com.usi.m9000.chart;

import java.util.ArrayList;

public class ChannelsUtil {

	ArrayList<ArrayList<Double>> analogChannelsList;
	ArrayList<ArrayList<Integer>> digitalChannelsList;
	private int totalAnalogChannels;
	private int totalDigitalChannels;
	
	public ChannelsUtil() {
		totalAnalogChannels = 0;
		analogChannelsList = new ArrayList<ArrayList<Double>>();
//		digitalChannelsList = new ArrayList<ArrayList<Integer>>();
	}

	public void createAnalogChannelsWithData (int channelsCnt, double[][] channelsData)
	{
		ArrayList<Double> dataArray; 
		totalAnalogChannels = channelsCnt;
		for (int i = 0; i < totalAnalogChannels; i++) {
			dataArray = new ArrayList<Double>();
			for (int j = 0; j < channelsData[i].length; j++) {
				dataArray.add(channelsData[i][j]);
			}
			analogChannelsList.add(dataArray);
		}
	}
	
	public void createDigitalChannelsWithData (int channelsCnt, int[][] channelsData)
	{
		ArrayList<Integer> dataArray; 
		totalDigitalChannels = channelsCnt;
		for (int i = 0; i < totalDigitalChannels; i++) {
			dataArray = new ArrayList<Integer>();
			for (int j = 0; j < channelsData[i].length; j++) {
				dataArray.add(channelsData[i][j]);
			}
			digitalChannelsList.add(dataArray);
		}
	}

	public void removeAnalogChannels()
	{
		totalAnalogChannels = 0;
		analogChannelsList.clear();
	}
	
	public ArrayList<ArrayList<Double>> getAnalogChannelsData()
	{
		return analogChannelsList;
	}
	public void clearAnalogChannelsData()
	{
		for (int i = 0; i < totalAnalogChannels; i++) {
			((ArrayList<Double>)analogChannelsList.get(i)).clear();
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

	public void setDigitalChannelsData(ArrayList<ArrayList<Integer>> lstEventData)
	{
//		System.out.println("lst Event Data to be set "+lstEventData.size());
		totalDigitalChannels = lstEventData.size();
		digitalChannelsList = lstEventData;
	}

	public void clearDigitalChannelsData()
	{
		for (int i = 0; i < totalDigitalChannels; i++) {
			((ArrayList<Integer>)digitalChannelsList.get(i)).clear();
		}
	}

	public int getAnalogChannelsCount()
	{
		return totalAnalogChannels;
	}
	
	public int getDigitalChannelsCount()
	{
		return totalDigitalChannels;
	}
	public void setChannelsCount(int count)
	{
		totalAnalogChannels = count; 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
