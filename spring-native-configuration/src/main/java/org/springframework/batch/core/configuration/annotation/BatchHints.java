/*
 * Copyright 2019-2021 the original author or authors.
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

package org.springframework.batch.core.configuration.annotation;

import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.batch.BasicBatchConfigurer;
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.boot.jdbc.AbstractDataSourceInitializer;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger=BatchConfigurationSelector.class,
		resources = @ResourceHint(patterns = {"org/springframework/batch/core/schema-h2.sql",
				"org/springframework/batch/core/schema-db2.sql",
				"org/springframework/batch/core/schema-derby.sql",
				"org/springframework/batch/core/schema-hsqldb.sql",
				"org/springframework/batch/core/schema-mysql.sql",
				"org/springframework/batch/core/schema-oracle10g.sql",
				"org/springframework/batch/core/schema-postgresql.sql",
				"org/springframework/batch/core/schema-sqlf.sql",
				"org/springframework/batch/core/schema-sqlite.sql",
				"org/springframework/batch/core/schema-sqlserver.sql",
				"org/springframework/batch/core/schema-sybase.sql"
		}),
		types = {
		@TypeHint(types = AbstractDataSourceInitializer.class , access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS),
		@TypeHint(types = {
			BasicBatchConfigurer.class,
			JobLauncherApplicationRunner.class,
			DataSourcePoolMetadataProvider.class,
			InfrastructureAdvisorAutoProxyCreator.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS),
		@TypeHint(types= {
				ModularBatchConfiguration.class,
				SimpleBatchConfiguration.class,
				ScopeConfiguration.class
		})}, jdkProxies = {
		@JdkProxyHint(typeNames = {
				"org.springframework.batch.core.repository.JobRepository",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.batch.core.launch.JobLauncher",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.batch.core.configuration.JobRegistry",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.batch.core.explore.JobExplorer",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.transaction.PlatformTransactionManager",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"java.util.concurrent.ConcurrentMap", 
				"java.io.Serializable",
				"java.util.Map",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"})
		}, aotProxies = {
		// Due to @Transactional inside SimpleJobOperator
		@AotProxyHint(targetClass=org.springframework.batch.core.launch.support.SimpleJobOperator.class, proxyFeatures = ProxyBits.IS_STATIC)
		})
public class BatchHints implements NativeConfiguration { }