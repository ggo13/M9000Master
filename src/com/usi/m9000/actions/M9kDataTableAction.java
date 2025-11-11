package com.usi.m9000.actions;

import java.io.File;
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
import com.usi.m9000.config.M9kComtradeParser;
import com.usi.m9000.dao.ComtradeDataDAO;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.ContinuousComtradeDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.faultLocation.M9kFaultLocation;
import com.usi.m9000.station.faultLocation.M9kFaultReport;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.MessageProperties;
import com.usi.m9000.xml.util.M9kXMLUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class M9kDataTableAction extends ActionSupport implements SessionAware,ServletRequestAware,ServletResponseAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ComtradeDataDAO comtradeDetailsDao ;
	private StationDAO mysqlStationDao;
	private List<ComtradeDataDTO> lstComtradeDataDtos;
	private Map<String, Object> session;
	private String stationId;
	private int contAnalogTimeLimit = 120; // In seconds. Defaults to 120 seconds
	private int contDataTimeLimit = 600; // In seconds. Defaults to 600 seconds
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kDataTableAction.class);
	
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
	  private List<ContinuousComtradeDTO> lstContinuousAnalogComtradeDto;
	  private List<ContinuousComtradeDTO> lstContinuousMeasurementsComtradeDto;
	  private String filters;
	  private String faultLocationDetails;
	  private String faultLocationStatus; 
	  private String saveFaultLocationStatus;
	  private Map<String,String> mapAllEvents = null;
	  private Map<String,String> mapLineGroups = null;
	  
	  public M9kDataTableAction() {
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		comtradeDetailsDao = m9kDAOFactory.getComtradeDataDAO(); 
		mysqlStationDao = m9kDAOFactory.getStationDAO();
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

	public String getFaults() throws M9000Exception
	{
		String result = SUCCESS;
		try {
			logger.debug("Entered getFaults with stationId "+stationId);
			if (getStationId() == null)
			{
				stationId = (String) session.get("stationId");
			}
			logger.debug("Page " + getPage() + " Rows " + getRows()
		    + " Sorting Order " + getSord() + " Index Row :" + getSidx());
		    logger.debug("Search :" + searchField + " " + searchOper + " "
		    + searchString);
//		    String paramValue = ServletActionContext.getRequest().getParameter("searchField");
//		    logger.debug("Search Field in request: "+paramValue);
		    logger.debug("Filter applied "+getFilters());
		    logger.debug("sort selected? "+sord+" column to be sorted "+getSidx());
		    
		    // Count all record (select count(*) from your_custumers)
//		    records = comtradeDetailsDao.getTotalFaultsCount(Integer.parseInt(stationId));
//		    logger.info("Getting total faults from station "+stationId+" Total: "+records);

		    // Calculate until rows ware selected
		    int to = (rows * page);

		    // Calculate the first row to read
		    int from = to - rows;
		    logger.debug("From "+from +" to "+to);
		    String strOrderBy = null;
		    if (getSidx() != null && !getSidx().equals(""))
		    {
		    	logger.debug("sidx "+getSidx()+(" is it displayTime?"+ (getSidx().equalsIgnoreCase("displayTime"))));
			    
			    if (sord != null && sord.equalsIgnoreCase("asc")) {
			    	if (getSidx().equalsIgnoreCase("faultId"))
			    	{
			    		strOrderBy=" order by fault_id asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("displayTime"))
			    	{
			    		strOrderBy=" order by time_stamp asc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("faultLogic"))
			    	{
			    		strOrderBy=" order by fault_logic asc";
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
			    		strOrderBy=" order by fault_id desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("displayTime"))
			    	{
			    		strOrderBy=" order by time_stamp desc";
			    	}
			    	else if (getSidx().equalsIgnoreCase("faultLogic"))
			    	{
			    		strOrderBy=" order by fault_logic desc";
			    	}
			    	else
			    	{
			    		strOrderBy=" order by "+getSidx()+" desc";
			    	}
			    }
		    }

		    StringBuffer strQueryCriteriaBuffer = new StringBuffer();;
		    if (searchField != null && !searchField.isEmpty()) {
		    	logger.debug("Search field entered..."+searchField);
	            if (searchField.equals("faultId")) {
			      if (searchString != null && searchOper != null) {
			      int id = Integer.parseInt(searchString);
			      if (searchOper.equalsIgnoreCase("eq")) {
			      logger.debug("search id equals " + id);
			      strQueryCriteriaBuffer.append(" fault_id = "+id);
			        }  else if (searchOper.equalsIgnoreCase("lt")) {
			          logger.debug("search id lesser then " + id);
			          strQueryCriteriaBuffer.append(" fault_id < "+id);
			        } else if (searchOper.equalsIgnoreCase("gt")) {
			        	strQueryCriteriaBuffer.append(" fault_id > "+id);
			          logger.debug("search id greater then " + id);
			        }
			      } 
	            }
	            else if (searchField.equals("activeEvents"))
	            {
				      if (searchString != null && searchOper != null) {
					      if (searchOper.equalsIgnoreCase("cn")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" events like '%"+searchString+"%'");
					        }
					      }
				      else if (searchOper.equalsIgnoreCase("nc")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" events not like '%"+searchString+"%'");
					  }				    	  
	            }
	            else if (searchField.equals("userComments"))
	            {
				      if (searchString != null && searchOper != null) {
					      if (searchOper.equalsIgnoreCase("cn")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" comments like '%"+searchString+"%'");
					        }
					      }
				      else if (searchOper.equalsIgnoreCase("nc")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" comments not like '%"+searchString+"%'");
					  }			
				      else if (searchOper.equalsIgnoreCase("bw")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" comments not like '"+searchString+"%'");
					  }	
	            }
	            else if (searchField.equals("faultLogic")) {
				      if (searchString != null && searchOper != null) {
					      if (searchOper.equalsIgnoreCase("eq")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" fault_logic like '%"+searchString+"%'");
					        } 
				      } 
			    }
	            else if (searchField.equals("displayTime")) {
	            	String strSearchDate = "date(from_unixtime(time_stamp div 1000000))";
				      if (searchString != null && searchOper != null) {
					      if (searchOper.equalsIgnoreCase("eq")) {
						      logger.debug("search id equals " + searchString);
						      strQueryCriteriaBuffer.append(" "+strSearchDate+" like STR_TO_DATE('"+searchString+"',\"%m/%d/%Y\")");
					      }
					      else if (searchOper.equalsIgnoreCase("ne")) {
						      logger.debug("search id equals " + searchString);
						      strQueryCriteriaBuffer.append(" "+strSearchDate+" not like STR_TO_DATE('"+searchString+"',\"%m/%d/%Y\")");
					      }
					      else if (searchOper.equalsIgnoreCase("gt")) {
						      logger.debug("search id equals " + searchString);
						      strQueryCriteriaBuffer.append(" "+strSearchDate+" > STR_TO_DATE('"+searchString+"',\"%m/%d/%Y\")");
					      }
					      else if (searchOper.equalsIgnoreCase("lt")) {
						      logger.debug("search id equals " + searchString);
						      strQueryCriteriaBuffer.append(" "+strSearchDate+" < STR_TO_DATE('"+searchString+"',\"%m/%d/%Y\")");
					      }
				      } 
			    }
	            else if (searchField.equals("faultLocationDetails")) {
				      if (searchString != null && searchOper != null) {
					      if (searchOper.equalsIgnoreCase("eq")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" fault_location like '"+searchString+"'");
					        } 
					      else if (searchOper.equalsIgnoreCase("ne")) {
					    	  strQueryCriteriaBuffer.append(" fault_location not like '"+searchString+"'");
					      }
					    	  
				      } 
			    }
	            else 
	            {
				      if (searchString != null && searchOper != null) {
					      if (searchOper.equalsIgnoreCase("cn")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" "+searchField+" like '%"+searchString+"%'");
					        }
					      }
				      else if (searchOper.equalsIgnoreCase("nc")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" "+searchField+" not like '%"+searchString+"%'");
					  }
				      else if (searchOper.equalsIgnoreCase("eq")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" "+searchField+" = '"+searchString+"'");
					  }
				      else if (searchOper.equalsIgnoreCase("gt")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" "+searchField+" > '"+searchString+"'");
					  }
				      else if (searchOper.equalsIgnoreCase("lt")) {
					      logger.debug("search id equals " + searchString);
					      strQueryCriteriaBuffer.append(" "+searchField+" < '"+searchString+"'");
					  }
	            }
		    }
		    else if (getFilters() != null && !getFilters().isEmpty())
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
			    	    	strQueryCriteriaBuffer.append(" "+groupOp +" ");
			    	    }
			    	    if (field.equals("faultId"))
			    	    {
			    	    	strQueryCriteriaBuffer.append("fault_id");
			    	    	strQueryCriteriaBuffer.append(" like '");
			    	    }
			    	    else if (field.equals("displayTime"))
			    	    {
			    	    	strQueryCriteriaBuffer.append("date_format(from_unixtime(time_stamp div 1000000),\"%m%d%Y%H%i%s\")");
			    	    	strQueryCriteriaBuffer.append(" like '%");
			    	    	data = data.replaceAll("[:\\/-]", "");
							data = data.replaceAll("[.]", "");
							data = data.replaceAll("[ ]", "");
			    	    }
			    	    else if (field.equals("faultLogic"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" fault_logic");
			    	    	strQueryCriteriaBuffer.append(" like '");
			    	    }
			    	    else if (field.equals("userComments"))
			    	    {
			    	    	strQueryCriteriaBuffer.append(" comments ");
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
		    	else // Multi Search enabled
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
					      int id = Integer.parseInt(data);
					      if (op.equalsIgnoreCase("eq")) {
					      strQueryCriteriaBuffer.append(" fault_id = "+id);
					        }  else if (op.equalsIgnoreCase("lt")) {
					          strQueryCriteriaBuffer.append(" fault_id < "+id);
					        } else if (op.equalsIgnoreCase("gt")) {
					        	strQueryCriteriaBuffer.append(" fault_id > "+id);
					        }
					      } 
			            }
			            else if (field.equals("activeEvents"))
			            {
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("cn")) {
							      strQueryCriteriaBuffer.append(" events like '%"+data+"%'");
							        }
							      else if (op.equalsIgnoreCase("nc")) {
								      strQueryCriteriaBuffer.append(" events not like '%"+data+"%'");
								  }
						      }
			            }
			            else if (field.equals("userComments"))
			            {
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("cn")) {
							      strQueryCriteriaBuffer.append(" comments like '%"+data+"%'");
							        }
							      else if (op.equalsIgnoreCase("nc")) {
								      strQueryCriteriaBuffer.append(" comments not like '%"+data+"%'");
								  }			
							      else if (op.equalsIgnoreCase("bw")) {
								      strQueryCriteriaBuffer.append(" comments like '"+data+"%'");
								  }
							      else if (op.equalsIgnoreCase("bn")) {
								      strQueryCriteriaBuffer.append(" comments not like '"+data+"%'");
								  }
						      }
			            }
			            else if (field.equals("faultLogic")) {
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
							      strQueryCriteriaBuffer.append(" fault_logic like '%"+data+"%'");
							        } 
						      } 
					    }
			            else if (field.equals("displayTime")) {
			            	String strSearchDate = "date(from_unixtime(time_stamp div 1000000))";
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
			            else if (field.equals("faultLocationDetails")) {
						      if (data != null && op != null) {
							      if (op.equalsIgnoreCase("eq")) {
							      strQueryCriteriaBuffer.append(" fault_location like '"+data+"'");
							        } 
							      else if (op.equalsIgnoreCase("ne")) {
							    	  strQueryCriteriaBuffer.append(" fault_location not like '"+data+"'");
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
		    logger.debug("FAULTS: About to set Grid Model "+strQueryCriteriaBuffer+" order by "+strOrderBy);
            setGridModel(comtradeDetailsDao.searchByCriteria(Integer.parseInt(stationId), strQueryCriteriaBuffer.toString(), strOrderBy, from, to));
            logger.debug("FAULTS: After setting Grid Model ");
            lstComtradeDataDtos = getGridModel();
            logger.debug("FAULTS: About to count by criteria");
		    records = comtradeDetailsDao.countByCriteria(Integer.parseInt(stationId), strQueryCriteriaBuffer.toString());
		    logger.debug("FAULTS: After count by criteria");
		    // Set to = max rows
		    if (to > records)
		    {
		    	to = records;
		    }

//		   // Set to = max rows
//		        if (to > records) to = records;

		        // Calculate total Pages
		        total = (int) Math.ceil((double) records / (double) rows);
		        logger.debug("FAULTS: Total at the end of function "+total);

//		      total = (int) Math.ceil((double) records / (double) rows);
		}  catch (Exception e) {
			result = ERROR;
			logger.error("Error Occurred. Unable to fetch faults.",e);
			addActionError("Error Occurred. Unable to fetch faults. ");
			throw new M9000Exception(e);
		}
		logger.debug("FAULTS: Returning from getFaults() method");
		return result;
	}

	public String editFaults()
	{
		if (oper.equalsIgnoreCase("del")) {
			deleteFaults(id);
//            StringTokenizer ids = new StringTokenizer(faultIds, ",");
//            while (ids.hasMoreTokens()) {
//                int removeId = Integer.parseInt(ids.nextToken());
//                logger.debug("Delete customer with id: "+removeId);
//                customerDao.delete(removeId);
//            }
        }
		return SUCCESS;
	}
	


    private Integer deleteFaults(String csvFaultIdsString)
	{
    	int numberOfFaultsDeleted = 0;
//		StringBuffer strbufFaultsId = null;
		String csvFaultFileNames = null;
		String deleteActionResult;
		boolean booStatus = true;
		try {
			logger.debug("Fault ids "+csvFaultIdsString);
			try
			{
				if (getStationId() == null)
				{
					stationId = (String) session.get("stationId");
				}
				csvFaultFileNames = getFaultFileNames(csvFaultIdsString);
				logger.debug("Fault File Names "+csvFaultFileNames);
				// Delete from the remote web master
				if (M9kUtils.isRemote())
				{
					String strRemoteMasterDelFaultsStatus = M9kUtils.deleteFilesFromRemoteMaster(csvFaultFileNames);
					if (strRemoteMasterDelFaultsStatus == null || strRemoteMasterDelFaultsStatus.toUpperCase().startsWith("ERROR"))
					{
						booStatus = false;
						numberOfFaultsDeleted = -1;
					}
				}
				// Delete from the station master
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("MESSAGE_TYPE", "DELETE_FAULTS_FILES");
				messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
				messageProperties.addProperty("DATA_TYPE", M9kConstants.FAULT_TYPE);
				
				deleteActionResult = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationId(), csvFaultFileNames.toString(), messageProperties);
				
				if (deleteActionResult == null || deleteActionResult.toUpperCase().startsWith("ERROR"))
				{
					booStatus = false;
					numberOfFaultsDeleted = -1;
				}

				if (booStatus)
				{
					// Delete from the database
					numberOfFaultsDeleted = comtradeDetailsDao.deleteFaults(Integer.parseInt(getStationId()), csvFaultIdsString.toString() );
				}
			}
			catch(Exception e1)
			{
				booStatus = false;
				logger.error("Error occured while deleting faults "+e1.getMessage());
				numberOfFaultsDeleted = -1;
				e1.printStackTrace();
			}

			
//			String fileName =  mySqlLongTermDataDAO.getLongTermDataFileName(Integer.parseInt(getStationId()), tsTrigger, ltType);
//			System.out.println("LTR File name recieved "+fileName);
//			if (fileName == null || fileName.isEmpty())
//			{
//				fileName = M9kUtils.getLTRFileName(Integer.parseInt(getStationId()), faultId, ltType);
//			}
//			if (fileName == null || fileName.isEmpty() || fileName.equalsIgnoreCase(M9kConstants.NO_DATA))
//			{
//				JOptionPane.showMessageDialog(tblFaults,
//						"Long term data still not available. Please try again after some time.",
//					    "Error",
//					    JOptionPane.ERROR_MESSAGE);
//			}
//			try {
//				saveLtrFiles(fileName);
//			} catch (M9000Exception e) {
//				System.out.println("Error in saving LTR file"+e);
//				JOptionPane.showMessageDialog(tblFaults,
//						"Unable to save files. Please try again."+e.getMessage(),
//					    "Error",
//					    JOptionPane.ERROR_MESSAGE);
//			}
//			((M9kContinuousAnalogDataPanel)sourceComponent).showComtradeViewer(comtradeData.getConfInputString(), comtradeData.getDatInputString());
		} catch (Exception e1) {
			e1.printStackTrace();
			numberOfFaultsDeleted = -1;

		}
		finally
		{
			
		}

		return numberOfFaultsDeleted;
	}


    /**
     * Gets the corresponding file names of the csvFaultIds in ;; separated format string
     * @param csvFaultsString
     * @return
     */
	private String getFaultFileNames(String csvFaultsString) {
		StringBuffer strbuffFaultNames = null;
		ComtradeDataDTO comtradeDataDTO;
		try {
			List<ComtradeDataDTO> lstComtradeDetails = comtradeDetailsDao.getFaultFileNames(Integer.parseInt(getStationId()), csvFaultsString.toString() );
			for (Iterator<ComtradeDataDTO> iterator = lstComtradeDetails.iterator(); iterator.hasNext();) {
				 comtradeDataDTO = iterator.next();
				if (strbuffFaultNames == null)
				{
					strbuffFaultNames = new StringBuffer(comtradeDataDTO.getFileName());
				}
				else
				{
					strbuffFaultNames.append(";;");
					strbuffFaultNames.append(comtradeDataDTO.getFileName());
				}
			}
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
		return strbuffFaultNames.toString();
	}

	public String calculateFaultLocation() 
	{
		try
		{
			if (getFaultIds() != null && !getFaultIds().isEmpty())
			{
				StringBuffer strFaultLocationDetails=null;
				String dataDir = M9kUtils.getDataDir();
				String fileName = getFaultFileNames(getFaultIds());
				logger.info("Fault file name received "+fileName+" dataDir "+dataDir);
				StationDTO stationDetails = (StationDTO) session.get("stationDetails");
				M9kStationXMLUtil.initXml(stationDetails.getConfigXml());
				M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File(dataDir+fileName+M9kConstants.DAT_FILE_EXTN));
				M9kFaultLocation m9kFaultLocation = new M9kFaultLocation(m9kComtradeParser.getProcessData());
				Map<LineGroupsAlgorithm, M9kFaultReport> mapFaultReport = m9kFaultLocation.findFaultLocation(true);
				M9kFaultReport faultReport;
				for (LineGroupsAlgorithm lineGroup : mapFaultReport.keySet()) {
					logger.debug("Line group Name "+lineGroup.getLineGroupName());
					faultReport = mapFaultReport.get(lineGroup);
					if (faultReport != null)
					{
						if (strFaultLocationDetails == null)
						{
							strFaultLocationDetails = new StringBuffer();
						}
						strFaultLocationDetails.append("Line Name: "+lineGroup.getLineGroupName()+M9kConstants.NEWLINE);
						strFaultLocationDetails.append(faultReport.getFaultReportDetails()+M9kConstants.NEWLINE);
						logger.debug("Details "+faultReport.getFaultReportDetails());
					}
				}
//				System.out.println("Fault Details "+mapFaultReport);

				if (strFaultLocationDetails != null)
				{
					if (!strFaultLocationDetails.toString().toUpperCase().startsWith("ERROR"))
					{
						setFaultLocationStatus("Fault Location Report"+M9kConstants.NEWLINE+M9kConstants.NEWLINE+strFaultLocationDetails);
					}
					else
					{
						setFaultLocationStatus("Error Fault Location Report"+M9kConstants.NEWLINE+M9kConstants.NEWLINE+strFaultLocationDetails);					
					}
				}
				else
				{
					setFaultLocationStatus("Unable to calculate fault location");
				}
			}
			else
			{
				setFaultLocationStatus("Unable to calculate fault location");
			}
		}catch (Exception e) {
			logger.error("Error in calculating fault location.",e);
		}
		return SUCCESS;
		
	}
	public String calculateFaultLocationOld()
	{
		try
		{
			logger.debug("Entered calculateFaultLocation with faultId " +getFaultIds());
			if (getFaultIds() != null && !getFaultIds().isEmpty())
			{
				String strFaultLocationDetails = null;
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("MESSAGE_TYPE", "FAULT_LOC");
				messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
				String fileName = getFaultFileNames(getFaultIds());
				strFaultLocationDetails = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationId(), fileName, messageProperties);
				System.out.println("Fault location details "+strFaultLocationDetails);
				if (strFaultLocationDetails != null)
				{
					if (!strFaultLocationDetails.toUpperCase().startsWith("ERROR"))
					{
						setFaultLocationStatus("Fault Location Report"+M9kConstants.NEWLINE+M9kConstants.NEWLINE+strFaultLocationDetails);
					}
					else
					{
						setFaultLocationStatus("Error Fault Location Report"+M9kConstants.NEWLINE+M9kConstants.NEWLINE+strFaultLocationDetails);					
					}
				}
			}
			else
			{
				setFaultLocationStatus("No Fault Id");
			}

	} catch (Exception e) {
		logger.error("Error in calculating fault location.",e);
		}
		return SUCCESS;
	}
	
	public String saveNewFaultLocationDetails() throws M9000Exception
	{
		try
		{
			logger.debug("Entered saveNewFaultLocationDetails  with faultId " +getFaultIds()+" station iD "+getStationId()+" faultLocation Details "+getFaultLocationDetails());
			setSaveFaultLocationStatus("Successfully updated the new fault location details");
			ComtradeDataDTO comtradeDetails = comtradeDetailsDao.findById(Integer.parseInt(getStationId()), Integer.parseInt(getFaultIds()));
			comtradeDetails.setFaultLocationDetails(faultLocationDetails);
			mysqlStationDao.updateFaultLocationDetails(Integer.parseInt(stationId), Integer.parseInt(getFaultIds()), faultLocationDetails);
		}
		catch(Exception e)
		{
			logger.error("Error in updating the new faul location report ",e);
			setSaveFaultLocationStatus("Error in updating the new faul location report "+e);
			throw new M9000Exception(e);
		}
		
		return SUCCESS;
	}

	public String fetchLinegroups() 
	{
		try
		{
			if (mapLineGroups == null)
			{
				LineGroupsAlgorithm lineGroupsAlgorithm;
				mapLineGroups = new LinkedHashMap<String, String>();
				List<LineGroupsAlgorithm> linegroupsLst = M9kXMLUtils.getLstLineGroups(M9kUtils.getStationDetails().getConfigXml());
				logger.debug("LinegroupsList "+linegroupsLst);
				for (Iterator<LineGroupsAlgorithm> iterator = linegroupsLst.iterator(); iterator.hasNext();) {
					lineGroupsAlgorithm = iterator.next();
					mapLineGroups.put(lineGroupsAlgorithm.getName(),lineGroupsAlgorithm.getDisplayName());
				}
			}
		}
		catch (Exception e) {
			logger.error("Unable to fetch list of line groups ",e);
			mapLineGroups = new LinkedHashMap<String, String>();
		}
		logger.debug("Returing fetchLinegroups with map "+mapLineGroups);
		return SUCCESS;
	}
	public String fetchEventsList() 
	{
		try
		{
			
			if (mapAllEvents == null)
			{
				mapAllEvents = new LinkedHashMap<String, String>();
				List<String> eventsLst = comtradeDetailsDao.getDistinctActiveEvents(M9kUtils.getStationDetails().getSystemStationId());
				logger.debug("eventsLst "+eventsLst);
				String event,key;
				int index = -1;
				for (Iterator<String> iterator = eventsLst.iterator(); iterator.hasNext();) {
					event = iterator.next();
					index = event.indexOf("-");
					key = event.substring(0, index-1).trim();
					 mapAllEvents.put(key, event);
				}
			}
		}
		catch (Exception e) {
			logger.error("Unable to fetch list of events ",e);
			mapAllEvents = new LinkedHashMap<String, String>();
		}
		logger.debug("Returing fetchEventsList with map "+mapAllEvents);
		return SUCCESS;
	}
		

	public String displayContAnalogData() throws M9000Exception{
//		lstContinuousAnalogComtradeDto = mysqlContinuousComtradeDAO.getListOfContinuousComtradeData(stationId, "ANALOG");
		contAnalogTimeLimit = M9kUtils.getContAnalogTimeLimit();
		return SUCCESS;

	}
	public String displayContMeasurementsData() throws M9000Exception{
//		lstContinuousMeasurementsComtradeDto = mysqlContinuousComtradeDAO.getListOfContinuousComtradeData(stationId, "MEASUREMENTS");
		contDataTimeLimit = M9kUtils.getContDataTimeLimit();
		return SUCCESS;

	}
	public String displayFaultsData()
	{
		logger.debug("Inside display DFR Data "+getStationId());
		
		return SUCCESS;
	}
	public String displayDdrAnalogData()
	{
		logger.debug("Inside display DDR Analog Data Long term type ");
		
		return SUCCESS;
	}
	
	public String displayDdrMeasurementsData()
	{
		logger.debug("Inside display DDR Measurements Data Long term type ");
		
		return SUCCESS;
	}
	public String displayAlarmsData()
	{
		logger.debug("Inside display Alarms Data type ");
		
		return SUCCESS;
	}

	public String displayHealthMonitor()
	{
		logger.debug("Inside display Health Monitor ");
		
		return SUCCESS;
	}

		public String displayDnp3Config()
	{
		logger.debug("Inside display DNP3 Config ");
		
		return SUCCESS;
	}

	public String displaySerData() throws M9000Exception
	{
		logger.debug("Inside display SER Data type ");
		
		return SUCCESS;
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

	public List<ContinuousComtradeDTO> getLstContinuousAnalogComtradeDto() {
		return lstContinuousAnalogComtradeDto;
	}

	public void setLstContinuousAnalogComtradeDto(List<ContinuousComtradeDTO> lstContinuousAnalogComtradeDto) {
		this.lstContinuousAnalogComtradeDto = lstContinuousAnalogComtradeDto;
	}

	public List<ContinuousComtradeDTO> getLstContinuousMeasurementsComtradeDto() {
		return lstContinuousMeasurementsComtradeDto;
	}

	public void setLstContinuousMeasurementsComtradeDto(List<ContinuousComtradeDTO> lstContinuousMeasurementsComtradeDto) {
		this.lstContinuousMeasurementsComtradeDto = lstContinuousMeasurementsComtradeDto;
	}

	public int getContAnalogTimeLimit() {
		return contAnalogTimeLimit;
	}

	public void setContAnalogTimeLimit(int contAnalogTimeLimit) {
		this.contAnalogTimeLimit = contAnalogTimeLimit;
	}

	public int getContDataTimeLimit() {
		return contDataTimeLimit;
	}

	public void setContDataTimeLimit(int contDataTimeLimit) {
		this.contDataTimeLimit = contDataTimeLimit;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public String getFaultLocationStatus() {
		return faultLocationStatus;
	}

	public void setFaultLocationStatus(String faultLocationStatus) {
		this.faultLocationStatus = faultLocationStatus;
	}

	public String getSaveFaultLocationStatus() {
		return saveFaultLocationStatus;
	}

	public void setSaveFaultLocationStatus(String saveFaultLocationStatus) {
		this.saveFaultLocationStatus = saveFaultLocationStatus;
	}

	public String getFaultLocationDetails() {
		return faultLocationDetails;
	}

	public void setFaultLocationDetails(String faultLocationDetails) {
		this.faultLocationDetails = faultLocationDetails;
	}

	public Map<String, String> getMapAllEvents() {
		return mapAllEvents;
	}

	public void setMapAllEvents(Map<String, String> mapAllEvents) {
		this.mapAllEvents = mapAllEvents;
	}

	public Map<String, String> getMapLineGroups() {
		return mapLineGroups;
	}

	public void setMapLineGroups(Map<String, String> mapLineGroups) {
		this.mapLineGroups = mapLineGroups;
	}

}
