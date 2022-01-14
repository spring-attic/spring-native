/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.sample.data.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(void.class)
public class EntityWithEmptyIdClass {

	@Id
	private Long f1;

	@Id
	private Long f2;

	public Long getF1() {
		return f1;
	}

	public void setF1(Long f1) {
		this.f1 = f1;
	}

	public Long getF2() {
		return f2;
	}

	public void setF2(Long f2) {
		this.f2 = f2;
	}
}
