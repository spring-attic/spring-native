package com.example.validator;

import javax.validation.Validator;
import javax.validation.constraints.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@EnableConfigurationProperties(Foo.class)
public class ValidatorApplication {
	
	public ValidatorApplication(Validator validator, Foo foo) throws InterruptedException {
		System.err.println("Valid: " + validator.validate(foo));
		Thread.sleep(Integer.parseInt(System.getProperty("delay","5"))*1000);
	}

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ValidatorApplication.class, args);
	}

	@Bean
	public static BeanDefinitionRegistryPostProcessor ripper() {
		return new BeanDefinitionRegistryPostProcessor() {

			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			}

			@Override
			public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
				if (registry.containsBeanDefinition("methodValidationPostProcessor")) {
					registry.removeBeanDefinition("methodValidationPostProcessor");
				}
			}

		};
	}

}

@ConfigurationProperties("app")
@Validated
class Foo {
	@Pattern(regexp = "[A-Z][a-z]+", message = "Invalid lastname")
	private String value;
	
	public Foo() {
	}

	public Foo(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Foo [value=" + this.value + "]";
	}
}