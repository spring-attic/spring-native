package org.springframework.aot.boot.actuate.web;

import java.util.function.Supplier;

import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

/**
 * A {@link ManagementContextFactory} that initializes a management context processed
 * at build time.
 *
 * @author Stephane Nicoll
 */
public class AotManagementContextFactory implements ManagementContextFactory {

	private final Supplier<ApplicationContextInitializer<GenericApplicationContext>> actuatorContextInitializer;

	private final boolean reactive;

	public AotManagementContextFactory(
			Supplier<ApplicationContextInitializer<GenericApplicationContext>> actuatorContextInitializer,
			boolean reactive) {
		this.actuatorContextInitializer = actuatorContextInitializer;
		this.reactive = reactive;
	}

	@Override
	public ConfigurableWebServerApplicationContext createManagementContext(ApplicationContext parent, Class<?>... configurationClasses) {
		ConfigurableWebServerApplicationContext actuatorContext = createContext();
		actuatorContext.setParent(parent);
		actuatorContextInitializer.get().initialize((GenericApplicationContext) actuatorContext);
		return actuatorContext;
	}

	private ConfigurableWebServerApplicationContext createContext() {
		return reactive ? new ReactiveWebServerApplicationContext() : new ServletWebServerApplicationContext();
	}

}
