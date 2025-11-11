package com.usi.m9000.test;

public class RandomGen {

	int min;
	int max;
	
	public RandomGen()
	{
		min = 0;
		max = 1;
	}
	public RandomGen(int min, int max)
	{
		this.min = min;
		this.max = max;
	}
	
	public int nextValue()
	{
		
		return min + (int)(Math.random() * ((max - min) + 1));
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RandomGen rand = new RandomGen(0,10);
		int i = 0;
		while(i++ <10)
		{
			System.out.println("Next val is "+rand.nextValue());
		}
//		5 + (int)(Math.random() * ((10 - 5) + 1))

	}
	

}
