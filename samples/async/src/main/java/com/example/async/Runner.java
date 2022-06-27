package com.example.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class Runner {

    @Async
    public void run() {
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
        }
        System.out.println(CLR.prefix.get() + "Asynchronous action running...");
    }

}
