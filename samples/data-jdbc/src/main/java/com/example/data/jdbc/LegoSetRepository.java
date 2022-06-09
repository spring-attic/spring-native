/*
 * Copyright 2020 the original author or authors.
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
package com.example.data.jdbc;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

// TODO: package private type cause issues with AOT generated code for the RepositoryFactory
public interface LegoSetRepository extends CrudRepository<LegoSet, Integer> {

	@Query("SELECT m.name model_name, m.description, l.name set_name" +
			"  FROM model m" +
			"  JOIN lego_set l" +
			"  ON m.lego_set = l.id" +
			"  WHERE :age BETWEEN l.min_age and l.max_age")
	List<ModelReport> reportModelForAge(@Param("age") int age);

	@Modifying
	@Query("UPDATE model set name = lower(name) WHERE name <> lower(name)")
	int lowerCaseMapKeys();
}
