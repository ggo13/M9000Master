package com.usi.m9000.station.util;

public enum LineChannels
{//Name             // Idx
   CHAN_VA,         //  0
   CHAN_VB,         //  1
   CHAN_VC,         //  2
   CHAN_IA,         //  3
   CHAN_IB,         //  4
   CHAN_IC,         //  5
   CHAN_IN,         //  6
//   NUM_LINE_CHAN  //  7  Change from 7 channel Line Group to 8
   CHAN_REF,        //  7  Changes for Santee Cooper
   NUM_LINE_CHAN ;   //  8
   
   private int channelId;
   
   public int getChannelId() {
		return channelId;
	}
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}
}