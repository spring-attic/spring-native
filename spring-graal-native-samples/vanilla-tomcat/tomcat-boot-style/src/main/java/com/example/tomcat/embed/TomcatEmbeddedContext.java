package com.example.tomcat.embed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.session.ManagerBase;

/**
 * Tomcat {@link StandardContext} used by {@link TomcatWebServer} to support deferred
 * initialization.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public class TomcatEmbeddedContext extends StandardContext {

	private TomcatStarter starter;

	@Override
	public boolean loadOnStartup(Container[] children) {
		// deferred until later (see deferredLoadOnStartup)
		return true;
	}

	@Override
	public void setManager(Manager manager) {
		if (manager instanceof ManagerBase) {
			manager.setSessionIdGenerator(new LazySessionIdGenerator());
		}
		super.setManager(manager);
	}

	void deferredLoadOnStartup() throws LifecycleException {
		doWithThreadContextClassLoader(getLoader().getClassLoader(),
				() -> getLoadOnStartupWrappers(findChildren()).forEach(this::load));
	}

	private Stream<Wrapper> getLoadOnStartupWrappers(Container[] children) {
		Map<Integer, List<Wrapper>> grouped = new TreeMap<>();
		for (Container child : children) {
			Wrapper wrapper = (Wrapper) child;
			int order = wrapper.getLoadOnStartup();
			if (order >= 0) {
				grouped.computeIfAbsent(order, (o) -> new ArrayList<>()).add(wrapper);
			}
		}
		return grouped.values().stream().flatMap(List::stream);
	}

	private void load(Wrapper wrapper) {
		try {
			wrapper.load();
		}
		catch (ServletException ex) {
			String message = sm.getString("standardContext.loadOnStartup.loadException", getName(), wrapper.getName());
			if (getComputedFailCtxIfServletStartFails()) {
				throw new RuntimeException(message, ex);
			}
			getLogger().error(message, StandardWrapper.getRootCause(ex));
		}
	}

	/**
	 * Some older Servlet frameworks (e.g. Struts, BIRT) use the Thread context class
	 * loader to create servlet instances in this phase. If they do that and then try to
	 * initialize them later the class loader may have changed, so wrap the call to
	 * loadOnStartup in what we think its going to be the main webapp classloader at
	 * runtime.
	 * @param classLoader the class loader to use
	 * @param code the code to run
	 */
	private void doWithThreadContextClassLoader(ClassLoader classLoader, Runnable code) {
		ClassLoader existingLoader = (classLoader != null) ? overrideThreadContextClassLoader(classLoader)
				: null;
		try {
			code.run();
		}
		finally {
			if (existingLoader != null) {
				overrideThreadContextClassLoader(existingLoader);
			}
		}
	}

	static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
		Thread currentThread = Thread.currentThread();
		ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
		if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
			currentThread.setContextClassLoader(classLoaderToUse);
			return threadContextClassLoader;
		} else {
			return null;
		}
	}

	void setStarter(TomcatStarter starter) {
		this.starter = starter;
	}

	TomcatStarter getStarter() {
		return this.starter;
	}

}
