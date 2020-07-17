package org.springframework.boot;

import java.util.Properties;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

public class NativePropertiesListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		Properties props = new Properties();
		props.put("server.servlet.register-default-servlet", "false"); // Lower footprint
		props.put("spring.aop.proxy-target-class", "false"); // Not supported in native images
		props.put("spring.cloud.refresh.enabled", "false"); // Sampler is a class and can't be proxied
		props.put("spring.sleuth.async.enabled", "false"); // Too much proxy created
		environment.getPropertySources().addFirst(new PropertiesPropertySource("native", props));
	}
}