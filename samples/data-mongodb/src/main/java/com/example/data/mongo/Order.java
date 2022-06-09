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
package com.example.data.mongo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;

@Document
public class Order {

	private String id;

	@Indexed
	private String customerId;

	@Field("order-date")
	private Date orderDate;

	private List<LineItem> items;

	@DBRef
	private Coupon simpleRef;

//	@DBRef(lazy = true) // TODO: ClassProxies
	private Coupon coupon;

	@DBRef(lazy = true) // JdkProxy
	private PriceReduction reduction;

	@DocumentReference
	private Discount documentRef;

//	@DocumentReference(lazy = true) // TODO: ClassProxies
	Discount lazyDocumentRef;

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

	public Coupon getCoupon() {
		return coupon;
	}

	public void setCoupon(Coupon coupon) {
		this.coupon = coupon;
	}

	public void setReduction(PriceReduction reduction) {
		this.reduction = reduction;
	}

	public PriceReduction getReduction() {
		return reduction;
	}

	public Coupon getSimpleRef() {
		return simpleRef;
	}

	public void setSimpleRef(Coupon simpleRef) {
		this.simpleRef = simpleRef;
	}

	public Discount getDocumentRef() {
		return documentRef;
	}

	public void setDocumentRef(Discount documentRef) {
		this.documentRef = documentRef;
	}

	public Discount getLazyDocumentRef() {
		return lazyDocumentRef;
	}

	public void setLazyDocumentRef(Discount lazyDocumentRef) {
		this.lazyDocumentRef = lazyDocumentRef;
	}

	@Override
	public String toString() {
		return "Order{" +
				"id='" + id + '\'' +
				", customerId='" + customerId + '\'' +
				", orderDate=" + orderDate +
				", items=" + items +
				", coupon=" + coupon +
				", reduction=" + reduction +
				", createdAt=" + createdAt +
				", createdBy=" + createdBy +
				", modifiedAt=" + modifiedAt +
				", modifiedBy=" + modifiedBy +
				'}';
	}

	// Reproducer for https://github.com/spring-projects-experimental/spring-native/issues/1376
	public static List<IndexDefinition> getIndexes(IndexResolver indexResolver) {
		List<IndexDefinition> ret = new ArrayList<>();
		indexResolver.resolveIndexFor(Order.class).forEach(ret::add);
		ret.add(new Index()
				.unique()
				.on("createdBy", Sort.Direction.ASC)
				.partial(PartialIndexFilter.of(Criteria.where("createdBy").exists(true))));
				return ret;
		}
}
