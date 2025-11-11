package com.usi.m9000.station.util;

public class M9kStationConstants {
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String ASCII = "ASCII";
	public static final String BINARY = "BINARY";
	public static final String COMMA_SEPERATOR = ",";
	public static final String ACTIVE_STATUS = "Active";
	public static final String INACTIVE_STATUS = "Inactive";
	public static final String ENABLE = "ENABLE";
	public static final String DISABLE = "DISABLE";
	public static final String SCRIPT_DELAY = "@DELAY";
	public static final String VOLTAGE = "Voltage";
	public static final String CURRENT = "Current";
	public static final String YES = "Yes";
	public static final String NO = "No";
	public static final String PASS = "PASS";
	public static final String FAIL = "FAIL";
	public static final String CALIBRATE = "CALIBRATE";
	public static final String CALIBRATE_VERIFY = "VERIFY";
	public static final String CALIBRATE_APPLY = "APPLY";
	public static final String EVENTTEST = "EVENTTEST";
	
	public static final double INFINITY_LARGE = 9.0E+30;   //positive infinity for divided by zero case
	public static final double INFINITY_SMALL = 9.0E-30;
	public static final String MASTER_TYPE_LOCAL = "local";
	public static final String MASTER_TYPE_REMOTE = "remote";
	public static final int LTR_RMS_OFFSET = 1000;
	public final static int COMTRADE_CHNL_OFFSET = 0;
	public static final String RMS = "RMS";
	public static final String ANALOG = "ANALOG";
	public static final String VIRTUAL_ANALOG = "VirtualAnalog";
	public static final String VIRTUAL_MEASUREMENT="VirtualMeasurement";
	public static final String SEQUENCE_TYPE="Sequence";
	public static final int SIZE_OF_SHORT = 2; // in bytes
	public static final int SIZE_OF_FLOAT = 4; // in bytes
	public static final String PUSH = "push";
	public static final String PULL = "pull";
	public static final String CONT_DATA_TYPE = "Cont";
	public static final String CONT_ANALOG_DATA_TYPE = "ContAnalog";
	public static final String MAGNITUDE = "MAGNITUDE";
	public static final String PHASE = "Phase";
	public static final String PHASOR = "Phasor";
	public static final String WATTS = "Watts";
	public static final String VARS = "VARs";
	public static final String WETTING_VOLTAGE = "WettingVoltage";
	public static final String VIRTUAL_PREFIX = "SUM";
	public static final String SEQUENCE = "sequence";
	public static final String LG_PREFIX = "LG_";
	public static final String COMPLEX = "complex";
	public static final String FLOAT = "float";
	
	public static final String OSC = "Osc";
	// Measurement types to compare in continuous
	public static final String PHASOR_SHORT = "Phsr";
	public final static String FREQUENCY = "FREQUENCY";
	public final static String FREQUENCY_SHORT = "Freq";
	public final static String HARMONIC = "HARMONIC";
	public final static String HARMONIC_SHORT = "Har";
	public final static String WATTS_REAL = "Real";
	public final static String A_PHASE_WATTS = "A_Phase_Watts";
	public final static String A_PHASE_WATTS_SHORT = "APhsW";
	
	public final static String B_PHASE_WATTS = "B_Phase_Watts";
	public final static String B_PHASE_WATTS_SHORT = "BPhsW";
	
	public final static String C_PHASE_WATTS = "C_Phase_Watts";
	public final static String C_PHASE_WATTS_SHORT = "CPhsW";
	
	public final static String ALL_PHASE_WATTS = "3_Phase_Watts";
	public final static String ALL_PHASE_WATTS_SHORT = "3PhsW";

	public final static String VARS_REACTIVE = "Reactive";
	public final static String A_PHASE_VARS = "A_Phase_VARs";
	public final static String A_PHASE_VARS_SHORT = "APhsV";
	
	public final static String B_PHASE_VARS = "B_Phase_VARs";
	public final static String B_PHASE_VARS_SHORT = "BPhsV";
	
	public final static String C_PHASE_VARS = "C_Phase_VARs";
	public final static String C_PHASE_VARS_SHORT = "CPhsV";
	
	public final static String ALL_PHASE_VARS = "3_Phase_VARs";
	public final static String ALL_PHASE_VARS_SHORT = "3PhsV";
	
	public final static String VARS_UNITS = "var";
	
	
	public final static String POSITIVE = "POSITIVE";
	public final static String NEGATIVE = "NEGATIVE";
	public final static String ZERO = "ZERO";
	public final static String SEQ_VOLTAGE = "Sequence_Voltage";
	public final static String SEQ_CURRENT = "Sequence_Current";
	public final static String POSITIVE_SEQ_VOLTAGE_SHORT = "PSVolt";
	public final static String POSITIVE_SEQ_CURRENT_SHORT = "PSCurr";
	public final static String NEGATIVE_SEQ_VOLTAGE_SHORT = "NSVolt";
	public final static String NEGATIVE_SEQ_CURRENT_SHORT = "NSCurr";
	public final static String ZERO_SEQ_VOLTAGE_SHORT = "ZSVolt";
	public final static String ZERO_SEQ_CURRENT_SHORT = "ZSCurr";


	public final static String REC_DEV_ID = "USI_M9000";
	public static final String MEASUREMENTS = "Measurements";
	public static final String AUTO_EXPORTS_DIR = "auto-exports";
	public static final String COMTRADE_FILE_DATE_FORMAT="yyMMdd,HHmmss";
	
	// START: 15-Sept-2022 - mysql event types as constants
	public static final String MYSQL_EVENT_NAME_DROP_CONT_OSC = "drop_event_cont_osc"; // DROP cont oscillography partitions
	public static final String MYSQL_EVENT_NAME_DROP_CONT_MEASUREMENTS = "drop_event_cont_measurements"; // drop const measurements partitions
	public static final String MYSQL_EVENT_NAME_CREATE_CONT_OSC = "create_event_cont_osc"; // CREATE cont oscillography partitions
	public static final String MYSQL_EVENT_NAME_CREATE_CONT_MEASUREMENTS = "create_event_cont_measurements"; // CREATE cont measurements partitions
	public static final String MYSQL_EVENT_NAME_REMOVE_OLD_SER = "remove_event_old_ser"; // Remove old ser data
	public static final String MYSQL_EVENT_NAME_REMOVE_OLD_DFRLOG = "remove_event_old_dfrlog"; // remove old dfr logs

}
