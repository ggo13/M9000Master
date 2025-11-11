package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.UsersDTO;

public class MySqlUsersDAO implements UsersDAO {

	Connection mysqlConn;
	Statement stmt;
	ResultSet rs;
	org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlUsersDAO.class);
	public MySqlUsersDAO()
	{

	}
	
	public void main(String[] args)
	{
		MySqlUsersDAO test = new MySqlUsersDAO();
//		UsersDTO userDto = new UsersDTO();
//		userDto.setUserName("usi");
//		userDto.setPassword("usi");
//		userDto.setRole("manager");
//		test.addUser(userDto);
			logger.debug(test.getUserDetails("usi", "usi").toString());
	}
	@Override
	public void addUser(UsersDTO userDto) {
		PreparedStatement ps = null;
		try
		{
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered adduser in DAO");
			int i = 0;
			ps = mysqlConn.prepareStatement("insert into users set userName= ?, password = SHA1(?) , role=?");
			ps.setString(++i, userDto.getUserName());
			ps.setString(++i, userDto.getPassword());
			ps.setString(++i, userDto.getRole());
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Inserted the user details "+userDto+" with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
				logger.error("Error in adding new user ",e);
			} 
		catch (Exception e) {
			logger.error("Error in in adding new user ",e);
		}
			finally
			{
				try {
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}
		
	}
	
	@Override
	public int updateUserDetails(UsersDTO userDto) {
		PreparedStatement ps = null;
		int updatedRowCount = 0;
		try
		{
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered adduser in DAO");
			int i = 0;
			ps = mysqlConn.prepareStatement("update users set userName= ?, password = SHA1(?) , role=? where idUsers=?");
			ps.setString(++i, userDto.getUserName());
			ps.setString(++i, userDto.getPassword());
			ps.setString(++i, userDto.getRole());
			ps.setInt(++i, userDto.getId());
			updatedRowCount = ps.executeUpdate();
			logger.debug("Updated the user details "+userDto+" with updated row count "+updatedRowCount);
		}
		
		catch (Exception e) {
			updatedRowCount = 0;
			logger.error("Error in in updating user ",e);
		}
			finally
			{
				try {
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}
		return updatedRowCount;
	}

	public UsersDTO getUserDetails( String userName, String password ) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		UsersDTO userDto = null;
		try
		{
			logger.debug("About to get database connection to get user details.");
			logger.debug("Is it really invoking the connection? ");
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null)
			{
				logger.debug("Entered getUserDetails in DAO to get details for usename "+userName);
				int i = 0;
				ps = mysqlConn.prepareStatement("select * from users where userName= ? and password=SHA1(?)");
				ps.setString(++i, userName);
				ps.setString(++i, password);
				rs = ps.executeQuery();
				while (rs.next())
				{
					userDto = new UsersDTO();
					userDto.setId(rs.getInt("idUsers"));
					userDto.setUserName(rs.getString("userName"));
					userDto.setRole(rs.getString("role"));
				}
				logger.debug("User details about to return "+userDto);
			}
		}
		catch (M9000Exception e) {
			userDto = new UsersDTO();
			userDto.setErrorMessage("ERROR: Login Unsuccessful. Issue with database connectivity. "+e.getMessage());
			mysqlConn = null;
			
		}
		catch (Exception e) {
			logger.error("Error in getting user details  ",e);
			userDto = new UsersDTO();
			userDto.setErrorMessage("Error in getting user details. "+e.getMessage());
		}
			finally
			{
				try {
					
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}
	return userDto;	
	}

	/**
	 * Get user details for given name. Return NUll if none
	 */
	public UsersDTO getUserDetails( String userName ) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		UsersDTO userDto = null;
		try
		{
			logger.debug("About to get database connection to get user details.");
			logger.debug("Is it really invoking the connection? ");
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null)
			{
				logger.debug("Entered getUserDetails in DAO to get details for usename "+userName);
				int i = 0;
				ps = mysqlConn.prepareStatement("select * from users where userName= ?");
				ps.setString(++i, userName);
				rs = ps.executeQuery();
				while (rs.next())
				{
					userDto = new UsersDTO();
					userDto.setId(rs.getInt("idUsers"));
					userDto.setUserName(rs.getString("userName"));
					userDto.setRole(rs.getString("role"));
				}
				logger.debug("User details about to return "+userDto);
			}
		}
		catch (Exception e) {
			userDto = null;
			logger.error("Error in getting user details for name "+userName,e);
		}
			finally
			{
				try {
					
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}
	return userDto;	
	}

	public List<UsersDTO> getAllUsers() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		UsersDTO userDto = null;
		List<UsersDTO> lstUsers = new ArrayList<UsersDTO>();
		try
		{
			logger.debug("About to get database connection to get user details.");
			logger.debug("Is it really invoking the connection? ");
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null)
			{
				ps = mysqlConn.prepareStatement("select * from users order by idUsers");
				rs = ps.executeQuery();
				while (rs.next())
				{
					userDto = new UsersDTO();
					userDto.setId(rs.getInt("idUsers"));
					userDto.setUserName(rs.getString("userName"));
					userDto.setRole(rs.getString("role"));
					lstUsers.add(userDto);
				}
				logger.debug("List of User details about to return "+lstUsers.size());
			}
		}
		catch (M9000Exception e) {
			logger.error("Exception while fetching list of users ",e);
			mysqlConn = null;
			
		}
		catch (Exception e) {
			logger.error("Error in fetching list of users ",e);
		}
			finally
			{
				try {
					
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

		return lstUsers;
	}

	public UsersDTO findById(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		UsersDTO userDto = null;
		try
		{
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null)
			{
				ps = mysqlConn.prepareStatement("select * from users where idUsers = ?");
				ps.setInt(1, id);
				rs = ps.executeQuery();
				if (rs.next())
				{
					userDto = new UsersDTO();
					userDto.setId(rs.getInt("idUsers"));
					userDto.setUserName(rs.getString("userName"));
					userDto.setRole(rs.getString("role"));
				}
				logger.debug("List of User details about to return "+userDto);
			}
		}
		catch (M9000Exception e) {
			logger.error("Exception while fetching user with id "+id,e);
			mysqlConn = null;
			
		}
		catch (Exception e) {
			logger.error("Error in fetching list of users ",e);
		}
			finally
			{
				try {
					
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

		return userDto;
	}

	public List<UsersDTO> findNotById(int id, int from, int to) {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		UsersDTO userDto = null;
		List<UsersDTO> lstUsers = new ArrayList<UsersDTO>();
		try
		{
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null)
			{
				ps = mysqlConn.prepareStatement("select * from users where idUsers != ? order by idUserslimit ?,?");
				ps.setInt(1, id);
				ps.setInt(2, from);
				ps.setInt(3, to);
				rs = ps.executeQuery();
				while (rs.next())
				{
					userDto = new UsersDTO();
					userDto.setId(rs.getInt("idUsers"));
					userDto.setUserName(rs.getString("userName"));
					userDto.setRole(rs.getString("role"));
					lstUsers.add(userDto);
				}
				logger.debug("List of User details about to return "+lstUsers.size());
			}
		}
		catch (M9000Exception e) {
			logger.error("Exception while fetching list of users ",e);
			mysqlConn = null;
			
		}
		catch (Exception e) {
			logger.error("Error in fetching list of users ",e);
		}
			finally
			{
				try {
					
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

		return lstUsers;
	}

	public List<UsersDTO> findGreaterAsId(int id, int from, int to) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		UsersDTO userDto = null;
		List<UsersDTO> lstUsers = new ArrayList<UsersDTO>();
		try
		{
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null)
			{
				ps = mysqlConn.prepareStatement("select * from users where idUsers > ? order by idUserslimit ?,?");
				ps.setInt(1, id);
				ps.setInt(2, from);
				ps.setInt(3, to);
				rs = ps.executeQuery();
				while (rs.next())
				{
					userDto = new UsersDTO();
					userDto.setId(rs.getInt("idUsers"));
					userDto.setUserName(rs.getString("userName"));
					userDto.setRole(rs.getString("role"));
					lstUsers.add(userDto);
				}
				logger.debug("List of User details about to return "+lstUsers.size());
			}
		}
		catch (M9000Exception e) {
			logger.error("Exception while fetching list of users ",e);
			mysqlConn = null;
			
		}
		catch (Exception e) {
			logger.error("Error in fetching list of users ",e);
		}
			finally
			{
				try {
					
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

		return lstUsers;
	}

	public List<UsersDTO> findLesserAsId(int id, int from, int to) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		UsersDTO userDto = null;
		List<UsersDTO> lstUsers = new ArrayList<UsersDTO>();
		try
		{
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null)
			{
				ps = mysqlConn.prepareStatement("select * from users where idUsers < ? order by idUsers limit ?,?");
				ps.setInt(1, id);
				ps.setInt(2, from);
				ps.setInt(3, to);
				rs = ps.executeQuery();
				while (rs.next())
				{
					userDto = new UsersDTO();
					userDto.setId(rs.getInt("idUsers"));
					userDto.setUserName(rs.getString("userName"));
					userDto.setRole(rs.getString("role"));
					lstUsers.add(userDto);
				}
				logger.debug("List of User details about to return "+lstUsers.size());
			}
		}
		catch (M9000Exception e) {
			logger.error("Exception while fetching list of users ",e);
			mysqlConn = null;
			
		}
		catch (Exception e) {
			logger.error("Error in fetching list of users ",e);
		}
			finally
			{
				try {
					
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

		return lstUsers;

	}

	public Integer getUsersCount() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int totalAlarmsCount = 0;
		try {
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			if (mysqlConn != null) {
				ps = mysqlConn.prepareStatement("select count(*) from users ");
				rs = ps.executeQuery();
				if (rs.next()) {
					totalAlarmsCount = rs.getInt(1);
				}
				logger.debug("Total number of users " + totalAlarmsCount);
			}
		} catch (M9000Exception e) {
			logger.error("Exception while getting count ", e);
			mysqlConn = null;

		} catch (Exception e) {
			logger.error("Error while getting count ", e);
		} finally {
			try {

				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (mysqlConn != null) {
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return totalAlarmsCount;
	}
	
	@Override
	public int removeUser(int id) {
		PreparedStatement ps = null;
		int updatedRowCount = 0;
		try
		{
			mysqlConn =  M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered adduser in DAO");
			int i = 0;
			ps = mysqlConn.prepareStatement("delete from users where idUsers=?");
			ps.setInt(++i, id);
			updatedRowCount = ps.executeUpdate();
			logger.debug("Deleted the user "+id+" with updated row count "+updatedRowCount);
		}
		
		catch (Exception e) {
			updatedRowCount = 0;
			logger.error("Error in in updating user ",e);
		}
			finally
			{
				try {
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}
		return updatedRowCount;
	}
}
