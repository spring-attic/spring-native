package com.example.messages;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;

public class MessagesApplication {

	public static void main(String[] args) throws InterruptedException {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("messages/messages");
		System.err.println(messageSource.getMessage("welcome", new Object[0], Locale.UK));
		ClassPathResource resource = new ClassPathResource("META-INF/resources/webjars/jquery/2.2.4/jquery.js");
		System.err.println(resource.exists());
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

}
