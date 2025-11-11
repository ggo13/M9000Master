package com.usi.m9000.util;

public class M9kXMLConstants {
public final static String XML_INPUT_START_TAG = "<Input>";
public final static String XML_INPUT_END_TAG = "</Input>";
public final static String XML_HIGH_LIMIT_START_TAG = "<HighLimit>";
public final static String XML_HIGH_LIMIT_END_TAG = "</HighLimit>";
public final static String XML_LOW_LIMIT_START_TAG = "<LowLimit>";
public final static String XML_LOW_LIMIT_END_TAG = "</LowLimit>";

public final static String XML_VALUE_INPUT = ")/_Object";
public final static String XML_PHASOR_VALUE_INPUT = ")/_Phasor";
public final static String EXPORT_XML_VALUE_INPUT = ")/_Value";
//public final static String XML_MAG_VALUE_INPUT = ")/_object";
public final static String XML_RMS_PREFIX = "Rms(";
public final static String XML_LIMITS_PREFIX = "Limits(";
public final static String XML_MAGNITUDE_PREFIX = "Magnitude(";
public final static String XML_FREQUENCY_PREFIX = "Frequency(";
public final static String XML_PHASOR_PREFIX = "Phase(";
public final static String XML_ZEROSEQUENCE_PREFIX = "ZeroSequence(";
public final static String XML_POSITIVESEQUENCE_PREFIX = "PositiveSequence(";
public final static String XML_NEGATIVESEQUENCE_PREFIX = "NegativeSequence(";
public final static String XML_FLOAT_PREFIX = "float(";
public final static String XML_ANALOG_CHANNELS_INPUT = "@Channels(Channels)/Analogs(Analogs)/AnalogInput(";
public final static String XML_EVENT_CHANNELS_INPUT = "@Channels(Channels)/Events(Events)/EventInput(";
public final static String XML_LINEGROUP_CHANNELS_INPUT = "@LineGroups(LineGroups)/LineGroup(";
public final static String XML_ALGORITHMS_INPUT = "@Algorithms(Algorithms)/";
public final static String XML_POWER_PREFIX = "Power(";
// START: 28-Jan-2020 - Transducer implementation
public final static String XML_AVERAGE_PREFIX = "Average(";
// END: 28-Jan-2020
//START: 06-Mar-2021 - Virtual Measurement for Delta Transformer 
public final static String XML_VIRTUALMEASUREMENT_PREFIX = "VirtualMeasurement(";
//END: 06-Mar-2021
}
