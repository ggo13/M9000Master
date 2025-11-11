package com.usi.m9000.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		File r;
		FileReader fr;
		BufferedReader br;
		try {
			r = new File(req.getParameter("filename"));
			fr = new FileReader(r);
			br = new BufferedReader(fr);
			if (!r.isFile()) { // Must be a directory or something
			} else {
				resp.sendError(resp.SC_NOT_FOUND);
				return;
			}
		} catch (FileNotFoundException e) {
			resp.sendError(resp.SC_NOT_FOUND);
			return;
		} catch (SecurityException se) { // Be unavailable permanently
			throw (new UnavailableException(this,
					"Servlet lacks appropriate privileges."));
		}
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		String text;
		while ((text = br.readLine()) != null)
			out.println(text);
		br.close();
	}
}
