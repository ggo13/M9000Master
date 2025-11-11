/**
 * 
 */
package com.usi.m9000.station.faultLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.DigitalInfo;
import com.usi.m9000.config.M9kComtradeParser;
import com.usi.m9000.station.dto.ProcessDatDTO;
import com.usi.m9000.station.util.FaultTypes;
import com.usi.m9000.station.util.LineChannels;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.triggers.LineGroupsAlgorithm;

/**
 * @author sramasamy
 *
 */
public class M9kFaultLocation {
	private M9kFaultReport faultReport;
	private ProcessDatDTO comtradeData;
	private Map<String, DigitalInfo> abnormalEvents;
	private List<LineGroupsAlgorithm> lstLineGroups;
	private M9kStationComtradeUtil m9kStationComtradeUtil;
	private boolean continueProcessing = true;
	int firstDataNum = 0;
	int nData = 0;
    int nDataFiltered;  //Number of data left after FilterData()

    //needed values
    int qW;    //number of data in quarter of a wave
    double zP;  //phase impedance
    double zPP; //phase-to-phase impedance
    double zPG; //phase-to-ground impedance
    double zK;  //
    boolean hasVirtualChan;

    //for fault examination
    float ZeroThr;

    //fault window start and end data index
    int FaultStartDataIndex;
    int FaultEndDataIndex;
    private M9kFaultReport currentFaultReport;
    List<Double> lstFaultChnlVFirst = null;
    List<Double> lstFaultChnlVSecond = null;
    List<Double> lstFaultChnlIFirst = null;
    List<Double> lstFaultChnlISecond = null;
    List<Double> lstFaultChnlRef = null;
	private static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kFaultLocation.class);

public M9kFaultLocation(ProcessDatDTO comtradeData)
{
	logger.debug("Entered M9kFaultLocation "+comtradeData.getBinaryData().size());
	m9kStationComtradeUtil = new M9kStationComtradeUtil();
	this.comtradeData = comtradeData;
	try {
		abnormalEvents = m9kStationComtradeUtil.getActiveEventsId(comtradeData);
//		logger.debug("Abnormal events "+abnormalEvents.keySet());
		if (abnormalEvents == null || abnormalEvents.isEmpty())
		{
			logger.info("No abnormal events. Fault location cannot be calculated");
			continueProcessing = false;
		}
		lstLineGroups = M9kStationXMLUtil.getLstLineGroups();
		
		if (lstLineGroups == null || lstLineGroups.isEmpty())
		{
			logger.info("No Line Groups defined. Fault location cannot be calculated");
			continueProcessing = false;
		}
		logger.debug("Returning M9kFaultLocation constructor with flag continueProcessing as "+continueProcessing);
//		System.out.println("Returning M9kFaultLocation constructor with flag continueProcessing as "+continueProcessing);
	} catch (M9000Exception e) {
		e.printStackTrace();
		logger.error("Error in fault location calculation",e);
		logger.info("No abnormal events. Fault location cannot be calculated"+e);
		continueProcessing = false;
	}
	
}

public Map<LineGroupsAlgorithm, M9kFaultReport> findFaultLocation(boolean forceFloc) throws M9000Exception 
{
	logger.debug("Entered findFaultLocation()... "+forceFloc);
//	System.out.println("Entered findFaultLocation() ");
	Map<LineGroupsAlgorithm, M9kFaultReport> linegroupFaultLoc = null;
	M9kFaultReport faultReport = null;
	LineGroupsAlgorithm lineGroupsAlgorithm;
	Map<LineChannels, List<Double>> mapData;
	for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
		lineGroupsAlgorithm =  iterator.next();
		try
		{
		logger.debug("Line Group name "+lineGroupsAlgorithm.getLineGroupName());
//		System.out.println("Line group name "+lineGroupsAlgorithm.getLineGroupName());
		faultReport = new M9kFaultReport(lineGroupsAlgorithm);
		faultReport.forceFloc = forceFloc;
		setCurrentFaultReport(faultReport);
		if(lineGroupsAlgorithm.getEnableAutoCalc().equalsIgnoreCase(M9kStationConstants.NO) && !forceFloc)
		{
			// Move to the next line group
			continue;
		}
		faultReport.isLtgOnly = isLtgOnlyLine(faultReport, lineGroupsAlgorithm);
		logger.debug("faultReport.isLtgOnly "+faultReport.isLtgOnly);
//		System.out.println("faultReport.isLtgOnly "+faultReport.isLtgOnly);
		logger.debug("Fault Decision logic ? "+faultReport.isLogicTrue);
//		System.out.println("Fault Decision logic ? "+faultReport.isLogicTrue);
		// START: 19-Apr-2018 - Fault Location doesn't pass through this logic. Hence commented. Need to revisit for Lightning correlation implementation
//		if(faultReport.isLtgOnly)
//		  {
////			System.out.println("Moving on to next group!!!");
//			// Move to the next line group
//		     continue;
//		  }
		// END: 19-Apr-2018
		firstDataNum = 0;
		nData = comtradeData.getSampleCnt();
		logger.debug("ndata "+nData);
		calCheckNeededValues(lineGroupsAlgorithm);
		mapData = getData(lineGroupsAlgorithm);
		logger.debug("IS logic true "+faultReport.isLogicTrue+" forcxe fault loc? "+faultReport.forceFloc);
	    if(faultReport.isLogicTrue || faultReport.forceFloc)
	    {
	    	try
	    	{
		        filterData(mapData);
		  	    findFaultWindow(mapData);
		        //calculate fault location
		        calFaultLoc(lineGroupsAlgorithm, mapData);
		        faultReport.isFaultLocExist = true;
	    	}
	    	catch (Exception e) {
	    		logger.error("Exception occured in LineGroup Name "+lineGroupsAlgorithm.getName(),e);
		        if (linegroupFaultLoc == null)
		        {
		        	linegroupFaultLoc = new HashMap<LineGroupsAlgorithm, M9kFaultReport>(lstLineGroups.size());
		        }
		        linegroupFaultLoc.put(lineGroupsAlgorithm, faultReport);

			}
	        if (linegroupFaultLoc == null)
	        {
	        	linegroupFaultLoc = new HashMap<LineGroupsAlgorithm, M9kFaultReport>(lstLineGroups.size());
	        }
	        linegroupFaultLoc.put(lineGroupsAlgorithm, faultReport);
	    }
		}
		catch (Exception e) {
			logger.error("Exception calculating fault location for line "+lineGroupsAlgorithm.getName()+" - id "+lineGroupsAlgorithm.getId(),e);
		}
	}
	logger.debug("Returning "+linegroupFaultLoc);
	return linegroupFaultLoc;
}

//take off dc offset
private void filterData(Map<LineChannels, List<Double>> mapData) throws Exception {
	  int i,q1,q2,q3;
	  double sw = 1/(double)(2.0*Math.sqrt(2.0)); //it is from dali code

	  nDataFiltered = nData-3*qW;
	  logger.debug("nDataFiletered "+nDataFiltered);
	  //let's say Va[i]=A*sin(x)+dc, then a quarter cycle away Va[i+Qw]=A*sin(x+90)+dc
	  //and 2 quarter away Va[i+2*Qw]=A*sin(x+180)+dc
	  //and 3/4 away Va[i+3*Qw]=A*sin(x+270)+dc so
	  //(Va[i]-Va[i+Qw]-Va[i+2*Qw]+Va[i+3*Qw]) = 2A*(sin(x) - cos(x))
	  //then 2A*(sin(x) - cos(x))/(2*sqrt(2)) produce the same peak amplitude and
	  //frequency of the original data (only with the phase shifted) but now
	  //the dc offset is taken off
	  //Since all data are filtered this way i.e. phase shift the same amount,
	  //the phase shift will not affect any calculation
	  List<Double> lstChnlVa = mapData.get(LineChannels.CHAN_VA);
	  List<Double> lstChnlVb = mapData.get(LineChannels.CHAN_VB);
	  List<Double> lstChnlVc = mapData.get(LineChannels.CHAN_VC);
	  List<Double> lstChnlIa = mapData.get(LineChannels.CHAN_IA);
	  List<Double> lstChnlIb = mapData.get(LineChannels.CHAN_IB);
	  List<Double> lstChnlIc = mapData.get(LineChannels.CHAN_IC);
	  List<Double> lstChnlIn = mapData.get(LineChannels.CHAN_IN);

//	  logger.debug("Actual list "+lstChnlVa);
//	  List<Double> lstChnlVaTemp = new ArrayList<Double>(nDataFiltered);
//	  List<Double> lstChnlVbTemp = new ArrayList<Double>(nDataFiltered);
//	  List<Double> lstChnlVcTemp = new ArrayList<Double>(nDataFiltered);
//	  List<Double> lstChnlIaTemp = new ArrayList<Double>(nDataFiltered);
//	  List<Double> lstChnlIbTemp = new ArrayList<Double>(nDataFiltered);
//	  List<Double> lstChnlIcTemp = new ArrayList<Double>(nDataFiltered);
//	  List<Double> lstChnlInTemp = new ArrayList<Double>(nDataFiltered);

	  for (i=0;i<nDataFiltered;i++)
	  {
	    q1=i+qW; q2=i+2*qW; q3=i+3*qW;

	    lstChnlVa.set(i, (lstChnlVa.get(i) - lstChnlVa.get(q1) - lstChnlVa.get(q2) + lstChnlVa.get(q3))*sw);
	    lstChnlVb.set(i,(lstChnlVb.get(i) - lstChnlVb.get(q1) - lstChnlVb.get(q2) + lstChnlVb.get(q3))*sw);
	    lstChnlVc.set(i,(lstChnlVc.get(i) - lstChnlVc.get(q1) - lstChnlVc.get(q2) + lstChnlVc.get(q3))*sw);
//	    Va[i]= (Va[i]-Va[q1]-Va[q2]+Va[q3])*sw;
//	    Vb[i]= (Vb[i]-Vb[q1]-Vb[q2]+Vb[q3])*sw;
//	    Vc[i]= (Vc[i]-Vc[q1]-Vc[q2]+Vc[q3])*sw;

	    if (lstChnlIa != null && !lstChnlIa.isEmpty())
	    {
	    	lstChnlIa.set(i,(lstChnlIa.get(i) - lstChnlIa.get(q1) - lstChnlIa.get(q2) + lstChnlIa.get(q3))*sw);
	    }
	    if (lstChnlIb != null && !lstChnlIb.isEmpty())
	    {
	    	lstChnlIb.set(i,(lstChnlIb.get(i) - lstChnlIb.get(q1) - lstChnlIb.get(q2) + lstChnlIb.get(q3))*sw);
	    }
	    if (lstChnlIc != null && !lstChnlIc.isEmpty())
	    {
	    	lstChnlIc.set(i,(lstChnlIc.get(i) - lstChnlIc.get(q1) - lstChnlIc.get(q2) + lstChnlIc.get(q3))*sw);
	    }
	    if (lstChnlIn != null && !lstChnlIn.isEmpty())
	    {
	    	lstChnlIn.set(i,(lstChnlIn.get(i) - lstChnlIn.get(q1) - lstChnlIn.get(q2) + lstChnlIn.get(q3))*sw);
	    }
//	    Ia[i]= (Ia[i]-Ia[q1]-Ia[q2]+Ia[q3])*sw;
//	    Ib[i]= (Ib[i]-Ib[q1]-Ib[q2]+Ib[q3])*sw;
//	    Ic[i]= (Ic[i]-Ic[q1]-Ic[q2]+Ic[q3])*sw;
//
//	    In[i]= (In[i]-In[q1]-In[q2]+In[q3])*sw;
	  } //end for (i=0 ...
//	  if (!lstChnlVaTemp.isEmpty())
//	  {
//		  logger.debug("new list "+lstChnlVaTemp);
//		  mapData.put(LineChannels.CHAN_VA, lstChnlVaTemp);
//		  mapData.put(LineChannels.CHAN_VB, lstChnlVbTemp);
//		  mapData.put(LineChannels.CHAN_VC, lstChnlVcTemp);
//		  mapData.put(LineChannels.CHAN_IA, lstChnlIaTemp);
//		  mapData.put(LineChannels.CHAN_IB, lstChnlIbTemp);
//		  mapData.put(LineChannels.CHAN_IC, lstChnlIcTemp);
//		  mapData.put(LineChannels.CHAN_IN, lstChnlInTemp);
//	  }
	
}


/*
 *  Returns True if line group is for Lightning purposes only & No FLOC is to be
 calculated.  False for a regular FLOC line
 Returns True only if NOT a regular line and passes Logic Funcion
 if ANY of the Line Channels are assigned then it is NOT a Ltg Only line
 * */
private boolean isLtgOnlyLine(M9kFaultReport faultReport, LineGroupsAlgorithm lineGroupsAlgorithm) throws M9000Exception
{
	M9kLineLogicFilter m9kLineLogicFilter = new M9kLineLogicFilter();
//	System.out.println("About to check Logic for linegroup name "+lineGroupsAlgorithm.getLineGroupName());
	if (lineGroupsAlgorithm.getDecisionLogic() != null && !lineGroupsAlgorithm.getDecisionLogic().isEmpty())
	{
		faultReport.isLogicTrue = m9kLineLogicFilter.isLogicTrue(abnormalEvents, lineGroupsAlgorithm.getDecisionLogic());
	}
	else
	{
		faultReport.isLogicTrue = false;
	}
	return faultReport.isLogicTrue;
}

//calculate and check the needed values
private int calCheckNeededValues(LineGroupsAlgorithm lineGroupsAlgorithm) throws M9000Exception
{
  double r1 = lineGroupsAlgorithm.getPositiveResistance();// PosSeqR;
  double x1 = lineGroupsAlgorithm.getPositiveReactance();//PosSeqX;
  double r0 = lineGroupsAlgorithm.getZeroResistance();// ZeroSeqR;
  double x0 = lineGroupsAlgorithm.getZeroReactance();// ZeroSeqX;
  double absZ1;

//  if (Cmtrd->Cfg.NRates != 1)
//  {
//    ErrorString = "NRate != 1, fault location calculation cannot proceed";
//    return 1;
//  } //end Cmtrd->Cfg.NRates

  //number of data in quarter of a cycle (so call quarter wave, Qw)
  qW = (int)(comtradeData.getSamplesPerCycle() + 0.5)/4; //Cmtrd->Cfg.SampleRate[0].Rate / Cmtrd->Cfg.LineFreq / 4;
  logger.debug("qW "+qW+" samples per cycle "+comtradeData.getSamplesPerCycle());
  //check whether there are enough data
  if(nData < (qW*8))
  { 
	  logger.error("FaultLoc: ndata<2cycles ERROR!!");
	  throw new M9000Exception();
  }

  //Z
  //Phase impedance
  zP = (Math.sqrt(r1*r1 + x1*x1) * 1.5);
  //Phase-to-phase impedance
  zPP = (Math.sqrt( ((r1*r1*2) + (x1*x1*2))/3 ) * 1.5);
  //Phase-to-ground impedance
  zPG = (Math.sqrt((r1*2+r0)*(r1*2+r0) + (x1*2+x0)*(x1*2+x0)) * 1.5/3);

  // Constant k = (Z0L/Z1L - 1)/3
  //
  absZ1 = Math.sqrt(r1*r1 + x1*x1);
  zK = absZ1>1 ? ((Math.sqrt(r0*r0 + x0*x0)/absZ1 - 1) / 3) : M9kStationConstants.INFINITY_LARGE;

//  Zp = Zpg; Zpp = Zpg; //DEBUG ADD

  return 0;
} //end CalCheckNeededValues


private Map<LineChannels, List<Double>> getData(LineGroupsAlgorithm lineGroupsAlgorithm) throws M9000Exception 
{
//  ClearData(); 

  Map<LineChannels, List<Double>> mapData = m9kStationComtradeUtil.getAnalogDataForSelectedChannels(comtradeData, lineGroupsAlgorithm.getMapLineChannels());
  
  //
//  chn = Lines[li].Chan[CHAN_IN];
//  //if neutral chan is specified
//  if (chn && Cmtrd->AnalogData(Lines[li].Chan[CHAN_IN],0,FromDataNum,NData,In))
//        {ErrorString = Cmtrd->ErrorString; return 1;}
//
//  //read channels data
//  //Voltages
//  cha= Lines[li].Chan[CHAN_VA];
//  chb= Lines[li].Chan[CHAN_VB];
//  chc= Lines[li].Chan[CHAN_VC];
//
//  if (Get3PhasesData(cha,chb,chc,Va,Vb,Vc,NULL,NData)) return 1;
//
//  //currents (note: chn is assigned above)
//  cha= Lines[li].Chan[CHAN_IA];
//  chb= Lines[li].Chan[CHAN_IB];
//  chc= Lines[li].Chan[CHAN_IC];
//
//  if (Get3PhasesData(cha,chb,chc,Ia,Ib,Ic,(chn ? In : NULL),NData)) return 1;

  // TODO: Automatic calculation of neutral channels need to be implemented
  
  //if neutral chan is not specified, In will be the -(sum of three current)
  //From Kirchoff's current law, all current go to the same point have to
  //sum to zero. i.e. Ia+Ib+Ic+In=0
//  if (!chn) {for (i=0;i<NData;i++) In[i]=-Ia[i]-Ib[i]-Ic[i];}

  return mapData;
} //end GetData


private int findFaultWindow(Map<LineChannels, List<Double>> mapData) throws M9000Exception 
{
  Integer ndata=0;
  currentFaultReport.start = 0;
  currentFaultReport.end = 0;
  currentFaultReport.thrI = 0.0;
  ndata=findMaxIWindow(mapData);
  logger.debug("start "+currentFaultReport.start+" end "+currentFaultReport.end+" thrI "+currentFaultReport.thrI);
//  if (!ndata) return 1;

  getCurrentFaultReport().faultNumData = ndata;
  getCurrentFaultReport().faultCycles  = (float)ndata/(4*qW);

  findSameFaultTypeWindow(mapData);

  return ndata;
} //end FindFaultWindow

/*-FindMaxIWindow---------------------------------------------------------------
 private functions (ONLY called by FindFautlWindow)
 return number of data in the window on success else 0

 thrI pointer -  is assigned here


 (1) Find Absolute Value Max of All Currents (Phases A, B & C)
     - Find MaxIa, MaxIb, MaxIc
     - Take the maxI = greatest of the 3
     - Assign onPhs to that Phase 'A', 'B' or 'C'

 (2) Find window where the onPhs current is above threshold for at least 2 cyc.
     - keep lowering threshold until window is 2 cycles
     - if threshold reaches 1% of maxI without being 2 cycles in length, then
       no FLOC can be found FAIL

 (3) start & end pointers contain the window sample points

 (4) FmChooseLine parameters
        int    ForcedLine;
        int    ForcedType;
        int    TotalLines;
        int    ForcedStartSample;
        int    ForcedSampleWidth;
        double ForcedMinCycles;

                                                                              */
private int findMaxIWindow(Map<LineChannels, List<Double>> mapData) throws M9000Exception
{
   double maxIa,maxIb,maxIc,maxI;
   double f;   //factor
   int n, least = 8 * qW; // number of data default 2 Cycles worth of samples
   currentFaultReport.onPhs = 'A';

//#ifdef MASTER_PROGRAM
//   if(FormChooseLine->ForcedMinCycles != 2.0)
//      least = Qw * FormChooseLine->ForcedMinCycles * 4.0;
//#endif

   if (mapData.get(LineChannels.CHAN_IA) != null)
   {
	   maxIa=findAbsMax(mapData.get(LineChannels.CHAN_IA));
	   maxI=maxIa;
   }
   else
   {
	   throw new M9000Exception("FindMaxIWindow: fault data less than 2*Cycles");
   }
   if (mapData.get(LineChannels.CHAN_IB) != null)
   {
	   maxIb=findAbsMax(mapData.get(LineChannels.CHAN_IB));
	   if (maxI<maxIb){ maxI=maxIb; currentFaultReport.onPhs++; }
   }
   else
   {
	   throw new M9000Exception("FindMaxIWindow: fault data less than 2*Cycles");
   }
   if (mapData.get(LineChannels.CHAN_IC) != null)
   {
	   maxIc=findAbsMax(mapData.get(LineChannels.CHAN_IC));
	   if (maxI<maxIc){ maxI=maxIc; currentFaultReport.onPhs++; }
   }
   else
   {
	   throw new M9000Exception("FindMaxIWindow: fault data less than 2*Cycles");
   }

   f = 0.5;

   do
   {
      currentFaultReport.thrI = maxI*f;
      logger.debug("Threshold current "+currentFaultReport.thrI);
      n=findThresholdWindow(mapData);
      if (n > least /*8*Qw*/) return n; //success data> least samples
      f *= 0.9;
   }
   while (f>=0.01);

   // If Falls through to here, then "No Calculable FLOC"
   logger.error("FindMaxIWindow: fault data less than 2*Cycles");
   throw new M9000Exception("FindMaxIWindow: fault data less than 2*Cycles");   //fail    end
} //end FindMaxIWindow

/*- FindThresholdWindow( ) -----------------------------------------------------
 ONLY called by FindMaxIWindow
 return number of data in the window

 start is the beginning sample#
 end   is the last sample#

                                                                              */
private int findThresholdWindow(Map<LineChannels, List<Double>> mapData)
{
  int i = 0;
  int j=nDataFiltered-1;
  double absIa, absIb, absIc;

  double rmsIa =0.0, rmsIb =0.0, rmsIc =0.0;
  double iRmsIa=0.0, iRmsIb=0.0, iRmsIc=0.0;
  double sumIa =0.0, sumIb =0.0, sumIc =0.0;
  double thrRms = 0;

//#ifdef MASTER_PROGRAM
//  // Force Window
//  if(FormChooseLine->ByZoom)
//  {
//     i = *start;
//     j = *end;
//  }
//#endif

  List<Double> lstChnlIa = mapData.get(LineChannels.CHAN_IA);
  List<Double> lstChnlIb = mapData.get(LineChannels.CHAN_IB);
  List<Double> lstChnlIc = mapData.get(LineChannels.CHAN_IC);
  
  //calculate start: Calculate the Irms at the point 1 phase 1st rises above thrI
  while (i < j &&
         (absIa=Math.abs(lstChnlIa.get(i))) < currentFaultReport.thrI &&
         (absIb=Math.abs(lstChnlIb.get(i))) < currentFaultReport.thrI &&
         (absIc=Math.abs(lstChnlIc.get(i))) < currentFaultReport.thrI   )
  {
     getRMS(i, sumIa, rmsIa, absIa, lstChnlIa);
     getRMS(i, sumIb, rmsIb, absIb, lstChnlIb);
     getRMS(i, sumIc, rmsIc, absIc, lstChnlIc);

     // Save the Rms at thrI point
     if(iRmsIa == 0.0) iRmsIa = rmsIa;
     if(iRmsIb == 0.0) iRmsIb = rmsIb;
     if(iRmsIc == 0.0) iRmsIc = rmsIc;
     ++i;
  }
  currentFaultReport.start = i;
  logger.debug("Start "+currentFaultReport.start);
  switch(currentFaultReport.onPhs)
  {
     case 'A': thrRms = rmsIa; break;
     case 'B': thrRms = rmsIb; break;
     case 'C': thrRms = rmsIc; break;
  }

  j = i + 1;

  // Continue and find the point where the RMS drops below
  while(j < nDataFiltered-1)
  {
     getRMS(j, sumIa, rmsIa, Math.abs(lstChnlIa.get(j)), lstChnlIa);
     getRMS(j, sumIb, rmsIb, Math.abs(lstChnlIb.get(j)), lstChnlIb);
     getRMS(j, sumIc, rmsIc, Math.abs(lstChnlIc.get(j)), lstChnlIc);

     //if(rmsIa < (5 * iRmsIa) && rmsIb < (5 * iRmsIb) && rmsIc < (5 * iRmsIc))break;
     if(rmsIa < thrRms && rmsIb < thrRms && rmsIc < thrRms) break;
     j++;
  }
  //find end
  while (j > i && Math.abs(lstChnlIa.get(j)) < currentFaultReport.thrI && Math.abs(lstChnlIb.get(j)) < currentFaultReport.thrI && Math.abs(lstChnlIc.get(j)) < currentFaultReport.thrI) --j;
  currentFaultReport.end = j;
  logger.debug("End "+currentFaultReport.end);
  return (j-i+1);  //return number of data in the window
} //end FindThresholdWindow( )

//return absolute maximum amplitude of v where n is number of data in v
double findAbsMax(List<Double> lstAnalogData)
{
  double max=0, absV;
  //int nMax;

//  for (int i = 0; i < n; i++)
  for (Iterator<Double> iterator = lstAnalogData.iterator(); iterator.hasNext();) 
  {
     absV = Math.abs(iterator.next());
     if(max < absV)
     {
        max  = absV;
        //nMax = i;
     }
  }

  return max;
} //end FindAbsMax

private void getRMS(int samp_i, double sum, double rms, double absI, List<Double> lstChnlI)
{
   sum += absI;
   if(samp_i > (4*qW)-1)
   {
      sum -= Math.abs(lstChnlI.get(samp_i-4*qW));
      rms = sum/(4*qW);
   }
}

/*- FindSameFaultTypeWindow( ) -------------------------------------------------
ONLY called by FindFaultWindow
Finds a largest window that agrees on the same fault type.
If the window is large enough, quantities are truncated from both the head
and the tail to reduce DC offset influence.
                                                                             */
void findSameFaultTypeWindow(Map<LineChannels, List<Double>> mapData) throws M9000Exception
{
  int mfl=0;     //max fault length
  int mfs=currentFaultReport.start; //fault starting data index where max fault length occur
  int fl=0;      //fault length
  int trc;       //truncate
  int di;        //data index
  FaultTypes mft = M9kFaultReport.forceFaultType; //UNKNOWN_FAULT; //fault type with max fault length
  FaultTypes ft  = M9kFaultReport.forceFaultType; //UNKNOWN_FAULT; //fault type
  FaultTypes x;

  logger.debug("Start "+currentFaultReport.start+"End "+currentFaultReport.end);
  for(di = currentFaultReport.end; di >= currentFaultReport.start; di--)
  {
     if(M9kFaultReport.forceFaultType.compareTo(FaultTypes.UNKNOWN_FAULT) > 0)
        x = M9kFaultReport.forceFaultType;
     else
        x = getFaultType(di,currentFaultReport.thrI, mapData);

     if (x == ft) fl++;
     else
     {
        if(fl > mfl && ft != FaultTypes.UNKNOWN_FAULT){ mft = ft; mfl = fl; mfs = di+1; }
        ft = x; fl = 1;
     } //end else
  } //end for

  if(fl > mfl && ft != FaultTypes.UNKNOWN_FAULT){ mft = ft; mfl = fl; mfs = di+1; }

  if(mfl > 22*qW) mfl = 22*qW; //max fault length is 5.5 cycles

  trc = mfl < 4*qW  ? 0    :  // 0 cycle
        mfl < 6*qW  ? qW   :  // 1/4 cycle
        mfl < 12*qW ? 2*qW :  // 1/2 cycle
        mfl < 16*qW ? 4*qW :  // 1 cycle
        mfl < 20*qW ? 6*qW :  // 1.5 cycles
                      8*qW ;  // 2 cycles

  //the fault window
  FaultStartDataIndex = mfs + trc;
  FaultEndDataIndex = mfs + mfl - trc;

  //Report.FaultCycles=(float)(FaultEndDataIndex-FaultStartDataIndex+1)/(Qw*4.0);

  reportFaultType(mft);
} //end FindSameFaultTypeWindow


//i is data index of Va,Vb,Vc,Ia,Ib,Ic,In (base-0)
private FaultTypes getFaultType(int di,double thrI,Map<LineChannels, List<Double>> mapData) throws M9000Exception
{
  double ampVa,ampVb,ampVc,ampIa,ampIb,ampIc,ampIn;
  double za,zb,zc;
  FaultTypes ft;

  List<Double> lstChnlVa = mapData.get(LineChannels.CHAN_VA);
  List<Double> lstChnlVb = mapData.get(LineChannels.CHAN_VB);
  List<Double> lstChnlVc = mapData.get(LineChannels.CHAN_VC);
  List<Double> lstChnlIa = mapData.get(LineChannels.CHAN_IA);
  List<Double> lstChnlIb = mapData.get(LineChannels.CHAN_IB);
  List<Double> lstChnlIc = mapData.get(LineChannels.CHAN_IC);
  List<Double> lstChnlIn = mapData.get(LineChannels.CHAN_IN);

  try
  {
  //Calculate the true vector amplitude of Va,Vb,Vc,Ia,Ib,Ic
  ampVa = Math.sqrt( lstChnlVa.get(di)*lstChnlVa.get(di) + lstChnlVa.get(di+qW)*lstChnlVa.get(di+qW));
  ampVb = Math.sqrt( lstChnlVb.get(di)*lstChnlVb.get(di) + lstChnlVb.get(di+qW)*lstChnlVb.get(di+qW));
  ampVc = Math.sqrt( lstChnlVc.get(di)*lstChnlVc.get(di) + lstChnlVc.get(di+qW)*lstChnlVc.get(di+qW));
  ampIa = Math.sqrt( lstChnlIa.get(di)*lstChnlIa.get(di) + lstChnlIa.get(di+qW)*lstChnlIa.get(di+qW));
  ampIb = Math.sqrt( lstChnlIb.get(di)*lstChnlIb.get(di) + lstChnlIb.get(di+qW)*lstChnlIb.get(di+qW));
  ampIc = Math.sqrt( lstChnlIc.get(di)*lstChnlIc.get(di) + lstChnlIc.get(di+qW)*lstChnlIc.get(di+qW));
  ampIn = Math.sqrt( lstChnlIn.get(di)*lstChnlIn.get(di) + lstChnlIn.get(di+qW)*lstChnlIn.get(di+qW));
//  ampVb = sqrt( Vb[di]*Vb[di] + Vb[di+Qw]*Vb[di+Qw] );
//  ampVc = sqrt( Vc[di]*Vc[di] + Vc[di+Qw]*Vc[di+Qw] );
//  ampIa = sqrt( Ia[di]*Ia[di] + Ia[di+Qw]*Ia[di+Qw] );
//  ampIb = sqrt( Ib[di]*Ib[di] + Ib[di+Qw]*Ib[di+Qw] );
//  ampIc = sqrt( Ic[di]*Ic[di] + Ic[di+Qw]*Ic[di+Qw] );
//  ampIn = sqrt( In[di]*In[di] + In[di+Qw]*In[di+Qw] );

  //Impedance of channels a, b, c
  za = ampIa > 0 ? ampVa/ampIa : M9kStationConstants.INFINITY_LARGE;
  zb = ampIb > 0 ? ampVb/ampIb : M9kStationConstants.INFINITY_LARGE;
  zc = ampIc > 0 ? ampVc/ampIc : M9kStationConstants.INFINITY_LARGE;

  //assign fault Type
/* DEBUG CUT
  ft  =  za<Zp && zb<Zp && zc<Zp            ? ABC_FAULT :
         za<zc && zb<zc && za<Zpp && zb<Zpp ? AB_FAULT  :
         zb<za && zc<za && zb<Zpp && zc<Zpp ? BC_FAULT  :
         zc<zb && za<zb && zc<Zpp && za<Zpp ? CA_FAULT  :
         za<zb && za<zc && za<Zpg           ? AG_FAULT  :
         zb<za && zb<zc && zb<Zpg           ? BG_FAULT  :
         zc<za && zc<zb && zc<Zpg           ? CG_FAULT  :
         UNKNOWN_FAULT;            //else unknown
*/
//* DEBUG ADD
  //za,zb,zc are calculate referencing to ground so Zpg should be used in all cases.
  ft  =  za<zPG && zb<zPG && zc<zPG         ? FaultTypes.ABC_FAULT :
         za<zc && zb<zc && za<zPG && zb<zPG ? FaultTypes.AB_FAULT  :
         zb<za && zc<za && zb<zPG && zc<zPG ? FaultTypes.BC_FAULT  :
         zc<zb && za<zb && zc<zPG && za<zPG ? FaultTypes.CAG_FAULT :
         za<zb && za<zc && za<zPG           ? FaultTypes.AG_FAULT  :
         zb<za && zb<zc && zb<zPG           ? FaultTypes.BG_FAULT  :
         zc<za && zc<zb && zc<zPG           ? FaultTypes.CG_FAULT  :
        FaultTypes.UNKNOWN_FAULT;            //else unknown
//*/

  //check double-line-to-ground fault
  //if ground current larger than threshold I
  if (ampIn>thrI)
  {
    switch (ft)
    {
      case AB_FAULT: ft=FaultTypes.ABG_FAULT; break;
      case BC_FAULT: ft=FaultTypes.BCG_FAULT; break;
      case CA_FAULT: ft=FaultTypes.CAG_FAULT; break;
	default:
		break;
    } //end switch (ft)
  } //end if (ampIn>thrI)
  }
  catch (Exception e) {
	  e.printStackTrace();
	logger.error("Failed to identify fault Type"+e);
	throw new M9000Exception("Failed to identify fault Type", e);
}
  return ft;
} //end FaultType

private void reportFaultType(FaultTypes ftype)
{
   // if Override ...
   if(M9kFaultReport.forceFaultType != FaultTypes.UNKNOWN_FAULT) ftype = M9kFaultReport.forceFaultType;

   getCurrentFaultReport().faultType = ftype;

   switch (ftype)
   {
      case ABC_FAULT: getCurrentFaultReport().faultTypeString = "ABC"; break;
      case AB_FAULT:  getCurrentFaultReport().faultTypeString = "AB";  break;
      case BC_FAULT:  getCurrentFaultReport().faultTypeString = "BC";  break;
      case CA_FAULT:  getCurrentFaultReport().faultTypeString = "CA";  break;
      case ABG_FAULT: getCurrentFaultReport().faultTypeString = "ABG";  break;
      case BCG_FAULT: getCurrentFaultReport().faultTypeString = "BCG";  break;
      case CAG_FAULT: getCurrentFaultReport().faultTypeString = "CAG";  break;
      case AG_FAULT:  getCurrentFaultReport().faultTypeString = "AG";  break;
      case BG_FAULT:  getCurrentFaultReport().faultTypeString = "BG";  break;
      case CG_FAULT:  getCurrentFaultReport().faultTypeString = "CG";  break;
      default:        getCurrentFaultReport().faultTypeString = "UNKNOWN";
   } //end switch
} //end ReportFaultType

/*- CalFaultLoc( ) -------------------------------------------------------------
1. Is there n #of continuous samples of the same fault type?
   This must be n samples FaultEndDataIndex - FaultStartDataIndex + 1

2.

                                                                           --*/
private void calFaultLoc(LineGroupsAlgorithm lineGroupsAlgorithm, Map<LineChannels, List<Double>> mapData) throws M9000Exception
{
  int    i;            //line index
  double  r1 = lineGroupsAlgorithm.getPositiveResistance(); //PosSeqR
  double  r0 = lineGroupsAlgorithm.getZeroResistance(); //ZeroSeqR;
  double  rt;
  double  angle, arc, dst, rxt;
  double angleSum,arcSum,dstSum,rxtSum;
  double angleSumSq,arcSumSq,dstSumSq,rxtSumSq;
  double  va,vaq,vb,vbq,ia,iaq,ib,ibq;
  double  in,inq,ir = 0;
  double  absV,absI;
  int    n = FaultEndDataIndex - FaultStartDataIndex + 1;
//  int    j;

  clearFlocReport( );

  logger.debug("FaultStartDataIndex "+FaultStartDataIndex);
  logger.debug("FaultEndDataIndex "+FaultEndDataIndex);
  if(n < 2)
  {
	  logger.error("CalFaultLoc: ndata in fault period < 2 samples."); 
	  throw new M9000Exception("CalFaultLoc: ndata in fault period < 2 samples.");
  }
try
{
	  getCurrentFaultReport().isResultReasonable = false;
	  List<Double> lstChnlVa = mapData.get(LineChannels.CHAN_VA);
	  List<Double> lstChnlVb = mapData.get(LineChannels.CHAN_VB);
	  List<Double> lstChnlVc = mapData.get(LineChannels.CHAN_VC);
	  List<Double> lstChnlIa = mapData.get(LineChannels.CHAN_IA);
	  List<Double> lstChnlIb = mapData.get(LineChannels.CHAN_IB);
	  List<Double> lstChnlIc = mapData.get(LineChannels.CHAN_IC);
	  List<Double> lstChnlIn = mapData.get(LineChannels.CHAN_IN);
	
	  //initialize sum values to zero
	  angleSum   = arcSum   = dstSum   = rxtSum   = 0;
	  angleSumSq = arcSumSq = dstSumSq = rxtSumSq = 0;
	
	  //=== Find rt
	  switch (getCurrentFaultReport().faultType)
	  {
	     case ABC_FAULT:                              rt = r1; break;
	     case AG_FAULT: case BG_FAULT: case CG_FAULT: rt = (r1*2.0 + r0)/3.0; break;
	     default:                                     rt = r1*2.0; //AB(G),BC(G),CA(G)
	  } //end switch
	
	  //=== Assign Vp1,Vp2,Ip1,Ip2 pointers for the two phases that are shorted
	  //Note: if it is phase-to-ground fault, vp2 and ip2 will be the unaffected phase
	  //voltage and current used for referencing.
	  switch (getCurrentFaultReport().faultType)
	  {
	     case ABC_FAULT:
	        chooseTwoPhasesForABCFault(mapData); break;
	     case CA_FAULT: case CAG_FAULT: case CG_FAULT:
	        setLstFaultChnlVFirst(lstChnlVc); setLstFaultChnlVSecond(lstChnlVa); setLstFaultChnlIFirst(lstChnlIc); setLstFaultChnlISecond(lstChnlIa); break;
	     case AB_FAULT: case ABG_FAULT: case AG_FAULT:
	    	 setLstFaultChnlVFirst(lstChnlVa); setLstFaultChnlVSecond(lstChnlVb); setLstFaultChnlIFirst(lstChnlIa); setLstFaultChnlISecond(lstChnlIb); break;
	     default: //case BC_FAULT: case BG_FAULT: case UNKNOWN_FAULT:
	    	 setLstFaultChnlVFirst(lstChnlVb); setLstFaultChnlVSecond(lstChnlVc); setLstFaultChnlIFirst(lstChnlIb); setLstFaultChnlISecond(lstChnlIc); break;
	  } //end switch
	
	  //=== Calculate dstSum i.e. distance to fault plus other related values
//	  for (i=FaultStartDataIndex,j=1; i<=FaultEndDataIndex; i++,j++)
	  for (i=FaultStartDataIndex; i<=FaultEndDataIndex; i++)
	  {
	     va = getLstFaultChnlVFirst().get(i); 
	     vaq = getLstFaultChnlVFirst().get(i+qW); 
	     vb = getLstFaultChnlVSecond().get(i); 
	     vbq = getLstFaultChnlVSecond().get(i+qW);
	     ia = getLstFaultChnlIFirst().get(i); 
	     iaq = getLstFaultChnlIFirst().get(i+qW); 
	     ib = getLstFaultChnlISecond().get(i); 
	     ibq = getLstFaultChnlISecond().get(i+qW);
	     // in=In[i]; inq=In[i+Qw]; ir=sqrt( in*in + inq*inq );
	     // if single line to ground fault (note: ir is only being used for ground fault)
	     if (isSLGFault( ))
	     {
	        vb=vbq=ib=ibq=0;
	        //TODO: IMplement virtual channel concept
	        //From Kirchoff's current law, all current go to the same point have to
	        //sum to zero. i.e. Ia+Ib+Ic+In=0
	//        in  = HasVirtualChan ? In[i]    : (-Ia[i]-Ib[i]-Ic[i]);           //in=In[i];
	//        inq = HasVirtualChan ? In[i+Qw] : (-Ia[i+Qw]-Ib[i+Qw]-Ic[i+Qw]);  //inq=In[i+Qw];
	
	        //TODO: Assuming no virtual channels
	       if (lstChnlIn != null && !lstChnlIn.isEmpty())
	       {
	    	   in  = lstChnlIn.get(i);           //in=In[i];
	 	      inq = lstChnlIn.get(i+qW) ;  //inq=In[i+Qw];
	       }
	       else
	       {
		      in = (-lstChnlIa.get(i)-lstChnlIb.get(i)-lstChnlIc.get(i));
		      inq = (-lstChnlIa.get(i+qW)-lstChnlIb.get(i+qW)-lstChnlIc.get(i+qW));
	       }
	        ir=Math.sqrt( in*in + inq*inq ); // Always gives you the Phasor Magnitude
	     } //end if (IsSLGFault())
	     
	     angle = angleOfVandI(va-vb,vaq-vbq,ia-ib,iaq-ibq);
	     absV  = Math.sqrt( (va-vb)*(va-vb) + (vaq-vbq)*(vaq-vbq) );
	     absI  = Math.sqrt( (ia-ib)*(ia-ib) + (iaq-ibq)*(iaq-ibq) );
	
	     // using fabs(...) for calculating rxt, dst and arc since they cannot be negative
	     // but sin(angle) or cos(angle) can be negative.
	
	     //if single-line-to-ground fault
	     if (isSLGFault())
	       rxt=((absI + ir*zK)!=0.0)? Math.abs(Math.sin(angle)*absV/(absI + ir*zK)) : M9kStationConstants.INFINITY_LARGE;
	     else
	       rxt= (absI!=0.0) ? Math.abs(Math.sin(angle)*absV/absI) : M9kStationConstants.INFINITY_LARGE;
	
	     //V = xZI ->  x = distance = Im(V/ZI) = Im(V/I)/Im(Z) where Im(.) is imaginary.
	     dst = (lineGroupsAlgorithm.getPositiveReactance()!=0.0) ? Math.abs(rxt/lineGroupsAlgorithm.getPositiveReactance()) : M9kStationConstants.INFINITY_LARGE;
	     
	     arc= (absI!=0.0) ? Math.abs(Math.cos(angle)*absV/absI - dst*rt) : M9kStationConstants.INFINITY_LARGE;
	     if (arc<0) arc = 0;
	     //
	     angleSum   += angle; arcSum += arc;
	     dstSum     += dst;   rxtSum += rxt;
	     angleSumSq += angle*angle; dstSumSq += dst*dst;
	     arcSumSq   += arc*arc;  rxtSumSq += rxt*rxt;
	  } //end for (i=FaultStartDataIndex ...
	
	  //=== Report the distance to fault and other related values
	  //distance to fault
	  getCurrentFaultReport().faultMiles     = (float)((dstSum/n)*lineGroupsAlgorithm.getLineMiles());
	  getCurrentFaultReport().lineMiles      = (float)lineGroupsAlgorithm.getLineMiles();  // New 2/2006 for Dominion
	  getCurrentFaultReport().faultAngle     = (float)(angleSum/n);
	  getCurrentFaultReport().arcResistance  = (float)(arcSum/n);
	  getCurrentFaultReport().posReactance   = (float)(rxtSum/n);
	  //standard deviation
	  //fabs(...) is for protection from computer crushing in case of sqrt(-ve)
	  getCurrentFaultReport().faultAngleStdDev = (float)Math.sqrt( Math.abs(angleSumSq-(angleSum*angleSum)/n)/(n-1) );
	  getCurrentFaultReport().arcResistanceStdDev = (float)Math.sqrt( Math.abs(arcSumSq-(arcSum*arcSum)/n)/(n-1) );
	  getCurrentFaultReport().faultMilesStdDev =
	    (float)(Math.sqrt( Math.abs(dstSumSq-(dstSum*dstSum)/n)/(n-1) )*lineGroupsAlgorithm.getLineMiles());
	  getCurrentFaultReport().posReactanceStdDev = (float)Math.sqrt( Math.abs(rxtSumSq-(rxtSum*rxtSum)/n)/(n-1) );
	  //is result reasonable ?
	  getCurrentFaultReport().isResultReasonable = true;
	  if (getCurrentFaultReport().faultMiles>=(lineGroupsAlgorithm.getLineMiles()*1.15))
	  {
	     getCurrentFaultReport().description += "Fault location beyond line length;";
	     getCurrentFaultReport().isResultReasonable = false;
	  }
	  if (getCurrentFaultReport().faultMilesStdDev>=(lineGroupsAlgorithm.getLineMiles()*0.15))
	  {
	     getCurrentFaultReport().description += "Fault location StdDev too large;";
	     getCurrentFaultReport().isResultReasonable = false;
	  }
	
	  //is Power lagging or leading
	  if (getCurrentFaultReport().faultAngle<=0) getCurrentFaultReport().description += "Power flowing from the station to the line;"; //lagging
	  else                      getCurrentFaultReport().description += "Power flowing from the line to the station;"; //leading
}
catch (Exception e) {
	logger.error("Error in Calculating fault location",e);
	throw new M9000Exception(e);
}
} //end CalFaultLoc

private void clearFlocReport()
{
   getCurrentFaultReport().faultAngle          = 0.0f;
   getCurrentFaultReport().faultMiles          = -99.0f; // Default to < 0 incase no calc is made.
   getCurrentFaultReport().arcResistance       = 0.0f;
   getCurrentFaultReport().posReactance        = 0.0f;
   getCurrentFaultReport().faultAngleStdDev    = 0.0f;
   getCurrentFaultReport().arcResistanceStdDev = 0.0f;
   getCurrentFaultReport().faultMilesStdDev    = 0.0f;
   getCurrentFaultReport().posReactanceStdDev  = 0.0f;
   getCurrentFaultReport().isResultReasonable  = false;
}

/*
  Report.FaultType    = 0;
  Report.FaultAngle   = 0.0;
  Report.FaultCycles  = 0.0;
  Report.FaultMiles   = 0.0;
*/

//---------------------------------------------------------------------------
//Only called by CalFaultLoc
private void chooseTwoPhasesForABCFault(Map<LineChannels, List<Double>> mapData)
{
 double ampVa,ampVb,ampVc,ampIa,ampIb,ampIc;
 double sumZa=0,sumZb=0,sumZc=0;

 List<Double> lstChnlVa = mapData.get(LineChannels.CHAN_VA);
 List<Double> lstChnlVb = mapData.get(LineChannels.CHAN_VB);
 List<Double> lstChnlVc = mapData.get(LineChannels.CHAN_VC);
 List<Double> lstChnlIa = mapData.get(LineChannels.CHAN_IA);
 List<Double> lstChnlIb = mapData.get(LineChannels.CHAN_IB);
 List<Double> lstChnlIc = mapData.get(LineChannels.CHAN_IC);
// List<Double> lstChnlIn = mapData.get(LineChannels.CHAN_IN);
 
 //---- Find sum of impedance
 for (int di=FaultStartDataIndex; di<=FaultEndDataIndex; di++) {
   //Calculate the true vector amplitude of Va,Vb,Vc,Ia,Ib,Ic
   ampVa = Math.sqrt( lstChnlVa.get(di)*lstChnlVa.get(di) + lstChnlVa.get(di+qW)*lstChnlVa.get(di+qW) );
   ampVb = Math.sqrt( lstChnlVb.get(di)*lstChnlVb.get(di) + lstChnlVb.get(di+qW)*lstChnlVb.get(di+qW) );
   ampVc = Math.sqrt( lstChnlVc.get(di)*lstChnlVc.get(di) + lstChnlVc.get(di+qW)*lstChnlVc.get(di+qW) );
   ampIa = Math.sqrt( lstChnlIa.get(di)*lstChnlIa.get(di) + lstChnlIa.get(di+qW)*lstChnlIa.get(di+qW) );
   ampIb = Math.sqrt( lstChnlIb.get(di)*lstChnlIb.get(di) + lstChnlIb.get(di+qW)*lstChnlIb.get(di+qW) );
   ampIc = Math.sqrt( lstChnlIc.get(di)*lstChnlIc.get(di) + lstChnlIc.get(di+qW)*lstChnlIc.get(di+qW) );
//   ampIn = Math.sqrt( lstChnlIn.get(di)*lstChnlIn.get(di) + lstChnlIn.get(di+qW)*lstChnlIn.get(di+qW) );
//   ampVb = Math.sqrt( Vb[di]*Vb[di] + Vb[di+Qw]*Vb[di+Qw] );
//   ampVc = Math.sqrt( Vc[di]*Vc[di] + Vc[di+Qw]*Vc[di+Qw] );
//   ampIa = Math.sqrt( Ia[di]*Ia[di] + Ia[di+Qw]*Ia[di+Qw] );
//   ampIb = Math.sqrt( Ib[di]*Ib[di] + Ib[di+Qw]*Ib[di+Qw] );
//   ampIc = Math.sqrt( Ic[di]*Ic[di] + Ic[di+Qw]*Ic[di+Qw] );
   
   //Impedance of channels a, b, c
   sumZa += ampIa > 0 ? ampVa/ampIa : M9kStationConstants.INFINITY_LARGE;
   sumZb += ampIb > 0 ? ampVb/ampIb : M9kStationConstants.INFINITY_LARGE;
   sumZc += ampIc > 0 ? ampVc/ampIc : M9kStationConstants.INFINITY_LARGE;
 } //end for

 //---- Determine which two phases to use
 if      (sumZa<sumZb && sumZc<sumZb) { setLstFaultChnlVFirst(lstChnlVc); setLstFaultChnlVSecond(lstChnlVa); setLstFaultChnlIFirst(lstChnlIc); setLstFaultChnlISecond(lstChnlIa); }
 else if (sumZa<sumZc && sumZb<sumZc) { setLstFaultChnlVFirst(lstChnlVa); setLstFaultChnlVSecond(lstChnlVb); setLstFaultChnlIFirst(lstChnlIa); setLstFaultChnlISecond(lstChnlIb); }
 else                                 { setLstFaultChnlVFirst(lstChnlVb); setLstFaultChnlVSecond(lstChnlVc); setLstFaultChnlIFirst(lstChnlIb); setLstFaultChnlISecond(lstChnlIc); }
} //end ChooseTwoPhasesForABCFault

//AngleOfVandI gives the angle of I reference to V so
//if I lag V, AngleOfVandI is -ve; if I lead V, AngleOfVandI is +ve
private double angleOfVandI(double v,double vq,double i,double iq)
{
double angle,vx,ix;

vx = (vq!=0) ? Math.atan(v/vq) : Math.PI/2; //if x-comp approaches 0, angle approaches 90
vx = (vq<0) ? vx+Math.PI : vx;           //if y-comp is neg, add 180 to flip sign

ix = (iq!=0) ? Math.atan(i/iq) : Math.PI/2; //  same for current
ix = (iq<0) ? ix+Math.PI : ix;           //

angle = ix - vx;

while (angle < (-Math.PI)) angle += Math.PI*2.0;
while (angle >= Math.PI) angle -= Math.PI*2.0;

return angle;
} //end AngleOfVandI


@SuppressWarnings("unused")
private boolean  isGrdFault() 
{ 
	return (getCurrentFaultReport().faultType.compareTo(FaultTypes.CA_FAULT) > 0); 
}  //Grd = Ground

private boolean  isSLGFault() 
{ 
	return (getCurrentFaultReport().faultType.compareTo(FaultTypes.CAG_FAULT) > 0); 
} //SLG = single line to ground

public M9kFaultReport getFaultReport() {
	return faultReport;
}

public void setFaultReport(M9kFaultReport faultReport) {
	this.faultReport = faultReport;
}

public Map<String, DigitalInfo> getAbnormalEvents() {
	return abnormalEvents;
}

public void setAbnormalEvents(Map<String, DigitalInfo> abnormalEvents) {
	this.abnormalEvents = abnormalEvents;
}
public ProcessDatDTO getComtradeData() {
	return comtradeData;
}
public void setComtradeData(ProcessDatDTO comtradeData) {
	this.comtradeData = comtradeData;
}
public List<LineGroupsAlgorithm> getLstLineGroups() {
	return lstLineGroups;
}
public void setLstLineGroups(List<LineGroupsAlgorithm> lstLineGroups) {
	this.lstLineGroups = lstLineGroups;
}

private M9kFaultReport getCurrentFaultReport() {
	return currentFaultReport;
}

private void setCurrentFaultReport(M9kFaultReport currentFaultReport) {
	this.currentFaultReport = currentFaultReport;
}

public List<Double> getLstFaultChnlVFirst() {
	return lstFaultChnlVFirst;
}

public void setLstFaultChnlVFirst(List<Double> lstFaultChnlVFirst) {
	this.lstFaultChnlVFirst = lstFaultChnlVFirst;
}

public List<Double> getLstFaultChnlVSecond() {
	return lstFaultChnlVSecond;
}

public void setLstFaultChnlVSecond(List<Double> lstFaultChnlVSecond) {
	this.lstFaultChnlVSecond = lstFaultChnlVSecond;
}

public List<Double> getLstFaultChnlIFirst() {
	return lstFaultChnlIFirst;
}

public void setLstFaultChnlIFirst(List<Double> lstFaultChnlIFirst) {
	this.lstFaultChnlIFirst = lstFaultChnlIFirst;
}

public List<Double> getLstFaultChnlISecond() {
	return lstFaultChnlISecond;
}

public void setLstFaultChnlISecond(List<Double> lstFaultChnlISecond) {
	this.lstFaultChnlISecond = lstFaultChnlISecond;
}

public List<Double> getLstFaultChnlRef() {
	return lstFaultChnlRef;
}

public void setLstFaultChnlRef(List<Double> lstFaultChnlRef) {
	this.lstFaultChnlRef = lstFaultChnlRef;
}

public static void main(String[] args)
{
	try {
		if (args.length < 2)
		{
			System.out.println("Usage: Requires two inputs. First configuration file sql or xml and Second Comtrade file name");
			System.exit(1);
		}
		String configFileName = args[0];
		if (!new File(configFileName).exists())
		{
			System.out.println("Configuration file doesn't exist "+configFileName+" Exiting.");
			System.exit(1);
		}

		String comtradeFileName = args[1];

		if (!comtradeFileName.endsWith("dat"))
		{
			if (comtradeFileName.indexOf(".") != -1)
			{
				comtradeFileName = comtradeFileName.substring(0, comtradeFileName.indexOf("."));
			}
			comtradeFileName+= ".dat";
		}
		if (!new File(comtradeFileName).exists())
		{
			System.out.println("Comtrade file doesn't exist "+comtradeFileName+" Exiting.");
			System.exit(1);
		}

//		String configCileContent = readConfigFile("C:/M9k/troubleshoot/Dominion/Chickahominy/config_07-24-19,10_49_49.xml");
//		String configCileContent = readConfigFile("C:/M9k/troubleshoot/Dominion/Chickahominy/Chickahominy-config-08072019_054201.sql");
		String configCileContent = readConfigFile(configFileName);
		String xmlContent;
		if (configCileContent.indexOf("<SubStation") == -1)
		{
			System.out.println("Invalid config file. "+args[1]);
		}
		if (!configCileContent.startsWith("<SubStation"))
		{
			xmlContent = configCileContent.substring(configCileContent.indexOf("<SubStation"), configCileContent.indexOf("</SubStation>")+"</SubStation>".length());
		}
		else
		{
			xmlContent = configCileContent;
		}
		
		M9kStationXMLUtil.initXml(xmlContent);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/USI 2002/Comtrade Data Files/040715,172956109,R83F0235,-5s,KETTLE CREEK,USI_2002,GTC.dat"), false);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/USI 2002/Comtrade Data Files/R06F0364.dat"), false);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/USI 2002/Comtrade Data Files/120218,221508995,-6td,KETTLE CREEK 230_115KV (GTC),USI_2002,Georgia Transmission Corporation,R83F3676.dat"), false);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/USI 2002/Comtrade Data Files/111119,135645060,-6td,KETTLE CREEK 230_115KV (GTC),USI_2002,Georgia Transmission Corporation,R83F3639.dat"), false);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/USI 2002/Comtrade Data Files/110915,125901076,-6td,KETTLE CREEK 230_115KV (GTC),USI_2002,Georgia Transmission Corporation,R83F3614.dat"), false); // Not working
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/USI 2002/Comtrade Data Files/110915,120237299,-6td,KETTLE CREEK 230_115KV (GTC),USI_2002,Georgia Transmission Corporation,R83F3608.dat"), false);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("//195.1.1.13/M9k/Data/3-Station Name/1_120319,142836183,-5t,Station Name,usi_m9000,USI.dat"), false);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("//195.1.1.13/M9k/Data/3-Station Name/1_120320,054845200,-5t,Station Name,usi_m9000,USI.dat"), false);
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/M9k/troubleshoot/ConEd/Buchanan/Buchanan-Faults/R16F2960_160316,181757500,-5t,Buchanan,USI_M9000,USI.dat"));
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/M9k/troubleshoot/ConEd/Millwood/faults/R07F726_160320,042736416,-5t,Millwood,USI_M9000,USI.dat"));
//		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File("C:/M9k/troubleshoot/ConEd/Pleasantville/faults/R14F549_160321,110749598,-5t,Pleasantville 345kV,USI_M9000,USI.dat"));
		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File(comtradeFileName));
		M9kFaultLocation m9kFaultLocation = new M9kFaultLocation(m9kComtradeParser.getProcessData());
		Map<LineGroupsAlgorithm, M9kFaultReport> mapFaultReport = m9kFaultLocation.findFaultLocation(true);
		M9kFaultReport faultReport;
		System.out.println("********************************************************************************************");
		for (LineGroupsAlgorithm lineGroup : mapFaultReport.keySet()) {
			System.out.println("Line Group Name: "+lineGroup.getLineGroupName());
//			System.out.println("Line group Name "+lineGroup.getLineGroupName());
			faultReport = mapFaultReport.get(lineGroup);
			if (faultReport != null)
			{
				System.out.println(faultReport.getFaultReportDetails());
//				System.out.println("Details "+faultReport.getFaultReportDetails());
			}
			System.out.println(String.format("%n"));
		}
		System.out.println("********************************************************************************************");
//		logger.debug("Fault Details "+mapFaultReport);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	logger.debug("Returning...");
	return;
}

public boolean isContinueProcessing() {
	return continueProcessing;
}

public void setContinueProcessing(boolean continueProcessing) {
	this.continueProcessing = continueProcessing;
}


private static String readConfigFile(String file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader (file));
    String         line = null;
    StringBuilder  stringBuilder = new StringBuilder();
    String         ls = System.getProperty("line.separator");

    try {
        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    } finally {
        reader.close();
    }
}
}