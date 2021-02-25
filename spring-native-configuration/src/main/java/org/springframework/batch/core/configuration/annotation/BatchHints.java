/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.configuration.annotation;

import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.batch.BasicBatchConfigurer;
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.boot.jdbc.AbstractDataSourceInitializer;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger=BatchConfigurationSelector.class,
		resources = {
			@ResourceHint(patterns = {
				"org/springframework/batch/core/schema-h2.sql"
			}),
		},
		types = {
		@TypeHint(types = {
			AbstractDataSourceInitializer.class,
		}, access=AccessBits.LOAD_AND_CONSTRUCT| AccessBits.DECLARED_METHODS),
		@TypeHint(types = {
			BasicBatchConfigurer.class,
			JobLauncherApplicationRunner.class,
			DataSourcePoolMetadataProvider.class,
			InfrastructureAdvisorAutoProxyCreator.class
		}, access=AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS),
		@TypeHint(types= {ModularBatchConfiguration.class,
				SimpleBatchConfiguration.class,
				ScopeConfiguration.class})},
		proxies = {
		@ProxyHint(typeNames = {
				"org.springframework.batch.core.repository.JobRepository",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyHint(typeNames = {
				"org.springframework.batch.core.launch.JobLauncher",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyHint(typeNames = {
				"org.springframework.batch.core.configuration.JobRegistry",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyHint(typeNames = {
				"org.springframework.batch.core.explore.JobExplorer",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyHint(typeNames = {
				"org.springframework.transaction.PlatformTransactionManager",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyHint(typeNames = {
				"java.util.concurrent.ConcurrentMap", 
				"java.io.Serializable",
				"java.util.Map",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"})
		})
public class BatchHints implements NativeConfiguration { }