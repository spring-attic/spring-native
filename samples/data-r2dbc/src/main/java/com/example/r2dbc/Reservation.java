/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.r2dbc;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

public class Reservation {

	@Id
	private Integer id;
	private String name;
	@CreatedDate
	Instant createdAt;
	@CreatedBy
	String createdBy;
	@LastModifiedDate
	Instant modifiedAt;
	@LastModifiedBy
	String modifiedBy;

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
