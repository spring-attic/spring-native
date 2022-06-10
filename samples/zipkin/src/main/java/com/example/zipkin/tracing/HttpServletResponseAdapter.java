package com.example.zipkin.tracing;

import java.util.Collection;

import io.micrometer.observation.transport.http.HttpServerRequest;
import io.micrometer.observation.transport.http.HttpServerResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Moritz Halbritter
 */
class HttpServletResponseAdapter implements HttpServerResponse {
	private final HttpServletResponse httpServletResponse;

	private final HttpServerRequest httpServerRequest;

	public HttpServletResponseAdapter(HttpServletResponse httpServletResponse, HttpServerRequest httpServerRequest) {
		this.httpServletResponse = httpServletResponse;
		this.httpServerRequest = httpServerRequest;
	}

	@Override
	public int statusCode() {
		return this.httpServletResponse.getStatus();
	}

	@Override
	public Collection<String> headerNames() {
		return this.httpServletResponse.getHeaderNames();
	}

	@Override
	public Object unwrap() {
		return this.httpServletResponse;
	}

	@Override
	public HttpServerRequest request() {
		return this.httpServerRequest;
	}
}
