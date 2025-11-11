package com.usi.m9000.data;

import com.usi.m9000.dao.AlarmsLogDAO;
import com.usi.m9000.dao.ComtradeContAnalogDAO;
import com.usi.m9000.dao.ComtradeContDAO;
import com.usi.m9000.dao.ComtradeDataDAO;
import com.usi.m9000.dao.ContinuousComtradeDataDAO;
import com.usi.m9000.dao.EmailDAO;
import com.usi.m9000.dao.HierarchyDAO;
import com.usi.m9000.dao.LongTermDataDAO;
import com.usi.m9000.dao.M9kCleanupDAO;
import com.usi.m9000.dao.M9kSerDAO;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.dao.UsersDAO;
import com.usi.m9000.dao.interfaces.Dnp3ConfigurationDAO;
import com.usi.m9000.dao.interfaces.Dnp3SourceTypeDAO;
import com.usi.m9000.util.M9kConstants;

public abstract class M9kDAOFactory {

	  // There will be a method for each DAO that can be 
	  // created. The concrete factories will have to 
	  // implement these methods.
	  public abstract ComtradeDataDAO getComtradeDataDAO();
	  public abstract ComtradeContDAO getContinuousComtradeDAO();
	  public abstract ComtradeContAnalogDAO getComtradeContinuousAnalogDAO();
	  public abstract M9kSerDAO getM9kSerDAO();
	  public abstract StationDAO getStationDAO();
	  public abstract UsersDAO getUsersDAO();
	  public abstract LongTermDataDAO getLongTermDataDAO();
	  public abstract ContinuousComtradeDataDAO getContinuousComtradeDataDAO();
	  public abstract AlarmsLogDAO getAlarmsLogDAO();
	  public abstract EmailDAO getEmailDAO();
	  public abstract M9kCleanupDAO getM9kCleanupDAO();
	  public abstract HierarchyDAO getM9kHierarchyDAO();
	  public abstract Dnp3ConfigurationDAO getDnp3ConfigurationDAO();
	  public abstract Dnp3SourceTypeDAO getDnp3SourceTypeDAO();
	  
	  public static M9kDAOFactory getDAOFactory(
	      int whichFactory) {
	  
	    switch (whichFactory) {
	      case M9kConstants.MYSQL: 
	          return new MysqlDAOFactory();
	      default           : 
	          return null;
	    }
	  }
}
