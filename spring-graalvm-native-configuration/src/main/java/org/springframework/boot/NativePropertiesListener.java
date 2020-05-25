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
		props.put("server.servlet.register-default-servlet", "false");
		props.put("spring.aop.proxy-target-class", "false");
		environment.getPropertySources().addFirst(new PropertiesPropertySource("native", props));
	}
}