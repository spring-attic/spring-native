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
package org.springframework.data;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.AuditingHandlerSupport;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Hints required for all auditing.
 */
@NativeHint(
		trigger = AuditingHandler.class,
		types = {
				@TypeHint(types = {LazyInitTargetSource.class,
						ObjectFactoryCreatingFactoryBean.class,
						ProxyFactoryBean.class}),
				@TypeHint(types = AbstractBeanFactoryBasedTargetSource.class, fields = {
						@FieldHint(name = "targetBeanName")}),
				@TypeHint(types = AdvisedSupport.class, fields = {
						@FieldHint(name = "targetSource")}),
				@TypeHint(types = AuditingHandlerSupport.class, fields = {
						@FieldHint(name = "dateTimeForNow"),
						@FieldHint(name = "dateTimeProvider"),
						@FieldHint(name = "modifyOnCreation")})
		})
public class DataAuditingHints implements NativeConfiguration {

}
