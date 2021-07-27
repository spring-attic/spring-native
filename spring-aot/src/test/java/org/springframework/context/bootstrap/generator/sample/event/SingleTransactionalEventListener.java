package org.springframework.context.bootstrap.generator.sample.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.transaction.event.TransactionalEventListener;

public class SingleTransactionalEventListener {

	@TransactionalEventListener
	public void onEvent(ApplicationEvent event) {

	}

}
