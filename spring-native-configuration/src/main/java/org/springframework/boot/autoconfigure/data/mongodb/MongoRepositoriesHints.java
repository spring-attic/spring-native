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

package org.springframework.boot.autoconfigure.data.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.data.SpringDataReactiveHints;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeSaveCallback;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.nativex.type.TypeSystemNativeConfiguration;
import org.springframework.util.ClassUtils;


@NativeHint(trigger = MongoDatabaseFactorySupport.class, types = {
		@TypeHint(types = {
				SimpleMongoRepository.class,
				BeforeConvertCallback.class,
				BeforeSaveCallback.class,
				AfterSaveCallback.class,
				AfterConvertCallback.class
		}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS})
})
@NativeHint(trigger = SimpleReactiveMongoDatabaseFactory.class,
		imports = SpringDataReactiveHints.class,
		types = @TypeHint(types = {
					SimpleReactiveMongoRepository.class,
					ReactiveBeforeConvertCallback.class,
					ReactiveBeforeSaveCallback.class,
					ReactiveAfterSaveCallback.class,
					ReactiveAfterConvertCallback.class
			}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS})

)
public class MongoRepositoriesHints implements NativeConfiguration, TypeSystemNativeConfiguration {

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {

		if (!ClassUtils.isPresent("org.springframework.data.mongodb.core.mapping.Document", null)) {
			return Collections.emptyList();
		}

		List<Type> scan = typeSystem.scan(type ->
				!type.getFieldsWithAnnotation("Lorg/springframework/data/mongodb/core/mapping/DBRef;", false).isEmpty()
				&& !type.getFieldsWithAnnotation("Lorg/springframework/data/mongodb/core/mapping/DocumentReference;", false).isEmpty());

		List<HintDeclaration> hints = scan.stream()
				.flatMap(type -> computeAssociations(typeSystem, type))
				.collect(Collectors.toList());

		return hints;
	}

	private Stream<HintDeclaration> computeAssociations(TypeSystem typeSystem, Type type) {
		return Stream.concat(
					computeAssociations(typeSystem, type, "Lorg/springframework/data/mongodb/core/mapping/DBRef;"),
					computeAssociations(typeSystem, type, "Lorg/springframework/data/mongodb/core/mapping/DocumentReference;")
				);
	}

	private Stream<HintDeclaration> computeAssociations(TypeSystem typeSystem, Type type, String annotation) {

		return type.getFieldsWithAnnotation(annotation, false).stream()
				.filter(field -> {
						if (field == null) {
							return false;
						}
						Map<String, String> annotationValuesInHierarchy = field.getAnnotationValuesInHierarchy(annotation);
						return "true".equals(annotationValuesInHierarchy.get("lazy"));
				})
				.map(field -> {

					if (field.getType().isInterface()) {

						HintDeclaration hintDeclaration = new HintDeclaration();
						List<String> interfaces = new ArrayList<>();

						field.getSignature().stream()
								.map(typeSystem::resolveSlashed)
								.map(Type::getDottedName)
								.forEach(interfaces::add);

						interfaces.add(0, "org.springframework.data.mongodb.core.convert.LazyLoadingProxy");
						interfaces.add("org.springframework.aop.SpringProxy");
						interfaces.add("org.springframework.aop.framework.Advised");
						interfaces.add("org.springframework.core.DecoratingProxy");

						JdkProxyDescriptor descriptor = new JdkProxyDescriptor(interfaces);
						hintDeclaration.addProxyDescriptor(descriptor);

						return hintDeclaration;
					}

					HintDeclaration hintDeclaration = new HintDeclaration();
					AotProxyDescriptor descriptor = new AotProxyDescriptor(field.getType().getDottedName(),
							Collections.singletonList("org.springframework.data.mongodb.core.convert.LazyLoadingProxy"),
							ProxyBits.IS_STATIC);
					hintDeclaration.addProxyDescriptor(descriptor);

					return hintDeclaration;
				});
	}
}
