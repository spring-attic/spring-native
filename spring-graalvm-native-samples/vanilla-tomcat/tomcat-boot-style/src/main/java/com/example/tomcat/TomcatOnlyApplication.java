package com.example.tomcat;

import com.example.tomcat.embed.LoaderHidingResourceRoot;
import com.example.tomcat.embed.TldSkipPatterns;
import com.example.tomcat.embed.TomcatEmbeddedContext;
import com.example.tomcat.embed.TomcatEmbeddedWebappClassLoader;
import com.example.tomcat.embed.TomcatWebServer;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.scan.StandardJarScanFilter;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

import static com.example.tomcat.embed.Utils.collectionToDelimitedString;
import static org.apache.tomcat.util.buf.ByteChunk.DEFAULT_CHARSET;

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
		connector.setThrowOnFailure(true);
		connector.setPort(8080);
		tomcat.getService().addConnector(connector);
		tomcat.setConnector(connector);
		tomcat.getHost().setAutoDeploy(false);

		TomcatEmbeddedContext context = new TomcatEmbeddedContext();
		context.setResources(new LoaderHidingResourceRoot(context));
		context.setName("ROOT");
		context.setDisplayName("sample-tomcat-context");
		context.setPath("");
		context.setDocBase(docBase.getAbsolutePath());
		context.addLifecycleListener(new Tomcat.FixContextListener());
		context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
		context.addLocaleEncodingMappingParameter(Locale.ENGLISH.toString(), DEFAULT_CHARSET.displayName());
		context.addLocaleEncodingMappingParameter(Locale.FRENCH.toString(), DEFAULT_CHARSET.displayName());
		context.setUseRelativeRedirects(false);
		try {
			context.setCreateUploadTargets(true);
		}
		catch (NoSuchMethodError ex) {
			// Tomcat is < 8.5.39. Continue.
		}
		StandardJarScanFilter filter = new StandardJarScanFilter();
		filter.setTldSkip(collectionToDelimitedString(TldSkipPatterns.DEFAULT, ",", "", ""));
		context.getJarScanner().setJarScanFilter(filter);
		WebappLoader loader = new WebappLoader(context.getParentClassLoader());
		loader.setLoaderClass(TomcatEmbeddedWebappClassLoader.class.getName());
		loader.setDelegate(true);
		context.setLoader(loader);

		Wrapper helloServlet = context.createWrapper();
		String servletName = HelloFromTomcatServlet.class.getSimpleName();
		helloServlet.setName(servletName);
		helloServlet.setServletClass(HelloFromTomcatServlet.class.getName());
		helloServlet.setLoadOnStartup(1);
		helloServlet.setOverridable(true);
		context.addChild(helloServlet);
		context.addServletMappingDecoded("/", servletName);

		tomcat.getHost().addChild(context);
		tomcat.getHost().setAutoDeploy(false);
		TomcatWebServer server = new TomcatWebServer(tomcat);
		server.start();
	}


}
