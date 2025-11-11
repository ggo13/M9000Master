package com.usi.m9000.test;

public class TestTemplate<T> {

	static int statInt = 0;
	private T localT;
	public TestTemplate(T t) {
		localT = t;
		
	}
	public String addUserValue(T a)
	{
		String result;
		System.out.println("Is localT instance of Intger??? "+(localT instanceof Integer));
		if (localT instanceof Integer)
		{
			result = ""+(Integer.parseInt(a.toString())+Integer.parseInt(localT.toString()));	
		}
		else
		{
			result = ""+a+localT;
		}
		statInt++;
		return result;
	}
	public static void main(String[] args) {
		TestTemplate test = new TestTemplate(10);
		System.out.println(test.addUserValue(10));
		System.out.println("static int 1--"+statInt);
		TestTemplate test1 = new TestTemplate(10);
		System.out.println(test1.addUserValue(10));
		System.out.println("static int 2--"+statInt);
		TestTemplate test2 = new TestTemplate("10");
		System.out.println(test2.addUserValue("10"));
		System.out.println("static int 3--"+statInt);
		TestTemplate test3 = new TestTemplate("10");
		System.out.println(test3.addUserValue("10"));
		System.out.println("static int 4--"+statInt);
	}

}
