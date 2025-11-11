package com.usi.m9000.util;

public class M9kConstants {
	public final static String DFR_IP = "195.1.1.71";
	public final static int DISABLE_ZERO = 0;
	public final static int ENABLE_ONE = 1;
	public final static String ENABLE = "Enable";
	public final static String DISABLE = "Disable";
	public final static String HARMONIC = "Harmonic";
	public final static String MAGNITUDE = "Magnitude";
	public final static String DELETE = "Delete";
	public final static String RMS = "Rms";
	public final static String PHASE = "Phase";
	public final static String PHASOR = "Phasor";
	public final static String FREQUENCY = "Frequency";
	public final static String ZSV = "ZSV_";
	public final static String ZSI = "ZSI_";
	public final static String PSV = "PSV_";
	public final static String PSI = "PSI_";
	public final static String NSV = "NSV_";
	public final static String NSI = "NSI_";
	public final static String ZERO_SEQUENCE = "ZeroSequence";
	public final static String POSITIVE_SEQUENCE = "PositiveSequence";
	public final static String NEGATIVE_SEQUENCE = "NegativeSequence";
	// START: 17-Mar-2021 - Changed to be uniform with Apparent Power implementation
	public final static String ZERO_SEQUENCE_V = "Zero Sequence_Voltage";
	public final static String ZERO_SEQUENCE_I = "Zero Sequence_Current";
	public final static String POSITIVE_SEQUENCE_V = "Positive Sequence_Voltage";
	public final static String POSITIVE_SEQUENCE_I = "Positive Sequence_Current";
	public final static String NEGATIVE_SEQUENCE_V = "Negative Sequence_Voltage";
	public final static String NEGATIVE_SEQUENCE_I = "Negative Sequence_Current";
//	public final static String ZERO_SEQUENCE_V = "Zero Sequence (Voltage)";
//	public final static String ZERO_SEQUENCE_I = "Zero Sequence (Current)";
//	public final static String POSITIVE_SEQUENCE_V = "Positive Sequence (Voltage)";
//	public final static String POSITIVE_SEQUENCE_I = "Positive Sequence (Current)";
//	public final static String NEGATIVE_SEQUENCE_V = "Negative Sequence (Voltage)";
//	public final static String NEGATIVE_SEQUENCE_I = "Negative Sequence (Current)";
	// END: 17-Mar-2021 - Changed to be uniform with Apparent Power implementation
	// START: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
	public final static String VIRTUAL_MEASUREMENT = "VirtualMeasurement";
	public final static String VIRTUAL_MEASUREMENT_INPUT_VMI = "VMI";
	public final static int VIRTUAL_MEASUREMENT_OFFSET = 60000;// Virtual measurements Id starts with 80001 - To avoid default rms id which can go up to dfr id times 1000
	// END: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
	public final static String VOLTAGE = "Voltage";
	public final static String CURRENT = "Current";
	public final static String UNITS_VOLTAGE = "VOLTS";
	public final static String UNITS_CURRENT = "AMPS";
	public final static String CURRENT_AC_EXTERNAL = "CurrentAcExternalShunt";
	public final static String CURRENT_AC_INTERNAL = "CurrentAcInternalShunt";
	public final static String CURRENT_DC_EXTERNAL = "CurrentDcExternalShunt";
	public final static String CURRENT_DC_INTERNAL = "CurrentDcInternalShunt";
	public final static String VOLTAGE_AC = "VoltageAc";
	public final static String VOLTAGE_DC = "VoltageDc";
	// START: 23-Jan-2020 - Implementing trigger
	public final static String TRANSDUCER_VOLTAGE = "TransducerVoltage";
	public final static String TRANSDUCER_CURRENT = "TransducerCurrentExternalShunt";
	// END
	// START: 29-Jun-2020 - Hall Effect offset correction
	public final static String CURRENT_AC_HALL_EFFECT_EXTERNAL_SHUNT = "CurrentAcHallEffectExternalShunt";
	// END
	public final static String TRIGGER_INPUT_TYPE_ANALOG = "ANALOG";
	public final static String TRIGGER_INPUT_TYPE_LINEGROUP = "LINEGROUP";
	public final static String TRIGGER_INPUT_TYPE_MEASUREMENT = "MEASUREMENT";
	public final static String DFR = "DFROnly";
	public final static String SER = "SEROnly";
	public final static String BOTH = "Both";
	public final static String DISABLED = "Disabled";
	public final static String OVER = "Over";
	public final static String UNDER = "Under";
	public final static String NEVER = "Never";
	public final static String ROC = "ROC";
	public final static String DEFAULT_TRIGGER_OUT_HOLD = "10000";
	public final static String ENABLED = "Enabled";
	public final static String MEASUREMENTS = "Measurements";

	// START: 5-June-2015 Power Algorithm
	// A_Phase_Watts,B_Phase_Watts,C_Phase_Watts,3_Phase_Watts,A_Phase_VARs,B_Phase_VARs,C_Phase_VARs,3_Phase_VARs
	
	public final static String POWER = "Power";
	//Real corresponds to Watts
	public final static String WATTS_REAL = "Real";
	// START: 17-Mar-2021 - Changed to be uniform with Apparent Power implementation
	public final static String WATTS = "Watts";
	public final static String WATTS_UNITS = "Watts";
	public final static String A_PHASE_WATTS = "A_Phase_Watts";
	public final static String B_PHASE_WATTS = "B_Phase_Watts";
	public final static String C_PHASE_WATTS = "C_Phase_Watts";
	public final static String ALL_PHASE_WATTS = "3_Phase_Watts";
//	public final static String A_PHASE_WATTS = "A Phase Real (Watts)";
//	public final static String B_PHASE_WATTS = "B Phase Real (Watts)";
//	public final static String C_PHASE_WATTS = "C Phase Real (Watts)";
//	public final static String ALL_PHASE_WATTS = "3 Phase Real (Watts)";

	// Reactive corresponds to VARs
	public final static String VARS_UNITS = "var";
	public final static String VARS = "VARs";
//	public final static String VARS = "(VARs)";
	public final static String VARS_REACTIVE = "Reactive";
	public final static String A_PHASE_VARS = "A_Phase_VARs";
	public final static String B_PHASE_VARS = "B_Phase_VARs";
	public final static String C_PHASE_VARS = "C_Phase_VARs";
	public final static String ALL_PHASE_VARS = "3_Phase_VARs";
//	public final static String A_PHASE_VARS = "A Phase Reactive (VARs)";
//	public final static String B_PHASE_VARS = "B Phase Reactive (VARs)";
//	public final static String C_PHASE_VARS = "C Phase Reactive (VARs)";
//	public final static String ALL_PHASE_VARS = "3 Phase Reactive (VARs)";
	// Apparent Powert corresponds to VAs
	public final static String VA = "VA";
	public final static String VA_APPARENT = "Apparent";
//	public final static String A_PHASE_APPARENT = "A Phase Apparent (VA)";
//	public final static String B_PHASE_APPARENT = "B Phase Apparent (VA)";
//	public final static String C_PHASE_APPARENT = "C Phase Apparent (VA)";
//	public final static String ALL_PHASE_APPARENT = "3 Phase Apparent (VA)";
	public final static String A_PHASE_APPARENT = "A_Phase_Apparent_VA";
	public final static String B_PHASE_APPARENT = "B_Phase_Apparent_VA";
	public final static String C_PHASE_APPARENT = "C_Phase_Apparent_VA";
	public final static String ALL_PHASE_APPARENT = "3_Phase_Apparent_VA";

	// END: 17-Mar-2021 - Changed to be uniform with Apparent Power implementation
	public final static String VA_UNITS = "va";
	// END: 17-Mar-2021 - Changed to be uniform with Apparent Power implementation
	//END 5-June-2015
	
		
	// Line groups types
	public final static String SEQ_VOLTAGE = "SequenceVoltage";
	public final static String SEQ_CURRENT = "SequenceCurrent";
	public final static String A_PHASE = "A-Phase";
	public final static String B_PHASE = "B-Phase";
	public final static String C_PHASE = "C-Phase";
	public final static String ALL_PHASE = "3-Phase";
	public final static String A_B_PHASE = "A-B-Phase";
	public final static String A_C_PHASE = "A-C-Phase";
	public final static String B_C_PHASE = "B-C-Phase";
	public final static String A_PHASE_AND_SEQ_CURRENT = "A-Phase_WITH_SEQ_CURRENT";
	public final static String B_PHASE_AND_SEQ_CURRENT = "B-Phase_WITH_SEQ_CURRENT";
	public final static String C_PHASE_AND_SEQ_CURRENT = "C-Phase_WITH_SEQ_CURRENT";
	
	// Calculating missing channels
	public final static String ALL_PHASE_MISSING_PHASE_A = "ALL_PHASE_BUT_A";
	public final static String ALL_PHASE_MISSING_PHASE_B = "ALL_PHASE_BUT_B";
	public final static String ALL_PHASE_MISSING_PHASE_C = "ALL_PHASE_BUT_C";
	public final static String ALL_PHASE_MISSING_PHASE_A_B = "ALL_PHASE_BUT_A_B";
	public final static String ALL_PHASE_MISSING_PHASE_B_C = "ALL_PHASE_BUT_B_C";
	public final static String ALL_PHASE_MISSING_PHASE_C_A = "ALL_PHASE_BUT_C_A";
	public final static String ALL_PHASE_MISSING_PHASE_A_B_AND_SEQ_VOLTAGE = "ALL_PHASE_BUT_A_B_WITH_SEQ_VOLTAGE";
	public final static String ALL_PHASE_MISSING_PHASE_B_C_AND_SEQ_VOLTAGE = "ALL_PHASE_BUT_B_C_WITH_SEQ_VOLTAGE";
	public final static String ALL_PHASE_MISSING_PHASE_C_A_AND_SEQ_VOLTAGE = "ALL_PHASE_BUT_C_A_WITH_SEQ_VOLTAGE";
	public final static String CURRENT_ALL_PHASE = "CURRENT_ALL_PHASE"; // Includes sequence current algorithm along with all power
	public final static String VOLTAGE_ALL_PHASE_MISSING_PHASE_A = "VOLTAGE_ALL_PHASE_BUT_A"; // Exclude single phase A power but include sequence voltage
	public final static String VOLTAGE_ALL_PHASE_MISSING_PHASE_B = "VOLTAGE_ALL_PHASE_BUT_B"; // Exclude single phase B power but include sequence voltage
	public final static String VOLTAGE_ALL_PHASE_MISSING_PHASE_C = "VOLTAGE_ALL_PHASE_BUT_C"; // Exclude single phase C power but include sequence voltage
	
	// Comtrade constants
	public final static String COMTRADE_PROPERTIES_PATH = "E:/M9K-Utilities/Projects/M9000Master/resources/";
	public final static int COMTRADE_REV_YEAR = 1999;
	public final static int COMTRADE_ANALOG_CHNL_SKEW = 0;
	public final static int COMTRADE_NO_OF_SAMPLING_RATES = 1;
	public final static int COMTRADE_CHNL_MULT = 1;
	public final static int COMTRADE_CHNL_OFFSET = 0;
	public final static int COMTRADE_PRIMARY_RATIO = 1;
	public final static int COMTRADE_SECONDARY_RATIO = 1;
	public final static String COMTRADE_ANALOG_DATA_RANGE_MIN = "-32768";
	public final static String COMTRADE_PRIMARY = "P";
	public final static String COMTRADE_SECONDARY = "S";
	public final static String COMTRADE_ANALOG_DATA_RANGE_MAX = "32767";
//	public static String NEWLINE = System.getProperty("line.separator");
	public static String NEWLINE = String.format("%n");
	public static String COMMA_SEPERATOR = ",";
	public static String CFG_FILE_EXTN = ".cfg";
	public static String DAT_FILE_EXTN = ".dat";
	public static String INF_FILE_EXTN = ".inf";
	public static String COMTRADE_TIME_CODE = "-5t";
	public static String COMTRADE_FILE_PATH = "E:/M9kCOnfig/Comtrade/";
	public static String ASCII = "ASCII";
	public static String Binary = "Binary";
	public static Integer BINARY_MISSING_DATA = 0x8000;
	public static Integer ASCII_MISSING_DATA = 99999;

	
	// Comtrade Viewer Constants
	public static String FAULT_TYPE = "Faults";
	public static String LTR_ANALOG_TYPE = "LTR_ANALOG";
	public static String LTR_MEASUREMENT_TYPE = "LTR_MEASUREMENT";
	public static String SER_TYPE = "Ser";
	public static String CONT_DATA_TYPE = "CONT_DATA";
	public static String SER_DATES_TYPE = "SerDates";
	public static String CONTINUOUS_TYPE = "Continuous";
	public static String CONTINUOUS_ANALOG_TYPE = "Analog";
	public static String ALARMS_TYPE = "Alarms";
	public static String LTR_DATA = "LTR_DATA";
	public static String LTR_DIR = "LTR";
	
	// Database constants
//	public final static String MYSQL_CONNECTION_URL = "jdbc:mysql://195.1.1.85:3306/m9000";
	public final static String STATION_MYSQL_CONNECTION_URL = "jdbc:mysql://localhost:3306/m9000";
	public final static String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	public final static String MYSQL_CONNECTION_USERNAME = "dfr";
	public final static String MYSQL_CONNECTION_PASSWORD = "usi";
    public static final int MYSQL = 1;

    // Master types
	public static final String MASTER_TYPE_LOCAL = "local";
	public static final String MASTER_TYPE_REMOTE = "remote";
	
	// PMU Input types
	public static final String PMU_INPUT_TYPE_ANALOG = "Analog";
	public static final String PMU_INPUT_TYPE_PHASOR = "Phasor";
	public static final String PMU_INPUT_TYPE_FREQ = "Freq";
	
	// Continuous display data constant
	public static final String CONTINUOUS_ANALOG_DATA = "ContAnalog";
	public static final String CONTINUOUS_DATA = "Cont";
	public static final int LTR_RMS_OFFSET = 1000;
	public static final String ANALOG = "ANALOG";
	public static final int SIZE_OF_SHORT = 2;
	public static final int SIZE_OF_FLOAT = 4;
	public static final String INTEGER = "int";
	public static final String FLOAT = "float";
	public static final String COMPLEX = "complex";
	public static final String NO_DATA = "NO_DATA";
	public static final String NO_PHASE = "NA";
	public static final String PASS = "Pass";
	public static final String FAIL = "Fail";
	public static final String CALIBRATE_VERIFY = "VERIFY";
	public static final String CALIBRATE_APPLY = "APPLY";
	public static final String EVENTTEST = "EVENTTEST";
	
	public static final String CONT_EXPORT_TYPE_RMS = "RMS";
	public static final String CONT_EXPORT_TYPE_FREQ = "Frequency";
	public static final String CONT_EXPORT_TYPE_PHASOR = "Phasor";
	// START: 04-June-2015 to Export Harmonics data
	public static final String CONT_EXPORT_TYPE_HARMONIC = "Harmonic";
	// END: 04-June-2015
	
	public static final String OPEN_STATE = "Open";
	public static final String CLOSE_STATE = "Close";
	public static final String NORMAL = "Normal";
	public static final String ABNORMAL = "Abnormal";
	public static final String RESOLVED = "Resolved";
	public static final String UNRESOLVED = "Unresolved";
	public static final String YES = "Yes";
	public static final String NO = "No";
	public static final String ADMIN = "admin";
	public static final String GUEST = "guest";
//	public static final String CONT_EXPORT_RMS_UNITS = "V";
//	public static final String CONT_EXPORT_FREQ_UNITS = "Hz";
//	public static final String CONT_EXPORT_PHASE_UNITS = "Phasor";
	
	public static final String NOT_APPLICABLE="N/A";
	
	public static final String VIRTUAL_ANALOG="VirtualAnalog";
	public static final String VIRTUAL_PREFIX = "SUM";
	
	public static final String PRIMARY = "P";
	public static final String SECONDARY = "S";
	
	// START: 12-Nov-2019 - SER email implementation
	public static final String PDF_TYPE = "PDF";
	// END: 12-Nov-2019
	
	// START: 28-Jan-2020 - Transducer implementation
	public final static String AVERAGE = "Average";
	// END: 28-Jan-2020
	
	// START: 18-Feb-2020 - Virtual channels updated to include different channels with different phases
	public static final String PHASE_NEUTRAL = "N";
	// Add all other phase constants 
	public static final String PHASE_A = "A";
	public static final String PHASE_B = "B";
	public static final String PHASE_C = "C";
	public static final String PHASE_AB = "AB";
	public static final String PHASE_BC = "BC";
	public static final String PHASE_CA = "CA";
	public static final String PHASE_A_B = "A_B";
	public static final String PHASE_B_C = "B_C";
	public static final String PHASE_C_A = "C_A";
	
	// END: 18-Feb-2020
	
	// START: 02-Jun-2020 - M9kMini - implementation
	// Moved from CreateChannelsAction class
	public static final int NO_OF_BOARDS_PER_DFR = 4;
	public static final int NO_OF_ANALOG_CHNLS_PER_BOARD = 8;
	public static final int MAX_NO_OF_ANALOG_CHNLS_PER_DFR = 32;
	public static final int NO_OF_DIGITAL_CHNLS_PER_BOARD = 32;
	public static final int MAX_NO_OF_DIGITAL_CHNLS_PER_DFR = 128;
	
	public static final int NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD = 4;
	public static final int NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD = 16;

	public static final int NO_OF_ANALOG_CHNLS_PER_MINI = 12;
	public static final int NO_OF_DIGITAL_CHNLS_PER_MINI = 16;

	// END: 02-Jun-2020 - M9kMini - implementation 

	// START: 25-Mar-2021 - Line group warning messages for user
	public static final String KEY_MULTIPLE_CHASSIS="MSG_MULTIPLE_CHASSIS";
	public static final String MSG_MULTIPLE_CHASSIS = "This line group has channels from multiple chassis and will not be able to calculate measurements";
	public static final String KEY_MIXED_VOLTAGE_CHANNELS="MSG_MIXED_VOLTAGE_CHANNELS";
	public static final String MSG_MIXED_VOLTAGE_CHANNELS = "This line Group has both line-line and line-neutral voltage channels. You will not be able to calculate any measurements on this.";
	public static final String KEY_MISSING_ONE_VOLTAGE_CHANNEL="MSG_MISSING_ONE_VOLTAGE_CHANNEL";
	public static final String MSG_MISSING_ONE_VOLTAGE_CHANNEL = "The missing voltage will be calculated from the other two voltages to calculate 3-phase power";
//	public static final String KEY_MISSING_TWO_VOLTAGE_CHANNEL="MSG_MISSING_TWO_VOLTAGE_CHANNEL";
//	public static final String MSG_MISSING_TWO_VOLTAGE_CHANNEL = "This line group has only ONE line-line voltage channel. Missing two channels will be automatically calculated";
	public static final String KEY_INVALID_CHANNEL_COMBINATION="MSG_INVALID_COMBINATION";
	public static final String MSG_INVALID_CHANNEL_COMBINATION = "This line group has more than one channel with same phase. You will not be able to calculate any measurements on this.";
	public static final String KEY_MISSING_ONE_CURRENT_CHANNEL="MSG_MISSING_ONE_CURRENT_CHANNEL";
	public static final String MSG_MISSING_ONE_CURRENT_CHANNEL = "3-phase power will be calculated as three times the average power of the other two phases";
	public static final String KEY_MISSING_TWO_CURRENT_CHANNELS="MSG_MISSING_TWO_CURRENT_CHANNELS";
	public static final String MSG_MISSING_TWO_CURRENT_CHANNELS = "3-phase power will be calculated as three times the power of the available phase";
	// END: 25-Mar-2021 
	public static final int TRUE = 1; // Boolean true for mysql
	public static final int FALSE = 0; // Boolean false for mysql
	public static final String ACTIVE_CONFIG_SQL = "ACTIVE_CONFIG.sql";
	public static final String ACTIVE_CONFIG_SQL_LOC = "/data/m9k/config-files/";
}
