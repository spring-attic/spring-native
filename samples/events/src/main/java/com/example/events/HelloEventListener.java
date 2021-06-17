package com.example.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class HelloEventListener {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void processHelloEvent(HelloEvent event) {
        System.out.println("TEL: Received hello event: "+event.getPerson());
    }
    
}