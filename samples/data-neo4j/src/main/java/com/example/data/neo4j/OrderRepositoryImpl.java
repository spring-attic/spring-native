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
package com.example.data.neo4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

/**
 * The manual implementation parts for {@link OrderRepository}. This will automatically be picked up by the Spring Data
 * infrastructure as we follow the naming convention of extending the core repository interface's name with {@code Impl}
 * .
 *
 * @author Thomas Darimont
 * @author Oliver Gierke
 * @author Michael J. Simons
 */
@Indexed
@Component
class OrderRepositoryImpl implements OrderRepositoryCustom {

	private final Neo4jTemplate template;

	private double taxRate = 0.19;

	public OrderRepositoryImpl(Neo4jTemplate template) {
		this.template = template;
	}

	@Override
	public Invoice getInvoiceFor(Order order) {
		String query = ""
			+ "MATCH (n:Order) - [r:HAS] -> (l:LineItem) "
			+ "WHERE n.id = $id "
			+ "WITH n, sum(l.price * l.quantity) AS netAmount, collect(r) as hasRel, collect(l) as lineItems "
			+ "RETURN {"
			+ " netAmount: netAmount,"
			+ "	taxAmount: netAmount * $taxRate,"
			+ " totalAmount: netAmount * (1+$taxRate),"
			+ " hasRel: hasRel, lineItems: lineItems"
			+ "}";

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", order.getId());
		parameters.put("taxRate", taxRate);

		return template.findOne(query, parameters, Invoice.class).get();
	}
}
