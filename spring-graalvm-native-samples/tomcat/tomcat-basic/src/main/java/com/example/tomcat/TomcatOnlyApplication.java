package com.example.tomcat;

import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.modeler.Registry;

import java.io.File;
import java.util.UUID;

public class TomcatOnlyApplication {

	static File tomcatBase = new File(
			System.getProperty("java.io.tmpdir"),
			TomcatOnlyApplication.class.getSimpleName() + "-" + UUID.randomUUID().toString()
	);
	static File docBase = new File(tomcatBase, "webapps");
	static File serverBase = new File(tomcatBase, "server");

	public static void main(String... args) throws Exception {
		Registry.disableRegistry();
		tomcatBase.mkdir();
		docBase.mkdir();
		serverBase.mkdir();

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(serverBase.getAbsolutePath());
		Connector connector = new Connector(Http11NioProtocol.class.getName());
		connector.setPort(8080);
		tomcat.setConnector(connector);

		StandardContext context = new StandardContext();
		context.setName("");
		context.setPath("");
		context.setDocBase(docBase.getAbsolutePath());
		context.addLifecycleListener(new Tomcat.FixContextListener());

		Wrapper helloServlet = context.createWrapper();
		String servletName = HelloFromTomcatServlet.class.getSimpleName();
		helloServlet.setName(servletName);
		helloServlet.setServlet(new HelloFromTomcatServlet());
		helloServlet.setLoadOnStartup(1);
		helloServlet.setOverridable(true);
		context.addChild(helloServlet);

		context.addServletMappingDecoded("/", servletName);
		context.addServletMappingDecoded("/*", HelloFromTomcatServlet.class.getSimpleName());
		StandardHost host = (StandardHost) tomcat.getHost();
		host.setAutoDeploy(false);
		host.getPipeline().addValve(new ErrorReportValve());
		host.addChild(context);
		tomcat.start();
	}

}
