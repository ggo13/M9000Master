package com.usi.m9000.test;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Copyright Alvin Alexander, http://devdaily.com.
 * This code is shared here under the Attribution 3.0 Unported License.
 * See this URL for details: http://creativecommons.org/licenses/by/3.0/
 *
 * Validates a user against a Kerberos server 
 * VM arguments:
 *    -Djava.security.auth.login.config="C:/Working/ADTest/jaas.conf"
 */
public class ActiveDirectoryValidator
{
  
  private static final String USERNAME = "FOO";
  private static final String PASSWORD = "BAR";
  
  private ActiveDirectoryValidator()
  {
  }
  
  /**
   * ActiveDirectoryValidator sets the environment
   * @param realm (domain in all caps)
   * @param kdc (domain controller)
   * @param configurationFile (path to jaas.conf)
   * @param debug (boolean)
   */
  public ActiveDirectoryValidator(String realm, 
                      String kdc, 
                      String configurationFile,
                      boolean debug)
  {
    super();
    System.setProperty("java.security.auth.login.config", configurationFile);
    System.setProperty("java.security.krb5.realm", realm);
    System.setProperty("java.security.krb5.kdc", kdc);
    if (debug)
    {
      System.setProperty("java.security.krb5.debug", "true");
    }
    else
    {
      System.setProperty("java.security.krb5.debug", "false");
    }
  }

  /**
   * validateUser accepts userName & password and returns true if credentials
   * successfully authenticate to the domain.
   * @return boolean True=valid credentials
   */
  public boolean validateUser(String userName, String password)
  {
  try
    {
      //LoginContext lc = new LoginContext("JaasConfig", new ADCallbackHandler());
      LoginContext lc = null;
      ADCallbackHandler ch = new ADCallbackHandler();
      ch.setUserId(userName);
      ch.setPassword(password);
      //lc = new LoginContext(ValidateUser.class.getName(), ch);
      lc = new LoginContext("JaasConfig", ch);
      lc.login();
      return true;
    }
    catch (LoginException le)
    {
      System.err.println("Authentication failed:");
      System.err.println("  " + le.getMessage());
      return false;
    }
    catch (NullPointerException e)
    {
    System.err.println("Authentication failed:");
    System.err.println("  " + e.getMessage());
    return false;
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    ActiveDirectoryValidator validateUser = new ActiveDirectoryValidator();
    if (validateUser.validateUser(USERNAME, PASSWORD))
    {
      System.out.print("Authentication Successful");
    }
    else
    {
      System.out.print("Authentication Failed");
    }
  }

}
