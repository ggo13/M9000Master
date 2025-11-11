/**
 * 
 */
package com.usi.m9000.test;

/**
 * @author sramasamy
 *
 */
public class TstBoardDetect {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TstBoardDetect test = new TstBoardDetect();
		test.printBoardDetect(235929600);
	}

	private void printBoardDetect(long brddet)
	{
		brddet = brddet & 0xffffffffL;
		
		for (int i = 0; i < 4; i++) {
			System.out.println("Analog["+i+"]\t: "+(( brddet & (1 << (27-i))) > 0  ? "present" : "empty" ) );
			
		}
		System.out.println();
		for (int i = 0; i < 4; i++) {
			System.out.println("Event["+i+"]\t: "+(( brddet & (1 << (23-i))) > 0 ? "present" : "empty" ) );
			
		}
	}

}
