/**
 * 
 */
package com.usi.m9000.actions;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author sramasamy
 *
 */
public class M9kCleanupSession implements HttpSessionListener, ServletContextListener {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kCleanupSession.class);
	
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
    	logger.debug("Inside session destroyed");
    	event.getSession().invalidate();
    }

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		logger.debug("Session Created with ID "+arg0.getSession().getId());
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.debug("Context Destroyed "+arg0.getServletContext().getServerInfo());
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.debug("Context initialized "+arg0.getServletContext().getServerInfo());
		
	}

    // ...
}