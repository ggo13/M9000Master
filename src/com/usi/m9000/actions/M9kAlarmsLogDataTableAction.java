package com.usi.m9000.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.AlarmsLogDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AlarmLogsDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class M9kAlarmsLogDataTableAction extends ActionSupport implements SessionAware,ServletRequestAware,ServletResponseAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AlarmsLogDAO mysqlAlarmsLogDAO;
	private List<AlarmLogsDTO> lstAlarmsLogDataDtos;
	private Map<String, Object> session;
	private String stationId;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kAlarmsLogDataTableAction.class);
	
	//JQGrid implementation
		private List<AlarmLogsDTO> gridModel;
	
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
	  private String filters;
	  
	public M9kAlarmsLogDataTableAction() {
		logger.debug("Entered M9kAlarmsLogDataTableAction ");
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		mysqlAlarmsLogDAO = m9kDAOFactory.getAlarmsLogDAO();
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

	public String getAlarmsData() throws M9000Exception
	{
		try {
			logger.debug("Entered getAlarmsData with stationId "+stationId);
			if (getStationId() == null)
			{
				stationId = (String) session.get("stationId");
			}
			logger.debug("Stilllllll DDR DATA : Page " + getPage() + " Rows " + getRows()
		    + " Sorting Order " + getSord() + " Index Row :" + getSidx());
		    logger.debug("Search :" + searchField + " " + searchOper + " "
		    + searchString);

		    logger.debug("sort selected? "+sord+" column to be sorted "+getSidx());
		    
//		    // Count all record (select count(*) from your_custumers)
//		    records = mysqlAlarmsLogDAO.getTotalAlarmsCount(Integer.parseInt(stationId));
//		    logger.info("Getting total faults from station "+stationId+" Total: "+records);

		    // Calculate until rows ware selected
		    int to = (rows * page);

		    // Calculate the first row to read
		    int from = to - rows;
		    logger.debug("From "+from +" to "+to);
		    String strOrderBy = null;
		    if (getSidx() != null && !getSidx().equals(""))
		    {
		    	logger.debug("sidx "+getSidx()+(" Is it displayTime?"+ (getSidx().equalsIgnoreCase("alarmTime"))));
		    	logger.debug("Is it ledName? "+(getSidx().equalsIgnoreCase("ledName")));
			    
			    if (sord != null && sord.equalsIgnoreCase("asc")) {
			    	if (getSidx().equalsIgnoreCase("alarmId"))
			    	{
			    		strOrderBy=" order by idalarms_log asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("alarmTime"))
			    	{
			    		strOrderBy=" order by updated asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("ledStatus"))
			    	{
			    		strOrderBy=" order by led_status asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("ledName"))
			    	{
			    		strOrderBy=" order by led_name asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("alarmDescription"))
			    	{
			    		strOrderBy=" order by description asc";
			    	}
			    	else
			    	{
			    		logger.debug("Did not match any col..Defaulting to.."+getSidx() );
			    		strOrderBy=" order by "+getSidx()+" asc";
			    	}
			    }
			    else
			    {
			    	if (getSidx().equalsIgnoreCase("alarmId"))
			    	{
			    		strOrderBy=" order by idalarms_log desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("alarmTime"))
			    	{
			    		strOrderBy=" order by updated desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("ledStatus"))
			    	{
			    		strOrderBy=" order by led_status desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("ledName"))
			    	{
			    		strOrderBy=" order by led_name desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("alarmDescription"))
			    	{
			    		strOrderBy=" order by description desc";
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
		    	if (searchField == null) {
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
			    	    if (field.equals("alarmId"))
			    	    {
			    	    	strQueryCriteriaBuffer.append("idalarms_log");
			    	    	strQueryCriteriaBuffer.append(" like '");
			    	    }
			    	    else if (field.equals("alarmTime"))
			    	    {
			    	    	strQueryCriteriaBuffer.append("date_format(updated, \"%Y%m%d%H%i%s\")");
			    	    	strQueryCriteriaBuffer.append(" like '%");
			    	    	data = data.replaceAll("[:\\/-]", "");
							data = data.replaceAll("[.]", "");
							data = data.replaceAll("[ ]", "");
			    	    }
			    	    else if (field.equals("ledName"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" led_name ");
			    	    	strQueryCriteriaBuffer.append(" like '%");
			    	    }
			    	    else if (field.equals("ledStatus"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" led_status ");
			    	    	strQueryCriteriaBuffer.append(" like '%");
			    	    }
			    	    else if (field.equals("alarmDescription"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" description ");
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
			    		if (field.equals("alarmId")) {
			    			// Search Customers
			    			if (data != null && op != null) {
			    				if (op.equalsIgnoreCase("eq")) {
			    					logger.debug("search idalarms_log equals " + data);
			    					strQueryCriteriaBuffer.append(" idalarms_log = "+data);
			    				}  else if (op.equalsIgnoreCase("lt")) {
			    					logger.debug("search idalarms_log lesser then " + data);
			    					strQueryCriteriaBuffer.append(" idalarms_log < "+data);
			    				} else if (op.equalsIgnoreCase("gt")) {
			    					strQueryCriteriaBuffer.append(" idalarms_log > "+data);
			    					logger.debug("search idalarms_log greater then " + data);
			    				}
			    			} 
			    		}
			    		else if (field.equals("alarmTime")) {
			    			String strSearchDate = "date(updated)";
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
								      logger.debug("search id equals " + data);
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" like STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
							      }
							      else if (op.equalsIgnoreCase("ne")) {
								      logger.debug("search id equals " + data);
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" not like STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
							      }
							      else if (op.equalsIgnoreCase("gt")) {
								      logger.debug("search id equals " + data);
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" > STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
							      }
							      else if (op.equalsIgnoreCase("lt")) {
								      logger.debug("search id equals " + data);
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" < STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
							      }
						      }  
			    		}
			    		else if (field.equals("ledName"))
			    	    {
			    			 if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
							    	  strQueryCriteriaBuffer.append(" led_name = '"+data+"' ");
							      }
							      else  if (op.equalsIgnoreCase("ne")) {
							    	  strQueryCriteriaBuffer.append(" led_name != '"+data+"' ");
								  }
			    			 }
			    	    }
			    		else if (field.equals("ledStatus"))
			    	    {
			    			 if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
							    	  strQueryCriteriaBuffer.append(" led_status = '"+data+"' ");
							      }
							      else  if (op.equalsIgnoreCase("ne")) {
							    	  strQueryCriteriaBuffer.append(" led_status != '"+data+"' ");
								  }
			    			 }
			    	    }
			    		else if (field.equals("alarmDescription"))
			            {
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("cn")) {
							      strQueryCriteriaBuffer.append(" description like '%"+data+"%'");
							        }
							      else if (op.equalsIgnoreCase("nc")) {
								      strQueryCriteriaBuffer.append(" description not like '%"+data+"%'");
								  }			
							      else if (op.equalsIgnoreCase("bw")) {
								      strQueryCriteriaBuffer.append(" description like '"+data+"%'");
								  }
							      else if (op.equalsIgnoreCase("bn")) {
								      strQueryCriteriaBuffer.append(" description not like '"+data+"%'");
								  }
						      }
			            }
			    		else 
			    	    {
			    			 if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
							    	  strQueryCriteriaBuffer.append(" "+field+" = '"+data+"' ");
							      }
							      else  if (op.equalsIgnoreCase("ne")) {
							    	  strQueryCriteriaBuffer.append(" "+field+" != '"+data+"' ");
								  }
			    			 }
			    	    }
			    	}
		    	}
		    }

		    logger.debug("Alarms log query to be executed "+strQueryCriteriaBuffer.toString());
            setGridModel(mysqlAlarmsLogDAO.searchByCriteria(Integer.parseInt(stationId),strQueryCriteriaBuffer.toString(), strOrderBy, from, to));
            lstAlarmsLogDataDtos = getGridModel();
		    records = mysqlAlarmsLogDAO.countByCriteria(Integer.parseInt(stationId), strQueryCriteriaBuffer.toString());
		    session.put("lstAlarmsLogDataDtos", lstAlarmsLogDataDtos);
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
			logger.debug("Error occured while fetching Alarms data ",e);
			throw new M9000Exception(e);
		}
		return SUCCESS;
	}

	public String createAlarmsExportFile() throws M9000Exception {
		StringBuffer strFileContent = new StringBuffer();
		String fileToBeWritten = new SimpleDateFormat("'Alarms-exports-'MMddyyyy_HHmmss'.csv'").format(new Date());
		try
		{
			AlarmLogsDTO m9kAlarmsDto;
			lstAlarmsLogDataDtos = (List<AlarmLogsDTO>) session.get("lstAlarmsLogDataDtos");
			// Write Headers into files first
			strFileContent.append("Station Name, Date-Time, Name,  Relays,Description, Status");
			strFileContent.append(M9kConstants.NEWLINE);
			logger.debug("In createSERExportFile gridModel "+getGridModel()+" list of Alarms data "+getLstAlarmsLogDataDtos());
			for (Iterator<AlarmLogsDTO> iterator = getLstAlarmsLogDataDtos().iterator(); iterator.hasNext();) {
				m9kAlarmsDto =  iterator.next();
					strFileContent.append(M9kUtils.getStationDetails().getSystemStationName());
					strFileContent.append("," + m9kAlarmsDto.getAlarmTime());
					strFileContent.append("," + m9kAlarmsDto.getLedName());
					strFileContent.append("," + m9kAlarmsDto.getRelays());
					strFileContent.append(",\"" + m9kAlarmsDto.getAlarmDescription());
					strFileContent.append("\"," + m9kAlarmsDto.getLedStatus());
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

	public List<AlarmLogsDTO> getGridModel() {
		return gridModel;
	}

	public void setGridModel(List<AlarmLogsDTO> gridModel) {
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

	public List<AlarmLogsDTO> getLstAlarmsLogDataDtos() {
		return lstAlarmsLogDataDtos;
	}

	public void setLstAlarmsLogDataDtos(List<AlarmLogsDTO> lstAlarmsLogDataDtos) {
		this.lstAlarmsLogDataDtos = lstAlarmsLogDataDtos;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
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

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

}
