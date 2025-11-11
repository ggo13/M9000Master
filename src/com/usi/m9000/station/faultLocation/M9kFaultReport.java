package com.usi.m9000.station.faultLocation;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.usi.m9000.station.util.FaultTypes;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.triggers.LineGroupsAlgorithm;

public class M9kFaultReport {

	LineGroupsAlgorithm lineGroupsAlgorithm;
	Integer start;
	Integer end;
	Double thrI;
	char onPhs = 'A';
	static FaultTypes forceFaultType = FaultTypes.UNKNOWN_FAULT;
	  boolean   isFaultLocExist = false;
	  boolean   isResultReasonable;
	  boolean   isLogicTrue = false;
	  boolean   isLtgOnly;         // Provision:Lines w/LineID, but no Assoc. Chans or Impedances
	  boolean   manualFaultType;   // User has manually changed errant Fault Type
	  boolean   forceFloc;         // Force a FLOC regardless of IsLogicTrue Status
	  String description;       //
	  int    lineNum;
	  String lineName;
	  int    lineID;            //Line ID or Facility ID
	  double dfrLat;    //DFR Latitude
	  double dfrLon;    //DFR Longitude
	  String faultTypeString;   //type of fault
	  FaultTypes faultType = FaultTypes.UNKNOWN_FAULT;         //enumeration of fault type i.e. ABC_FAULT ...
	  float  faultCycles;       //duration of fault in cycles
	  int    faultNumData;      //
	  //first column result
	  float  faultAngle;         //in degree
	  float  faultAngleStdDev;   //standard deviation of FaultAngle
	  float  faultMiles;         //distance to fault in miles
	  float  faultMilesStdDev;
	  float  arcResistance;      //in Ohms
	  float  arcResistanceStdDev;
	  float  posReactance;       //in Ohms
	  float  posReactanceStdDev;
	  //Fault Examination
	  long inceptionTime;
	  int    inceptionDataIndex;   //base-0
	  float  faultExtendCycles;
	  boolean breakerRestrike;
	  float  rmsPrefault[];
	  float  faultMagnitude[];
	  float  percentOfPrefault[];
	  float  lineMiles;     // New 2/2006 for Dominion

	  public M9kFaultReport(LineGroupsAlgorithm lineGroupsAlgorithm) {
			this.lineGroupsAlgorithm = lineGroupsAlgorithm;
			initReport();
		}
	  public M9kFaultReport(LineGroupsAlgorithm lineGroupsAlgorithm, FaultTypes inForceFaultType) {
		  this.lineGroupsAlgorithm = lineGroupsAlgorithm;
		  forceFaultType= inForceFaultType;
		  initReport();
		}
	private void initReport() {
		if (lineGroupsAlgorithm.getLineGroupName() != null && !lineGroupsAlgorithm.getLineGroupName().isEmpty())
		{
			lineName           = lineGroupsAlgorithm.getLineGroupName();
		}
		else
		{
		    lineName           = lineGroupsAlgorithm.getName();
		}
		    lineID             = lineGroupsAlgorithm.getId();
		    dfrLat             = lineGroupsAlgorithm.getLattitude();
		    dfrLon             = lineGroupsAlgorithm.getLongitude();
		    isFaultLocExist    = false; //No fault location or inception time at first
		    inceptionDataIndex = 0;
		    description        = "";    //Clear description
		    faultType       = forceFaultType;
		    faultTypeString    = "";

		
	}

	public LineGroupsAlgorithm getLineGroupsAlgorithm() {
		return lineGroupsAlgorithm;
	}

	public void setLineGroupsAlgorithm(LineGroupsAlgorithm lineGroupsAlgorithm) {
		this.lineGroupsAlgorithm = lineGroupsAlgorithm;
	}
	@Override
	public String toString() {
		return "M9kFaultReport [lineGroupsAlgorithm=" + lineGroupsAlgorithm
				+ ", isFaultLocExist=" + isFaultLocExist
				+ ", isResultReasonable=" + isResultReasonable
				+ ", isLogicTrue=" + isLogicTrue + ", isLtgOnly=" + isLtgOnly
				+ ", manualFaultType=" + manualFaultType + ", forceFloc="
				+ forceFloc + ", description=" + description + ", lineNum="
				+ lineNum + ", lineName=" + lineName + ", lineID=" + lineID
				+ ", dfrLat=" + dfrLat + ", dfrLon=" + dfrLon
				+ ", faultTypeString=" + faultTypeString + ", faultType="
				+ faultType + ", faultCycles=" + faultCycles
				+ ", faultNumData=" + faultNumData + ", faultAngle="
				+ faultAngle + ", faultAngleStdDev=" + faultAngleStdDev
				+ ", faultMiles=" + faultMiles + ", faultMilesStdDev="
				+ faultMilesStdDev + ", arcResistance=" + arcResistance
				+ ", arcResistanceStdDev=" + arcResistanceStdDev
				+ ", posReactance=" + posReactance + ", posReactanceStdDev="
				+ posReactanceStdDev + ", inceptionTime=" + inceptionTime
				+ ", inceptionDataIndex=" + inceptionDataIndex
				+ ", faultExtendCycles=" + faultExtendCycles
				+ ", breakerRestrike=" + breakerRestrike + ", rmsPrefault="
				+ Arrays.toString(rmsPrefault) + ", faultMagnitude="
				+ Arrays.toString(faultMagnitude) + ", percentOfPrefault="
				+ Arrays.toString(percentOfPrefault) + ", lineMiles="
				+ lineMiles + "]";
	}
	public String getFaultTypeString() {
		return faultTypeString;
	}
	public void setFaultTypeString(String faultTypeString) {
		this.faultTypeString = faultTypeString;
	}
	public float getFaultCycles() {
		return faultCycles;
	}
	public void setFaultCycles(float faultCycles) {
		this.faultCycles = faultCycles;
	}
	public float getFaultMiles() {
		return faultMiles;
	}
	public void setFaultMiles(float faultMiles) {
		this.faultMiles = faultMiles;
	}

	public String getFaultReportDetails()
	{
		StringBuffer faultReportDetails = new StringBuffer();
		String type = getFaultTypeString();
		double distance = getFaultMiles();
		DecimalFormat df = new DecimalFormat("0.000");
		if (type.equalsIgnoreCase("UNKNOWN"))
		{
			type+="(HighZ?)";
		}
		faultReportDetails.append("Type: "+type+M9kStationConstants.NEWLINE);
		faultReportDetails.append("Duration: "+df.format(getFaultCycles())+" Cycles"+M9kStationConstants.NEWLINE);
		if (distance >= 0)
		{
			faultReportDetails.append("Distance: "+df.format(getFaultMiles())+" Miles ("+df.format(getFaultMiles()*1.61)+" Km)"+M9kStationConstants.NEWLINE);
		}
		else
		{
			faultReportDetails.append("Distance: None found"+M9kStationConstants.NEWLINE);
		}
		return faultReportDetails.toString();
	}
}
