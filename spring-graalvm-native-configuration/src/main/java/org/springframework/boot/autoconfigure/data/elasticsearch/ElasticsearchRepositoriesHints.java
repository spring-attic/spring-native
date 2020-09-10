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
package org.springframework.boot.autoconfigure.data.elasticsearch;

import org.elasticsearch.common.xcontent.XContentParser;
import org.springframework.boot.autoconfigure.data.SpringDataReactiveHints;
import org.springframework.data.elasticsearch.core.event.AfterConvertCallback;
import org.springframework.data.elasticsearch.core.event.AfterSaveCallback;
import org.springframework.data.elasticsearch.core.event.BeforeConvertCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveAfterConvertCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveAfterSaveCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveBeforeConvertCallback;
import org.springframework.data.elasticsearch.repository.config.ElasticsearchRepositoryConfigExtension;
import org.springframework.data.elasticsearch.repository.config.ReactiveElasticsearchRepositoryConfigurationExtension;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.data.elasticsearch.repository.support.ReactiveElasticsearchRepositoryFactoryBean;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.SimpleReactiveElasticsearchRepository;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ProxyInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

/**
 * @author Christoph Strobl
 */
@NativeImageHint(trigger = ElasticsearchRepositoriesAutoConfiguration.class,
		typeInfos = {
				@TypeInfo(types = {
						ElasticsearchRepositoryFactoryBean.class,
						ElasticsearchRepositoryConfigExtension.class,
						SimpleElasticsearchRepository.class,
						BeforeConvertCallback.class,
						AfterSaveCallback.class,
						AfterConvertCallback.class,
				})
		})

@NativeImageHint(trigger = ReactiveElasticsearchRepositoriesAutoConfiguration.class,
		importInfos = {SpringDataReactiveHints.class},
		typeInfos = {
				@TypeInfo(
						types = {
								ReactiveElasticsearchRepositoryFactoryBean.class,
								ReactiveElasticsearchRepositoryConfigurationExtension.class,
								SimpleReactiveElasticsearchRepository.class,
								ReactiveBeforeConvertCallback.class,
								ReactiveAfterSaveCallback.class,
								ReactiveAfterConvertCallback.class,

								XContentParser.class
						},
						typeNames = { // todo check access only on fromXContent method
								"org.elasticsearch.action.search.SearchResponse",
								"org.elasticsearch.action.get.GetResponse",
								"org.elasticsearch.action.get.MultiGetResponse",
								"org.elasticsearch.action.get.MultiGetShardResponse",
								"org.elasticsearch.action.bulk.BulkResponse",
								"org.elasticsearch.action.main.MainResponse",
								"org.elasticsearch.action.search.MultiSearchResponse",
								"org.elasticsearch.action.search.ClearScrollResponse",
						})
		},
		proxyInfos = {
				@ProxyInfo(types = {XContentParser.class})
		}
)

// org.elasticsearch.client.RestClient - required logging configuration
@NativeImageHint(trigger = ElasticsearchRepositoriesAutoConfiguration.class,
		typeInfos = {
				@TypeInfo(types = {
						org.apache.logging.log4j.message.DefaultFlowMessageFactory.class,
						org.apache.logging.log4j.message.ParameterizedMessageFactory.class,
						org.apache.logging.log4j.message.ReusableMessageFactory.class
				}, access = AccessBits.DECLARED_CONSTRUCTORS)
		})
public class ElasticsearchRepositoriesHints implements NativeImageConfiguration {

}
