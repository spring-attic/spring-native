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
package org.springframework.context.annotation;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.DefaultEventListenerFactory;
import org.springframework.context.event.EventListenerMethodProcessor;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.graalvm.extension.MethodInfo;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

/*
// Not quite right... this is a superclass of a selector we've already added...
proposedHints.put(AdviceModeImportSelector,
		new CompilationHint(true, true, new String[0]
		));
		*/
@NativeImageHint(trigger = AdviceModeImportSelector.class, abortIfTypesMissing = true, follow = true)
// TODO can be {@link Configuration}, {@link ImportSelector}, {@link ImportBeanDefinitionRegistrar}
// @Imports has @CompilationHint(skipIfTypesMissing=false?, follow=true)
@NativeImageHint(trigger = Import.class, abortIfTypesMissing = false, follow = true, applyToFunctional = false) // TODO verify these flags...
@NativeImageHint(trigger = Conditional.class, extractTypesFromAttributes = { "value" }, applyToFunctional = false) // TODO need extract?
// These don't specify a triggering value target so are always exposed
@NativeImageHint(typeInfos = { @TypeInfo(types = { ComponentScan.class,
		Configuration.class }, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS) })
// TODO Check required access for enums like this FilterType
@NativeImageHint(typeInfos = { @TypeInfo(types = { FilterType.class }, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_FIELDS) })
@NativeImageHint(typeInfos = {
	@TypeInfo(typeNames = "org.springframework.context.annotation.ConfigurationClassParser$DefaultDeferredImportSelectorGroup",
			  access=AccessBits.LOAD_AND_CONSTRUCT)})
@NativeImageHint(typeInfos = {
		@TypeInfo(types = { 
				org.springframework.context.ApplicationContext.class, // petclinic-jpa shows errors in startup log without this
				EmbeddedValueResolverAware.class,EnvironmentAware.class,
				AnnotationConfigApplicationContext.class,
				CommonAnnotationBeanPostProcessor.class,
				AnnotationScopeMetadataResolver.class,
				AutoConfigurationExcludeFilter.class,
				EventListenerMethodProcessor.class,
				DefaultEventListenerFactory.class,
				AutowiredAnnotationBeanPostProcessor.class
				}, access = AccessBits.CLASS | AccessBits.DECLARED_CONSTRUCTORS),
		@TypeInfo( types= {ComponentScan.Filter.class},access=AccessBits.CLASS|AccessBits.DECLARED_METHODS),
		@TypeInfo(types = { ConfigurationClassPostProcessor.class },
		methods = {
				@MethodInfo(name="setMetadataReaderFactory",parameterTypes=MetadataReaderFactory.class)
		},
		access=AccessBits.LOAD_AND_CONSTRUCT)
})
public class ContextAnnotationHints implements NativeImageConfiguration {
}
