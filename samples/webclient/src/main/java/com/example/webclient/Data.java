package com.example.webclient;

import java.util.List;

public class Data {

	private String url;

	private String origin;

	private String method;

	public Data() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@java.lang.Override
	public java.lang.String toString() {
		return "Data{" +
				"url='" + url + '\'' +
				", origin='" + origin + '\'' +
				", method='" + method + '\'' +
				'}';
	}
}
