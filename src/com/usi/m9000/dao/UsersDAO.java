package com.usi.m9000.dao;

import java.util.List;

import com.usi.m9000.dto.UsersDTO;



public interface UsersDAO {
	public void addUser(UsersDTO usersDto);
	public UsersDTO getUserDetails(String userName, String password);
	 public List<UsersDTO> getAllUsers();
	 public UsersDTO findById(int id);
	 public List<UsersDTO> findNotById(int id, int from, int to);
	 public List<UsersDTO> findGreaterAsId(int id, int from, int to);
	 public List<UsersDTO> findLesserAsId(int id, int from, int to);
	 public Integer getUsersCount();
	 public UsersDTO getUserDetails(String userName);
	 public int updateUserDetails (UsersDTO usersDto);
	 public int removeUser(int id);
}
