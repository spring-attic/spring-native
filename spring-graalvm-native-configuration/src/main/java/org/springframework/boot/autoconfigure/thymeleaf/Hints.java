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
package org.springframework.boot.autoconfigure.thymeleaf;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.thymeleaf.extras.java8time.expression.Temporals;
import org.thymeleaf.spring5.expression.Fields;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafView;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveView;
import org.thymeleaf.standard.expression.AdditionExpression;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@NativeImageHint(trigger=ThymeleafAutoConfiguration.class,typeInfos= {
		@TypeInfo(
				types= {
				AbstractConfigurableTemplateResolver.class,ITemplateResolver.class,AbstractTemplateResolver.class,
				SpringResourceTemplateResolver.class,Fields.class,
				ThymeleafView.class,
// Surely one of many... I wonder if there is a better way for these rather than conditional on autoconfig, conditional on jar presence? (maven coords?)
				Temporals.class,
				AdditionExpression.class,
				ThymeleafReactiveView.class
				}, typeNames= {
						"org.thymeleaf.spring5.expression.Mvc$Spring41MvcUriComponentsBuilderDelegate",
						"org.thymeleaf.spring5.expression.Mvc$NonSpring41MvcUriComponentsBuilderDelegate"
				},
				access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS)
})
public class Hints implements NativeImageConfiguration {
}
