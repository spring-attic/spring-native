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
package org.springframework.data.web.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcHints;
import org.springframework.data.TypeProcessor;
import org.springframework.hateoas.config.HateoasHints;
import org.springframework.hateoas.mediatype.hal.HalMediaTypeConfiguration;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.TypeSystem;
import reactor.core.publisher.Flux;

@NativeHint(trigger = RepositoryRestMvcAutoConfiguration.class, types = {
		@TypeHint(types = {

				Flux.class,
				org.reactivestreams.Publisher.class,
				HalMediaTypeConfiguration.class,

				org.springframework.stereotype.Controller.class,
				org.springframework.data.repository.core.support.RepositoryFactoryInformation.class,
				org.springframework.data.repository.support.Repositories.class,
				org.springframework.data.repository.support.RepositoryInvoker.class,
				org.springframework.data.repository.support.RepositoryInvokerFactory.class,
		},
				typeNames = {

//						"org.springframework.hateoas.EntityModel",
//						"org.springframework.hateoas.CollectionModel",
//						"org.springframework.hateoas.AffordanceModel",
//						"org.springframework.hateoas.config.RestTemplateHateoasConfiguration",
//						"org.springframework.hateoas.mediatype.hal.forms.HalFormsMediaTypeConfiguration",

						"org.springframework.data.rest.core.annotation.Description",
						"org.springframework.data.rest.core.config.EnumTranslationConfiguration",
						"org.springframework.data.rest.core.config.RepositoryRestConfiguration",

						"org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration",
						"org.springframework.data.rest.webmvc.RepositoryRestController",
						"org.springframework.data.rest.webmvc.BasePathAwareController",

						"org.springframework.data.rest.webmvc.config.WebMvcRepositoryRestConfiguration",

						// BOOT CONFIG
						"org.springframework.boot.autoconfigure.data.rest.SpringBootRepositoryRestConfigurer",

						// PLUGIN
						"org.springframework.plugin.core.support.PluginRegistryFactoryBean",
						"org.springframework.plugin.core.OrderAwarePluginRegistry",
						"org.springframework.plugin.core.Plugin",
						"org.springframework.plugin.core.PluginRegistry",
						"org.springframework.plugin.core.PluginRegistrySupport",
						"org.springframework.plugin.core.SimplePluginRegistry",
						"org.springframework.plugin.core.config.EnablePluginRegistries",
						"org.springframework.plugin.core.config.PluginRegistriesBeanDefinitionRegistrar",
						"org.springframework.plugin.core.support.AbstractTypeAwareSupport",
						"org.springframework.plugin.core.support.PluginRegistryFactoryBean",

						// JACKSON
//						"org.springframework.hateoas.EntityModel$MapSuppressingUnwrappingSerializer",

						// EvoInflector
						"org.atteo.evo.inflector.English"
				},

				access = AccessBits.ALL

		),
},
		proxies = {
				@ProxyHint(typeNames = {"org.springframework.data.rest.webmvc.BasePathAwareController", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"org.springframework.data.rest.webmvc.RepositoryRestController", "org.springframework.data.rest.webmvc.BasePathAwareController", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"java.util.List", "org.springframework.aop.SpringProxy", "org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy"}),
				@ProxyHint(typeNames = {"org.springframework.web.bind.annotation.RequestParam", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"org.springframework.web.bind.annotation.RequestBody", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"org.springframework.web.bind.annotation.PathVariable", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"org.springframework.web.bind.annotation.ModelAttribute", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"org.springframework.stereotype.Controller", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"org.springframework.web.bind.annotation.ControllerAdvice", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@ProxyHint(typeNames = {"org.springframework.web.bind.annotation.RequestHeader", "org.springframework.core.annotation.SynthesizedAnnotation"})
		},

		initialization = {
				@InitializationHint(types = {org.springframework.hateoas.MediaTypes.class, org.springframework.util.MimeTypeUtils.class}, initTime = InitializationTime.BUILD)
		},
		imports = {
				WebMvcHints.class, HateoasHints.class
		}

)
public class DataRestHints implements NativeConfiguration {

	public static final String BASE_PATH_AWARE_CONTROLLER = "Lorg/springframework/data/rest/webmvc/BasePathAwareController;";

	/**
	 * Registers full reflection for types (meta)annotated with @BasePathAwareController.
	 * Not interested in any fields reachable from these types but inspecting the methods for web-bind annotations.
	 */
	TypeProcessor restControllerProcessor = new TypeProcessor(
			(type, context) -> type.hasAnnotationInHierarchy(BASE_PATH_AWARE_CONTROLLER), // just the root types
			(type, registrar) -> registrar.addReflectiveAccess(type, new AccessDescriptor(AccessBits.FULL_REFLECTION)),
			(annotation, registrar) -> registrar.addReflectiveAccess(annotation, new AccessDescriptor(AccessBits.ANNOTATION))
	).named("RestControllerProcessor")
			.includeAnnotationsMatching(annotation -> {
				return annotation.isPartOfDomain("org.springframework.web.bind.annotation") || annotation.isPartOfDomain("org.springframework.data.rest");
			})
			.skipFieldInspection();

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {

		List<HintDeclaration> hints = new ArrayList<>();

		// Rest Controllers
		{
			hints.addAll(restControllerProcessor.use(typeSystem)
					.toProcessTypes(ts -> ts.findTypesAnnotated(BASE_PATH_AWARE_CONTROLLER, true)
					.stream()
					.map(ts::resolveName)
			));
		}

		// TODO; we'd also need to register all types that extend org.springframework.data.rest.webmvc.configRepositoryRestConfigurer.

		return hints;
	}

	// TODO: add a component processor that resolves types implementing RepositoryRestConfigurer

	// TODO: split up and refine access to hateos and plugin configuration classes

	// TODO: inspect RepositoryRestResource for excerpt projections and register proxy
	//  <excerpt projection interface name>, org.springframework.data.projection.TargetAware, org.springframework.aop.SpringProxy, org.springframework.core.DecoratingProxy
}
