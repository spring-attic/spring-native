package com.example.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
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

		Context context = tomcat.addContext("", docBase.getAbsolutePath());
		tomcat.addServlet(context, HelloFromTomcatServlet.class.getSimpleName(), new HelloFromTomcatServlet());
		context.addServletMappingDecoded("/*", HelloFromTomcatServlet.class.getSimpleName());
		tomcat.getHost().setAutoDeploy(false);
		tomcat.start();
	}

}
