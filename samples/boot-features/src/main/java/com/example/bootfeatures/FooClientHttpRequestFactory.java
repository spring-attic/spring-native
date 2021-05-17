package com.example.bootfeatures;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequestFactoryWrapper;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class FooClientHttpRequestFactory extends AbstractClientHttpRequestFactoryWrapper {

	protected FooClientHttpRequestFactory() {
		super(new SimpleClientHttpRequestFactory());
	}

	@Override
	protected ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod, ClientHttpRequestFactory clientHttpRequestFactory) throws IOException {
		return clientHttpRequestFactory.createRequest(uri, httpMethod);
	}
}
