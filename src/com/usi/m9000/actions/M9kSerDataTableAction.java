package com.usi.m9000.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.M9kSerDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.M9kSerDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.reports.M9kSERReportPDF;
import com.usi.m9000.reports.util.Column;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class M9kSerDataTableAction extends ActionSupport implements SessionAware,ServletRequestAware,ServletResponseAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private M9kSerDAO serDao;
	private List<M9kSerDTO> lstSerDataDtos;
	private Map<String, Object> session;
	private String stationId;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kSerDataTableAction.class);
	
	//JQGrid implementation
		private List<M9kSerDTO> gridModel;
	
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
	  private InputStream inputStream;
      private long contentLength;
      private String fileName = "";
	  private String contentType="application/octet-stream";
	  private UsersDTO userDto;
	  private String filters;
	  private List<String> lstSelectedSerDates;
	  private Map<String,String> mapSerDates = null;
	  List<M9kSerDTO> lstOfActiveEvents; // All available distinct events for search list
	  private Map<String,String> mapAllAvailableSERDates = new HashMap<String, String>();
	  
	public M9kSerDataTableAction() {
		logger.debug("Entered M9kSerDataTableAction ");
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		serDao = m9kDAOFactory.getM9kSerDAO();
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		userDto = (UsersDTO) session.get("userDetails");

	}

	@Override
	public void setServletResponse(HttpServletResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	public String getSerData() throws M9000Exception
	{
		try {
			logger.debug("Entered getSerData with stationId "+stationId);
			if (getStationId() == null)
			{
				stationId = (String) session.get("stationId");
			}
			logger.debug("Stilllllll DDR DATA : Page " + getPage() + " Rows " + getRows()
		    + " Sorting Order " + getSord() + " Index Row :" + getSidx());
		    logger.debug("Search :" + searchField + " " + searchOper + " "
		    + searchString);

		    logger.debug("sort selected? "+sord+" column to be sorted "+getSidx());
		    logger.debug("serdates selected? "+lstSelectedSerDates);
//		    // Count all record (select count(*) from your_custumers)
//		    records = serDao.getTotalSerCount(Integer.parseInt(stationId));
//		    logger.info("Getting total SERs from station "+stationId+" Total: "+records);

		    // Calculate until rows ware selected
		    int to = (rows * page);

		    // Calculate the first row to read
		    int from = to - rows;
		    logger.debug("From "+from +" to "+to);
		    String strOrderBy = null;
		    if (getSidx() != null && !getSidx().equals(""))
		    {
		    	logger.debug("sidx "+getSidx()+("is it displayTime?"+ (getSidx().equalsIgnoreCase("alarmTime"))));
			    
			    if (sord != null && sord.equalsIgnoreCase("asc")) {
			    	if (getSidx().equalsIgnoreCase("currentStateAsString"))
			    	{
			    		strOrderBy=" order by state asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("displayTime"))
			    	{
			    		strOrderBy=" order by ts asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("status"))
			    	{
			    		strOrderBy=" order by state asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("lockedAsString"))
			    	{
			    		strOrderBy=" order by locked asc";
			    	}
			    	else
			    	{
			    		logger.debug("Did not match any col..Defaulting to.."+getSidx() );
			    		strOrderBy=" order by "+getSidx()+" asc";
			    	}
			    }
			    else
			    {
			    	if (getSidx().equalsIgnoreCase("currentStateAsString"))
			    	{
			    		strOrderBy=" order by state desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("displayTime"))
			    	{
			    		strOrderBy=" order by ts desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("status"))
			    	{
			    		strOrderBy=" order by state desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("lockedAsString"))
			    	{
			    		strOrderBy=" order by locked desc";
			    	}
			    	else
			    	{
			    		strOrderBy=" order by "+getSidx()+" desc";
			    	}
			    }
		    }

		    StringBuffer strQueryCriteriaBuffer = new StringBuffer();
		    if (getFilters() != null && !getFilters().isEmpty())
		    {
		    	logger.debug("Entered filters "+getFilters());
		    	JSONObject filterObject = JSONObject.fromObject(getFilters());
		    	logger.debug("json object filter "+filterObject.toString());
		    	String groupOp = filterObject.getString("groupOp");
		    	String field;
		    	String op;
		    	String data;
		    	JSONArray rules = filterObject.getJSONArray("rules");
		    	if (searchField == null ) {
			    	for (int i = 0; i < rules.size(); i++)
			    	{
			    	    field = rules.getJSONObject(i).getString("field");
			    	    op = rules.getJSONObject(i).getString("op");
			    	    data = rules.getJSONObject(i).getString("data");
			    	    logger.debug("Field "+field+" op "+op+" data "+data);
			    	    if (strQueryCriteriaBuffer.length() > 0)
			    	    {
			    	    	strQueryCriteriaBuffer.append(" and ");
			    	    }
			    	    if (field.equals("currentStateAsString"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" (normal^state) ");
			    	    	strQueryCriteriaBuffer.append(" like '");
			    	    }
			    	    else if (field.equals("displayTime"))
			    	    {
			    	    	strQueryCriteriaBuffer.append("date_format(from_unixtime(ts div 1000000),\"%m%d%Y%H%i%s\")");
			    	    	strQueryCriteriaBuffer.append(" like '%");
			    	    	data = data.replaceAll("[:\\/-]", "");
							data = data.replaceAll("[.]", "");
							data = data.replaceAll("[ ]", "");
			    	    }
			    	    else if (field.equals("status"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" state ");
			    	    	strQueryCriteriaBuffer.append(" like '");
			    	    }
			    	    else if (field.equals("lockedAsString"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" locked ");
			    	    	strQueryCriteriaBuffer.append(" like '%");
			    	    }
			    	    else
			    	    {
			    	    	strQueryCriteriaBuffer.append(field);
			    	    	strQueryCriteriaBuffer.append(" like '");
			    	    }
			    	    
		    	    	strQueryCriteriaBuffer.append(data);
		    	    	strQueryCriteriaBuffer.append("%' ");
			    	}
		    	}
		    	else
		    	{
		    		for (int i = 0; i < rules.size(); i++)
			    	{
			    	    field = rules.getJSONObject(i).getString("field");
			    	    op = rules.getJSONObject(i).getString("op");
			    	    data = rules.getJSONObject(i).getString("data");
			    	    logger.debug("Field "+field+" op "+op+" data "+data);
			    	    if (strQueryCriteriaBuffer.length() > 0)
			    	    {
			    	    	strQueryCriteriaBuffer.append(" "+groupOp +" ");
			    	    }
				    	logger.debug("Search field entered..."+field);
				    	if (field.equals("currentStateAsString"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" (normal^state) ");
			    	    	strQueryCriteriaBuffer.append(" like '%"+data+"%'");
			    	    }
				    	else if (field.equals("status"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" state ");
			    	    	strQueryCriteriaBuffer.append(" like '%"+data+"%'");
			    	    }
			    	    else if (field.equals("lockedAsString"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" locked ");
			    	    	strQueryCriteriaBuffer.append(" like '%"+data+"%'");
			    	    }
			    	    else if (field.equals("displayTime")) {
			            	String strSearchDate = "ser_date";
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" = '"+data+"'");
							      }
							      else if (op.equalsIgnoreCase("le")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" <= '"+data+"'");
							      }
							      else if (op.equalsIgnoreCase("ge")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" >= '"+data+"'");
							      }
							      else if (op.equalsIgnoreCase("gt")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" > '"+data+"'");
							      }
							      else if (op.equalsIgnoreCase("lt")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" < '"+data+"'");
							      }
							      else if (op.equalsIgnoreCase("ne")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" != '"+data+"'");
							      }
						      } 
					    }

				    	 else 
				            {
							      if (data != null && op != null) {
								      if (op.equalsIgnoreCase("cn")) {
								      strQueryCriteriaBuffer.append(" "+field+" like '%"+data+"%'");
								        }
								      else if (op.equalsIgnoreCase("nc")) {
									      strQueryCriteriaBuffer.append(" "+field+" not like '%"+data+"%'");
									  }
								      else if (op.equalsIgnoreCase("bw")) {
									      strQueryCriteriaBuffer.append(" "+field+" like '"+data+"%'");
									  }
								      else if (op.equalsIgnoreCase("bn")) {
									      strQueryCriteriaBuffer.append(" "+field+" not like '"+data+"%'");
									  }
								      else if (op.equalsIgnoreCase("eq")) {
									      strQueryCriteriaBuffer.append(" "+field+" = '"+data+"'");
									  }
								      else if (op.equalsIgnoreCase("ne")) {
									      strQueryCriteriaBuffer.append(" "+field+" != '"+data+"'");
									  }
								      else if (op.equalsIgnoreCase("gt")) {
									      strQueryCriteriaBuffer.append(" "+field+" > '"+data+"'");
									  }
								      else if (op.equalsIgnoreCase("lt")) {
									      strQueryCriteriaBuffer.append(" "+field+" < '"+data+"'");
									  }
								      else if (op.equalsIgnoreCase("bw")) {
									      strQueryCriteriaBuffer.append(" comments like '"+data+"%'");
									  }	
							      }
				            }
			    	}
		    	}
		    }
		    if (lstSelectedSerDates != null && !lstSelectedSerDates.isEmpty())
		    {
		    	lstSelectedSerDates.remove("-1");
		    	String strquerySerDates = getDateSearchSQLString(lstSelectedSerDates);
		    	logger.debug("Dates as sql query stmt "+strquerySerDates);
		    	if (strquerySerDates != null && !strquerySerDates.isEmpty())
		    	{
			    	if (strQueryCriteriaBuffer.length() > 0)
			    	{
			    		strQueryCriteriaBuffer.append(" and ");
			    	}
			    	strQueryCriteriaBuffer.append(strquerySerDates);
		    	}
		    }
		    logger.debug("Final SER sql criteria to be executed "+strQueryCriteriaBuffer);
            setGridModel(serDao.searchByCriteria(Integer.parseInt(stationId),strQueryCriteriaBuffer.toString(), strOrderBy, from, to));
            lstSerDataDtos = getGridModel();
            session.put("lstSerDataDtos", lstSerDataDtos);
		    records = serDao.countByCriteria(Integer.parseInt(stationId), strQueryCriteriaBuffer.toString());
		    
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
			logger.debug("Error occured while fetching ser data ",e);
			throw new M9000Exception(e);
		}
		return SUCCESS;
	}

	public String createSERExportFile() throws M9000Exception {
		StringBuffer strFileContent = new StringBuffer();
		String fileToBeWritten = new SimpleDateFormat("'ser-exports-'MMddyyyy_HHmmss'.csv'").format(new Date());
		try
		{
			M9kSerDTO m9kSerDTO;
			lstSerDataDtos = (List<M9kSerDTO>) session.get("lstSerDataDtos");
			// Write Headers into files first
			strFileContent.append("Date, Time, Local Time Code, Substation, Device, State");
			strFileContent.append(M9kConstants.NEWLINE);
			logger.debug("In createSERExportFile gridModel "+getGridModel()+" list of ser data "+getLstSerDataDtos());
			for (Iterator<M9kSerDTO> iterator = getLstSerDataDtos().iterator(); iterator.hasNext();) {
				m9kSerDTO =  iterator.next();
					strFileContent.append(m9kSerDTO.getDisplayTime().replace(" -", ","));
					strFileContent.append("," + M9kUtils.getLocalTimeCode());
					strFileContent.append("," + M9kUtils.getStationDetails().getSystemStationName());
					strFileContent.append("," + m9kSerDTO.getName());
					strFileContent.append("," + m9kSerDTO.getCurrentStateAsString());
					strFileContent.append(M9kConstants.NEWLINE);
			}
	        setContentType("application/octet-stream");
	        setContentLength(strFileContent.length());
	        setFileName(M9kUtils.getStationDetails().getSystemStationName()+"_"+fileToBeWritten);
			inputStream = new ByteArrayInputStream(strFileContent.toString().getBytes());
		}
		catch(Exception e)
		{
			throw new M9000Exception(e);
		}
		finally{
		}
		return SUCCESS;
		
	}
	
	public String printSerData() throws Exception
	{
		ByteArrayOutputStream baos = null;
//		System.out.println("Entered prinSerData");
		lstSerDataDtos = (List<M9kSerDTO>) session.get("lstSerDataDtos");
		logger.debug("In printSerData gridModel "+getGridModel()+" list of ser data "+getLstSerDataDtos());
		if (getLstSerDataDtos().size() <= 0)
		{
			throw new M9000Exception("No Data available for print preview");
		}
		List<Column> columnsList = createColumnData();
		String reportTitle = "Sequence Of Event Recorder Report";
		M9kSERReportPDF m9kSERReportPDF = new M9kSERReportPDF(reportTitle, columnsList, getLstSerDataDtos());
		JasperReportBuilder serReport = m9kSERReportPDF.buildPDF();
		if (serReport != null)
    	{
			String fileToBeWritten = new SimpleDateFormat("'ser-report-'MMddyyyy_HHmmss'.pdf'").format(new Date());
	    	baos = new ByteArrayOutputStream();
	    	serReport.toPdf(baos);
	    	inputStream = new ByteArrayInputStream(baos.toByteArray());
	        setContentType("application/pdf");
	        setContentLength(baos.toByteArray().length);
	        setFileName(M9kUtils.getStationDetails().getSystemStationName()+fileToBeWritten);
	        logger.debug("Content length set "+getContentLength());
	        logger.debug("Filename "+getFileName());
//	        logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" printed config file in PDF format for station "+stationDetails.getStationDisplayName());
    	}
    	else
        {
        	logger.error("PDF turned out to be null. ");
//        	logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" encountered errors while printing config file in PDF format for station "+stationDetails.getStationDisplayName());
			throw new M9000Exception("No Report generated");
        }
		return SUCCESS;
	}

	/**
	 * 
	 */
	private List<Column> createColumnData() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("Date-Time","Date-Time","string",150));//dataType = "String", "STRING", "java.lang.String", "text"
		columns.add(new Column("Event",    "Event",  "integer",50));//dataType = "Integer", "INTEGER", "java.lang.Integer"
		columns.add(new Column("Current",  "Current", "string", 50));//dataType = "bigdecimal", "BIGDECIMAL", "java.math.BigDecimal"
		columns.add(new Column("Status",  "Status", "string",60));
		columns.add(new Column("Sync",  "Sync", "string",30));//dataType = "dateyeartofraction", "DATEYEARTOFRACTION"
		columns.add(new Column("Description",  "Description", "string",150));//dataType = "DateYear", "dateyear", "DATEYEAR"
		return columns;

		
	}

	public String getSerDatesToView() throws M9000Exception
	{
		List<String> lstSerDates;
		try {
			lstSerDates = serDao.getAllAvailableSerDates(Integer.parseInt(stationId), M9kUtils.getSerDatesViewLimit());
		} 
		catch (Exception e)
		{
			lstSerDates = new ArrayList<String>();
			throw new M9000Exception(e);
		}
		String serDate;
		if (mapSerDates == null)
		{
			mapSerDates = new LinkedHashMap<String, String>(lstSerDates.size());
		}
		for (Iterator<String> iterator = lstSerDates.iterator(); iterator.hasNext();) {
			serDate = iterator.next();
			// START: 15-May-2020 - Using the newly added ser_date column instead of ts to improve performance with huge data
			serDate = serDate.substring(0, serDate.indexOf(" ")); // Remove the time part of the string
//			mapSerDates.put(serDate.replaceAll("/", "-"), M9kUtils.convertDateFormatString(serDate, "yyyy/mm/dd", "mm/dd/yyyy"));
			mapSerDates.put(serDate, M9kUtils.convertDateFormatString(serDate, "yyyy-MM-dd", "MM/dd/yyyy"));
			// END: 15-May-2020
		}
		logger.debug("total ser dates "+mapSerDates.size());
		return SUCCESS;
	}	
	
	/**
	 * Gets all available ser dates without any limit
	 * @return
	 * @throws M9000Exception
	 */
	public String getListOfAllAvailableSerDates() throws M9000Exception
	{
		List<String> lstSerDates;
		try {
			lstSerDates = serDao.getAllAvailableSerDates(Integer.parseInt(stationId), -1);
		} 
		catch (Exception e)
		{
			lstSerDates = new ArrayList<String>();
			throw new M9000Exception(e);
		}
		if (lstSerDates.size() > 0)
		{
			mapAllAvailableSERDates = convertListToMap(lstSerDates);
		}
		
		logger.debug("total avaialble ser dates "+mapAllAvailableSERDates.size());
		return SUCCESS;
	}	
	
	/**
	 * convert list to map
	 */
	private Map<String, String> convertListToMap(List<String> lstSerDates) throws M9000Exception
	{
		String serDate;
		Map <String, String> mapSerDatesLocal  = new LinkedHashMap<String, String>(lstSerDates.size());
		for (Iterator<String> iterator = lstSerDates.iterator(); iterator.hasNext();) {
			serDate = iterator.next();
			// START: 15-May-2020 - Using the newly added ser_date column instead of ts to improve performance with huge data
			serDate = serDate.substring(0, serDate.indexOf(" ")); // Remove the time part of the string
//			mapSerDates.put(serDate.replaceAll("/", "-"), M9kUtils.convertDateFormatString(serDate, "yyyy/mm/dd", "mm/dd/yyyy"));
			mapSerDatesLocal.put(serDate, M9kUtils.convertDateFormatString(serDate, "yyyy-MM-dd", "MM/dd/yyyy"));
			// END: 15-May-2020
		}
		logger.debug("total ser dates "+mapSerDatesLocal.size());
		return mapSerDatesLocal;
	}
	/**
	 * 
	 * @return
	 * @throws M9000Exception
	 */
	public String getActiveEvents() throws M9000Exception
	{
		lstOfActiveEvents = serDao.getAllAvailableEvents(Integer.parseInt(stationId));
		return SUCCESS;
	}
	private String getDateSearchSQLString(List<String> lstDates)
	{
		String strDateSearchQuery = null;
		StringBuffer strQueryBuf =null;
		String dateToMatch = null;
		for (Iterator<String> iterator = lstDates.iterator(); iterator.hasNext();) {
//				targetDates.append("'"+iterator.next()+"'");
			dateToMatch = iterator.next();
			if (dateToMatch == null || dateToMatch.trim().isEmpty() || dateToMatch.trim().equalsIgnoreCase("-1"))
			{
				continue;
			}
			// START: 18-May-2020 - Use SER_DATE column instead of ts just to make query string shorter
			if (strQueryBuf == null)
			{
				strQueryBuf =  new StringBuffer(" SER_DATE IN (");
			}
//			try {
//				strQueryBuf.append(getDateSearchSQLString(dateToMatch));
				strQueryBuf.append("'"+dateToMatch+"'");
//			} catch (Exception e) {
//				strQueryBuf.append(" (date(from_unixtime(ts div 1000000)) like '"+dateToMatch+"' ) ");
//				logger.warn("Couldn't construct SER sql string for date search due to exception ",e);
//			}
			
			if (iterator.hasNext())
			{
//					targetDates.append(" or ");
				strQueryBuf.append(" , ");
			}
			// END: 18-May-2020 
		}
		if (strQueryBuf != null)
		{
			strQueryBuf.append(") ");
			strDateSearchQuery = strQueryBuf.toString();
		}
		return strDateSearchQuery;
	}
	
	@SuppressWarnings("unused")
	private String getDateSearchSQLString(String dateToSearch) throws Exception
	{
		String dateSearchSQLStr = " (ts between unix_timestamp('START')*1000000 and unix_timestamp ('END')*1000000) ";
		DateFormat searchDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal  = Calendar.getInstance();
		cal.setTime(searchDateFormat.parse(dateToSearch));
		
		dateSearchSQLStr = dateSearchSQLStr.replace("START", (searchDateFormat.format(cal.getTime())+" 00:00:00"));
        cal.add(Calendar.DATE, 1);
        dateSearchSQLStr = dateSearchSQLStr.replace("END", (searchDateFormat.format(cal.getTime())+" 00:00:00"));
        
        logger.debug("Query string to be returned..."+dateSearchSQLStr);
        
        return dateSearchSQLStr;
	}

//	public String getJSON() throws M9000Exception
//	{
//		return getAvailableSerDates();
//	}
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

	public List<M9kSerDTO> getGridModel() {
		return gridModel;
	}

	public void setGridModel(List<M9kSerDTO> gridModel) {
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

	public List<M9kSerDTO> getLstSerDataDtos() {
		return lstSerDataDtos;
	}

	public void setLstAlarmsLogDataDtos(List<M9kSerDTO> lstSerDataDtos) {
		this.lstSerDataDtos = lstSerDataDtos;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public UsersDTO getUserDto() {
		if (userDto == null)
    	{
    		userDto = (UsersDTO) session.get("userDetails");
    	}
		return userDto;
	}

	public void setUserDto(UsersDTO userDto) {
		this.userDto = userDto;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public List<String> getLstSelectedSerDates() {
		return lstSelectedSerDates;
	}

	public void setLstSelectedSerDates(List<String> lstSelectedSerDates) {
		this.lstSelectedSerDates = lstSelectedSerDates;
	}

	public Map<String, String> getMapSerDates() {
		return mapSerDates;
	}

	public void setMapSerDates(Map<String, String> mapSerDates) {
		this.mapSerDates = mapSerDates;
	}

	public List<M9kSerDTO> getLstOfActiveEvents() {
		return lstOfActiveEvents;
	}

	public void setLstOfActiveEvents(List<M9kSerDTO> lstOfActiveEvents) {
		this.lstOfActiveEvents = lstOfActiveEvents;
	}

	public Map<String, String> getMapAllAvailableSERDates() {
		return mapAllAvailableSERDates;
	}

	public void setMapAllAvailableSERDates(Map<String, String> mapAllAvailableSERDates) {
		this.mapAllAvailableSERDates = mapAllAvailableSERDates;
	}

}
