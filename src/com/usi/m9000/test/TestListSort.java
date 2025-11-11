package com.usi.m9000.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.usi.m9000.dto.DfrDTO;

public class TestListSort {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestListSort testL = new TestListSort();
		testL.testSortCollection();
	}

	public void test()
	{
		List<DfrDTO> lstScopeDfrs = new ArrayList<DfrDTO>();
		DfrDTO dfr4 = new DfrDTO("DFR4");
		dfr4.setDfrId(4);
		lstScopeDfrs.add(dfr4);
		DfrDTO dfr1 = new DfrDTO("DFR1");
		dfr1.setDfrId(1);
		lstScopeDfrs.add(dfr1);
		DfrDTO dfr2 = new DfrDTO("DFR2");
		dfr2.setDfrId(2);
		lstScopeDfrs.add(dfr2);
		DfrDTO dfr3 = new DfrDTO("DFR3");
		dfr3.setDfrId(3);
		lstScopeDfrs.add(dfr3);
		System.out.println("Before sort list "+lstScopeDfrs);
		Collections.sort(lstScopeDfrs, new Comparator<DfrDTO>() {

			@Override
			public int compare(DfrDTO o1, DfrDTO o2) {
				System.out.println("LHS Name: "+o1.getDfrName()+" id: "+o1.getDfrId());
				System.out.println("RHS Name: "+o2.getDfrName()+" id: "+o2.getDfrId());
				System.out.println("Returning "+((o1.getDfrId() < o2.getDfrId())?0:1));
				return ((o1.getDfrId() > o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
			}

		});
		System.out.println("After sort list "+lstScopeDfrs);
		
		String strDate = "10/25/2012 17:33:12.049647";
		Double milli = Double.parseDouble(strDate.substring(strDate.indexOf("."))); 
		System.out.println("milli " + milli.toString());

	}
	public void testSortCollection()
	{
		List<Integer> lstComtradeDataDtos = new ArrayList<Integer>();
		for (int i = 0; i < 338; i++) {
			lstComtradeDataDtos.add(338-i);
		}
		System.out.println("Before sort "+lstComtradeDataDtos);
		Collections.sort(lstComtradeDataDtos, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				int returnVal;
					if (o1.intValue() < o2.intValue())
					// END: 12-FEB-2015
					{
						returnVal =  -1;
					}
					else
					{
						returnVal = 1;
					}
				return returnVal;
			}
		});
		System.out.println("Before sort "+lstComtradeDataDtos);
	}
}
