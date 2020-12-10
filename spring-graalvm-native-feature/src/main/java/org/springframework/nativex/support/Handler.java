package org.springframework.nativex.support;

import org.springframework.nativex.type.TypeSystem;

public class Handler {

	protected ConfigurationCollector collector;

	protected TypeSystem ts;

	Handler(ConfigurationCollector collector) {
		this.collector = collector;
	}
	
	public void setTypeSystem(TypeSystem ts) {
		this.ts = ts;
	}
}
