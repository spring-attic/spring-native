/*
 * Copyright 2013-2021 the original author or authors.
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
package com.example.data.cassandra;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("orders")
public class Order {

	@PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
	private String id;

	@PrimaryKeyColumn(ordinal = 1)
	@Indexed
	private String customerId;

	@Column("order_date")
	private Date orderDate;

	private List<LineItem> items;

	@CreatedDate
	Instant createdAt;
	@CreatedBy
	String createdBy;
	@LastModifiedDate
	Instant modifiedAt;
	@LastModifiedBy
	String modifiedBy;

	protected Order() {
	}

	protected Order(String customerId) {
		this(customerId, null);
	}

	@PersistenceConstructor
	public Order(String id, String customerId, Date orderDate, List<LineItem> items) {

		this.id = id;
		this.customerId = customerId;
		this.orderDate = orderDate;
		this.items = items;
	}

	/**
	 * Creates a new {@link Order} for the given customer id and order date.
	 *
	 * @param customerId {@link String ID} of the {@literal customer}.
	 * @param orderDate {@link Date} of this {@link Order}.
	 */
	public Order(String customerId, Date orderDate) {
		this(null, customerId, orderDate, new ArrayList<>());
	}

	/**
	 * Adds a {@link LineItem} to the {@link Order}.
	 *
	 * @param item {@link LineItem Item} ordered.
	 * @return this {@link Order}.
	 */
	public Order addItem(LineItem item) {

		this.items.add(item);
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public List<LineItem> getItems() {
		return items;
	}

	public void setItems(List<LineItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "Order{" +
			   "id='" + id + '\'' +
			   ", customerId='" + customerId + '\'' +
			   ", orderDate=" + orderDate +
			   ", items=" + items +
			   ", createdAt=" + createdAt +
			   ", createdBy=" + createdBy +
			   ", modifiedAt=" + modifiedAt +
			   ", modifiedBy=" + modifiedBy +
			   '}';
	}

}
