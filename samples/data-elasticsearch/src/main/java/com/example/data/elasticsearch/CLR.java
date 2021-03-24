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
package com.example.data.elasticsearch;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired
	private ReactiveElasticsearchOperations reactiveOps;

	@Autowired
	private ElasticsearchOperations operations;

	@Autowired
	private ConferenceRepository repository;

	@Autowired
	private ReactiveConferenceRepository reactiveRepository;

	@Override
	public void run(String... args) throws Exception {

		{
			System.out.println("refresh index");
			repository.deleteAll();
			operations.indexOps(Conference.class).refresh();
		}

		{
			System.out.println("\n--- REPOSITORY ---");
			// Save data sample
			repository.save(Conference.builder().date("2014-11-06").name("Spring eXchange 2014 - London")
					.keywords(Arrays.asList("java", "spring")).location(new GeoPoint(51.500152D, -0.126236D)).build());
			repository.save(Conference.builder().date("2014-12-07").name("Scala eXchange 2014 - London")
					.keywords(Arrays.asList("scala", "play", "java")).location(new GeoPoint(51.500152D, -0.126236D)).build());
			repository.save(Conference.builder().date("2014-11-20").name("Elasticsearch 2014 - Berlin")
					.keywords(Arrays.asList("java", "elasticsearch", "kibana")).location(new GeoPoint(52.5234051D, 13.4113999))
					.build());
			repository.save(Conference.builder().date("2014-11-12").name("AWS London 2014")
					.keywords(Arrays.asList("cloud", "aws")).location(new GeoPoint(51.500152D, -0.126236D)).build());
			repository.save(Conference.builder().date("2014-10-04").name("JDD14 - Cracow")
					.keywords(Arrays.asList("java", "spring")).location(new GeoPoint(50.0646501D, 19.9449799)).build());

			System.out.println("repository.count(): " + repository.count());
		}

		{
			System.out.println("\n--- CUSTOM REPOSITORY ---");

			SearchPage<Conference> searchPage = repository.findBySomeCustomImplementation("eXchange", PageRequest.of(0, 10));
			System.out.println("custom implementation finder.size(): " + searchPage.getSearchHits().getTotalHits());
		}

		String expectedDate = "2014-10-29";
		String expectedWord = "java";
		CriteriaQuery query = new CriteriaQuery(
				new Criteria("keywords").contains(expectedWord).and(new Criteria("date").greaterThanEqual(expectedDate)));

		{
			System.out.println("\n--- TEMPLATE FIND ---");
			SearchHits<Conference> result = operations.search(query, Conference.class, IndexCoordinates.of("conference-index"));
			System.out.println("result.size(): " + result.getSearchHits().size());
		}

		{
			System.out.println("\n--- REPOSITORY FINDER ---");
			List<Conference> result = repository.findByKeywordsContaining("spring");
			System.out.println("result.size(): " + result.size());
		}

		{
			System.out.println("\n--- REACTIVE TEMPLATE ---");
			System.out.println("reactiveTemplate.count(): " + reactiveOps.count(Query.findAll(), Conference.class, IndexCoordinates.of("conference-index")).block());
		}

		{
			System.out.println("\n--- REACTIVE REPOSITORY ---");
			System.out.println("reactiveRepository.count(): " + reactiveRepository.count().block());
		}

		{
			// does currently not work in SD ES - wrong reactor-netty dependency
//			System.out.println("\n--- REACTIVE REPOSITORY FINDER ---");
//			System.out.println("result.size(): " + reactiveRepository.findByKeywordsContaining("spring").collectList().block().size());
		}

		System.out.println("DONE");
	}
}
