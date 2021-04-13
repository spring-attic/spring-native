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

package org.thymeleaf;

import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.thymeleaf.engine.IterationStatusVar;
import org.thymeleaf.expression.*;
import org.thymeleaf.extras.java8time.expression.Temporals;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.expression.Fields;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafView;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.standard.expression.AdditionExpression;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@NativeHint(trigger = ISpringTemplateEngine.class, types = {
		@TypeHint( types = {
				AbstractConfigurableTemplateResolver.class,
				ITemplateResolver.class,
				AbstractTemplateResolver.class,
				SpringResourceTemplateResolver.class,
		}),
		@TypeHint(
				types= {
						ThymeleafView.class,
						ThymeleafReactiveView.class
				}, typeNames= {
						"org.thymeleaf.spring5.expression.Mvc$Spring41MvcUriComponentsBuilderDelegate",
						"org.thymeleaf.spring5.expression.Mvc$NonSpring41MvcUriComponentsBuilderDelegate"
				}
		),
		@TypeHint(types = { Fields.class, Temporals.class, AdditionExpression.class }, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS),
		@TypeHint(types = IterationStatusVar.class, access = AccessBits.FULL_REFLECTION),
		@TypeHint(types = { Aggregates.class,
							Arrays.class,
							Bools.class,
							Calendars.class,
							Conversions.class,
							Dates.class,
							ExecutionInfo.class,
							Ids.class,
							Lists.class,
							Maps.class,
							Messages.class,
							Numbers.class,
							Objects.class,
							Sets.class,
							Strings.class,
							Uris.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS)
})
public class ThymeleafHints implements NativeConfiguration {
}
