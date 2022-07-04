package com.example.websocket.stomp.dto;

public class HelloMessage {

	private String name;

	public HelloMessage() {
	}

	public HelloMessage(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "HelloMessage{" + "name='" + name + '\'' + '}';
	}

}
