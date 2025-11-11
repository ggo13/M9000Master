package com.usi.m9000.util;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.usi.m9000.dto.UsersDTO;

/**
 * Example code for retrieving a Users Primary Group
 * from Microsoft Active Directory via. its LDAP API
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 */
public class LDAPUtils {
    public static final String DISTINGUISHED_NAME = "distinguishedName";
    public static final String CN = "cn";
    public static final String MEMBER = "member";
    public static final String MEMBER_OF = "memberOf";
    public static final String SEARCH_BY_SAM_ACCOUNT_NAME = "(SAMAccountName={0})";
    public static final String SEARCH_GROUP_BY_GROUP_CN = "(&(objectCategory=group)(cn={0}))";
	private static PropertiesConfiguration config = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(LDAPUtils.class);
	private static String ldapServer;
	private static String userDomain;
    private static String defaultSearchBase = "DC=USI,DC=local";
    private static boolean isAdmin = false;
    private static String adminGroup;
    private static String guestGroup;
    private static boolean isLDAPEnabled;

    static
    {
    	init();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NamingException {
        
//        final String ldapAdServer = "ldap://195.1.1.103:389";
        
        final String ldapUsername = "sramasamy";
        final String ldapPassword = "S7aspeP";
        
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, getLdapServer());
        // The value of Context.SECURITY_PRINCIPAL must be the logon username with the domain name
        env.put(Context.SECURITY_PRINCIPAL, getUserDomain()+"\\"+ldapUsername);
        // The value of the Context.SECURITY_CREDENTIALS should be the user's password
        env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
        DirContext ctx;
        try {
            // Authenticate the logon user
            ctx = new InitialDirContext(env);
            System.out.println("Authentication Successful"+ldapUsername);
            
            SearchResult sr = executeSearchSingleResult(ctx, SearchControls.SUBTREE_SCOPE, getDefaultSearchBase(),
                    MessageFormat.format( SEARCH_BY_SAM_ACCOUNT_NAME, new Object[] {ldapUsername}),
                    new String[] {DISTINGUISHED_NAME, CN, MEMBER_OF}
                    );
            
            Attribute memberOf = sr.getAttributes().get(MEMBER_OF);
            if (memberOf != null) {
                for ( Enumeration<?> e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
                    String unprocessedGroupDN = e1.nextElement().toString();
                    String unprocessedGroupCN = getCN(unprocessedGroupDN);
                    System.out.println("what? "+unprocessedGroupDN + " CN= "+unprocessedGroupCN);
                    // Quick check for direct membership
                    if (isSame ("Manuals", unprocessedGroupCN) && isSame ("Manuals", unprocessedGroupCN)) {
                        System.out.println(ldapUsername + " is authorized.");
//                        System.exit(0);
                        break;
                    }
                    else if (isSame ("PDF Scans", unprocessedGroupCN) && isSame ("PDF Scans", unprocessedGroupCN)) {
                        System.out.println(ldapUsername + " is authorized.");
//                        System.exit(0);
                        break;
                    }
                    else {
//                        unProcessedUserGroups.put(unprocessedGroupDN, unprocessedGroupCN);
                        System.out.println("Else "+unprocessedGroupDN + " "+unprocessedGroupCN);
                    }
                }
                
            }
            memberOf = sr.getAttributes().get(CN);
            System.out.println("memberOf"+memberOf);
            String displayName = "";
            for ( Enumeration<?> e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
            	System.out.println("In loop ");
            	displayName = displayName.concat(getCN(e1.nextElement().toString()));
            }
            System.out.println("Display Name "+displayName);
            
        } catch (NamingException ex) {
            // Authentication failed, just check on the exception and do something about it.
        	ex.printStackTrace();
        }
        System.out.println("Above to authenticate... ");
        System.out.println(LDAPUtils.isUserAuthenticated(ldapUsername, ldapPassword));;
        System.out.println("Is Admin user? "+LDAPUtils.isAdmin());
}
   
    private static void init()
    {
    	try {
			config = new PropertiesConfiguration("m9k-master.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
			logger.error("Error in reading m9k-master.properties files. Enabling health poll by default and runs at frequency of 60 seconds",e);
			config=null;
		}
    }
    public static UsersDTO isUserAuthenticated(String user, String password)
    {
    	UsersDTO userDto = null;
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, getLdapServer());
        // The value of Context.SECURITY_PRINCIPAL must be the logon username with the domain name
        env.put(Context.SECURITY_PRINCIPAL, getUserDomain()+"\\"+user);
        // The value of the Context.SECURITY_CREDENTIALS should be the user's password
        env.put(Context.SECURITY_CREDENTIALS, password);
        DirContext ctx;
        try {
            // Authenticate the logon user
            ctx = new InitialDirContext(env);
            logger.debug("Authentication Successful");
            userDto = new UsersDTO();
			userDto.setUserName(user);

            SearchResult sr = executeSearchSingleResult(ctx, SearchControls.SUBTREE_SCOPE, getDefaultSearchBase(),
                    MessageFormat.format( SEARCH_BY_SAM_ACCOUNT_NAME, new Object[] {user}),
                    new String[] {DISTINGUISHED_NAME, CN, MEMBER_OF}
                    );
            
            Attribute memberOf = sr.getAttributes().get(MEMBER_OF);
            if (memberOf != null) {
                for ( Enumeration<?> e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
                    String unprocessedGroupDN = e1.nextElement().toString();
                    String unprocessedGroupCN = getCN(unprocessedGroupDN);
                    logger.debug("Available "+unprocessedGroupDN + " CN= "+unprocessedGroupCN);
                    
                    if (isSame (getAdminGroup(), unprocessedGroupCN) && isSame (getAdminGroup(), unprocessedGroupCN)) {
                    	logger.debug(user + " is authorized.");
                        userDto.setRole("admin");
            			break;
                    }
                    else if (isSame (getGuestGroup(), unprocessedGroupCN) && isSame (getGuestGroup(), unprocessedGroupCN)) {
                    	logger.debug(user + " is authorized.");
        				userDto.setRole("guest");
                    }


                }
                
            }
            memberOf = sr.getAttributes().get(CN);
            String displayName = "";
            for ( Enumeration<?> e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
            	displayName = displayName.concat(getCN(e1.nextElement().toString()));
            }
            logger.debug("Display Name "+displayName);
            userDto.setDisplayName(displayName);
            
            
        } catch (NamingException ex) {
            logger.error("Error in aunthenticating the user "+user,ex);
        }

        return userDto ;
    }
    public static boolean isSame(String target, String candidate) {
        if (target != null && target.equalsIgnoreCase(candidate)) {
            return true;
        }
        return false;
    }
    
    private static String getCN(String cnName) {
        if (cnName != null && cnName.toUpperCase().startsWith("CN=")) {
            cnName = cnName.substring(3);
        }
        int position = cnName.indexOf(',');
        if (position == -1) {
            return cnName;
        } else {
            return cnName.substring(0, position);
        }
    }
    
    private static NamingEnumeration<?> executeSearch(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
        // Create the search controls
        SearchControls searchCtls = new SearchControls();

        // Specify the attributes to return
        if (attributes != null) {
            searchCtls.setReturningAttributes(attributes);
        }

        // Specify the search scope
        searchCtls.setSearchScope(searchScope);

        // Search for objects using the filter
        NamingEnumeration<?> result = ctx.search(searchBase, searchFilter,searchCtls);
        return result;
    }
    
    private static SearchResult executeSearchSingleResult(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
        NamingEnumeration<?> result = executeSearch(ctx, searchScope,  searchBase, searchFilter, attributes);

        SearchResult sr = null;
        // Loop through the search results
        while (result.hasMoreElements()) {
            sr = (SearchResult) result.next();
            break;
        }
        return sr;
    }
	public static boolean isAdmin() {
		return isAdmin;
	}
	public static void setAdmin(boolean isAdmin) {
		LDAPUtils.isAdmin = isAdmin;
	}
	public static String getLdapServer() {
		if (config != null)
		{
			ldapServer=config.getString("ldap-server-url", "");
		}
		return ldapServer;
	}
	public static void setLdapServer(String ldapServer) {
		LDAPUtils.ldapServer = ldapServer;
	}
	public static String getDefaultSearchBase() {
		if (config != null)
		{
			defaultSearchBase=config.getString("ldap-default-search-base", "ldap://10.10.0.10:389");
		}
		logger.debug("defaultSearchBase "+defaultSearchBase);
		return defaultSearchBase;
	}
	public static void setDefaultSearchBase(String defaultSearchBase) {
		LDAPUtils.defaultSearchBase = defaultSearchBase;
	}
	public static String getAdminGroup() {
		if (config != null)
		{
			adminGroup=config.getString("ldap-admin-group", "");
		}
		return adminGroup;
	}
	public static void setAdminGroup(String adminGroup) {
		LDAPUtils.adminGroup = adminGroup;
	}
	public static String getUserDomain() {
		if (config != null)
		{
			userDomain=config.getString("user-domain", "");
		}
		return userDomain;
	}
	public static void setUserDomain(String userDomain) {
		LDAPUtils.userDomain = userDomain;
	}

	public static boolean isLDAPEnabled() {
//		init();
		if (config != null)
		{
			String ldapStatus = config.getString("ldap", M9kConstants.DISABLE);
			if (ldapStatus.equalsIgnoreCase(M9kConstants.ENABLE))
			{
				isLDAPEnabled = true;
			}
			else
			{
				isLDAPEnabled = false;
			}
		}
		return isLDAPEnabled;
	}

	public static void setLDAPEnabled(boolean isLDAPEnabled) {
		LDAPUtils.isLDAPEnabled = isLDAPEnabled;
	}

	public static String getGuestGroup() {
		if (config != null)
		{
			guestGroup=config.getString("ldap-guest-group", "");
		}
		return guestGroup;
	}

	public static void setGuestGroup(String guestGroup) {
		LDAPUtils.guestGroup = guestGroup;
	}
}