package com.example.rsocket;

import java.time.Instant;

public class Message {
	private String origin;
	private String interaction;
	private long index;
	private long created = Instant.now().getEpochSecond();

	public Message() {
	}

	public Message(String origin, String interaction) {
		this.origin = origin;
		this.interaction = interaction;
		this.index = 0;
	}

	public Message(String origin, String interaction, long index) {
		this.origin = origin;
		this.interaction = interaction;
		this.index = index;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getInteraction() {
		return interaction;
	}

	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}

	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}
}