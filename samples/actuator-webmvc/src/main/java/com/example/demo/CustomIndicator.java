package com.example.demo;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomIndicator implements HealthIndicator {

	@Override
	public Health health() {
		return Health.up().build();
	}

}
