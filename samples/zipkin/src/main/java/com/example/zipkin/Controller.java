package com.example.zipkin;

import io.micrometer.tracing.Tracer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	private final Tracer tracer;

	public Controller(Tracer tracer) {
		this.tracer = tracer;
	}

	@GetMapping("/")
	public String bar() {
		return "Hello from tomcat [" + this.tracer.currentSpan().context().traceId() + "]";
	}

}
