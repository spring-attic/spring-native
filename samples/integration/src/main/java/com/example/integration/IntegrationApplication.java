package com.example.integration;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.config.IntegrationConverter;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@SpringBootApplication
@EnableMessageHistory("dateChannel")
@EnableIntegrationManagement
@EnableIntegrationGraphController("/integration-graph")
public class IntegrationApplication {

	public static void main(String[] args) throws InterruptedException {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(IntegrationApplication.class, args);
		applicationContext.getBean("dateSourceEndpoint", SourcePollingChannelAdapter.class).start();

		Thread.sleep(1000);

		String integrationGraph =
				WebClient.create()
						.get()
						.uri("http://localhost:8080/integration-graph")
						.accept(MediaType.APPLICATION_JSON)
						.retrieve()
						.bodyToMono(String.class)
						.block(Duration.ofSeconds(10));

		System.out.println("CURRENT INTEGRATION GRAPH:\n" + integrationGraph);
	}

	@Bean
	MeterRegistry simpleMeterRegistry() {
		return new SimpleMeterRegistry();
	}

	@Bean
	IntegrationFlow printFormattedSecondsFlow() {
		return IntegrationFlows
				.fromSupplier(Date::new,
						e -> e.id("dateSourceEndpoint")
								.poller(p -> p.fixedDelay(1000, 1000)))
				.channel("dateChannel")
				.gateway(subflow -> subflow.convert(Integer.class,
						e -> e.advice(new RequestHandlerRetryAdvice())))
				.handle(m -> System.out.println("Current seconds: " + m.getPayload()))
				.get();
	}

	@Bean
	@GlobalChannelInterceptor(patterns = "dateChannel")
	WireTap loggingWireTap() {
		return new WireTap("loggingChannel");
	}

	@Bean
	@ServiceActivator(inputChannel = "loggingChannel")
	MessageHandler loggingHandler() {
		LoggingHandler loggingHandler = new LoggingHandler(LoggingHandler.Level.TRACE);
		loggingHandler.setLoggerName("tracing.data");
		return loggingHandler;
	}

	@Bean
	@IntegrationConverter
	Converter<Date, Integer> currentSeconds() {
		return new Converter<Date, Integer>() { // Not lambda for generic info presence

			@Override
			public Integer convert(Date date) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				return calendar.get(Calendar.SECOND);
			}

		};
	}

}
