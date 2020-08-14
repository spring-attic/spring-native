package app.main;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleConfiguration {

	@Value("${app.value}")
	private String message;

	@Bean
	public Foo foo() {
		return new Foo("foo");
	}

	@Bean
	public Bar bar(Foo foo) {
		return new Bar(foo);
	}

	@Bean
	public CommandLineRunner runner(Bar bar, ConfigurableListableBeanFactory beans) {
		return args -> {
			System.out.println("Message: " + message);
			System.out.println("Bar: " + bar);
			System.out.println("Foo: " + bar.getFoo());
			System.err.println("Class count: " + ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
			System.err.println("Bean count: " + beans.getBeanDefinitionNames().length);
			System.err.println("Bean names: " + Arrays.asList(beans.getBeanDefinitionNames()));
		};
	}

}
