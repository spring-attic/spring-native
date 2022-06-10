package com.example.zipkin.tracing;

import java.io.IOException;

import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Scope;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.http.context.HttpServerContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Moritz Halbritter
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TracingFilter implements Filter {
	private final ObservationRegistry observationRegistry;

	TracingFilter(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServerContext context = new HttpServerContext();
		context.setRequest(new HttpServletRequestAdapter((HttpServletRequest) servletRequest));
		Observation observation = Observation.start("request", context, this.observationRegistry);

		try (Scope ignored = observation.openScope()) {
			filterChain.doFilter(servletRequest, servletResponse);
		}
		catch (RuntimeException | IOException | ServletException e) {
			observation.error(e);
			throw e;
		}
		finally {
			context.setResponse(new HttpServletResponseAdapter((HttpServletResponse) servletResponse, context.getRequest()));
			observation.stop();
		}
	}
}
