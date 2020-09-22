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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.batch.BasicBatchConfigurer;
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.boot.jdbc.AbstractDataSourceInitializer;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ProxyInfo;
import org.springframework.graalvm.extension.ResourcesInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(trigger=BatchConfigurationSelector.class, 
		resourcesInfos = { 
			@ResourcesInfo(patterns = { 
				"org/springframework/batch/core/schema-h2.sql",
				// This exception happens on the batch samplewithout this next entry - why on earth is that?
//com.oracle.svm.core.jdk.UnsupportedFeatureError: ObjectOutputStream.writeObject()
//	at com.oracle.svm.core.util.VMError.unsupportedFeature(VMError.java:86) ~[na:na]
//	at java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:68) ~[na:na]
//	at org.springframework.util.SerializationUtils.serialize(SerializationUtils.java:47) ~[na:na]
//	at org.springframework.batch.core.repository.dao.MapJobExecutionDao.copy(MapJobExecutionDao.java:55) ~[na:na]
//	at org.springframework.batch.core.repository.dao.MapJobExecutionDao.saveJobExecution(MapJobExecutionDao.java:65) ~[na:na]
//	at org.springframework.batch.core.repository.support.SimpleJobRepository.createJobExecution(SimpleJobRepository.java:151) ~[na:na]
//	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:na]
//	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344) ~[na:na]
//	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198) ~[na:na]
//	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[na:na]
//	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:367) ~[na:na]
				"META-INF/spring-autoconfigure-metadata.properties"
			}),
		},
		typeInfos = {
		@TypeInfo(types = {
			AbstractDataSourceInitializer.class,
		}, access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS),
		@TypeInfo(types = {
			BasicBatchConfigurer.class,
			JobLauncherApplicationRunner.class,
			DataSourcePoolMetadataProvider.class
		}, access=AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS),
		@TypeInfo(types= {ModularBatchConfiguration.class,
				SimpleBatchConfiguration.class,
				ScopeConfiguration.class})},
		proxyInfos = {
		@ProxyInfo(typeNames = {
				"org.springframework.batch.core.repository.JobRepository",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyInfo(typeNames = {
				"org.springframework.batch.core.launch.JobLauncher",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyInfo(typeNames = {
				"org.springframework.batch.core.configuration.JobRegistry",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyInfo(typeNames = {
				"org.springframework.batch.core.explore.JobExplorer",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyInfo(typeNames = {
				"org.springframework.transaction.PlatformTransactionManager",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"}),
		@ProxyInfo(typeNames = {
				"java.util.concurrent.ConcurrentMap", 
				"java.io.Serializable",
				"java.util.Map",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"})
		})
public class BatchHints implements NativeImageConfiguration { }