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
package org.springframework.nativex.type.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

/**
 * @author Christoph Strobl
 */
@Entity // annotation on entity
public class Order {

	@GeneratedValue
	private final Long id; // simple value

	@SomeAnnotation // annotation with meta annotation @Nullable
	String name;

	@OneToMany // javax.persistence annotation
	List<LineItem> itemList; // List and element type with

	public Order() {
		this(null);
	}

	public Order(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public List<LineItem> getItemList() {
		return itemList;
	}
	
	public Date getOrderDate() { // capture method return type
		return null;
	}
}
