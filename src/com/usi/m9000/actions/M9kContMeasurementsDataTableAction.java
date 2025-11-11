package com.usi.m9000.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.ContinuousComtradeDataDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.ContinuousComtradeDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.MessageProperties;
import com.usi.m9000.xml.util.M9kXMLUtils;

public class M9kContMeasurementsDataTableAction extends ActionSupport implements SessionAware,ServletRequestAware,ServletResponseAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ContinuousComtradeDataDAO mysqlContinuousComtradeDAO;
	private List<ContinuousComtradeDTO> lstComtradeDataDtos;
	private Map<String, Object> session;
	private String stationId;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kContMeasurementsDataTableAction.class);
	
	//JQGrid implementation
		private List<ContinuousComtradeDTO> gridModel;
	
	  // get how many rows we want to have into the grid - rowNum attribute in the
	  // grid
	  private Integer rows = 0;

	  // Get the requested page. By default grid sets this to 1.
	  private Integer page = 0;

	  // sorting order - asc or desc
	  private String sord;

	  // get index row - i.e. user click to sort.
	  private String sidx;

	  // Search Field
	  private String searchField;

	  // The Search String
	  private String searchString;

	  // Limit the result when using local data, value form attribute rowTotal
	  private Integer totalrows;

	  // he Search Operation
	  // ['eq','ne','lt','le','gt','ge','bw','bn','in','ni','ew','en','cn','nc']
	  private String searchOper;

	  // Your Total Pages
	  private Integer total = 0;

	  // All Records
	  private Integer records = 0;

	  private boolean loadonce = false;

	  private String oper = "edit";
	  private String faultIds;
	  private String id;
	  private static final String contDataType = M9kConstants.MEASUREMENTS;
	  private String measurementsStartTime;
	  private String measurementsEndTime;
	  private String statusMessage="";
  	  private List<String> lstMeasurementTypes = null;
  	  private String[] measurementTypes;
  	private String fileName = null;
	public M9kContMeasurementsDataTableAction() {
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		mysqlContinuousComtradeDAO = m9kDAOFactory.getContinuousComtradeDataDAO();
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;

	}

	@Override
	public void setServletResponse(HttpServletResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	public String getContMeasurementsData() throws M9000Exception
	{
		try {
			logger.debug("Entered getContMeasurementsData with stationId "+stationId+" lt type "+contDataType);
			if (getStationId() == null)
			{
				stationId = (String) session.get("stationId");
			}
			logger.debug("Stilllllll ContMeasurements DATA : Page " + getPage() + " Rows " + getRows()
		    + " Sorting Order " + getSord() + " Index Row :" + getSidx());
		    logger.debug("Search :" + searchField + " " + searchOper + " "
		    + searchString);

		    
//		    // Count all record (select count(*) from your_custumers)
//		    records = mysqlContinuousComtradeDAO.getTotalLTRCount(Integer.parseInt(stationId),contDataType);
//		    logger.info("Getting total faults from station "+stationId+" Total: "+records);

		    // Calculate until rows ware selected
		    int to = (rows * page);

		    // Calculate the first row to read
		    int from = to - rows;
		    logger.debug("From "+from +" to "+to);
		    String strOrderBy = null;
		    if (getSidx() != null && !getSidx().equals(""))
		    {
		    	logger.debug("sidx "+getSidx()+("is it displayTime?"+ (getSidx().equalsIgnoreCase("displayTime"))));
			    
			    if (sord != null && sord.equalsIgnoreCase("asc")) {
			    	if (getSidx().equalsIgnoreCase("id"))
			    	{
			    		strOrderBy=" order by id asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("displayTime"))
			    	{
			    		strOrderBy=" order by tsTrigger asc";
			    	}
			    	else
			    	{
			    		logger.debug("Did not match any col..Defaulting to.."+getSidx() );
			    		strOrderBy=" order by "+getSidx()+" asc";
			    	}
			    }
			    else
			    {
			    	if (getSidx().equalsIgnoreCase("id"))
			    	{
			    		strOrderBy=" order by id desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("displayTime"))
			    	{
			    		strOrderBy=" order by tsTrigger desc";
			    	}
			    	else
			    	{
			    		strOrderBy=" order by "+getSidx()+" desc";
			    	}
			    }
		    }

		    StringBuffer strQueryCriteriaBuffer = new StringBuffer();
		    if (searchField != null) {
	            if (searchField.equals("id")) {
		      // Search Customers
		      if (searchString != null && searchOper != null) {
		      int id = Integer.parseInt(searchString);
		      if (searchOper.equalsIgnoreCase("eq")) {
		      logger.debug("search id equals " + id);
		      strQueryCriteriaBuffer.append(" id = "+id);
		        }  else if (searchOper.equalsIgnoreCase("lt")) {
		          logger.debug("search id lesser then " + id);
		          strQueryCriteriaBuffer.append(" id < "+id);
		        } else if (searchOper.equalsIgnoreCase("gt")) {
		        	strQueryCriteriaBuffer.append(" id > "+id);
		          logger.debug("search id greater then " + id);
		        }
		      } 
	            }
		    }
            setGridModel(mysqlContinuousComtradeDAO.searchByCriteria(Integer.parseInt(stationId),contDataType, strQueryCriteriaBuffer.toString(), strOrderBy, from, to));
            lstComtradeDataDtos = getGridModel();
		    records = mysqlContinuousComtradeDAO.countByCriteria(Integer.parseInt(stationId), contDataType, strQueryCriteriaBuffer.toString());
		    
		    // Set to = max rows
		    if (to > records)
		    {
		    	to = records;
		    }

//		   // Set to = max rows
//		        if (to > records) to = records;

		        // Calculate total Pages
		        total = (int) Math.ceil((double) records / (double) rows);


//		      total = (int) Math.ceil((double) records / (double) rows);
		}  catch (Exception e) {
			throw new M9000Exception(e);
		}
		return SUCCESS;
	}
    public String showContMeasurementsData()
	{
    	String returnStatus = SUCCESS;
		try {
			logger.debug("BEfore conversion "+measurementsStartTime+" end "+measurementsEndTime);
			String convertedStartDate = M9kUtils.convertDateToSearch(getMeasurementsStartTime(),"MM/dd/yyyy HH:mm:ss","yyyy-MM-dd HH:mm:ss");
			String convertedEndDate = M9kUtils.convertDateToSearch(getMeasurementsEndTime(),"MM/dd/yyyy HH:mm:ss","yyyy-MM-dd HH:mm:ss");
			logger.debug("After Conversion: Start Date "+convertedStartDate+" End date "+convertedEndDate+" Station ID "+stationId);
			if (getStationId() == null)
			{
				stationId = (String) session.get("stationId");
			}
			Object receivedObj = null;
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
			messageProperties.addProperty("FROM_DATETIME", convertedStartDate);
			messageProperties.addProperty("TO_DATETIME", convertedEndDate);
			messageProperties.addProperty("DATA_TYPE", M9kConstants.CONTINUOUS_DATA);
			messageProperties.addProperty("MESSAGE_TYPE", "CONT_DATA_RETRIEVE");
			String strMeasurementsTypesCSV;
			if (getMeasurementTypes() == null || getMeasurementTypes().length == 0)
			{
				statusMessage = "Issue with fetching continuous data. Reason: No Measurement type selected";
				addActionError(statusMessage);
				return ERROR;
			}
			else
			{
				strMeasurementsTypesCSV = M9kUtils.getCommaSeperatedString(getMeasurementTypes());
			}
			messageProperties.addProperty("DATA_EXPORT_TYPE", strMeasurementsTypesCSV);

//			if (contDataType.equalsIgnoreCase(M9kConstants.CONTINUOUS_ANALOG_DATA))
//			{
				receivedObj = M9kMessagesUtil.getContinuousData(stationId+"-Files", messageProperties );
//			}
//			else
//			{
//				messageProperties.addProperty("DATA_EXPORT_TYPE", M9kConstants.CONTINUOUS_ANALOG_DATA);
//				receivedObj = M9kMessagesUtil.getContinuousData(stationId+"-Files", messageProperties );
//			}
			logger.debug("REceived object from message q "+receivedObj);
        	if (receivedObj == null)
        	{
//        		logger.error("Error in fetching file name: NULL object returned from station master");
//        		throw new M9000Exception("NULL object returned from station master");
        		statusMessage = "Issue fetching continuous data. Reason: "+fileName;
				addActionError(statusMessage);
				returnStatus = ERROR;
        	}
        	else
        	{
        		fileName = (String)receivedObj;
        		logger.info("cont measurements File Name received "+fileName);
        		File srcFile = new File(M9kUtils.getDataDir()+fileName);
    	        if (!srcFile.exists() || srcFile.length() == 0)
    	        {
    	        	M9kUtils.getFileIfNotAvailable(fileName);
    	        }
        	}
//        	logger.debug("The received string filename "+fileName);
//			if (fileName == null || fileName.isEmpty() || fileName.equalsIgnoreCase(M9kConstants.NO_DATA)|| fileName.toUpperCase().indexOf("ERROR") != -1)
//				{
//				statusMessage = "Issue with fetching continuous data. Reason: "+fileName;
//				addActionError(statusMessage);
//				returnStatus = ERROR;
//				}
//			else if (fileName.startsWith("ERROR:"))
//			{
//				statusMessage = fileName;
//				addActionError(statusMessage);
//				returnStatus = ERROR;
//			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Error occured while creating the continuous data file",e1);
		}
		return returnStatus;
	}

	public String fetchMeasurementTypes() {
		logger.debug("Inside fetchMeasurementTypes");
    	lstMeasurementTypes = null;
		try {
			lstMeasurementTypes = M9kXMLUtils.getMeasurementTypes();
			if (lstMeasurementTypes == null || lstMeasurementTypes.isEmpty())
			{
				lstMeasurementTypes = new ArrayList<String>(1);
				lstMeasurementTypes.add(M9kConstants.RMS);				
			}
			logger.debug("Measurement types "+lstMeasurementTypes);
		} catch (Exception e) {
			logger.error("Unable to get measurement names from config xml. Returning default RMS.",e);
			lstMeasurementTypes = new ArrayList<String>(1);
			lstMeasurementTypes.add(M9kConstants.RMS);
		}
		logger.debug("About to return measurement types "+lstMeasurementTypes);
		return SUCCESS;
	}
    
	public String editContMeasurementsData()
	{
		if (oper.equalsIgnoreCase("del")) {
			deleteContMeasurementsRecords(id);
        }
		return SUCCESS;
	}
	
	private int deleteContMeasurementsRecords(String csvIdsString) {

    	int numberOfRecordsDeleted = 0;
//		StringBuffer strbufFaultsId = null;
		String csvFaultFileNames = null;
		String deleteActionResult;
		boolean booStatus = true;
		try {
			logger.debug("ContMeasurement Record ids "+csvIdsString);
			try
			{
				if (getStationId() == null)
				{
					stationId = (String) session.get("stationId");
				}
				csvFaultFileNames = getContinuousFileNames(csvIdsString);
				logger.debug("Fault File Names "+csvFaultFileNames);
				// Delete from the remote web master
				if (M9kUtils.isRemote())
				{
					String strRemoteMasterDelFaultsStatus = M9kUtils.deleteFilesFromRemoteMaster(csvFaultFileNames);
					if (strRemoteMasterDelFaultsStatus == null || strRemoteMasterDelFaultsStatus.toUpperCase().startsWith("ERROR"))
					{
						booStatus = false;
						numberOfRecordsDeleted = -1;
					}
				}
				// Delete from the station master
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("MESSAGE_TYPE", "DELETE_FAULTS_FILES");
				messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
				messageProperties.addProperty("DATA_TYPE", M9kConstants.CONTINUOUS_DATA);

				
				deleteActionResult = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationId(), csvFaultFileNames.toString(), messageProperties);
				
				if (deleteActionResult == null || deleteActionResult.toUpperCase().startsWith("ERROR"))
				{
					booStatus = false;
					numberOfRecordsDeleted = -1;
				}

				if (booStatus)
				{
					// Delete from the database
					numberOfRecordsDeleted = mysqlContinuousComtradeDAO.deleteRecords(Integer.parseInt(getStationId()), csvIdsString.toString() );
				}
			}
			catch(Exception e1)
			{
				booStatus = false;
				logger.error("Error occured while deleting faults "+e1.getMessage());
				numberOfRecordsDeleted = -1;
				e1.printStackTrace();
			}

			
		} catch (Exception e1) {
			e1.printStackTrace();
			numberOfRecordsDeleted = -1;

		}
		finally
		{
			
		}

		return numberOfRecordsDeleted;
		}

    /**
     * Gets the corresponding file names of the csvFaultIds in ;; separated format string
     * @param csvFaultsString
     * @return
     */
	private String getContinuousFileNames(String csvFaultsString) {
		String ltrFileNames = null;
		try {
			ltrFileNames = mysqlContinuousComtradeDAO.getContinuousDataFileNames(Integer.parseInt(getStationId()), csvFaultsString.toString());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Unable to get the file names of the cont files from the database. Physical cont files are not deleted for the faults "+csvFaultsString, e);
		}
		catch (M9000Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Unable to get the file names of the cont files from the database. Physical cont files are not deleted for the faults "+csvFaultsString, e);
		}
		return ltrFileNames;
	}

	public List<ContinuousComtradeDTO> getLstComtradeDataDtos() {
		return lstComtradeDataDtos;
	}

	public void setLstComtradeDataDtos(List<ContinuousComtradeDTO> lstComtradeDataDtos) {
		this.lstComtradeDataDtos = lstComtradeDataDtos;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public String getSord() {
		return sord;
	}

	public void setSord(String sord) {
		this.sord = sord;
	}

	public String getSidx() {
		return sidx;
	}

	public void setSidx(String sidx) {
		this.sidx = sidx;
	}

	public String getSearchField() {
		return searchField;
	}

	public void setSearchField(String searchField) {
		this.searchField = searchField;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Integer getTotalrows() {
		return totalrows;
	}

	public void setTotalrows(Integer totalrows) {
		this.totalrows = totalrows;
	}

	public String getSearchOper() {
		return searchOper;
	}

	public void setSearchOper(String searchOper) {
		this.searchOper = searchOper;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getRecords() {
		return records;
	}

	public void setRecords(Integer records) {
		this.records = records;
	}

	public boolean isLoadonce() {
		return loadonce;
	}

	public void setLoadonce(boolean loadonce) {
		this.loadonce = loadonce;
	}

	public List<ContinuousComtradeDTO> getGridModel() {
		return gridModel;
	}

	public void setGridModel(List<ContinuousComtradeDTO> gridModel) {
		logger.debug("Inside Grid Model ");
		this.gridModel = gridModel;
	}

	public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}

	public String getFaultIds() {
		return faultIds;
	}

	public void setFaultIds(String faultIds) {
		this.faultIds = faultIds;
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMeasurementsStartTime() {
		return measurementsStartTime;
	}

	public void setMeasurementsStartTime(String measurementsStartTime) {
		this.measurementsStartTime = measurementsStartTime;
	}

	public String getMeasurementsEndTime() {
		return measurementsEndTime;
	}

	public void setMeasurementsEndTime(String measurementsEndTime) {
		this.measurementsEndTime = measurementsEndTime;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public List<String> getLstMeasurementTypes() {
		return lstMeasurementTypes;
	}

	public void setLstMeasurementTypes(List<String> lstMeasurementTypes) {
		this.lstMeasurementTypes = lstMeasurementTypes;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String[] getMeasurementTypes() {
		return measurementTypes;
	}

	public void setMeasurementTypes(String[] measurementTypes) {
		this.measurementTypes = measurementTypes;
	}


}
