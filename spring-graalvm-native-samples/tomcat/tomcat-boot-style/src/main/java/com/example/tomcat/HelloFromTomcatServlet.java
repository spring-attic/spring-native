package com.example.tomcat;

import java.io.IOException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class HelloFromTomcatServlet extends GenericServlet {
	@Override
	public void service(ServletRequest req, ServletResponse res) throws IOException {
		res.getWriter().write("Hello from tomcat");
	}
}
