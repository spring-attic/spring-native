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
package org.springframework.cloud.task;

import java.sql.DatabaseMetaData;

import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.boot.jdbc.AbstractDataSourceInitializer;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.cloud.task.batch.configuration.JobLaunchCondition;
import org.springframework.cloud.task.batch.configuration.TaskBatchAutoConfiguration;
import org.springframework.cloud.task.batch.configuration.TaskBatchExecutionListenerBeanPostProcessor;
import org.springframework.cloud.task.batch.handler.TaskJobLauncherApplicationRunner;
import org.springframework.cloud.task.batch.listener.BatchEventAutoConfiguration;
import org.springframework.cloud.task.batch.listener.TaskBatchExecutionListener;
import org.springframework.cloud.task.batch.listener.support.TaskBatchEventListenerBeanPostProcessor;
import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.cloud.task.configuration.SimpleTaskAutoConfiguration;
import org.springframework.cloud.task.repository.support.TaskRepositoryInitializer;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger= SimpleTaskAutoConfiguration.class,
		resources = @ResourceHint(patterns = {"org/springframework/cloud/task/schema-h2.sql",
				"org/springframework/cloud/task/schema-mysql.sql",
				"org/springframework/cloud/task/schema-oracle10g.sql",
				"org/springframework/cloud/task/schema-postgresql.sql",
				"org/springframework/cloud/task/schema-hsqldb.sql",
				"org/springframework/cloud/task/schema-sqlserver.sql"}),
		types = {
		@TypeHint(types = {
			AbstractDataSourceInitializer.class,
		}, access = AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS),
		@TypeHint(types = {
			DefaultTaskConfigurer.class,
			JobLauncherApplicationRunner.class,
			TaskJobLauncherApplicationRunner.class,
			DataSourcePoolMetadataProvider.class,
			TaskRepositoryInitializer.class,
			LockRegistry.class,
			TaskBatchExecutionListener.class,
			TaskBatchAutoConfiguration.class,
			TaskBatchExecutionListenerBeanPostProcessor.class,
			TaskBatchEventListenerBeanPostProcessor.class,
			JobLaunchCondition.class,
			BatchEventAutoConfiguration.class
		}, access = AccessBits.ALL),
		@TypeHint(typeNames = { "org.springframework.cloud.task.batch.configuration.JobLaunchCondition$FailOnJobFailureCondition"
		}, access = AccessBits.ALL),
		@TypeHint(typeNames = { "org.springframework.cloud.task.batch.configuration.JobLaunchCondition$SpringBatchJobCondition"
		}, access = AccessBits.ALL),
		@TypeHint(types= {DatabaseMetaData.class})},
		jdkProxies = {
		@JdkProxyHint(typeNames = {
				"org.springframework.cloud.task.batch.configuration.TaskBatchExecutionListenerBeanPostProcessor",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.cloud.task.batch.configuration.TaskBatchEventListenerBeanPostProcessor",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.cloud.task.repository.TaskRepository",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@JdkProxyHint(typeNames = {
				"org.springframework.cloud.task.repository.TaskExplorer",
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
		})
public class CloudTaskHints implements NativeConfiguration { }
