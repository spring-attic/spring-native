package com.example.integration;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "controlBus.input")
public interface ControlBusGateway {

	void startEndpoint(String id);

}
