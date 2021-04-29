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

package org.springframework.hateoas.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.autoconfigure.hateoas.HypermediaHttpMessageConverterConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonHints;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.core.DefaultLinkRelationProvider;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.hateoas.server.core.LastInvocationAware;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.UriComponentsContributor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.MissingTypeException;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeProcessor;
import org.springframework.nativex.type.TypeSystem;


@NativeHint(trigger = WebStackImportSelector.class, types = {
		@TypeHint(types = {
				WebMvcHateoasConfiguration.class,
				WebFluxHateoasConfiguration.class
		})
})
@NativeHint(trigger = HypermediaConfigurationImportSelector.class, types =
@TypeHint(types = {
		HypermediaConfigurationImportSelector.class,
		EnableHypermediaSupport.class,
		HypermediaType.class,
		HypermediaType[].class,
		MediaTypeConfigurationProvider.class
}))
@NativeHint(trigger = HypermediaAutoConfiguration.class,
		types = {
				@TypeHint(types = {

						HypermediaType.class,
						ExposesResourceFor.class,
						RepresentationModelAssembler.class,
						DefaultLinkRelationProvider.class,
						EvoInflectorLinkRelationProvider.class,
						LastInvocationAware.class,
						Relation.class,
						UriComponentsContributor.class,

						HypermediaConfigurationImportSelector.class,
						HateoasConfiguration.class,
						HypermediaHttpMessageConverterConfiguration.class,

						Affordance.class,
						AffordanceModel.class,
						RestTemplateHateoasConfiguration.class,
				}, access = AccessBits.ALL),
				@TypeHint(
						types = CollectionModel.class,
						fields = @FieldHint(name = "content", allowUnsafeAccess = true, allowWrite = true)
				)
		},
		imports = {
				JacksonHints.class
		}
)
public class HateoasHints implements NativeConfiguration {

	private static final String ENTITY_LINKS = "org/springframework/hateoas/server/EntityLinks";
	private static final String JACKSON_ANNOTATION = "Lcom/fasterxml/jackson/annotation/JacksonAnnotation;";

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {

		List<HintDeclaration> hints = new ArrayList<>();

		hints.addAll(computeAtConfigurationClasses(typeSystem));
		hints.addAll(computeRepresentationModels(typeSystem));
		hints.addAll(computeEntityLinks(typeSystem));
		hints.addAll(computeJacksonMappings(typeSystem));

		return hints;
	}

	private List<HintDeclaration> computeAtConfigurationClasses(TypeSystem typeSystem) {

		return TypeProcessor.namedProcessor("HateoasHints - Configuration Classes")
				.skipAnnotationInspection()
				.skipMethodInspection()
				.skipFieldInspection()
				.onTypeDiscovered((type, context) -> context.addReflectiveAccess(type, new AccessDescriptor(AccessBits.ALL)))
				.use(typeSystem)
				.toProcessTypesMatching(type -> type.isPartOfDomain("org.springframework.hateoas") && type.isAtConfiguration());
	}

	private List<HintDeclaration> computeEntityLinks(TypeSystem typeSystem) {

		return TypeProcessor.namedProcessor("HateoasHints - EntityLinks")
				.filterAnnotations(annotation ->
						annotation.isPartOfDomain("org.springframework"))
				.skipTypesMatching(type -> !type.isPartOfDomain("org.springframework.hateoas"))
				.use(typeSystem)
				.toProcessTypesMatching(type -> type.implementsInterface(ENTITY_LINKS, true));
	}


	/**
	 * Use {@link org.springframework.nativex.type.Type types} that extend {@literal org.springframework.hateoas.RepresentationModel}
	 * or implement {@literal org.springframework.hateoas.RepresentationModelProcessor} as a starting point to register configuration
	 * for domain types and annotations from {@literal org.springframework} or {@literal com.fasterxml.jackson}.
	 *
	 * @param typeSystem must not be {@literal null}.
	 * @return
	 */
	List<HintDeclaration> computeRepresentationModels(TypeSystem typeSystem) {

		return TypeProcessor.namedProcessor("HateoasHints - RepresentationModel")
				.skipTypesMatching(type -> type.isPartOfDomain("org.springframework.") || type.isPartOfDomain("com.fasterxml.jackson."))
				.filterAnnotations(annotation -> {
					return annotation.isPartOfDomain("org.springframework") ||
							annotation.isPartOfDomain("com.fasterxml.jackson.annotation");
				})
				.use(typeSystem)
				.toProcessTypesMatching(type -> {

					try {
						return type.extendsClass("Lorg/springframework/hateoas/RepresentationModel;") ||
								type.implementsInterface("org/springframework/hateoas/server/RepresentationModelProcessor", true);
					} catch (MissingTypeException e) {

					}
					return false;
				});
	}

	List<HintDeclaration> computeJacksonMappings(TypeSystem typeSystem) {

		return TypeProcessor.namedProcessor("HateoasHints - Jackson Mapping Candidates")
				.skipTypesMatching(type -> {
					if (type.isPartOfDomain("com.fasterxml.jackson.") || type.isPartOfDomain("java.")) {
						return true;
					}
					if (type.isPartOfDomain("org.springframework.")) {
						if (!type.isPartOfDomain("org.springframework.hateoas.")) {
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

			// capture types like: Jackson2HalModules
			if (type.implementsInterface("com/fasterxml/jackson/databind/Module", true)) {
				return true;
			}

			// capture types like: AlpsJsonHttpMessageConverter
			if (type.extendsClass("Lorg/springframework/http/converter/json/MappingJackson2HttpMessageConverter;")) {
				return true;
			}

			// capture types like: Jackson2HalModules$TrueOnlyBooleanSerializer
			if (type.extendsClass("Lcom/fasterxml/jackson/databind/JsonSerializer;") || type.extendsClass("Lcom/fasterxml/jackson/databind/JsonDeserializer;")) {
				return true;
			}

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
}
