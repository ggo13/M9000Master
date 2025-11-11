package com.usi.m9000.test;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Login extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

public Login() {
super();
}

protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

final String SUCCESS = "Success.html";
final String FAILURE = "Failure.html";
String strUrl = "login.html";
String username = request.getParameter("username");
String password = request.getParameter("password");

Hashtable env = new Hashtable(11);

boolean b = false;

env.put(Context.INITIAL_CONTEXT_FACTORY,
"com.sun.jndi.ldap.LdapCtxFactory");
env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
env.put(Context.SECURITY_AUTHENTICATION, "simple");
env.put(Context.SECURITY_PRINCIPAL, "uid="+ username +",ou=system");
env.put(Context.SECURITY_CREDENTIALS, password);

try {
// Create initial context
DirContext ctx = new InitialDirContext(env);

// Close the context when we're done
b = true;
ctx.close();

} catch (NamingException e) {
b = false;
}finally{
if(b){
System.out.print("Success");
strUrl = SUCCESS;
}else{
System.out.print("Failure");
strUrl = FAILURE;
}
}
RequestDispatcher rd = request.getRequestDispatcher(strUrl);
rd.forward(request, response);

}

protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
processRequest(request,response);
}

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
processRequest(request,response);
} 
}
