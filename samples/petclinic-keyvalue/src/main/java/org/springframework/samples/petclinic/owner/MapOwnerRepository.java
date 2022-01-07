/*
 * Copyright 2012-2019 the original author or authors.
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
package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.stereotype.Component;

@Component
class MapOwnerRepository implements OwnerRepository {

	private final KeyValueTemplate template;

	public MapOwnerRepository(KeyValueTemplate template) {
		this.template = template;
	}

	public List<PetType> findPetTypes() {
		List<PetType> result = new ArrayList<>();
		template.findAll(PetType.class).iterator().forEachRemaining(vet -> result.add(vet));
		return result;
	};

	public Collection<Owner> findAll() {
		Collection<Owner> result = new ArrayList<>();
		template.findAll(Owner.class).iterator().forEachRemaining(value -> result.add(value));
		return result;
	};

	public Page<Owner> findAll(Pageable pageable) {
		List<Owner> result = new ArrayList<>();
		template.findInRange(pageable.getOffset(), pageable.getPageSize(), Owner.class).iterator()
				.forEachRemaining(value -> result.add(value));
		return new PageImpl<>(result, pageable, template.count(Owner.class));
	}

	@Override
	public Page<Owner> findByLastName(String lastName, Pageable pageable) {
		if (lastName.length() == 0) {
			return findAll(pageable);
		}
		KeyValueQuery<String> query = new KeyValueQuery<String>("lastName=='" + lastName + "'");
		List<Owner> result = new ArrayList<>();
		// TODO: extract a page
		template.find(query, Owner.class).iterator().forEachRemaining(value -> result.add(value));
		return new PageImpl<>(result, pageable, result.size());
	}

	@Override
	public Owner findById(Integer id) {
		return template.findById(id, Owner.class).orElse(null);
	}

	@Override
	public void save(Owner owner) {
		if (owner.isNew()) {
			Integer id = Long.valueOf(template.count(Owner.class) + 1).intValue();
			owner.setId(id);
			template.insert(id, owner);
		}
		else {
			template.update(owner.getId(), owner);
		}
	};

}
