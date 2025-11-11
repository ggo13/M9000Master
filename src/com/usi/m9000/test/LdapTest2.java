package com.usi.m9000.test;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LdapTest2 {

	public static void main(String[] args) {

		final String PROVIDER_URL = "ldap://195.1.1.103:389/DC=Maginst,DC=local";  //Enter LDAP URL here
			
		final Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,	"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, PROVIDER_URL);

		try {

			DirContext ctx = new InitialDirContext(env);

			String filter = "(cn=USI0334-PC)"; // Enter User ID here. 
			String[] attrIDs = {"uid","cn","mail"}; // Enter list of attributes to retrieve from LDAP here
			
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(attrIDs);

			NamingEnumeration<?> answer = ctx.search("OU=Win 7 Computers,OU=Utility Systems", filter, ctls);

			SearchResult searchResult = null;
			String cn=null;
			String uid=null;
			String mail=null;

			while (answer.hasMore()) {

				searchResult = (SearchResult) answer.next();
				Attributes attr = searchResult.getAttributes();
				cn=attr.get("cn").get(0).toString();
				uid=attr.get("uid").get(0).toString();
				mail=attr.get("mail").get(0).toString();
				
				System.out.println("Name: "+cn);
				System.out.println("User ID: "+uid);
				System.out.println("E-mail Address: "+mail);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

