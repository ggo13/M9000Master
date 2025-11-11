package com.usi.m9000.test;

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

/**
 * Example code for retrieving a Users Primary Group
 * from Microsoft Active Directory via. its LDAP API
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 */
public class LDAPAuthenticate {
    public static final String DISTINGUISHED_NAME = "distinguishedName";
    public static final String CN = "cn";
    public static final String MEMBER = "member";
    public static final String MEMBER_OF = "memberOf";
    public static final String SEARCH_BY_SAM_ACCOUNT_NAME = "(SAMAccountName={0})";
    public static final String SEARCH_GROUP_BY_GROUP_CN = "(&(objectCategory=group)(cn={0}))";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NamingException {
        
        final String ldapAdServer = "ldap://10.10.0.10:389";
        
        final String ldapUsername = "USI\\sramasamy";
        final String ldapPassword = "S7aspeP";
        String defaultSearchBase = "DC=USI,DC=local";
        
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, ldapAdServer);
        // The value of Context.SECURITY_PRINCIPAL must be the logon username with the domain name
        env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
        // The value of the Context.SECURITY_CREDENTIALS should be the user's password
        env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
        DirContext ctx;
        try {
            // Authenticate the logon user
            ctx = new InitialDirContext(env);
            System.out.println("Authentication Successful"+ldapUsername.substring(ldapUsername.indexOf("\\")+1));
            
            SearchResult sr = executeSearchSingleResult(ctx, SearchControls.SUBTREE_SCOPE, defaultSearchBase,
                    MessageFormat.format( SEARCH_BY_SAM_ACCOUNT_NAME, new Object[] {ldapUsername.substring(ldapUsername.indexOf("\\")+1)}),
                    new String[] {DISTINGUISHED_NAME, CN, MEMBER_OF}
                    );
            
            Attribute memberOf = sr.getAttributes().get(MEMBER_OF);
            if (memberOf != null) {
                for ( Enumeration<?> e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
                    String unprocessedGroupDN = e1.nextElement().toString();
                    String unprocessedGroupCN = getCN(unprocessedGroupDN);
                    System.out.println(unprocessedGroupDN + " CN= "+unprocessedGroupCN);
                    // Quick check for direct membership
                    if (isSame ("Manualss", unprocessedGroupCN) && isSame ("Manualss", unprocessedGroupCN)) {
                        System.out.println(ldapUsername + " is authorized.");
                        System.exit(0);
                    }
                    else if (isSame ("PDF Scans", unprocessedGroupCN) && isSame ("PDF Scans", unprocessedGroupCN)) {
                        System.out.println(ldapUsername + " is authorized.");
                        System.exit(0);
                    }
                    else {
//                        unProcessedUserGroups.put(unprocessedGroupDN, unprocessedGroupCN);
                        System.out.println(unprocessedGroupDN + " "+unprocessedGroupCN);
                    }
                }
                
            }
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
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
}