package com.usi.m9000.test;

import java.io.IOException;

public class HexaToBin{
  public static void main(String[] args) throws IOException{
  System.out.println("Enter the hexa value!");
  String hex = "8000";
  byte[] byt = new byte[4];
  byt = hex.getBytes();
  Long i = Long.parseLong(hex, 16);
  Integer val = Integer.parseInt(hex, 16);
  
  String by = Integer.toBinaryString(i.intValue());
  System.out.println("This is Binary: " + val);
  System.out.println("short val "+val.shortValue());
  System.out.println("Current milli seconds "+System.currentTimeMillis());
//  long num = Long.parseLong(hex,16);
//  System.out.println("This is long:=" + num);
  }
} 
