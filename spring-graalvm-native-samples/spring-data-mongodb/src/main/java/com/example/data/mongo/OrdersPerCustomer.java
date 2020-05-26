/*
 * Copyright 2019 the original author or authors.
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

import org.springframework.data.annotation.Id;

/**
 * @author Christoph Strobl
 */
public class OrdersPerCustomer {

	@Id //
	private String customerId;
	private Long total;

	public OrdersPerCustomer(String customerId, Long total) {

		this.customerId = customerId;
		this.total = total;
	}

	public String getCustomerId() {
		return customerId;
	}

	public Long getTotal() {
		return total;
	}

	@Override
	public String toString() {
		return "OrdersPerCustomer{" +
				"customerId='" + customerId + '\'' +
				", total=" + total +
				'}';
	}
}
