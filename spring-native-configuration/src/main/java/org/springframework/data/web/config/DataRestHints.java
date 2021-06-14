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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcHints;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.MissingTypeException;
import org.springframework.nativex.type.TypeProcessor;
import org.springframework.nativex.type.TypeProcessor.TypeHintCreatingProcessor;
import org.springframework.hateoas.config.HateoasHints;
import org.springframework.hateoas.mediatype.hal.HalMediaTypeConfiguration;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeName;
import org.springframework.nativex.type.TypeSystem;
import reactor.core.publisher.Flux;

@NativeHint(trigger = RepositoryRestMvcAutoConfiguration.class, types = {
		@TypeHint(types = {

				Flux.class,
				org.reactivestreams.Publisher.class,
				HalMediaTypeConfiguration.class,

				org.springframework.data.repository.core.support.RepositoryFactoryInformation.class,
				org.springframework.data.repository.support.Repositories.class,
				org.springframework.data.repository.support.RepositoryInvoker.class,
				org.springframework.data.repository.support.RepositoryInvokerFactory.class,
		},
				typeNames = {

						"org.springframework.data.rest.core.annotation.Description",
						"org.springframework.data.rest.core.config.EnumTranslationConfiguration",
						"org.springframework.data.rest.core.config.RepositoryRestConfiguration",

						"org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration",
						"org.springframework.data.rest.webmvc.RepositoryRestController",
						"org.springframework.data.rest.webmvc.BasePathAwareController",

						"org.springframework.data.rest.webmvc.config.WebMvcRepositoryRestConfiguration",

						// JACKSON
//						"org.springframework.hateoas.EntityModel$MapSuppressingUnwrappingSerializer",

						// EvoInflector
						"org.atteo.evo.inflector.English"
				},

				access = AccessBits.ALL

		),
},
		jdkProxies = {
				@JdkProxyHint(typeNames = {"org.springframework.data.rest.webmvc.BasePathAwareController", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.data.rest.webmvc.RepositoryRestController", "org.springframework.data.rest.webmvc.BasePathAwareController", "org.springframework.core.annotation.SynthesizedAnnotation"}),
		},
		imports = {
				WebMvcHints.class, HateoasHints.class
		}

)
public class DataRestHints implements NativeConfiguration {

	private static final String BASE_PATH_AWARE_CONTROLLER = "Lorg/springframework/data/rest/webmvc/BasePathAwareController;";
	private static final String REPOSITORY_REST_CONFIGURER = "org/springframework/data/rest/webmvc/config/RepositoryRestConfigurer";
	private static final String REPOSITORY_REST_RESOURCE = "Lorg/springframework/data/rest/core/annotation/RepositoryRestResource;";
	private static final String JACKSON_ANNOTATION = "Lcom/fasterxml/jackson/annotation/JacksonAnnotation;";

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {

		List<HintDeclaration> hints = new ArrayList<>();

		hints.addAll(computeRestControllerHints(typeSystem));
		hints.addAll(computeRepositoryRestConfigurer(typeSystem));
		hints.addAll(computeExcerptProjectionHints(typeSystem));
		hints.addAll(computeJacksonMappingCandidates(typeSystem));

		// TODO: what about RestResource and others

		return hints;
	}

	/**
	 * Compute {@link HintDeclaration hints} for all types that extend org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer.
	 * like "org.springframework.boot.autoconfigure.data.rest.SpringBootRepositoryRestConfigurer"
	 *
	 * @param typeSystem must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	private List<HintDeclaration> computeRepositoryRestConfigurer(TypeSystem typeSystem) {

		return TypeProcessor.namedProcessor("RestMvcConfigurationProcessor - RepositoryRestConfigurer")
				.skipAnnotationInspection()
				.skipMethodInspection()
				.skipFieldInspection()
				.skipConstructorInspection()
				.onTypeDiscovered((type, context) -> {
					context.addReflectiveAccess(type, new AccessDescriptor(AccessBits.ALL));
				})
				.use(typeSystem)
				.toProcessTypesMatching(it ->
						it.implementsInterface(REPOSITORY_REST_CONFIGURER, true)
				);
	}

	/**
	 * Compute {@link HintDeclaration hints} for all types (meta)annotated with @BasePathAwareController.
	 * Not interested in any fields reachable from these types but inspecting the methods for web-bind annotations.
	 *
	 * @param typeSystem must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	private List<HintDeclaration> computeRestControllerHints(TypeSystem typeSystem) {

		return TypeProcessor.namedProcessor("RestMvcConfigurationProcessor - RestController")
				.skipTypesMatching(type -> !type.hasAnnotationInHierarchy(BASE_PATH_AWARE_CONTROLLER))
				.skipFieldInspection()
				.skipConstructorInspection()
				.filterAnnotations(annotation ->
						annotation.isPartOfDomain("org.springframework.web") ||
						annotation.isPartOfDomain("org.springframework.data.rest")
				)
				.use(typeSystem)
				.toProcessTypes(ts -> ts.findTypesAnnotated(BASE_PATH_AWARE_CONTROLLER, true)
						.stream()
						.map(ts::resolveName)
				);
	}

	/**
	 * Compute {@link HintDeclaration hints} for all types (meta)annotated with @RepositoryRestResource.
	 * Extract the excerptProjection and register types as well as proxy configuration.
	 *
	 * @param typeSystem must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	private List<HintDeclaration> computeExcerptProjectionHints(TypeSystem typeSystem) {

		TypeHintCreatingProcessor excerptProjectionProcessor = TypeProcessor.namedProcessor("RestMvcConfigurationProcessor - ExcerptProjection")
				.skipFieldInspection().use(typeSystem);

		return typeSystem.findTypesAnnotated(REPOSITORY_REST_RESOURCE, true)
				.stream()
				.map(typeSystem::resolveName)
				.flatMap(type -> {

					Map<String, String> annotationValuesInHierarchy = type.getAnnotationValuesInHierarchy(REPOSITORY_REST_RESOURCE);
					if (!annotationValuesInHierarchy.containsKey("excerptProjection")) {
						return Stream.empty();
					}

					String excerptProjectionSignatureType = annotationValuesInHierarchy.get("excerptProjection");
					Type targetProjectionType = typeSystem.resolve(TypeName.fromTypeSignature(excerptProjectionSignatureType));

					if (targetProjectionType == null || targetProjectionType.getDottedName().equals("org.springframework.data.rest.core.annotation.RepositoryRestResource$None")) {
						return Stream.empty();
					}

					List<HintDeclaration> projectionHints = new ArrayList<>();

					// types reachable
					projectionHints.addAll(excerptProjectionProcessor.toProcessType(targetProjectionType));

					// the projection proxy
					HintDeclaration proxyHint = new HintDeclaration();
					JdkProxyDescriptor proxyDescriptor = new JdkProxyDescriptor(Arrays.asList(targetProjectionType.getDottedName(), "org.springframework.data.projection.TargetAware", "org.springframework.aop.SpringProxy", "org.springframework.core.DecoratingProxy"));
					proxyHint.addProxyDescriptor(proxyDescriptor);

					projectionHints.add(proxyHint);

					return projectionHints.stream();
				})
				.collect(Collectors.toList());
	}

	List<HintDeclaration> computeJacksonMappingCandidates(TypeSystem typeSystem) {

		return TypeProcessor.namedProcessor("RestMvcConfigurationProcessor - Jackson Mapping Candidates")
				.skipTypesMatching(type -> {
					if(type.isPartOfDomain("com.fasterxml.jackson.") || type.isPartOfDomain("java.")) {
						return true;
					}
					if(type.isPartOfDomain("org.springframework.")) {
						if(!type.isPartOfDomain("org.springframework.data.rest")) {
							return true;
						}
					}
					return false;
				})
				.filterAnnotations(annotation -> annotation.isPartOfDomain("com.fasterxml.jackson."))
				.use(typeSystem)
				.toProcessTypesMatching(this::usesJackson);
	}

	private boolean usesJackson(Type type) {

		try {
			if (type.getAnnotations().stream().filter(ann -> ann.isAnnotated(JACKSON_ANNOTATION)).findAny().isPresent()) {
				return true;
			}

			if (!type.getMethodsWithAnnotation(JACKSON_ANNOTATION, true).isEmpty()) {
				return true;
			}

			return !type.getFieldsWithAnnotation(JACKSON_ANNOTATION, true).isEmpty();
		} catch (MissingTypeException e) {

		}

		return false;
	}
	// TODO: split up and refine access to hateos and plugin configuration classes
}
