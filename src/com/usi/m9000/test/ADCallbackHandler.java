package com.usi.m9000.test;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Copyright Alvin Alexander, http://devdaily.com.
 * This code is shared here under the Attribution 3.0 Unported License.
 * See this URL for details: http://creativecommons.org/licenses/by/3.0/
 *
 * This is an implementation of CallbackHandler to pass credentials to ActiveDirectoryValidator.java.
 * See JAAS documentation for more info.
 */
public class ADCallbackHandler implements CallbackHandler
{
  private String ADUserId;
  private char[] ADPassword;

  public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException
  {
    for (int i = 0; i < callbacks.length; i++)
    {
      if (callbacks[i] instanceof NameCallback)
      {
        NameCallback cb = (NameCallback)callbacks[i];
        cb.setName(ADUserId);
      }
      else if (callbacks[i] instanceof PasswordCallback)
      {
        PasswordCallback cb = (PasswordCallback)callbacks[i];
        cb.setPassword(ADPassword);
      }
      else
      {
        throw new UnsupportedCallbackException(callbacks[i]);
      }
    }
  }

  public void setUserId(String userid)
  {
    ADUserId = userid;
  }

  public void setPassword(String password)
  {
    ADPassword = new char[password.length()];
    password.getChars(0, ADPassword.length, ADPassword, 0);
  }

  public static void main(String[] args) throws IOException, UnsupportedCallbackException
  {
    // Test handler
    ADCallbackHandler ch = new ADCallbackHandler();
    ch.setUserId("test");
    ch.setPassword("test");

    Callback[] callbacks = new Callback[] { new NameCallback("user id:"), new PasswordCallback("password:", true) };

    ch.handle(callbacks);
  }
}