package com.usi.m9000.actions;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.LongTermDataDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.MessageProperties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class M9kDDRMeasurementsDataTableAction extends ActionSupport implements SessionAware,ServletRequestAware,ServletResponseAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LongTermDataDAO longTermDataDAO;
	private List<ComtradeDataDTO> lstComtradeDataDtos;
	private Map<String, Object> session;
	private String stationId;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kDDRMeasurementsDataTableAction.class);
	
	//JQGrid implementation
		private List<ComtradeDataDTO> gridModel;
	
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
	  private static final String ltType = M9kConstants.MEASUREMENTS;
	  private String filters;
	  
	public M9kDDRMeasurementsDataTableAction() {
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		longTermDataDAO = m9kDAOFactory.getLongTermDataDAO();
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

	public String getDDRData() throws M9000Exception
	{
		try {
			logger.debug("Entered getDDRDataDebug with stationId "+stationId+" lt type "+ltType);
			if (getStationId() == null)
			{
				stationId = (String) session.get("stationId");
			}
			logger.debug("Stilllllll DDR DATA : Page " + getPage() + " Rows " + getRows()
		    + " Sorting Order " + getSord() + " Index Row :" + getSidx());
		    logger.debug("Search :" + searchField + " " + searchOper + " "
		    + searchString);

//		    if (lstComtradeDataDtos == null) {
//		      logger.debug("Build new List");
//				lstComtradeDataDtos = comtradeDetailsDao.getLstOfComtradeDetails(Integer.parseInt(stationId));
//				setGridModel(lstComtradeDataDtos);
//		    }

		    logger.debug("sort selected? "+sord+" column to be sorted "+getSidx());
//		    ComtradeDataDTO comtradeDataDTO;
//		    StringBuffer faultIdsStr = new StringBuffer();
//		    for (Iterator<ComtradeDataDTO> iterator = lstComtradeDataDtos.iterator(); iterator.hasNext();) {
//				comtradeDataDTO = (ComtradeDataDTO) iterator.next();
//				faultIdsStr.append(comtradeDataDTO.getFaultId()+"_"+comtradeDataDTO.getDisplayTime()+"_"+comtradeDataDTO.getLength()+",");
//			}
//		    logger.debug("Before sorting: "+faultIdsStr);
//		    String strOrderBy = "";
//		    if (getSidx() != null && !getSidx().equals(""))
//			    {
//	
//			    if (sord != null && sord.equalsIgnoreCase("asc")) {
//			    	strOrderBy=" order by "+getSidx()+" asc";
//			      Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {
//	
//						@Override
//						public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
//							if (getSidx().equalsIgnoreCase("faultId"))
//							{
//								int lhs = o1.getFaultId(); 
//								int rhs = o2.getFaultId();
//								return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
//							}else if (getSidx().equalsIgnoreCase("displayTime"))
//							{
//								String lhs = o1.getDisplayTime(); 
//								String rhs = o2.getDisplayTime();
//								return (lhs.compareTo(rhs));
//							}
//							else if (getSidx().equalsIgnoreCase("length"))
//							{
//								double lhs = o1.getLength(); 
//								double rhs = o2.getLength();
//								return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
//							}
//							else  // Sort fault logic
//							{
//								String lhs = ""+o1.isFaultLogic(); 
//								String rhs = ""+o2.isFaultLogic();
//								return (lhs.compareTo(rhs));
//							}
//						}
//					});
//			    }
//			    else 
//			    {
//			    	strOrderBy=" order by "+getSidx()+" desc";
//			    }
//			    	if (sord != null && sord.equalsIgnoreCase("desc")) {
//				      Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {
//		
//							@Override
//							public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
//								if (getSidx().equalsIgnoreCase("faultId"))
//								{
//									int lhs = o1.getFaultId(); 
//									int rhs = o2.getFaultId();
//									return ((lhs > rhs)? -1 : (lhs == rhs)?0:1);
//								}else if (getSidx().equalsIgnoreCase("displayTime"))
//								{
//									String lhs = o1.getDisplayTime(); 
//									String rhs = o2.getDisplayTime();
//									return (rhs.compareTo(lhs));
//								}
//								else if (getSidx().equalsIgnoreCase("length"))
//								{
//									double lhs = o1.getLength(); 
//									double rhs = o2.getLength();
//									return ((lhs > rhs)? -1 : (lhs == rhs)?0:1);
//								}
//								else  // Sort fault logic
//								{
//									String lhs = ""+o1.isFaultLogic(); 
//									String rhs = ""+o2.isFaultLogic();
//									return (rhs.compareTo(lhs));
//								}
//							}
//						});
//		//		      Collections.reverse(lstComtradeDataDtos);
//				    }
//				    faultIdsStr = new StringBuffer();
//				    for (Iterator<ComtradeDataDTO> iterator = lstComtradeDataDtos.iterator(); iterator.hasNext();) {
//						comtradeDataDTO = (ComtradeDataDTO) iterator.next();
//						faultIdsStr.append(comtradeDataDTO.getFaultId()+"_"+comtradeDataDTO.getDisplayTime()+"_"+comtradeDataDTO.getLength()+",");
//					}
//				    logger.debug("After sorting: "+faultIdsStr);
//				    setGridModel(lstComtradeDataDtos);  
//			    }
		    
//		    // Count all record (select count(*) from your_custumers)
//		    records = longTermDataDAO.getTotalDDRCount(Integer.parseInt(stationId),ltType);
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
			    	if (getSidx().equalsIgnoreCase("faultId"))
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
			    	if (getSidx().equalsIgnoreCase("faultId"))
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
		    if (getFilters() != null  && !getFilters().isEmpty())
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
			    	    if (field.equals("faultId"))
			    	    {
			    	    	strQueryCriteriaBuffer.append("id");
			    	    	strQueryCriteriaBuffer.append(" like '");
			    	    }
			    	    else if (field.equals("displayTime"))
			    	    {
			    	    	strQueryCriteriaBuffer.append("date_format(from_unixtime(tsTrigger div 1000000),\"%m%d%Y%H%i%s\")");
			    	    	strQueryCriteriaBuffer.append(" like '%");
			    	    	data = data.replaceAll("[:\\/-]", "");
							data = data.replaceAll("[.]", "");
							data = data.replaceAll("[ ]", "");
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
			            if (field.equals("faultId")) {
					      if (data != null && op != null) {
					      if (op.equalsIgnoreCase("eq")) {
					      strQueryCriteriaBuffer.append(" id = "+data);
					        }  else if (op.equalsIgnoreCase("lt")) {
					          strQueryCriteriaBuffer.append(" id < "+data);
					        } else if (op.equalsIgnoreCase("gt")) {
					        	strQueryCriteriaBuffer.append(" id > "+data);
					        }
					      } 
			            }
			            else if (field.equals("displayTime")) {
			            	String strSearchDate = "date(from_unixtime(tsTrigger div 1000000))";
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" like STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
							      }
							      else if (op.equalsIgnoreCase("ne")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" not like STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
							      }
							      else if (op.equalsIgnoreCase("gt")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" > STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
							      }
							      else if (op.equalsIgnoreCase("lt")) {
								      strQueryCriteriaBuffer.append(" "+strSearchDate+" < STR_TO_DATE('"+data+"',\"%m/%d/%Y\")");
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
							      else if (op.equalsIgnoreCase("eq")) {
								      strQueryCriteriaBuffer.append(" "+field+" = '"+data+"'");
								  }
							      else if (op.equalsIgnoreCase("gt")) {
								      strQueryCriteriaBuffer.append(" "+field+" > '"+data+"'");
								  }
							      else if (op.equalsIgnoreCase("lt")) {
								      strQueryCriteriaBuffer.append(" "+field+" < '"+data+"'");
								  }
						      }
			            }
			    	}
		    	}
		    }
            setGridModel(longTermDataDAO.searchByCriteria(Integer.parseInt(stationId),ltType, strQueryCriteriaBuffer.toString(), strOrderBy, from, to));
            lstComtradeDataDtos = getGridModel();
		    records = longTermDataDAO.countByCriteria(Integer.parseInt(stationId), ltType, strQueryCriteriaBuffer.toString());
		    
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
			logger.debug("Error occured while fetching DDR Analog data ",e);
			throw new M9000Exception(e);
		}
		return SUCCESS;
	}

	public String editDdrMeasurementsData()
	{
		if (oper.equalsIgnoreCase("del")) {
			deleteDdrMeasurements(id);
        }
		return SUCCESS;
	}
	
	private int deleteDdrMeasurements(String csvIdsString) {

    	int numberOfLtrsDeleted = 0;
//		StringBuffer strbufFaultsId = null;
		String csvFaultFileNames = null;
		String deleteActionResult;
		boolean booStatus = true;
		try {
			logger.debug("DDR Measurement ids "+csvIdsString);
			try
			{
				if (getStationId() == null)
				{
					stationId = (String) session.get("stationId");
				}
				csvFaultFileNames = getLtrFileNames(csvIdsString);
				logger.debug("Fault File Names "+csvFaultFileNames);
				// Delete from the remote web master
				if (M9kUtils.isRemote())
				{
					String strRemoteMasterDelFaultsStatus = M9kUtils.deleteFilesFromRemoteMaster(csvFaultFileNames);
					if (strRemoteMasterDelFaultsStatus == null || strRemoteMasterDelFaultsStatus.toUpperCase().startsWith("ERROR"))
					{
						booStatus = false;
						numberOfLtrsDeleted = -1;
					}
				}
				// Delete from the station master
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("MESSAGE_TYPE", "DELETE_FAULTS_FILES");
				messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
				messageProperties.addProperty("DATA_TYPE", M9kConstants.LTR_MEASUREMENT_TYPE);
				
				deleteActionResult = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationId(), csvFaultFileNames.toString(), messageProperties);
				
				if (deleteActionResult == null || deleteActionResult.toUpperCase().startsWith("ERROR"))
				{
					booStatus = false;
					numberOfLtrsDeleted = -1;
				}

				if (booStatus)
				{
					// Delete from the database
					numberOfLtrsDeleted = longTermDataDAO.deleteLTRRecords(Integer.parseInt(getStationId()), csvIdsString.toString() );
				}
			}
			catch(Exception e1)
			{
				booStatus = false;
				logger.error("Error occured while deleting faults "+e1.getMessage());
				numberOfLtrsDeleted = -1;
				e1.printStackTrace();
			}

			
		} catch (Exception e1) {
			e1.printStackTrace();
			numberOfLtrsDeleted = -1;

		}
		finally
		{
			
		}

		return numberOfLtrsDeleted;
		}

    /**
     * Gets the corresponding file names of the csvFaultIds in ;; separated format string
     * @param csvFaultsString
     * @return
     */
	private String getLtrFileNames(String csvFaultsString) {
		String ltrFileNames = null;
		try {
			ltrFileNames = longTermDataDAO.getLongTermDataFileNames(Integer.parseInt(getStationId()), csvFaultsString.toString(), M9kConstants.MEASUREMENTS);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Unable to get the file names of the faults from the database. Physical faults are not deleted for the faults "+csvFaultsString, e);
		}
		catch (M9000Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Unable to get the file names of the faults from the database. Physical faults are not deleted for the faults "+csvFaultsString, e);
		}
		return ltrFileNames;
	}

	
	public List<ComtradeDataDTO> getLstComtradeDataDtos() {
		return lstComtradeDataDtos;
	}

	public void setLstComtradeDataDtos(List<ComtradeDataDTO> lstComtradeDataDtos) {
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

	public List<ComtradeDataDTO> getGridModel() {
		return gridModel;
	}

	public void setGridModel(List<ComtradeDataDTO> gridModel) {
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

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

}
