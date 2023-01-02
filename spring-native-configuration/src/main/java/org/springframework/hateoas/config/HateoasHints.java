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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.jackson.JacksonHints;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.core.DefaultLinkRelationProvider;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.hateoas.server.core.LastInvocationAware;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.UriComponentsContributor;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.MissingTypeException;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeProcessor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.nativex.type.TypeSystemNativeConfiguration;
import org.springframework.plugin.PluginHints;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@NativeHint(trigger = HalConfiguration.class,
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
						Affordance.class,
						AffordanceModel.class,
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS}),
				@TypeHint(typeNames = {
						"org.atteo.evo.inflector.English",
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS}),
				@TypeHint(
						types = CollectionModel.class,
						fields = @FieldHint(name = "content", allowUnsafeAccess = true, allowWrite = true)
				)
		},
		initialization = {
				@InitializationHint(types = {org.springframework.hateoas.MediaTypes.class, org.springframework.util.MimeTypeUtils.class}, initTime = InitializationTime.BUILD)
		},
		jdkProxies = {
				@JdkProxyHint(typeNames = {"java.util.List", "org.springframework.aop.SpringProxy", "org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy"}),
				@JdkProxyHint(typeNames = {"org.springframework.web.bind.annotation.RequestParam", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.web.bind.annotation.RequestBody", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.web.bind.annotation.PathVariable", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.web.bind.annotation.ModelAttribute", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.stereotype.Controller", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.web.bind.annotation.ControllerAdvice", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.web.bind.annotation.RequestHeader", "org.springframework.core.annotation.SynthesizedAnnotation"}),
				@JdkProxyHint(typeNames = {"org.springframework.hateoas.server.core.Relation", "org.springframework.core.annotation.SynthesizedAnnotation"})
		},
		imports = {
				PluginHints.class,
				JacksonHints.class,
		},
		resources = @ResourceHint(patterns = "org/springframework/web/context/ContextLoader.properties")
)
public class HateoasHints implements NativeConfiguration, TypeSystemNativeConfiguration {

	private static final String ENTITY_LINKS = "org/springframework/hateoas/server/EntityLinks";
	private static final String JACKSON_ANNOTATION = "Lcom/fasterxml/jackson/annotation/JacksonAnnotation;";
	private static final String ENABLE_HYPERMEDIA_SUPPORT = "Lorg/springframework/hateoas/config/EnableHypermediaSupport;";

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {

		if (!ClassUtils.isPresent("org.springframework.hateoas.config.EnableHypermediaSupport", null)) {
			return Collections.emptyList();
		}

		Set<String> hypermediaFormats = computeConfiguredHypermediaFormats(typeSystem);

		List<HintDeclaration> hints = new ArrayList<>();
		hints.addAll(computePlugins(typeSystem));

		hints.addAll(computeAtConfigurationClasses(typeSystem, hypermediaFormats));
		hints.addAll(computeRepresentationModels(typeSystem));
		hints.addAll(computeEntityLinks(typeSystem));
		hints.addAll(computeJacksonMappings(typeSystem, hypermediaFormats));
		hints.addAll(computeControllerProxies(typeSystem));

		return hints;
	}

	private Set<String> computeConfiguredHypermediaFormats(TypeSystem typeSystem) {

		return typeSystem.scanUserCodeDirectoriesAndSpringJars(type -> {
			try {
				return type.isAnnotated(ENABLE_HYPERMEDIA_SUPPORT);
			} catch (MissingTypeException e) {
				return false;
			}
		})
				.flatMap(type -> {
					try {
						String formats = type.getAnnotationValuesInHierarchy(ENABLE_HYPERMEDIA_SUPPORT).getOrDefault("type", "");
						return StringUtils.hasText(formats) ? Stream.of(formats.split(";")) : Stream.empty();
					} catch (MissingTypeException ex) {
						return Stream.empty();
					}
				})
				.map(it -> {
					String value = it.replaceAll(".*org\\.springframework\\.hateoas\\.config\\.EnableHypermediaSupport\\$HypermediaType\\.", "").toLowerCase();
					switch (value) {
						case "hal_forms":
							return "hal";
						case "collection_json":
							return "collectionjson";
						default:
							return value;
					}
				})
				.collect(Collectors.toSet());
	}

	private List<HintDeclaration> computeAtConfigurationClasses(TypeSystem typeSystem, Set<String> hypermediaFormats) {

		return TypeProcessor.namedProcessor("HateoasHints - Configuration Classes")
				.skipTypesMatching(type -> {
					if (type.isPartOfDomain("org.springframework.hateoas.mediatype")) {
						return !isSupportedHypermediaFormat(type, hypermediaFormats);
					}
					return false;
				})
				.skipAnnotationInspection()
				.skipMethodInspection()
				.skipFieldInspection()
				.onTypeDiscovered((type, context) -> context.addReflectiveAccess(type, new AccessDescriptor(AccessBits.FULL_REFLECTION)))
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
				.onTypeDiscovered((type, ctx) -> {
					if(type.belongsToPackage("java", true)) {
						ctx.addReflectiveAccess(type, TypeAccess.RESOURCE);
					} else {
						ctx.addReflectiveAccess(type, TypeAccess.PUBLIC_METHODS, TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.DECLARED_FIELDS);
					}
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

	/**
	 * Create proxies for all {@link org.springframework.stereotype.Controller} classes and the return types of their methods
	 * annotated with {@link org.springframework.web.bind.annotation.RequestMapping}.
	 *
	 * @param typeSystem must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	List<HintDeclaration> computeControllerProxies(TypeSystem typeSystem) {

		return typeSystem.scanUserCodeDirectoriesAndSpringJars(HateoasHints::isWebControllerProxyCandidate)
				.flatMap(type -> {
					return Stream.concat(Stream.of(type), type.getMethods().stream().filter(Method::isAtMapping).map(Method::getReturnType));
				})
				.distinct()
				.filter(Objects::nonNull)
				.filter(t -> !t.isFinal())
				.map(type -> {

					HintDeclaration hint = new HintDeclaration();
					hint.addProxyDescriptor(lastInvocationAwareProxyDescriptor(type));
					return hint;
				})
				.collect(Collectors.toList());
	}


	List<HintDeclaration> computeJacksonMappings(TypeSystem typeSystem, Set<String> hypermediaFormats) {

		return TypeProcessor.namedProcessor("HateoasHints - Jackson Mapping Candidates")
				.skipTypesMatching(type -> {
					if (type.isPartOfDomain("com.fasterxml.jackson.") || type.isPartOfDomain("java.")) {
						return true;
					}
					if (type.isPartOfDomain("org.springframework.")) {
						if (!type.isPartOfDomain("org.springframework.hateoas.")) {
							return true;
						}
						return !isSupportedHypermediaFormat(type, hypermediaFormats);
					}
					return false;
				})
				.filterAnnotations(annotation -> annotation.isPartOfDomain("com.fasterxml.jackson."))
				.use(typeSystem)
				.toProcessTypesMatching(this::usesJackson);
	}

	private List<HintDeclaration> computePlugins(TypeSystem typeSystem) {

		// TODO: maybe move to PluginHints.
		return typeSystem.scanUserCodeDirectoriesAndSpringJars(type -> type.implementsInterface("org/springframework/plugin/core/Plugin", true))
				.map(type -> {
					HintDeclaration hint = new HintDeclaration();
					hint.addDependantType(type.getDottedName(), new AccessDescriptor(AccessBits.FULL_REFLECTION));
					return hint;
				}).collect(Collectors.toList());
	}

	private static boolean isWebControllerProxyCandidate(Type type) {
		return (type.isAtController() || type.isAnnotated("Lorg/springframework/data/rest/webmvc/BasePathAwareController;")) && !type.isAnnotation();
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

	private boolean isSupportedHypermediaFormat(Type type, Set<String> hypermediaFormats) {

		if (hypermediaFormats.isEmpty()) {
			return true;
		}

		if (!type.isPartOfDomain("org.springframework.hateoas.mediatype")) {
			return true;
		}

		if (type.getPackageName().equals("org.springframework.hateoas.mediatype")) {
			return true;
		}

		for (String hypermediaFormat : hypermediaFormats) {
			if (type.getPackageName().contains(hypermediaFormat)) {
				return true;
			}
		}
		return false;
	}

	private JdkProxyDescriptor lastInvocationAwareProxyDescriptor(Type type) {

		if (!type.isInterface()) {
			return new AotProxyDescriptor(type.getDottedName(), Collections.singletonList("org.springframework.hateoas.server.core.LastInvocationAware"), ProxyBits.IS_STATIC);
		}

		List<String> proxyInterfaces = new ArrayList<>();
		proxyInterfaces.add("org.springframework.hateoas.server.core.LastInvocationAware");
		proxyInterfaces.add(type.getDottedName());
		for (Type intface : type.getInterfaces()) {
			proxyInterfaces.add(intface.getDottedName());
		}
		proxyInterfaces.add("org.springframework.aop.SpringProxy");
		proxyInterfaces.add("org.springframework.aop.framework.Advised");
		proxyInterfaces.add("org.springframework.core.DecoratingProxy");

		return new JdkProxyDescriptor(proxyInterfaces);
	}
}
