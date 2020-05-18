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
package org.springframework.security.config.annotation.method.configuration;

import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger=GlobalMethodSecuritySelector.class, typeInfos = {
		@TypeInfo(types= {EnableGlobalMethodSecurity.class,GlobalMethodSecurityConfiguration.class,
				AutoProxyRegistrar.class,GlobalMethodSecurityAspectJAutoProxyRegistrar.class,
				MethodSecurityMetadataSourceAdvisorRegistrar.class,Jsr250MetadataSourceConfiguration.class
				})})
@NativeImageHint(trigger=ReactiveMethodSecuritySelector.class, typeInfos = {
		@TypeInfo(types= {AutoProxyRegistrar.class,ReactiveMethodSecurityConfiguration.class})})
public class Hints implements NativeImageConfiguration { }