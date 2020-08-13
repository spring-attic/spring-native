/*
 * Copyright 2013-2018 the original author or authors.
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

import org.springframework.data.annotation.PersistenceConstructor;

public class LineItem {

	private final String caption;
	private final double price;

	int quantity = 1;

	public LineItem(String caption, double price) {

		this.caption = caption;
		this.price = price;
	}

	@PersistenceConstructor
	public LineItem(String caption, double price, int quantity) {

		this(caption, price);
		this.quantity = quantity;
	}

	public String getCaption() {
		return caption;
	}

	public double getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "LineItem{" +
				"caption='" + caption + '\'' +
				", price=" + price +
				", quantity=" + quantity +
				'}';
	}
}
