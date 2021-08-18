package com.example.events;

import org.springframework.aot.SpringAotApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.TypeHint;

// @AotProxyHint(targetClass=com.example.events.SamplePublisher.class, proxyFeatures = ProxyBits.IS_STATIC)
@SpringBootApplication
public class EventApplication {
	
	public static void main(String[] args) throws InterruptedException {
		SpringAotApplication.run(EventApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
