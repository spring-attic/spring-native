/*
 * Copyright 2014-2018 the original author or authors.
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
package com.example.data.rest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.math.BigDecimal;

import org.springframework.util.ObjectUtils;

/**
 * @author Oliver Gierke
 */
@Entity
public class LineItem {

	private @GeneratedValue @Id Long id;
	private final String description;
	private final BigDecimal price;

	LineItem() {
		this.description = null;
		this.price = null;
	}

	public LineItem(String description, BigDecimal price) {

		this.description = description;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LineItem lineItem = (LineItem) o;

		return ObjectUtils.nullSafeEquals(id, lineItem.id);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(id);
	}

	@Override
	public String toString() {
		return "LineItem{" +
				"id=" + id +
				", description='" + description + '\'' +
				", price=" + price +
				'}';
	}
}
