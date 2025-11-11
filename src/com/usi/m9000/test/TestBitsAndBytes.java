package com.usi.m9000.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestBitsAndBytes {
	
public static void main(String args[]) throws IOException 
{
//	StringBuffer events = new StringBuffer("000011");
//	events.reverse().toString();
//	String zeros = "0000000000000000";
	TestBitsAndBytes test = new TestBitsAndBytes();
//	System.out.println("Reveresed string "+events);
//	System.out.println("Binary "+Long.parseLong(events.toString()));
//	long val = Long.parseLong(events.toString(),2); //test.byteArrayToInt(byteArr);
//	System.out.println("In val "+ val);
//	System.out.println("Binary value "+Long.toBinaryString(val));
//	String hex = Long.toHexString(val);
//	System.out.println("Hex value "+hex);
//	System.out.println(Long.toHexString(Long.reverseBytes(val)));//.substring(0,4));
//	String datEvent = Long.toHexString(Long.reverseBytes(val)).substring(0,4);
//	System.out.println("Final event val for dat "+datEvent);
//	String bin = Long.toBinaryString(val);
//	String leadZero = zeros.substring(0,16-bin.length());
//	bin=leadZero.concat(bin);
//	System.out.println("Bin value "+bin);
	File testFile =  new File("C:/M9k/Data/test.dat");
	DataOutputStream dos = new DataOutputStream(new FileOutputStream(testFile));
	String binStr = "00000000000010000";
	Short i = 0x0000000000001000;
//	i = 16;
	short eventHexValue = (short)test.convertEventToHex(binStr);
//	String zeros = "0000000000000000";
//	int hex = test.convertEventToHex(i);
	short s;
	System.out.println(i);
	dos.writeShort(i);
//	System.out.println("REversed short "+Short.reverseBytes(eventHexValue));
//	dos.writeShort(Short.reverseBytes(eventHexValue));
	dos.close();
	DataInputStream dis = new DataInputStream(new FileInputStream(testFile));
	byte tstData[] = new byte[2];
//	dis.read(tstData);
	s = dis.readShort();
	System.out.println("Short read as it is "+s);
	s = Short.reverseBytes(s);
	System.out.println("Short after reversed "+s);
////	ByteBuffer roBuff =  ByteBuffer.wrap(tstData);
////	roBuff.order(ByteOrder.LITTLE_ENDIAN);
//	StringBuffer str = new StringBuffer(Integer.toBinaryString(s)); 
//	String leadZero = zeros.substring(0,16-str.toString().length());
//	str=new StringBuffer(leadZero.concat(str.toString()));
//	System.out.println("read short "+Integer.toBinaryString(s));
////	System.out.println("Reveresed str "+str.reverse());
//	
//	int lhs = 1;
//	int rhs = 1;
	
//	System.out.println((lhs&rhs) == 1);
	
//	int j = 0;
//	String newEvents = "1000000000000000";
//	String eventBits = "0";
////	logger.debug("BINARY: eventsIndexCnt "+eventsIndexCnt +" eventsCnt "+eventsCnt);
//	while(j < newEvents.length()) // Total Number of events in this particular DFR
//	{
////		logger.debug("BIG-BINARY: eventsBits "+eventBits +" j "+j+" NewEvents "+newEvents);
//		eventBits = newEvents.substring(j++, j) + eventBits; // Append to the already available events, if any.
//		if (eventBits.length() == 16)
//		{
//			System.out.println("BIG-BINARY: eventsBits "+eventBits);
//		}
//	}
	
}
	public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
}
	
	public static final int byteArrayToInt(byte [] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
}
	
	private int convertEventToHex(String events)
	{
		int hexVal;
		System.out.println("BINARY-HEX: Events string to be converted "+events);
		Integer val = Integer.parseInt(events,2);
		System.out.println("BINARY-HEX: Long value of Events string to be converted "+val);
		String zeros = "0000";
		String hexString = Integer.toHexString(val);
		System.out.println("BINARY-HEX: before padding Hex value of converted "+hexString);
		if (hexString.length() > 4)
		{
			hexString = hexString.substring(0,4);
		}
		else
		{
			String leadZero = zeros.substring(0,4-hexString.length());
			hexString=leadZero.concat(hexString);			
		}
//		hexString = new StringBuffer(hexString).reverse().toString();
		System.out.println("BINARY-HEX: After padding Hex value converted "+hexString);
		hexVal = Integer.parseInt(hexString,16);
		System.out.println("BINARY-HEX: Integer Hex value aafter conversion to be returned "+hexVal);
		return hexVal;
	}
}
