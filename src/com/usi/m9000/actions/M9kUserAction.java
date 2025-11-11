package com.usi.m9000.actions;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import com.usi.m9000.dao.UsersDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class M9kUserAction extends ActionSupport implements SessionAware, ServletContextListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Object> session;
    private List<UsersDTO> m9kAppUsers;
    private M9kDAOFactory m9kDAOFactory;
    private UsersDAO usersDao;
    private UsersDTO userDto;

    // Save parameters
    private String id;
    private String userName;
    private String password;
    private String role;
    
    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kUserAction.class);

    public M9kUserAction() {
    	m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
    	usersDao = m9kDAOFactory.getUsersDAO();
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
        userDto = (UsersDTO) this.session.get("userDetails");
    }

    @Override
    @Action(value = "displayUsers", results = {
    @Result(name="success",location="/jsp/manageUsers.jsp")})
    @SkipValidation
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Actions({@Action(value = "usersList-json", results = {
            @Result(type = "json", params = {"includeProperties", "m9kAppUsers.*"})}
    )
    })
    @SkipValidation
    public String getAllUsers()
    {
    	logger.debug("Get all users gets invoked!!");
        m9kAppUsers = usersDao.getAllUsers();
    	return SUCCESS;
    }
    @Action(value = "saveUsers", interceptorRefs = {@InterceptorRef("jsonValidationWorkflowStack"),@InterceptorRef("defaultStack"),@InterceptorRef("exceptionMappingStack")}, results = {
            @Result(name = SUCCESS, type = "json", params = {"includeProperties", "actionMessages.*,m9kAppUsers.*",
                    "ignoreHierarchy", "false"}),
            @Result(name = ERROR, type = "json", params = {"includeProperties", "actionErrors.*", "ignoreHierarchy",
                    "false", "statusCode", "500"})})
    public String saveUserDetails()
    {
    	String resultString = SUCCESS;
    	
    	try {
            UsersDTO userDto = new UsersDTO();
            userDto.setUserName(getUserName());
            userDto.setPassword(getPassword());
            userDto.setRole(getRole());
            logger.debug("get id "+getId());
            if (getId() != null && !"".equals(getId())) {
                userDto.setId(Integer.parseInt(getId()));
                int updatedCount = usersDao.updateUserDetails(userDto);
                if (updatedCount == 0)
                {
                	logger.error("No user was updated");
                	resultString = ERROR;
                    this.addActionError("No User was updated");
                }
                else
                {
                	logger.info("User details updated for "+getId()+" user "+userDto.getDisplayName());
                }
            } else {
            	usersDao.addUser(userDto);
                logger.debug("Created new user "+getUserName());
            }
        } catch (Exception e) {
        	resultString = ERROR;
            this.addActionError("Error while saving user : " + e.getMessage());
        }
    	return resultString;
    }
    
    @Action(value = "deleteUsers", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
            @Result(name = SUCCESS, type = "json", params = {"includeProperties", "actionMessages.*",
                    "ignoreHierarchy", "false"}),
            @Result(name = ERROR, type = "json", params = {"includeProperties", "actionErrors.*", "ignoreHierarchy",
                    "false", "statusCode", "500"})})
    @SkipValidation
    public String deleteUser() throws Exception {
        String resultString = SUCCESS;
        try {
            int removeId = Integer.parseInt(id);
            if (userDto.getId() == removeId)
            {
            	addActionError("You are logged in with the same user that you are trying to delete. You cannot remove yourself.");
            	resultString = ERROR;
            }
            else
            {
	            logger.debug("Delete user " + removeId);
	            int deleteCount = usersDao.removeUser(removeId);
	            if (deleteCount == 0)
	            {
	            	logger.error("No user was deleted");
	            	resultString = ERROR;
	                this.addActionError("No User was deleted");
	            }
            }
        } catch (Exception e) {
        	resultString = ERROR;
            this.addActionError("Error while deleting user : " + e.getMessage());
        }
        return resultString;
    }

    @Action(value = "displayHierarchy", results = {
    	    @Result(name=SUCCESS,location="/jsp/demoHierarchy.jsp")})
    	    @SkipValidation
    	    public String getHierarchy() throws Exception {
    	        return SUCCESS;
    	    }

	public List<UsersDTO> getM9kAppUsers() {
		return m9kAppUsers;
	}

	public void setM9kAppUsers(List<UsersDTO> m9kAppUsers) {
		this.m9kAppUsers = m9kAppUsers;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@RequiredStringValidator(fieldName = "name", type = ValidatorType.FIELD, message = "User Name is required")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@RequiredStringValidator(fieldName = "password", type = ValidatorType.FIELD, message = "Password is required")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@RequiredStringValidator(fieldName = "role", type = ValidatorType.FIELD, message = "User Role is required")
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public void validate()
	{
		logger.debug("Entered user validate method user name "+getUserName()+" user role "+getRole());
		if (getId() != null && getId().isEmpty())
		{
			UsersDTO existingUser = usersDao.getUserDetails(getUserName());
			if (existingUser != null)
			{
				addFieldError("userName", "User Name already exists");
			}
		}
		if (!M9kUtils.isValidUserName(getUserName()))
		{
			addFieldError("userName", "User Name should have no special characters. It should not start with number and should be between 3 to 15 characters");
		}
		if (!M9kUtils.isValidPassword(getPassword()))
		{
			addFieldError("password", "Password should contain at least one lower case, one upper case, one number and one of these special characters !@#$%^&. Length should be between 4 to 20 characters");
		}		
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
