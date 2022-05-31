/*
 * Copyright 2019-2022 the original author or authors.
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
package app.main.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;

import java.time.Instant;
import java.util.List;

@EntityListeners(AuditingListener.class)
@Entity
public class Foo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String val;
	@Enumerated
	private SomeEnum enumerated;
	@OneToOne(cascade = CascadeType.ALL)
	private Flurb flurb;
	@CreatedDate
	Instant createdAt;
	@CreatedBy
	String createdBy;
	@LastModifiedDate
	Instant modifiedAt;
	@LastModifiedBy
	String modifiedBy;

	@OrderBy
	@ElementCollection
	private List<String> phoneNumbers;

	public Foo() {
	}

	public Foo(String val) {
		this.val = val;
	}

	public String getVal() {
		return this.val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public Flurb getFlurb() {
		return flurb;
	}

	public void setFlurb(Flurb flurb) {
		this.flurb = flurb;
	}

	enum SomeEnum {
		ONE, TWO
	}

	@Override
	public String toString() {
		return String.format(
				"Foo[id=%d, value='%s']",
				id, val);
	}

}
