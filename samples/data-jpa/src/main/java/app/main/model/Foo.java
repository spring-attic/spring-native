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
package app.main.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import java.time.Instant;
import java.util.List;

@EntityListeners(AuditingListener.class)
@Entity
public class Foo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String value;
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

	public Foo(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
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
				id, value);
	}

}
