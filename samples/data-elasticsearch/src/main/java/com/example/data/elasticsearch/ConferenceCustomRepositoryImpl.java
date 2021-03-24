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
package com.example.data.elasticsearch;

import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

@Indexed
@Component // required for spring.components
public class ConferenceCustomRepositoryImpl implements ConferenceCustomRepository {

	private final ElasticsearchOperations operations;

	public ConferenceCustomRepositoryImpl(ElasticsearchOperations operations) {
		this.operations = operations;
	}

	@Override
	public SearchPage<Conference> findBySomeCustomImplementation(String name, Pageable page) {

		Query query = new NativeSearchQueryBuilder()
				.withQuery(QueryBuilders.matchQuery("name", name))
				.build();

		SearchHits<Conference> searchHits = operations.search(query, Conference.class);
		return SearchHitSupport.searchPageFor(searchHits, page);
	}
}
