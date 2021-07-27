package org.springframework.context.bootstrap.generator.sample.event;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class AnotherEventListener {

	@EventListener(ContextRefreshedEvent.class)
	public void onRefresh() {

	}

}
