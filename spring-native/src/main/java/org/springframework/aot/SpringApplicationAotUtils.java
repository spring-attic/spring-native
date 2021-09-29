package org.springframework.aot;

import java.lang.reflect.InvocationTargetException;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Additional methods and fields required for {@link SpringApplication} substitution.
 *
 * @author Sebastien Deleuze
 */
public class SpringApplicationAotUtils {

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
