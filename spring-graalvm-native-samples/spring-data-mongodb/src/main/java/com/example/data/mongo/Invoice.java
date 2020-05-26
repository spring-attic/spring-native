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

import java.util.List;

public class Invoice {

	private final String orderId;
	private final double taxAmount;
	private final double netAmount;
	private final double totalAmount;
	private final List<LineItem> items;

	public Invoice(String orderId, double taxAmount, double netAmount, double totalAmount, List<LineItem> items) {

		this.orderId = orderId;
		this.taxAmount = taxAmount;
		this.netAmount = netAmount;
		this.totalAmount = totalAmount;
		this.items = items;
	}

	public String getOrderId() {
		return orderId;
	}

	public double getTaxAmount() {
		return taxAmount;
	}

	public double getNetAmount() {
		return netAmount;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public List<LineItem> getItems() {
		return items;
	}

	@Override
	public String toString() {
		return "Invoice{" +
				"orderId='" + orderId + '\'' +
				", taxAmount=" + taxAmount +
				", netAmount=" + netAmount +
				", totalAmount=" + totalAmount +
				", items=" + items +
				'}';
	}
}
