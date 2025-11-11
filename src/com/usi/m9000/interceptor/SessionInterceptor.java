package com.usi.m9000.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.interceptor.ValidationAware;

public class SessionInterceptor implements Interceptor{

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(SessionInterceptor.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Map<String, Object> session;
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String intercept(ActionInvocation actionInvocation) throws Exception {
		logger.debug("Entered intercept method of sessionInterceptor"+actionInvocation.getAction().toString());
		session = ActionContext.getContext().getSession();
//		System.out.println(" retrived session "+ session);	
//		if(!actionInvocation.getAction().toString().contains("M9kLogin") && (session == null || (session != null && session.get("userName") == null))) { 
		if((session == null || session.isEmpty() || (session != null && session.get("userDetails") == null))) {
			logger.debug(" session expired..."+session); 
			addActionError(actionInvocation, "Session expired due to inactivity or a restart of the server");
			return "sessionexpired"; 
		} 
		else
		{
			logger.debug("interceptor else cond..."+actionInvocation.getAction().toString()+" And session keys : "+session.keySet());
		}

		 final ActionContext context = actionInvocation.getInvocationContext();
	        HttpServletResponse response = (HttpServletResponse)context.get(StrutsStatics.HTTP_RESPONSE);
	        if(response!=null){
	            response.setHeader("Cache-control", "no-cache, no-store");
	            response.setHeader("Pragme", "no-cache");
	            response.setHeader("Expires", "-1");
	        }
		String actionResult = actionInvocation.invoke(); 
//		System.out.println("action result "+actionResult);
		return actionResult; 
	}

	private void addActionError(ActionInvocation invocation, String message) {
		Object action = invocation.getAction();
		logger.debug("Action "+action+" instance of validation aware ?"+(action instanceof ValidationAware)+" or instance of actionsupport? "+(action instanceof  ActionSupport));
		if(action instanceof ValidationAware) {
			((ValidationAware) action).addActionError(message);
		}
		else if (action instanceof  ActionSupport)
		{
			((ActionSupport) action).addActionError(message);
		}
	}

}
