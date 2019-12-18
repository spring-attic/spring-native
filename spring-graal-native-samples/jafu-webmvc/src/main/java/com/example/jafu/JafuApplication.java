package com.example.jafu;

import java.util.Arrays;

import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.context.MessageSourceInitializer;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ResourceConverterInitializer;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerInitializer;
import org.springframework.boot.autoconfigure.web.servlet.StringConverterInitializer;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctionDsl;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

public class JafuApplication {

	private final ApplicationContextInitializer<GenericApplicationContext> initializer;

	private ServerProperties serverProperties = new ServerProperties();

	private HttpProperties httpProperties = new HttpProperties();

	private WebMvcProperties webMvcProperties = new WebMvcProperties();

	private ResourceProperties resourceProperties = new ResourceProperties();

	protected JafuApplication() {
		this.initializer = context -> {
			new MessageSourceInitializer().initialize(context);
			context.registerBean(CommandLineRunner.class, () -> args -> System.out.println("jafu running!"));
			context.registerBean(BeanDefinitionReaderUtils.uniqueBeanName(RouterFunctionDsl.class.getName(), context), RouterFunction.class, () ->
				RouterFunctions.route().GET("/", request -> ServerResponse.ok().body("Hello")).build());
			serverProperties.setPort(8080);
			new StringConverterInitializer().initialize(context);
			new ResourceConverterInitializer().initialize(context);
			new ServletWebServerInitializer(serverProperties, httpProperties, webMvcProperties, resourceProperties).initialize(context);
		};
	}

	public ConfigurableApplicationContext run() {
		return run("", new String[0]);
	}

	public ConfigurableApplicationContext run(String profiles) {
		return run(profiles, new String[0]);
	}

	public ConfigurableApplicationContext run(String[] args) {
		return run("", args);
	}

	public ConfigurableApplicationContext run(String profiles, String[] args) {
		SpringApplication app = new SpringApplication(JafuApplication.class) {
			@Override
			protected void load(ApplicationContext context, Object[] sources) {
				// We don't want the annotation bean definition reader
			}
		};
		initializeWebApplicationContext(app);
		if (!profiles.isEmpty()) {
			app.setAdditionalProfiles(Arrays.stream(profiles.split(",")).map(it -> it.trim()).toArray(String[]::new));
		}
		app.addInitializers(this.initializer);
		System.setProperty("spring.backgroundpreinitializer.ignore", "true");
		return app.run(args);
	}

	protected void initializeWebApplicationContext(SpringApplication app) {
		app.setWebApplicationType(WebApplicationType.SERVLET);
		app.setApplicationContextClass(ServletWebServerApplicationContext.class);
	}

	public static void main(String[] args) throws InterruptedException {
		new JafuApplication().run(args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
