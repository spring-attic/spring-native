package com.example.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class HelloEventListener2 {

    @EventListener
    public void processHelloEvent(HelloEvent event) {
        System.out.println("EL: Received hello event: "+event.getPerson());
    }
    
}