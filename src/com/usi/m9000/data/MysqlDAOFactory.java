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
import com.usi.m9000.dao.MySqlAlarmLogsDAO;
import com.usi.m9000.dao.MySqlComtradeContAnalogDAO;
import com.usi.m9000.dao.MySqlComtradeContDAO;
import com.usi.m9000.dao.MySqlComtradeDetailsDAO;
import com.usi.m9000.dao.MySqlContinuousComtradeDAO;
import com.usi.m9000.dao.MySqlDnp3ConfigurationDAO;
import com.usi.m9000.dao.MySqlDnp3SourceTypeDAO;
import com.usi.m9000.dao.MySqlEmailDAO;
import com.usi.m9000.dao.MySqlHierarchyDAO;
import com.usi.m9000.dao.MySqlLongTermDataDAO;
import com.usi.m9000.dao.MySqlM9kCleanupDAO;
import com.usi.m9000.dao.MySqlSerDAO;
import com.usi.m9000.dao.MySqlStationDAO;
import com.usi.m9000.dao.MySqlUsersDAO;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.dao.UsersDAO;
import com.usi.m9000.dao.interfaces.Dnp3ConfigurationDAO;
import com.usi.m9000.dao.interfaces.Dnp3SourceTypeDAO;

public class MysqlDAOFactory extends M9kDAOFactory {

	public MysqlDAOFactory ()
	{
	}

	@Override
	public ComtradeDataDAO getComtradeDataDAO() {
		return new MySqlComtradeDetailsDAO();
	}

	@Override
	public ComtradeContDAO getContinuousComtradeDAO() {
		return new MySqlComtradeContDAO();
	}

	@Override
	public ComtradeContAnalogDAO getComtradeContinuousAnalogDAO() {
		return new MySqlComtradeContAnalogDAO();
	}

	@Override
	public M9kSerDAO getM9kSerDAO() {
		return new MySqlSerDAO();
	}

	@Override
	public StationDAO getStationDAO() {
		return new MySqlStationDAO();
	}

	@Override
	public LongTermDataDAO getLongTermDataDAO() {
		return new MySqlLongTermDataDAO();
	}

	@Override
	public UsersDAO getUsersDAO() {
		return new MySqlUsersDAO();
	}

	/* (non-Javadoc)
	 * @see com.usi.m9000.data.M9kDAOFactory#getContinuousComtradeDataDAO()
	 */
	@Override
	public ContinuousComtradeDataDAO getContinuousComtradeDataDAO() {
		return new MySqlContinuousComtradeDAO();
	}

	/* (non-Javadoc)
	 * @see com.usi.m9000.data.M9kDAOFactory#getAlarmsLogDAO()
	 */
	@Override
	public AlarmsLogDAO getAlarmsLogDAO() {
		return new MySqlAlarmLogsDAO();
	}

	@Override
	public EmailDAO getEmailDAO() {
		return new MySqlEmailDAO();
	}

	@Override
	public M9kCleanupDAO getM9kCleanupDAO() {
		return new MySqlM9kCleanupDAO();
	}

	@Override
	public HierarchyDAO getM9kHierarchyDAO() {
		return new MySqlHierarchyDAO();
	}

    @Override
    public Dnp3ConfigurationDAO getDnp3ConfigurationDAO() {
        return new MySqlDnp3ConfigurationDAO();
    }

	@Override
	public Dnp3SourceTypeDAO getDnp3SourceTypeDAO() {
		return new MySqlDnp3SourceTypeDAO();
	}
}
