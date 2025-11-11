package com.usi.m9000.config;

import com.usi.m9000.util.M9kConstants;

public class ChannelsListInfo {
	int analogCnt;
	int digitalCnt;
	String selectedValue;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ChannelsListInfo.class);

	/**
	 * 
	 */
	public ChannelsListInfo() {
//		logger.info("Entered Constructor...");
//		populateChannelsList();
	}

	/**
	 * @return the analogCnt
	 */
	public int getAnalogCnt() {
		return analogCnt;
	}

	/**
	 * @param analogCnt the analogCnt to set
	 */
	public void setAnalogCnt(int analogCnt) {
		this.analogCnt = analogCnt;
	}

	/**
	 * @return the eventCnt
	 */
	public int getDigitalCnt() {
		return digitalCnt;
	}

	/**
	 * @param eventCnt the eventCnt to set
	 */
	public void setDigitalCnt(int digitalCnt) {
		this.digitalCnt = digitalCnt;
	}

	/**
	 * @return the selectedValue
	 */
	public String getSelectedValue() {
		// START: 18-Jun-2020 - Mini implementation - If 12A and 16D - Prefix with mini
		if (getAnalogCnt() == M9kConstants.NO_OF_ANALOG_CHNLS_PER_MINI && getDigitalCnt() == M9kConstants.NO_OF_DIGITAL_CHNLS_PER_MINI)
		{
			selectedValue = "mini-"+getAnalogCnt()+"A-"+ getDigitalCnt()+"D";
		}
		else if((getAnalogCnt() % M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD) > 0 || (getDigitalCnt() % M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD) > 0 )
		{
			selectedValue = (getAnalogCnt() - M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD)+"A-"+ (getDigitalCnt() - M9kConstants.NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD)+"D-ANEV";
		}
		else
		{
			selectedValue = getAnalogCnt()+"A-"+ getDigitalCnt()+"D";
		}
		// END: 18-Jun-2020
		return selectedValue;
	}

	/**
	 * @param selectedValue the selectedValue to set
	 */
	public void setSelectedValue(String lstValue) {
		String strChnlCnt[];
		if (lstValue != null)
		{
			this.selectedValue = lstValue;
			// START: 19-Jun-2020 - Mini implementation - If 12A and 16D - Prefix with mini
			if (lstValue.indexOf("ANEV") != -1)
			{
				lstValue = lstValue.substring(0, lstValue.lastIndexOf("-"));
				strChnlCnt = lstValue.split("-");
				setAnalogCnt(Integer.parseInt(strChnlCnt[0].substring(0, (strChnlCnt[0].length()-1))) + M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD);
				setDigitalCnt(Integer.parseInt(strChnlCnt[1].substring(0, (strChnlCnt[1].length()-1))) + M9kConstants.NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD);					
			}
			else 
			{
				if (lstValue.indexOf("mini") != -1)
				{
					lstValue = lstValue.substring(lstValue.indexOf("-")+1);
				}
				strChnlCnt = lstValue.split("-");
				setAnalogCnt(Integer.parseInt(strChnlCnt[0].substring(0, (strChnlCnt[0].length()-1))));
				setDigitalCnt(Integer.parseInt(strChnlCnt[1].substring(0, (strChnlCnt[1].length()-1))));
			}
			
			// END: 19-Jun-2020
		}
	}
	
	
public static void main(String args[])
{
	ChannelsListInfo test = new ChannelsListInfo();
	test.setSelectedValue("32A-128D");
	
//	System.out.println("Analog cnt..."+test.getAnalogCnt());
//	System.out.println("Digital cnt..."+test.getDigitalCnt());
	
	
}

}
