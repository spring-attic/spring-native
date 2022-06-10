package com.example.zipkin.tracing;

import java.util.Collection;
import java.util.Collections;

import io.micrometer.observation.transport.http.HttpServerRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Moritz Halbritter
 */
class HttpServletRequestAdapter implements HttpServerRequest {
	private final HttpServletRequest httpServletRequest;

	public HttpServletRequestAdapter(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	@Override
	public String method() {
		return httpServletRequest.getMethod();
	}

	@Override
	public String path() {
		return httpServletRequest.getRequestURI();
	}

	@Override
	public String url() {
		return httpServletRequest.getRequestURL().toString();
	}

	@Override
	public String header(String name) {
		return httpServletRequest.getHeader(name);
	}

	@Override
	public Collection<String> headerNames() {
		return Collections.list(httpServletRequest.getHeaderNames());
	}

	@Override
	public Object unwrap() {
		return httpServletRequest;
	}
}
