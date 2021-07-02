package com.example.schedulingtasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasksComponent {
	
	private static final Logger log = LoggerFactory.getLogger(ScheduledTasksComponent.class);

	private static int i = 0;

	@Scheduled(fixedRate = 100)
	public void reportCurrentTime() {
		log.info("The other number is now {}",(i++));
	}
}
