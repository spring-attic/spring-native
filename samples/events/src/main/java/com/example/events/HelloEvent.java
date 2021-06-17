package com.example.events;

public class HelloEvent {
    String person;

    public HelloEvent(String person) {
        this.person = person;
    }
    
    public String getPerson() {
        return person;
    }
    
}
