package com.example.schedulingtasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

// Not a spring component but uses scheduled annotation
public class ScheduledTasksNonComponent {
	
	private static final Logger log = LoggerFactory.getLogger(ScheduledTasksNonComponent.class);

	private static int i = 0;

	@Scheduled(fixedRate = 100)
	public void reportCurrentTime() {
		log.info("The number is now {}",(i++));
	}
}
