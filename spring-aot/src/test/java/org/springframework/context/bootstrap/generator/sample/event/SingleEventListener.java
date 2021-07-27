package org.springframework.context.bootstrap.generator.sample.event;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

public class SingleEventListener {

	@EventListener
	public void onStartup(ApplicationStartedEvent event) {

	}

}
