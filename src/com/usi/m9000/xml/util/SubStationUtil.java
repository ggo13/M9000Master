package com.usi.m9000.xml.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;

import com.usi.AlgorithmsDocument;
import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.AnalogsDocument.Analogs;
import com.usi.ChannelsDocument.Channels;
import com.usi.DFRDocument.DFR;
import com.usi.DFRsDocument.DFRs;
import com.usi.DataPoolDocument.DataPool;
import com.usi.EventInputDocument.EventInput;
import com.usi.EventsDocument.Events;
import com.usi.LimitsDocument;
import com.usi.LimitsDocument.Limits;
import com.usi.OutputDocument;
import com.usi.OutputDocument.Output;
import com.usi.SubStationDocument;
import com.usi.SubStationDocument.SubStation;
import com.usi.TriggerDocument;
import com.usi.TriggerDocument.Trigger;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.xml.M9000XmlConfig;

public class SubStationUtil {

	public M9000XmlConfig xmlConfig;
	public String stationId;
	public String dfrId;
	File xmlFile = new File("E:/Software/JFreeChart/jfreechart-1.0.13/source/org/jfree/chart/demo/M9000Config.xml");
	private SubStation currentSubstation;
	
	public SubStationUtil(M9000XmlConfig xmlConfig){
			this.xmlConfig = xmlConfig;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public SubStation addNewSubstation(String name)
	{
		SubStation newSubstation = SubStationDocument.Factory.newInstance().addNewSubStation();
		newSubstation.setName(name);
		return newSubstation;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SubStationUtil subStation = new SubStationUtil(new M9000XmlConfig("C:/M9K/WorkSpace/M9000Master/resources/RinzoSource1.7.xml"));

			SubStation substation = subStation.addNewSubstation("R1001");
			substation.setName("R1001");
			DFRs dfrs = substation.addNewDFRs();
			DFR dfr = dfrs.addNewDFR();
			dfr.setId(1);
			dfr.setIPAddress("195.1.1.70");
			DataPool dataPool = dfr.addNewDataPool();
			Channels channels = dataPool.addNewChannels();
			Analogs analogs = channels.addNewAnalogs();
			AnalogInput analogInput = analogs.addNewAnalogInput();
			analogInput.setName("Analog1");
			analogInput.setRange(5);
			analogInput.setChannel(0);
			analogInput.setCircuitName("Cripple Creek Lateral 2");
			analogInput.setInputType("Voltage");
			analogInput.setPhase("A");
			analogInput.setTransformerPrimary(100);
			analogInput.setTransformerSecondary(200);
			
			Events events = channels.addNewEvents();
			EventInput eventInput = events.addNewEventInput();
			
			DFR dfr1 = dfrs.addNewDFR();
			dfr1.setId(2);
			dfr1.setIPAddress("195.1.1.71");
			DataPool dataPool1 = dfr1.addNewDataPool();
			Channels channels1 = dataPool1.addNewChannels();
			Analogs analogs1 = channels1.addNewAnalogs();
			AnalogInput analogInput1 = analogs1.addNewAnalogInput();
			analogInput1.setName("Analog2");
			analogInput1.setRange(5);
			analogInput1.setChannel(0);
			analogInput1.setCircuitName("Cripple Creek Lateral 2");
//			analogInput1.setOffset(0);
			analogInput1.setInputType("Current");
			analogInput1.setPhase("A");
			
			Algorithms algorithms = AlgorithmsDocument.Factory.newInstance().addNewAlgorithms();
			Trigger trigger = TriggerDocument.Factory.newInstance().addNewTrigger();
			trigger.setChanId(1);
//			System.out.println("Trigger obje created ..."+trigger);
			Limits limits = LimitsDocument.Factory.newInstance().addNewLimits();
			limits.setName("");
			Output output = OutputDocument.Factory.newInstance().addNewOutput();
				XmlOptions xmlOptions = new XmlOptions(); 
				xmlOptions.setSaveOuter();
				xmlOptions.setSavePrettyPrint();
				xmlOptions.setUseDefaultNamespace();
				Map prefixes = new HashMap();
				prefixes.put("", "http://www.usi.com");
				xmlOptions.setSaveImplicitNamespaces(prefixes);

//				System.out.println("XML Test: "+substation.xmlText(xmlOptions));
		}  catch (M9000Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getDfrId() {
		return dfrId;
	}
	public void setDfrId(String dfrId) {
		this.dfrId = dfrId;
	}
	public File getXmlFile() {
		return xmlFile;
	}
	public void setXmlFile(File xmlFile) {
		this.xmlFile = xmlFile;
	}
	public SubStation getCurrentSubstation() {
		return currentSubstation;
	}
	public void setCurrentSubstation(SubStation currentSubstation) {
		this.currentSubstation = currentSubstation;
	}

}
