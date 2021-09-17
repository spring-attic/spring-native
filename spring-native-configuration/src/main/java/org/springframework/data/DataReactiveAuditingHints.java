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

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.core.DecoratingProxy;
import org.springframework.data.auditing.ReactiveAuditingHandler;
import org.springframework.data.auditing.ReactiveIsNewAwareAuditingHandler;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.TypeHints;
import org.springframework.nativex.type.NativeConfiguration;

/**
 * Hints required by all reactive auditing.
 */
@TypeHints({
		@TypeHint(types = ReactiveIsNewAwareAuditingHandler.class),
		@TypeHint(types = ReactiveAuditingHandler.class, fields = {
				@FieldHint(name = "auditorAware")
		})
})
@JdkProxyHint(types = {
		ReactiveAuditorAware.class,
		SpringProxy.class,
		Advised.class,
		DecoratingProxy.class
})
public class DataReactiveAuditingHints implements NativeConfiguration {

}
