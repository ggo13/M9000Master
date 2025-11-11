package com.usi.m9000.chart.applet;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jnlp.ExtendedService;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;

/**
 * @author sramasamy
 *
 */
public class M9kChart {//extends ApplicationFrame {//implements Runnable{

	private ChannelRawData channelDataList = null;
	M9kStationCommandClient commandClient;
	private String scopeCommand;
	private int channelCnt; // default value 1
	private int samplesCnt = 2000; // default value 2000
	private float sampleRate; // default value 6000
	private int lineFreq; // default value 60
//	String dateString = "21/09/2009,03:36:28.043751";
//	String triggerStart = "21/09/2009,03:36:28.204584";
	public List<String> toolTips[];

	Timestamp dataValueTime ;
	Timestamp triggerPointTime;
	String[] channelsToDisplay;
	private int digitalChannelCnt = 0;

	JFreeChart chart, digitalChart;
	ChartPanel chartPanel;
	XYSeriesCollection dataset = new XYSeriesCollection();
	XYSeriesCollection digitalDataset = new XYSeriesCollection();
	XYSeries[] series1 = null;
	String[] scopeRmsValues;
	XYSeries[] digitalSeries = null;
	ExtendedService es;
//	private URL dataSrc;
	private List<DfrDTO>  lstDfrs;
	DfrDTO selectedDfr;
	StationDTO stationDto;
	private List<Color> lstEventChannelStatus;
	private String channelFactor = M9kConstants.COMTRADE_SECONDARY; // Secondary or primary
	private boolean isPrimary = false;
//	public static boolean booflag = true;
	private int totalSamples;
	private double[] arrSinData = null;
	private double[] arrCosData = null;
	private double imagSinData;
	private double realCosData;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kChart.class);
	public M9kChart()
	{
		channelDataList = new ChannelRawData();
	}
	
	public M9kChart(StationDTO stationDto)
	{
		this();
		this.stationDto = stationDto;

	}
	/**
	 * Constructor 
	 * @param dataSrc
	 */
	public M9kChart(URL dataSrc)
	{
		this();
//		this.dataSrc = dataSrc;
	}

//	/**
//	 * Constructor 
//	 * @param dataSrc
//	 */
//	public M9kChart(M9kStationCommandClient commandClient)
//	{
//		this();
//		this.commandClient = commandClient;
//		channelDataList = new ChannelRawData();
//	}

	
	/**
	 * Constructor 
	 * @param dataSrc
	 */

	/**
	 * Constructor 
	 * @param dataSrc
	 */
	public M9kChart(URL dataSrc, int sampleRate, int lineFreq, int channelCnt, int samplesCnt)
	{
		this();
//		this.dataSrc = dataSrc;
		if (sampleRate > 0)
		{
			this.sampleRate = sampleRate;
		}
		if (lineFreq > 0)
		{
			this.lineFreq = lineFreq;
		}
		if (channelCnt > 0)
		{
			this.channelCnt = channelCnt;
		}
		if (samplesCnt > 0)
		{
			this.samplesCnt = samplesCnt;
		}
	}
	
	public M9kChart(URL dataSrc, int digitalChannelCnt, int samplesCnt)
	{
		this();
//		this.dataSrc = dataSrc;
		if (digitalChannelCnt > 0)
		{
			this.digitalChannelCnt = digitalChannelCnt;
		}
		if (samplesCnt > 0)
		{
			this.samplesCnt = samplesCnt;
		}
	}

	public JFreeChart constructChart() throws M9000Exception
	{
		XYDataset dataset = createUpdateDataSet();
		chart = createChart(dataset);
		logger.debug("New chart constructed.."+getChart());
		return chart;
	}
	
//	public JFreeChart constructDigitalChart() throws Exception
//	{
//		XYDataset dataset = createUpdateDigitalDataSet();
//		digitalChart = createDigitalChart(dataset);
//
//		return digitalChart;
//	}
	public XYDataset createUpdateDataSet() throws M9000Exception
	{
		XYDataset dataset;
		Collection<ArrayList<Double>> allChnlData = ParseChannelData();
		dataset = createBinaryDatasetWithCorrection(allChnlData);

		return dataset;
	}
	
	public List<Map<Integer, Double>> getDataForJQ() throws M9000Exception
	{
		Collection<ArrayList<Double>> allChnlData = ParseChannelData();
		List<Map<Integer, Double>> lstOfMapDataSetForJQ = createBinaryDatasetWithCorrectionForJQ(allChnlData);
		return lstOfMapDataSetForJQ;
	}
//	private XYDataset createUpdateDigitalDataSet() throws Exception
//	{
//		XYDataset dataset;
//		Collection<ArrayList<Integer>> allChnlData = ParseDigitalChannelData();
//		dataset = createDigitalDataset(allChnlData);
//
//		return dataset;
//	}

	public ChartPanel getChartPanelToDisplay(JFreeChart chart)
	{
		
//		JScrollPane jsp = new JScrollPane(chartScrollBar);
		ChartPanel chartPanel = new ChartPanel(chart, true);
		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setMouseZoomable(true);
//		chartPanel.setRangeZoomable(false);
		chartPanel.setDoubleBuffered(false);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
//		System.out.println("\t\t\t\t Layout: "+chartPanel.getLayout());
//		chartPanel.setLayout(new BorderLayout());
//		chartPanel.add(chartScrollBar, BorderLayout.EAST);
//		chartPanel.getChartRenderingInfo().setEntityCollection(null);

		return chartPanel;
	}


	
	private  Collection<ArrayList<Double>> ParseChannelData() throws M9000Exception
	{
//		BufferedReader buffReader = null;
		DataInputStream dis = null;
		try{
			int samplesCnt = sampleCntWithBuffer();
			if (commandClient == null)
			{
				//TODO: Change IPAddress to hostname
//				System.out.println("ParseChannelData MESSAGE Q: StationDto "+getStationDto());
//				System.out.println("ParseChannelData MESSAGE Q: MEssage queue to be sent to "+getStationDto().getSystemStationName());
				
				byte[] data = M9kMessagesUtil.sendAndReceiveScopeMessage(""+stationDto.getSystemStationId(), getScopeCommand());
				if (data == null)
				{
					logger.error("Error in data retrieval");
					throw new M9000Exception("Error in data retrieval");
				}
//				if ((getSelectedDfr().getDfrId() == 4 || getSelectedDfr().getDfrId() == 3) && booflag)
//				{
//					OutputStream out = null;
////					booflag = false;
//					try
//					{
//						System.out.println("Data read length "+data.length+" total samples cnt "+samplesCnt);
//					out = new FileOutputStream(new File("/home/dfr/m9k/completeScopeData-"+getSelectedDfr().getDfrId()+".bin"), true);
//					out.write(data, 0, data.length);
//					out.close();
//					}
//					catch (Exception e) {
//						out.close();
//					}
//					finally
//					{
//						out.close();
//					}
//				}

	//			System.out.println("After send receive messages "+data.length);
				dis = new DataInputStream(new ByteArrayInputStream(data));
			}
			else
			{
				commandClient.setCommand(getScopeCommand().substring(getScopeCommand().indexOf(",")+1));
				dis = new DataInputStream(commandClient.testSendAndReceiveBinary());
			}
			logger.debug("SCOPE-BUG: channelCnt..."+channelCnt+"samplesCnt..."+samplesCnt);
//			System.out.println("channelCnt..."+channelCnt+" Sample Cnt.."+samplesCnt+" dis.available() > 0"+dis.available());
//			byte[] fullRead = new byte[samplesCnt * channelCnt];
//			System.out.println("Full read capacity.."+(samplesCnt * channelCnt));
//			dis.readFully(fullRead);
//			System.out.println("Bytes available...."+fullRead.length);
			short dataMatrix[][] = new short[channelCnt][samplesCnt];
			
			int dataCnt = 0;
			int chnlCnt = 0;
			while (dataCnt < samplesCnt && dis.available() > -1)
			{
//				System.out.println("\n\nEntered while chnlCnt "+chnlCnt);
				chnlCnt = 0;
				while ( chnlCnt < channelCnt)
				{
//					System.out.println("chnlCnt "+chnlCnt+" dataCnt ["+dataCnt+"]");
					dataMatrix[chnlCnt++][dataCnt] = dis.readShort();
//					if ((getSelectedDfr().getDfrId() == 4 || getSelectedDfr().getDfrId() == 3) && booflag)
//					{
//						System.out.println("chnl "+chnlCnt+" DataCnt "+dataCnt+" -> "+dataMatrix[chnlCnt-1][dataCnt]);
//					}
				}
				dataCnt++;
			}
			if (getSelectedDfr().getDigitalChnlCnt() > 0)
			{
				populateEventsStatus(dis);
			}
//			System.out.println("getChannelsToDisplay().length......"+getChannelsToDisplay().length);
//			for (int i = 0; i < getChannelsToDisplay().length; i++) {
//				System.out.println("\t\t\t\tchannels to display: "+getChannelsToDisplay()[i]);
//			}
//			System.out.println("IN parseChannelData channels count "+getChannelCnt());
			if (getChannelsToDisplay() == null || getChannelsToDisplay().length == 0)
			{
				logger.debug("No channels selected ");
				channelDataList.createChannelsWithData(getChannelCnt(), dataMatrix);
			}
			else
			{
				channelDataList.createChannelsWithData(getChannelCnt(), dataMatrix, getChannelsToDisplay());
			}

		}catch (ConnectException ce) {
			//			ce.printStackTrace();
			throw new M9000Exception(ce);

		} catch (IOException ioe) {
			//			ioe.printStackTrace();
			throw new M9000Exception(ioe);
		}
		catch(M9000Exception me)
		{
			throw me;
		}
		catch(Exception e)
		{
			//			e.printStackTrace();
			throw new M9000Exception(e);
		}
		finally
		{
			try
			{
				if (dis != null)
				{
					dis.close();					
				}
			}
			catch (IOException ioe) {
			}
		}
		logger.debug("Returning...."+channelDataList.getBinaryChannelsData().size());
		return channelDataList.getBinaryChannelsData();

	}

	private void populateEventsStatus(DataInputStream dis) throws IOException {
		// Read events
		int ichnlCnt = 0;
		int iBitCnt = 0;
		int totalNoOfDigitalChnl = 0;
		short digitalChnl = 0;
		String digitalBits;
		int eventChannelCnt = getSelectedDfr().getDigitalChnlCnt();
		short numOfDigitalBytes = (short)(Math.ceil((float)eventChannelCnt/16)); 
		short[] digitalData = new short[eventChannelCnt];
		String zeros = "0000000000000000";
		String leadZero;
		lstEventChannelStatus = new ArrayList<Color>();
		int eventChnlStatus;
		while (ichnlCnt++ < numOfDigitalBytes)
		{
//			digitalChnl = Short.reverseBytes(dis.readShort());//Short.reverseBytes(roBuff.getShort());
			digitalChnl = dis.readShort();
			digitalBits = Integer.toBinaryString(digitalChnl);
//			if ((getSelectedDfr().getDfrId() == 4 || getSelectedDfr().getDfrId() == 3)  && booflag)
//			{
//				System.out.println("short read "+digitalChnl);
//				System.out.println("After binary conversion "+digitalBits);
//			}
			if (digitalBits.length() > 16)
			{
				digitalBits = digitalBits.substring(digitalBits.length() - 16);
				
			}
			else
			{
//				digitalBits = stuffLeadingzeroes(digitalBits);							
				leadZero = zeros.substring(0,16-digitalBits.length());
				digitalBits=leadZero.concat(digitalBits);
			}
//			if ((getSelectedDfr().getDfrId() == 4 || getSelectedDfr().getDfrId() == 3)  && booflag)
//			{
//				System.out.println("After stuffing zeroes "+digitalBits);
//			}
			iBitCnt = 16;
			while (iBitCnt > 0 && totalNoOfDigitalChnl < eventChannelCnt)
			{
				digitalData[totalNoOfDigitalChnl] = Short.parseShort(digitalBits.substring(iBitCnt-1, iBitCnt));
				eventChnlStatus = getSelectedDfr().getLstEventsInfo().get(totalNoOfDigitalChnl).getStatus();
//				if ((getSelectedDfr().getDfrId() == 4 || getSelectedDfr().getDfrId() == 3) && booflag)
//				{
//					System.out.println("Data read for channel digitalData["+totalNoOfDigitalChnl+"] -> "+digitalData[totalNoOfDigitalChnl]);
//					System.out.println("chnl status "+eventChnlStatus+" event description "+ getSelectedDfr().getLstEventsInfo().get(totalNoOfDigitalChnl).getCircuitName());
//				}
					if (eventChnlStatus == 0) // y = 0 indicates OPEN in config file
					{
						if (digitalData[totalNoOfDigitalChnl] == 1)
						{
							lstEventChannelStatus.add(Color.RED);
						}
						else
						{
							lstEventChannelStatus.add(Color.GREEN);
						}
					}
					else if (eventChnlStatus == 1) // y = 0 indicates CLOSE in config file
					{
						if (digitalData[totalNoOfDigitalChnl] == 0 )
						{
							lstEventChannelStatus.add(Color.RED);
						}
						else
						{
							lstEventChannelStatus.add(Color.GREEN);
						}
					}
					
					totalNoOfDigitalChnl++;
				iBitCnt--;
			}
		}
//		if ((getSelectedDfr().getDfrId() == 4 || getSelectedDfr().getDfrId() == 3) && booflag)
//		{
//			booflag = false;
//		}
		
	}

	public void parseEventsData() throws M9000Exception
	{
		DataInputStream dis = null;
		try{
			if (commandClient == null)
			{
				//TODO: Change IPAddress to hostname
//				System.out.println("parseEventsData MESSAGE Q: StationDto "+getStationDto());
//				System.out.println("parseEventsData MESSAGE Q: MEssage queue to be sent to "+getStationDto().getSystemStationName()+"\n Message to be sent "+("SCOPE,"+getSelectedDfr().getDfrName()));
				byte[] data = M9kMessagesUtil.sendAndReceiveScopeMessage(""+stationDto.getSystemStationId(), (getSelectedDfr().getIpAddress()+",SCOPE,"+getSelectedDfr().getDfrName()));
				if (data == null)
				{
					logger.error("Error in data rerieval");
					throw new M9000Exception("Error in data retrieval");
				}
	//			System.out.println("After send receive messages "+data.length);
				dis = new DataInputStream(new ByteArrayInputStream(data));
			}
			else
			{
				commandClient.setCommand(getScopeCommand());
				dis = new DataInputStream(commandClient.testSendAndReceiveBinary());
			}

			populateEventsStatus(dis);
		}catch (ConnectException ce) {
			//			ce.printStackTrace();
			throw new M9000Exception(ce);

		} catch (IOException ioe) {
			//			ioe.printStackTrace();
			throw new M9000Exception(ioe);
		}
		catch(M9000Exception me)
		{
			throw me;
		}
		catch(Exception e)
		{
			//			e.printStackTrace();
			throw new M9000Exception(e);
		}
		finally
		{
			try
			{
				if (dis != null)
				{
					dis.close();					
				}
			}
			catch (IOException ioe) {
			}
		}
		
	}
//	private  Collection<ArrayList<Integer>> ParseDigitalChannelData(M9kStationCommandClient commandClient) throws ConnectException, IOException, Exception
//	{
//		BufferedReader buffReader = null;
//		DataInputStream dis = null;
//		if (dataSrc == null)
//		{
//			System.out.println("Invalid Datasource URL");
//			return null;
//		}
//		try{
//
//			dis = new DataInputStream(dataSrc.openStream());
////			System.out.println("samplesCnt..."+samplesCnt +" file size "+dis.available());
////			System.out.println("channelCnt..."+digitalChannelCnt);
////			byte[] fullRead = new byte[samplesCnt * channelCnt];
////			System.out.println("Full read capacity.."+(samplesCnt * digitalChannelCnt));
////			dis.readFully(fullRead);
////			System.out.println("Bytes available...."+fullRead.length);
//			int dataMatrix[][] = new int[digitalChannelCnt][samplesCnt];
//			int dataCnt = 0;
//			int chnlCnt = 0;
//			int byteCnt = 0;
//			while (dataCnt < samplesCnt && dis.available() > 0)
//			{
//				chnlCnt = 0;
////				System.out.print(dataCnt+":");
//				while ( chnlCnt < (digitalChannelCnt))
//				{
////					if (chnlCnt < (digitalChannelCnt-1))
////					{
//						dataMatrix[chnlCnt][dataCnt] = dis.readShort();
//						byteCnt+=2;
////						if (dataMatrix[chnlCnt][dataCnt] != 0)
////						System.out.print(dataCnt + " - data: "+dataMatrix[chnlCnt][dataCnt]+" : ");
////					}
////					else
////					{
////						System.out.println(chnlCnt+" Unused Channel" + dataCnt);
////						dis.readShort();
////						
////					}
//					chnlCnt++;
////					dataOffset += (channelCnt*2); 
//				}
//				dataCnt++;
//			}
//
////			while (chnlCnt < channelCnt && chnlOffset <= (fullRead.length - 2))
////			{
////				dataOffset = chnlOffset;
////				dataCnt = 0;
////				while (dataCnt < samplesCnt && dataOffset <= (fullRead.length - 2))
////				{
////					dataMatrix[chnlCnt][dataCnt++] = readShort(fullRead, dataOffset);
////					System.out.println(dataCnt-1 + " - data: "+dataMatrix[chnlCnt][dataCnt-1]);
////					dataOffset += (channelCnt*2); 
////				}
////				chnlCnt++;
////				chnlOffset += 2;
////			}
//			channelDataList.createDigitalChannelsWithData(getDigitalChannelCnt(), dataMatrix);
//
//		}catch (ConnectException ce) {
//			//			ce.printStackTrace();
//			throw ce;
//
//		} catch (IOException ioe) {
//			//			ioe.printStackTrace();
//			throw ioe;
//		}
//		catch(Exception e)
//		{
//			//			e.printStackTrace();
//			throw e;
//		}
//		finally
//		{
//			try
//			{
//				if (buffReader != null)
//				{
//					buffReader.close();
//					buffReader = null;
//				}
//				if (dis != null)
//				{
//					dis.close();
//					dis = null;
//				}
//			}
//			catch (IOException ioe) {
//			}
//		}
//		return channelDataList.getDigitalChannelsData();
//
//	}


//	private static short readShort(byte[] data, int offset) {
//		return (short) (((data[offset] << 8)) | ((data[offset + 1] & 0xff)));
//	}

	private  JFreeChart createChart(XYDataset dataset) throws M9000Exception {
		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Analog Channels", // chart title
				"Samples", // x axis label
				"Values", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL,
				true, // include legend
				true, // tooltips
				false // urls
		);
		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation...

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		//		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		plot.getDomainAxis().setLowerMargin(0.0);
		plot.getDomainAxis().setUpperMargin(0.0);
//		plot.getDomainAxis().setAutoRange(false);
//		plot.getDomainAxis().setRange(0, getSamplesCnt()-500);
//		((XYLineAndShapeRenderer)plot.getRenderer()).setLegendItemToolTipGenerator(new XYSeriesLabelGenerator() {
//			
//			@Override
//			public String generateLabel(XYDataset arg0, int arg1) {
//				String channelName = getSelectedDfr().getLstAnalogChannelNames().get(Integer.parseInt(getChannelsToDisplay()[arg1])-getSelectedDfr().getAnalogChannelStart());
////				channelName = channelName.substring(channelName.indexOf("-")+1) ;
//				return channelName;
//			}
//		});
//		plot.getRenderer().setLegendItemLabelGenerator(new XYSeriesLabelGenerator() {
//			
//			@Override
//			public String generateLabel(XYDataset arg0, int arg1) {
//				// TODO Auto-generated method stub
////				System.out.println("INdex of channel "+arg1 + " channel number to display "+getChannelsToDisplay()[arg1]);
//				String channelName = getSelectedDfr().getLstAnalogChannelNames().get(Integer.parseInt(getChannelsToDisplay()[arg1])-getSelectedDfr().getAnalogChannelStart());
//				logger.debug("Channel name for legend "+channelName);
////				channelName = channelName.substring(channelName.lastIndexOf("-")+1) ;
////				String labelName = "A"+getChannelsToDisplay()[arg1] +" "+channelName+" Rms: "+getScopeRmsValues()[arg1];
//				String labelName = channelName+getScopeRmsValues()[arg1];
//				logger.debug("Label name for legend "+labelName);
//				return labelName;
//			}
//		});
		// OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}

	@SuppressWarnings("unused")
	private  JFreeChart createDigitalChart(XYDataset dataset) {
		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Digital Channels", // chart title
				"X", // x axis label
				"Y", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL,
				false, // include legend
				true, // tooltips
				false // urls
		);
		
//		ValueAxis yAxis = new SymbolAxis("Symbol", new String[] { "", "TwoTwoTwoTwoTwoTwoTwoTwoTwoTwoTwoTwo", "Three", "Four" , "Two1", "Three1", "Four1" , "Two2", "Three2", "Four2" , "Two3", "Three3", "Four3" , "Two4", "Three4", "Four4", "Four5"  });
		ValueAxis yAxis = new SymbolAxis("Channels", getSymbolsForSymbolAxis());
//		((SymbolAxis)yAxis).setLabelAngle(Math.PI / 6.0);
		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation...

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		//		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainCrosshairVisible(true);
		plot.setDomainCrosshairLockedOnData(true);
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeCrosshairVisible(true);
		plot.setDomainCrosshairValue(0.0);
//		plot.setDomainCrosshairStroke(new BasicStroke(2.0f));
		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		plot.getDomainAxis().setLowerMargin(0.0);
		plot.getDomainAxis().setUpperMargin(0.0);
		plot.setRangeAxis(yAxis);
		// OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}

	public XYLineAndShapeRenderer getRenderer()
	{
		XYLineAndShapeRenderer renderer = null;
		if (getPlot() != null)
		{
			renderer = (XYLineAndShapeRenderer) getPlot().getRenderer();
		}
		return renderer;

	}

	public XYPlot getPlot()
	{
		XYPlot plot = null; 
		if (chart != null)
		{
			plot  = (XYPlot)chart.getPlot();
		}
		return plot;
	}
	public XYLineAndShapeRenderer getDigitalRenderer()
	{
		XYLineAndShapeRenderer renderer = null;
		if (getDigitalPlot() != null)
		{
			renderer = (XYLineAndShapeRenderer) getDigitalPlot().getRenderer();
		}
		return renderer;

	}

	public XYPlot getDigitalPlot()
	{
		XYPlot plot = null; 
		if (digitalChart != null)
		{
			plot  = (XYPlot)digitalChart.getPlot();
		}
		return plot;
	}

	private  XYDataset createBinaryDatasetWithCorrection(Collection<ArrayList<Double>> allChnlData) throws M9000Exception
	{
		double rms = 0.0;
		double scopeAverage = 0.0;
		double fMagnitude = 0.0;
		logger.debug("chnl Cnt from createBinaryDatasetWithCorrection .."+channelDataList.getChannelsCount());
//		System.out.println("getChannelsToDisplay() "+getChannelsToDisplay()[0]);
		series1 = new XYSeries[channelDataList.getChannelsCount()];
		Map<Integer, Double> mapSeriesForJQ = null;
		logger.debug("series1 size "+series1.length);
		scopeRmsValues = new String[channelDataList.getChannelsCount()];
//		System.out.println("Correcting channel data");
		int channelNo = 0;
		Iterator<ArrayList<Double>> chnlDataIterator = allChnlData.iterator();
		boolean booCorrection = true;
		ArrayList<Double> arrayList;
		Iterator<Double> chnlData;
		int dataCnt;
		double currentInt;
		int chnlDataIndex = 0;
		String channelName;
		
		while (chnlDataIterator.hasNext()) {
			arrayList = (ArrayList<Double>) chnlDataIterator.next();
//			System.out.println("Total no of samples in array "+arrayList.size());
			chnlData = arrayList.iterator();
			dataCnt = 0;
			rms = 0;
			scopeAverage = 0;
			currentInt = 0;
			Double data = 0.0;
			booCorrection = true;
			int correctedIndex = 0;
			double chnlMultiplier = 1;
			double chnlOffset = 0;
			double chnlRatio = 0; // Secondary divided by primary
			int calculatedChannelIndex = 0;
			int dataIndex = 0;
			imagSinData = 0.0;
			realCosData = 0.0;
			// Correction to the other channels in the phase
			while (correctedIndex < chnlDataIndex)
			{
				chnlData.next();
				correctedIndex++;
			}
//			System.out.println("Corrected index "+correctedIndex+" arrayList.size() "+arrayList.size());
			while (chnlData.hasNext() ) {
				try {
//					System.out.println("Dat cnt in while loop..."+dataCnt);
					data = (Double) chnlData.next();
					currentInt = data.shortValue();
					if (booCorrection && channelNo == 0)
					{
						chnlMultiplier = getSelectedDfr().getLstAnalogsInfo().get(0).getChnlMultiplier();
						chnlOffset = getSelectedDfr().getLstAnalogsInfo().get(0).getChnlOffset();
						currentInt = (currentInt*chnlMultiplier)+chnlOffset;
						if (currentInt < 0 )
						{
							while (currentInt < 0 )
							{
								if (!chnlData.hasNext())
								{
									break;
								}
								data = (Double) chnlData.next();
								chnlDataIndex++;
								currentInt = (data.shortValue()*chnlMultiplier)+chnlOffset;
							}
							booCorrection = false;
						}
						else if (currentInt > 0)
						{
							while (currentInt > 0)
							{
//								System.out.println("has next? "+chnlData.hasNext());
								if (!chnlData.hasNext())
								{
									break;
								}
								data = (Double) chnlData.next();
								chnlDataIndex++;
								currentInt = (data.shortValue()*chnlMultiplier)+chnlOffset;
							}
							while (currentInt < 0 )
							{
								if (!chnlData.hasNext())
								{
									break;
								}
								data = (Double) chnlData.next();
								chnlDataIndex++;
								currentInt = (data.shortValue()*chnlMultiplier)+chnlOffset;
							}
							booCorrection = false;

						}
//						System.out.println("Corrected index"+chnlDataIndex);
					}
					if (chnlDataIndex == (arrayList.size()-1))
					{
//						System.out.println("No correction required flag?"+booCorrection);
						chnlDataIndex = 0;
						chnlData = arrayList.iterator();
						continue;
					}
					if (series1[channelNo] == null)
					{
						if (getChannelsToDisplay() == null || getChannelsToDisplay().length == 0)
						{
							channelName = "Channel"+(channelNo+1);
							calculatedChannelIndex = channelNo;
						}
						else
						{
							calculatedChannelIndex = Integer.parseInt(getChannelsToDisplay()[channelNo])-getSelectedDfr().getAnalogChannelStart();
//							channelName = "Channel"+getChannelsToDisplay()[channelNo];
//							System.out.println("size of analog channels "+getSelectedDfr().getLstAnalogChannelNames().size());
//							System.out.println("Channel no "+channelNo+" getChannelsToDisplay()[channelNo] "+getChannelsToDisplay()[channelNo]+" getChannelsToDisplay()[0]"+getChannelsToDisplay()[0]);
							channelName = getSelectedDfr().getLstAnalogChannelNames().get(Integer.parseInt(getChannelsToDisplay()[channelNo])-getSelectedDfr().getAnalogChannelStart());
							channelName = channelName.substring(0, (channelName.indexOf("-")));
						}
						chnlMultiplier = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlMultiplier();
						chnlOffset = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlOffset();
						channelFactor = ""+getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getScalingFactor();
						series1[channelNo] = new XYSeries(channelName);
						mapSeriesForJQ = new HashMap<Integer, Double>();
					}
					data = (data*chnlMultiplier)+chnlOffset;
					if (!isPrimary && channelFactor.equalsIgnoreCase(M9kConstants.COMTRADE_PRIMARY))
					{
						chnlRatio = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getSecondary() / getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getPrimary();
						data *= chnlRatio ;
					}
					else if (isPrimary && channelFactor.equalsIgnoreCase(M9kConstants.COMTRADE_SECONDARY))
					{
						chnlRatio = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getPrimary() / getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getSecondary();
						data *= chnlRatio ;						
					}
					series1[channelNo].add(dataCnt++, data);
					mapSeriesForJQ.put(dataCnt++, data);
//					System.out.println("dataCnt :+"+(dataCnt-1)+" value: "+data.doubleValue());
					
					rms+=(data * data);
					scopeAverage+=data;
					imagSinData += arrSinData[dataIndex%totalSamples] * data;
					realCosData += arrCosData[dataIndex%totalSamples] * data;

					if (dataCnt == getSamplesCnt())
					{
//						System.out.println("\n\n\n\n\n\t\t\t\tNo of samples to plot "+getSamplesCnt()+" data Cnt "+dataCnt);
						break;
					}
					dataIndex++;
				} catch (Exception e) {
					e.printStackTrace();
					throw new M9000Exception(e);
				}
			}
//			logger.info("series1 for channel"+series1[channelNo]+" size.."+series1[channelNo].getItemCount()+" for channel..."+channelNo);
//			System.out.println("rms val "+rms+" datacnt "+(dataCnt));
			rms = Math.sqrt(rms/(dataCnt));
			scopeAverage = (scopeAverage/dataCnt);
			imagSinData = (imagSinData/dataCnt);
			realCosData = (realCosData/dataCnt);
			fMagnitude = Math.sqrt(2)*Math.sqrt((imagSinData*imagSinData)+(realCosData*realCosData));
//			System.out.println("calculated rms val "+rms);
//			System.out.println("Math.floor(rms * 100) "+Math.floor(rms * 100));
//			rms = rms > 0 ? Math.floor(rms * 100) / 100.0 : Math.ceil(rms * 100) / 100.0;
			BigDecimal bd = new BigDecimal(Double.toString(rms));
			bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
			rms = bd.doubleValue();
//			System.out.println("Channel no "+channelNo+"rms "+rms+"\t\t Channel unit"+getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlUnit());
			scopeRmsValues[channelNo] = " RMS: "+rms+" "+getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlUnit()+", fRMS: "+String.format("%.3f", fMagnitude)+", Avg: "+String.format("%.3f", scopeAverage);
			logger.debug("SCOPE values "+scopeRmsValues[channelNo]);
			channelNo++;
		}

		dataset.removeAllSeries();
		for (int i = 0; i < series1.length; i++) {
			if (series1[i] != null)
			{
				series1[i].setKey(scopeRmsValues[i]);
				dataset.addSeries(series1[i]);
			}
		}

		return dataset;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> readMapFromFile()
	{
		Map<String, String> map = new HashMap<String, String>(this.channelCnt);
		ObjectInputStream ois= null;
		try {
			ois = new ObjectInputStream(new URL("file:///E:/M9K-Utilities/Projects/M9000Master/resources/testDigitalNames.dat").openStream());
			map = (HashMap<String, String>)ois.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	private String[] getToolTipText()
	{
		Map<String, String> map = readMapFromFile();
		String[] chnlId = (String[])map.values().toArray(new String[0]); 
//		System.out.println(chnlId);
		return chnlId;
		
	}
	private String[] getSymbolsForSymbolAxis()
	{
		Map<String, String> map = readMapFromFile();
		String[] circuitNames =(String[])map.keySet().toArray(new String[0]); 
		LinkedList<String> result = new LinkedList<String>(Arrays.asList(circuitNames));
		result.add(0, "");
		circuitNames = (String[]) result.toArray(new String[0]);
//		System.out.println("circuit Name: "+ circuitNames.toString() +" : size: "+circuitNames.length);
		return circuitNames;		
	}
	@SuppressWarnings({ "unused", "unchecked" })
	private  XYDataset createDigitalDataset(Collection<ArrayList<Integer>> allChnlData)
	{
//		String[] toolTipText =  new String[] {"TwoTwoTwoTwoTwoTwoTwoTwoTwoTwoTwoTwo", "Three", "Four" , "Two1", "Three1", "Four1" , "Two2", "Three2", "Four2" , "Two3", "Three3", "Four3" , "Two4", "Three4", "Four4", "Four5"  };
		String[] toolTipText = getToolTipText();
//		if (toolTipText == null || toolTipText.length == 0)
//		{
//			toolTipText =  new String[] {"Twoo", "Three", "Four" , "Two1", "Three1", "Four1" , "Two2", "Three2", "Four2" , "Two3", "Three3", "Four3" , "Two4", "Three4", "Four4", "Four5"  };
//		}
		digitalSeries = new XYSeries[channelDataList.getDigitalChannelsCount()];
		toolTips = new ArrayList[channelDataList.getDigitalChannelsCount()];
//		System.out.println("Creating digital channel dataset" + channelDataList.getDigitalChannelsCount());
		int channelNo = 0;
		Iterator<ArrayList<Integer>> chnlDataIterator = allChnlData.iterator();
		ArrayList<Integer> arrayList;
		Iterator<Integer> chnlData;
		int dataCnt;
		int timeData = (int) ((triggerPointTime.getTime() - dataValueTime.getTime()));
//		System.out.println("base time dat: "+timeData);
		float samplingTime = 0;
//		System.out.println("Before while loop "+chnlDataIterator.hasNext());
		while (chnlDataIterator.hasNext()) {
			arrayList = (ArrayList<Integer>) chnlDataIterator.next();
			chnlData = arrayList.iterator();
			dataCnt = 1;
			Integer data = 0;
			
			while (chnlData.hasNext() ) {
				try {
					data = (Integer) chnlData.next();

					if (digitalSeries[channelNo] == null)
					{
						digitalSeries[channelNo] = new XYSeries("Channel"+channelNo);
						toolTips[channelNo] = new ArrayList<String>();
					}
					samplingTime = ((1/sampleRate)*1000)* dataCnt;
					if (data.intValue() == 0)
					{
//						digitalSeries[channelNo].add(dataCnt++, null);
						digitalSeries[channelNo].add((samplingTime - timeData), null);
						toolTips[channelNo].add("");
					}
					else
					{
//						digitalSeries[channelNo].add(dataCnt++, channelNo+1);
						digitalSeries[channelNo].add((samplingTime - timeData), channelNo+1);
						toolTips[channelNo].add(toolTipText[channelNo]);
					}
					dataCnt++;
					if (dataCnt == getNoOfSamplesToPlot())
					{
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			channelNo++;
		}

		digitalDataset.removeAllSeries();
		for (int i = 0; i < digitalSeries.length; i++) {
			digitalDataset.addSeries(digitalSeries[i]);				
		}

		return digitalDataset;
	}
	
	/**
	 * 
	 */
	public void synthesizeDataForFilteredRMS() {
		totalSamples = (int) (selectedDfr.getSampleRate()/selectedDfr.getLineFreq());
		logger.debug("Total samples "+totalSamples);
		arrSinData = new double[totalSamples];
		arrCosData = new double[totalSamples];
//		System.out.println("SIN	,	COS");
		for (int i = 0; i < totalSamples; i++) {
			arrSinData[i] = Math.sin(2*Math.PI*i/totalSamples);
			arrCosData[i] = Math.cos(2*Math.PI*i/totalSamples);
//			System.out.println(arrSinData[i]+","+arrCosData[i]);
		}

	}

//	public URL getDataSrc() {
//		return dataSrc;
//	}
//
//	public void setDataSrc(URL dataSrc) {
//		this.dataSrc = dataSrc;
//	}

	public float getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getLineFreq() {
		return lineFreq;
	}

	public void setLineFreq(int lineFreq) {
		this.lineFreq = lineFreq;
	}

	public int getChannelCnt() {
		return channelCnt;
	}

	public void setChannelCnt(int channelCnt) {
		this.channelCnt = channelCnt;
	}

	public int getSamplesCnt() {
		return samplesCnt;
	}

	public void setSamplesCnt(int samplesCnt) {
		this.samplesCnt = samplesCnt;
	}

	public float getNoOfSamplesToPlot() {
		return samplesCnt - (sampleRate/lineFreq);
	}


	public JFreeChart getChart() {
		return chart;
	}

	public void setChart(JFreeChart chart) {
		this.chart = chart;
	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	public void setChartPanel(ChartPanel chartPanel) {
		this.chartPanel = chartPanel;
	}

	public int getDigitalChannelCnt() {
		return digitalChannelCnt;
	}

	public void setDigitalChannelCnt(int digitalChannelCnt) {
		this.digitalChannelCnt = digitalChannelCnt;
	}
	
	public static Timestamp diff (java.util.Date t1, java.util.Date t2)
	{
	    // Make sure the result is always > 0
	    if (t1.compareTo (t2) < 0)
	    {
	        java.util.Date tmp = t1;
	        t1 = t2;
	        t2 = tmp;
	    }

	    // Timestamps mix milli and nanoseconds in the API, so we have to separate the two
	    long diffSeconds = (t1.getTime () / 1000) - (t2.getTime () / 1000);
	    // For normals dates, we have millisecond precision
	    int nano1 = ((int) t1.getTime () % 1000) * 1000000;
	    // If the parameter is a Timestamp, we have additional precision in nanoseconds
	    if (t1 instanceof Timestamp)
	        nano1 = ((Timestamp)t1).getNanos ();
	    int nano2 = ((int) t2.getTime () % 1000) * 1000000;
	    if (t2 instanceof Timestamp)
	        nano2 = ((Timestamp)t2).getNanos ();

	    int diffNanos = nano1 - nano2;
	    if (diffNanos < 0)
	    {
	        // Borrow one second
	        diffSeconds --;
	        diffNanos += 1000000000;
	    }

//	    System.out.println(("before milli mix: "+diffNanos));
	    // mix nanos and millis again
	    Timestamp result = new Timestamp ((diffSeconds * 1000) + (diffNanos / 1000000));
	    // setNanos() with a value of in the millisecond range doesn't affect the value of the time field
	    // while milliseconds in the time field will modify nanos! Damn, this API is a *mess*
	    result.setNanos (diffNanos);
//	    System.out.println((diffNanos));
	    return result;
	}

	private  synchronized List<Map<Integer, Double>> createBinaryDatasetWithCorrectionForJQ(Collection<ArrayList<Double>> allChnlData) throws M9000Exception
	{
		double rms = 0.0;
		double scopeAverage = 0.0;
		double fMagnitude = 0.0;
		logger.debug("chnl Cnt from createBinaryDatasetWithCorrection .."+channelDataList.getChannelsCount()+" allChnlData size "+allChnlData.size());
//		System.out.println("getChannelsToDisplay() "+getChannelsToDisplay()[0]);
		List<Map<Integer, Double>> lstOfMapDataSetForJQ =new ArrayList<Map<Integer, Double>>(channelDataList.getChannelsCount());
		Map<Integer, Double> mapSeriesForJQ = null;
		scopeRmsValues = new String[channelDataList.getChannelsCount()];
//		System.out.println("Correcting channel data");
		int channelNo = 0;
		Iterator<ArrayList<Double>> chnlDataIterator = allChnlData.iterator();
		boolean booCorrection = true;
		List<Double> arrayList;
		Iterator<Double> chnlData;
		int dataCnt;
		double currentInt;
		int chnlDataIndex = 0;
		String channelName="";
		// START: 28-Jan-2020 - Transducer implementation - Primary/Secondary display with appropriate units
		String unit="";
		//END: 28-Jan-2020
		
		while (chnlDataIterator.hasNext()) {
			arrayList = Collections.synchronizedList(chnlDataIterator.next());
			logger.debug("Processing channel no "+channelNo+" sample size "+arrayList.size());
//			System.out.println("Total no of samples in array "+arrayList.size());
			chnlData = arrayList.iterator();
			dataCnt = 0;
			rms = 0;
			scopeAverage = 0;
			currentInt = 0;
			Double data = 0.0;
			booCorrection = true;
			int correctedIndex = 0;
			double chnlMultiplier = 1;
			double chnlOffset = 0;
			double chnlRatio = 0; // Secondary divided by primary
			int calculatedChannelIndex = 0;
			int dataIndex = 0;
			imagSinData = 0.0;
			realCosData = 0.0;
			boolean isTransducerChannel = false;
			// Correction to the other channels in the phase
			while (correctedIndex < chnlDataIndex)
			{
				chnlData.next();
				correctedIndex++;
			}
//			System.out.println("Corrected index "+correctedIndex+" arrayList.size() "+arrayList.size());
			while (chnlData.hasNext() ) {
				try {
//					System.out.println("Dat cnt in while loop..."+dataCnt);
					data = (Double) chnlData.next();
					currentInt = data.shortValue();
					if (booCorrection && channelNo == 0)
					{
						chnlMultiplier = getSelectedDfr().getLstAnalogsInfo().get(0).getChnlMultiplier();
						chnlOffset = getSelectedDfr().getLstAnalogsInfo().get(0).getChnlOffset();
						currentInt = (currentInt*chnlMultiplier)+chnlOffset;
						if (currentInt < 0 )
						{
							while (currentInt < 0 )
							{
								if (!chnlData.hasNext())
								{
									break;
								}
								data = (Double) chnlData.next();
								chnlDataIndex++;
								currentInt = (data.shortValue()*chnlMultiplier)+chnlOffset;
							}
							booCorrection = false;
						}
						else if (currentInt > 0)
						{
							while (currentInt > 0)
							{
//								System.out.println("has next? "+chnlData.hasNext());
								if (!chnlData.hasNext())
								{
									break;
								}
								data = (Double) chnlData.next();
								chnlDataIndex++;
								currentInt = (data.shortValue()*chnlMultiplier)+chnlOffset;
							}
							while (currentInt < 0 )
							{
								if (!chnlData.hasNext())
								{
									break;
								}
								data = (Double) chnlData.next();
								chnlDataIndex++;
								currentInt = (data.shortValue()*chnlMultiplier)+chnlOffset;
							}
							booCorrection = false;

						}
//						System.out.println("Corrected index"+chnlDataIndex);
					}
					if (chnlDataIndex == (arrayList.size()-1))
					{
//						System.out.println("No correction required flag?"+booCorrection);
						chnlDataIndex = 0;
						chnlData = arrayList.iterator();
						continue;
					}
					if (mapSeriesForJQ == null)
					{
						if (getChannelsToDisplay() == null || getChannelsToDisplay().length == 0)
						{
							channelName = "Channel"+(channelNo+1);
							calculatedChannelIndex = channelNo;
						}
						else if ((getChannelsToDisplay().length-1) < channelNo)
						{
							logger.debug("DEBUG-SCOPE: ChannelNo index is greater than display channels. ChannelNo "+channelNo+" available channelsToDisplay length "+getChannelsToDisplay().length);
							continue;
						}
						else
						{
							logger.debug(" getChannelsToDisplay()[channelNo] "+getChannelsToDisplay()[channelNo]+" getSelectedDfr().getAnalogChannelStart() "+getSelectedDfr().getAnalogChannelStart());
							calculatedChannelIndex = Integer.parseInt(getChannelsToDisplay()[channelNo])-getSelectedDfr().getAnalogChannelStart();
							logger.debug("calculateChannelIndex "+calculatedChannelIndex+" getSelectedDfr().getAnalogChannelStart() "+getSelectedDfr().getAnalogChannelStart());
//							channelName = "Channel"+getChannelsToDisplay()[channelNo];
							logger.debug("size of analog channels "+getSelectedDfr().getLstAnalogChannelNames().size());
							logger.debug("Channel no "+channelNo+" getChannelsToDisplay()[channelNo] "+getChannelsToDisplay()[channelNo]+" getChannelsToDisplay()[0]"+getChannelsToDisplay()[0]);
							logger.debug("index to query fro lst analog chnl names "+(Integer.parseInt(getChannelsToDisplay()[channelNo])-getSelectedDfr().getAnalogChannelStart()));
							channelName = getSelectedDfr().getLstAnalogChannelNames().get(Integer.parseInt(getChannelsToDisplay()[channelNo])-getSelectedDfr().getAnalogChannelStart());
							// START: 10-Dec-2019 SCOPE channel name display
//							channelName = channelName.substring(0, (channelName.indexOf("-")));
							// END: 10-Dec-2019
						}
						chnlMultiplier = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlMultiplier();
						chnlOffset = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlOffset();
						channelFactor = ""+getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getScalingFactor();
						mapSeriesForJQ = new LinkedHashMap<Integer, Double>();
					}
					data = (data*chnlMultiplier)+chnlOffset;
					// START: 29-Jan-2020 - Transducer implementation
					// Units are seperated with : for transducer channels for primary and secondary scope display
					unit = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlUnit();
					int  indexStr = unit.indexOf(":");
					if (getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).isTranducer() || indexStr != -1)
					{
						isTransducerChannel = true;
						if (indexStr != -1)
						{
							if (isPrimary)
							{
								unit = unit.substring(0,indexStr);
							}
							else
							{
								unit = unit.substring(indexStr+1);
							}
						}
						// Calculate the secondary value by using the slope and offset
						// Primary ratio field is used as Slope transdcucer cahnnels
						// Secondary ratio field is used as offset for transdcucer cahnnels
						if (!isPrimary)
						{
							// (DesiredValue + Intercept) divided by Slope for Transducer
							data = (data - getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getSecondary()) / getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getPrimary() ;  
						}
					}
					else
					{
						if (!isPrimary && channelFactor.equalsIgnoreCase(M9kConstants.COMTRADE_PRIMARY))
						{
							chnlRatio = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getSecondary() / getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getPrimary();
							data *= chnlRatio ;
						}
						else if (isPrimary && channelFactor.equalsIgnoreCase(M9kConstants.COMTRADE_SECONDARY))
						{
							chnlRatio = getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getPrimary() / getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getSecondary();
							data *= chnlRatio ;						
						}
					}
					// END: 29-Jan-2020 
					mapSeriesForJQ.put((dataCnt++), data);
//					System.out.println("dataCnt :+"+(dataCnt-1)+" value: "+data.doubleValue());
					
					rms+=(data * data);
					scopeAverage+=data;
					imagSinData += arrSinData[dataIndex%totalSamples] * data;
					realCosData += arrCosData[dataIndex%totalSamples] * data;

					if (dataCnt == getSamplesCnt())
					{
//						System.out.println("\n\n\n\n\n\t\t\t\tNo of samples to plot "+getSamplesCnt()+" data Cnt "+dataCnt);
						break;
					}
					dataIndex++;
				} catch (Exception e) {
					logger.error("Error occured while data creation ",e);
					throw new M9000Exception(e);
				}
			}
//			logger.info("series1 for channel"+series1[channelNo]+" size.."+series1[channelNo].getItemCount()+" for channel..."+channelNo);
			logger.debug("rms val "+rms+" datacnt "+(dataCnt));
			if (dataCnt > 0)
			{
				rms = Math.sqrt(rms/(dataCnt));
				scopeAverage = (scopeAverage/dataCnt);
				imagSinData = (imagSinData/dataCnt);
				realCosData = (realCosData/dataCnt);
				fMagnitude = Math.sqrt(2)*Math.sqrt((imagSinData*imagSinData)+(realCosData*realCosData));
				logger.debug("SCOPE-BUG:calculated rms val "+rms);
	//			System.out.println("Math.floor(rms * 100) "+Math.floor(rms * 100));
	//			rms = rms > 0 ? Math.floor(rms * 100) / 100.0 : Math.ceil(rms * 100) / 100.0;
				BigDecimal bd = new BigDecimal(Double.toString(rms));
				// START: 31-Jan-2020 Transducer implementation - accuracy up to 5 digits after decimal point 
				bd = bd.setScale(5, BigDecimal.ROUND_HALF_UP);
				// END: 31-Jan-2020
				rms = bd.doubleValue();
	//			System.out.println("Channel no "+channelNo+"rms "+rms+"\t\t Channel unit"+getSelectedDfr().getLstAnalogsInfo().get(calculatedChannelIndex).getChnlUnit());
				// START: 31-Jan-2020 Transducer implementation - Remove RMS keyword from Legend display
				if (isTransducerChannel)
				{
					scopeRmsValues[channelNo] = channelName+" : "+rms+" "+unit+", fRMS: "+String.format("%.5f", fMagnitude)+", Avg: "+String.format("%.5f", scopeAverage);
				}
				else
				{
					scopeRmsValues[channelNo] = channelName+" - "+" RMS: "+rms+" "+unit+", fRMS: "+String.format("%.5f", fMagnitude)+", Avg: "+String.format("%.5f", scopeAverage);
				}
				// END: 31-Jan-2020
				logger.debug("Units "+unit+"SCOPE values "+scopeRmsValues[channelNo]);
				lstOfMapDataSetForJQ.add(mapSeriesForJQ);
	//			logger.debug("JQ data map "+mapSeriesForJQ);
				mapSeriesForJQ = null;
			}
			channelNo++;
		}


		return lstOfMapDataSetForJQ;
	}

	public void testTime()
	{
		// 1s == 1000ms == 1,000,000us == 1,000,000,000ns (1 billion ns)
//	    final  BigInteger ONE_BILLION = new BigInteger ("1000000000");
	        final Timestamp t1;
	        final Timestamp t2;
//	        final BigInteger firstTime;
//	        final BigInteger secondTime;
//	        final BigInteger diffTime;

	        t1 = dataValueTime;//new Timestamp(System.currentTimeMillis());
	        t2 = triggerPointTime;//new Timestamp(System.currentTimeMillis());

//	        System.out.println("\n\n\ndatavaluetime: "+t1);
//	        System.out.println("\n\n\nTrigger time: "+t2);
//	        firstTime  = BigInteger.valueOf(t1.getTime() / 1000 * 1000).multiply(ONE_BILLION ).add(BigInteger.valueOf(t1.getNanos()));
//	        secondTime = BigInteger.valueOf(t2.getTime() / 1000 * 1000).multiply(ONE_BILLION ).add(BigInteger.valueOf(t2.getNanos()));
//	        diffTime   = firstTime.subtract(secondTime);
//	        System.out.println("First time: "+firstTime);
//	        System.out.println("Second Time: "+secondTime);
//	        System.out.println("Difference: "+diffTime+"\n\n\n\n");
//	        System.out.println(new Timestamp(diffTime.longValue()).getNanos());
	        
	        
//	        System.out.println("difffffff: "+diff);
	        System.out.println(t1.getTime() - t2.getTime());//Math.abs(t2.getTime() - t1.getTime()));

	    }
//	private static long getTimeNoMillis(Timestamp t) {
//        return t.getTime() - (t.getNanos()/1000000);
//    }
	
	public int getSamplesPerCycle()
	{
//		System.out.println("Samples per cycle "+(int)(getSampleRate()/getLineFreq()));
//		System.out.println("Samples cnt "+getSamplesCnt());
		return (int)(getSampleRate()/getLineFreq());
	}
	
	public int sampleCntWithBuffer()
	{
		int sampleCntBuff = getSamplesCnt() + getSamplesPerCycle();
		
		return sampleCntBuff;
	}

	/**
	 * @return the channelsToDisplay
	 */
	public String[] getChannelsToDisplay() {
		return channelsToDisplay;
	}
	/**
	 * @param channelsToDisplay the channelsToDisplay to set
	 */
	public void setChannelsToDisplay(String[] channelsToDisplay) {
		logger.debug("\n\n\n\t\t\tIn M9KChart channelsDisplay set length "+channelsToDisplay.length);
		this.channelsToDisplay = channelsToDisplay;
	}
	/**
	 * @return the lstDfrs
	 */
	public List<DfrDTO> getLstDfrs() {
		return lstDfrs;
	}
	/**
	 * @param lstDfrs the lstDfrs to set
	 */
	public void setLstDfrs(List<DfrDTO> lstDfrs) {
		this.lstDfrs = lstDfrs;
	}
	/**
	 * @return the selectedDfr
	 */
	public DfrDTO getSelectedDfr() {
		return selectedDfr;
	}
	/**
	 * @param selectedDfr the selectedDfr to set
	 */
	public void setSelectedDfr(DfrDTO selectedDfr) {
		this.selectedDfr = selectedDfr;
	}
	/**
	 * @return the scopeRmsValues
	 */
	public String[] getScopeRmsValues() {
		return scopeRmsValues;
	}
	/**
	 * @param scopeRmsValues the scopeRmsValues to set
	 */
	public void setScopeRmsValues(String[] scopeRmsValues) {
		this.scopeRmsValues = scopeRmsValues;
	}
	public M9kStationCommandClient getCommandClient() {
		return commandClient;
	}
	public void setCommandClient(M9kStationCommandClient commandClient) {
		this.commandClient = commandClient;
	}
	public String getScopeCommand() {
		logger.debug("scope command to be sent ... "+scopeCommand);
		return scopeCommand;
	}
	public void setScopeCommand(String scopeCommand) {
		this.scopeCommand = scopeCommand;
	}
	public ChannelRawData getChannelDataList() {
		return channelDataList;
	}
	public void setChannelDataList(ChannelRawData channelDataList) {
		this.channelDataList = channelDataList;
	}

	public StationDTO getStationDto() {
		return stationDto;
	}

	public void setStationDto(StationDTO stationDto) {
		this.stationDto = stationDto;
	}

	public List<Color> getLstEventChannelStatus() {
		return lstEventChannelStatus;
	}

	public void setLstEventChannelStatus(List<Color> lstEventChannelStatus) {
		this.lstEventChannelStatus = lstEventChannelStatus;
	}

	public String getChannelFactor() {
		return channelFactor;
	}

	public void setChannelFactor(String channelFactor) {
		this.channelFactor = channelFactor;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	/**
	 * @return the arrSinData
	 */
	public double[] getArrSinData() {
		return arrSinData;
	}

	/**
	 * @param arrSinData the arrSinData to set
	 */
	public void setArrSinData(double[] arrSinData) {
		this.arrSinData = arrSinData;
	}

	/**
	 * @return the arrCosData
	 */
	public double[] getArrCosData() {
		return arrCosData;
	}

	/**
	 * @param arrCosData the arrCosData to set
	 */
	public void setArrCosData(double[] arrCosData) {
		this.arrCosData = arrCosData;
	}



	}
