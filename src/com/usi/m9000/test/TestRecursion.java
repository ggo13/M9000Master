package com.usi.m9000.test;

public class TestRecursion {

public void print(String text)
{
	if (text.length() == 0)
	{
		return;
	}
	print(text.substring(1,text.length()));
	System.out.println(text.substring(0,1));
}
	public static void main(String args[])
	{
		TestRecursion test = new TestRecursion();
		String strName = "KRITHIK";
		for(int i = 0; i<strName.length();i++) {
			System.out.println(strName.charAt(i));
		}
			
	}
}
