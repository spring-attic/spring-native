package com.example.webflux;

import org.springframework.data.annotation.Id;

public class Reservation {

	@Id
	private Integer id;
	private String name;

	public String toString() {
		return "Reservation(id="+id+",name="+name+")";
	}

	@Override
	public int hashCode() {
		return id * 37 + name.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof Reservation) {
			Reservation that = (Reservation) o;
			return this.id == that.id && this.name.equals(that.name);
		}
		return false;
	}

	public Reservation(Integer id, String name) {
		this.id = id;
		this.name = name;

	}

	public Reservation() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
