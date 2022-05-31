package com.example.events;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SamplePublisher {
    @Autowired
    private ApplicationEventPublisher publisher;
    
    @Transactional
    public void publishCustomEvent() {
        System.out.println("Publishing event (transactional)");
        HelloEvent event = new HelloEvent("andy");
        publisher.publishEvent(event);
    }
    
    public void nonTransactionalPublishCustomEvent() {
        System.out.println("Publishing event (non-transactional)");
        HelloEvent event = new HelloEvent("sebastien");
        publisher.publishEvent(event);
    }
    
}
