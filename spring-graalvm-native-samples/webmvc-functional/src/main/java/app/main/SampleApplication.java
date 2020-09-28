package app.main;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@SpringBootConfiguration
@ImportAutoConfiguration({ PropertyPlaceholderAutoConfiguration.class, ConfigurationPropertiesAutoConfiguration.class,
		ServletWebServerFactoryAutoConfiguration.class, WebMvcAutoConfiguration.class,
		DispatcherServletAutoConfiguration.class, ErrorMvcAutoConfiguration.class })
@ComponentScan
public class SampleApplication {

	@Bean
	public RouterFunction<ServerResponse> userEndpoints() {
		return route().GET("/", req -> ok().body("Hello")).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class,args);
	}

}