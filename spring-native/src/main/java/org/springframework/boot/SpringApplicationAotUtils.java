package org.springframework.boot;

import java.lang.reflect.InvocationTargetException;

import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.NativeDetector;

/**
 * Additional methods and fields required for {@link SpringApplication} substitution.
 *
 * @author Sebastien Deleuze
 */
class SpringApplicationAotUtils {

	public static final boolean SPRING_AOT = "true".equals(System.getProperty("springAot"))
			|| NativeDetector.inNativeImage();

	public static ApplicationContextFactory AOT_FACTORY = (webApplicationType) -> {
		try {
			switch (webApplicationType) {
				case SERVLET:
					return new ServletWebServerApplicationContext();
				case REACTIVE:
					return new ReactiveWebServerApplicationContext();
				default:
					return new GenericApplicationContext();
			}
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable create an AOT ApplicationContext instance, "
					+ "you may need a custom ApplicationContextFactory", ex);
		}
	};

	public static ApplicationContextInitializer<?> getBootstrapInitializer() {
		try {
			return (ApplicationContextInitializer<?>) Class.forName("org.springframework.aot.ContextBootstrapInitializer").getDeclaredConstructor().newInstance();
		}
		catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

}
