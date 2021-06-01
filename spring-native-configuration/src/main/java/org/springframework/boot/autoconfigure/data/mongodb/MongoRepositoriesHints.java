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

import org.springframework.boot.autoconfigure.data.SpringDataReactiveHints;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeSaveCallback;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.config.ReactiveMongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;
import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.domain.proxies.JdkProxyDescriptor;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;


@NativeHint(trigger = MongoRepositoriesAutoConfiguration.class, types = {
		@TypeHint(types = {
				MongoRepositoryFactoryBean.class,
				MongoRepositoryConfigurationExtension.class,
				MongoConfigurationSupport.class,
				SimpleMongoRepository.class,
				BeforeConvertCallback.class,
				BeforeSaveCallback.class,
				AfterSaveCallback.class,
				AfterConvertCallback.class
		})
})
@NativeHint(trigger = MongoReactiveRepositoriesAutoConfiguration.class,
		imports = SpringDataReactiveHints.class,
		types = @TypeHint(types = {
				ReactiveMongoRepositoryFactoryBean.class,
				ReactiveMongoRepositoryConfigurationExtension.class,
				MongoConfigurationSupport.class,
				SimpleReactiveMongoRepository.class,
				ReactiveBeforeConvertCallback.class,
				ReactiveBeforeSaveCallback.class,
				ReactiveAfterSaveCallback.class,
				ReactiveAfterConvertCallback.class
		})
)
public class MongoRepositoriesHints implements NativeConfiguration {

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {

		if (!typeSystem.canResolve("org/springframework/data/mongodb/core/mapping/Document")) {
			return Collections.emptyList();
		}

		List<Type> scan = typeSystem.scan(type -> !type.getFieldsWithAnnotation("Lorg/springframework/data/mongodb/core/mapping/DBRef;", false).isEmpty());
		List<HintDeclaration> hints = scan.stream()
				.flatMap(type -> {
					return type.getFieldsWithAnnotation("Lorg/springframework/data/mongodb/core/mapping/DBRef;", false)
							.stream()
							.filter(field -> {
										if (field == null) {
											return false;
										}
										Map<String, String> annotationValuesInHierarchy = field.getAnnotationValuesInHierarchy("Lorg/springframework/data/mongodb/core/mapping/DBRef;");
										return annotationValuesInHierarchy.get("lazy") == "true";
									}
							)
							.map(field -> {

								if (field.getType().isInterface()) {

									HintDeclaration hintDeclaration = new HintDeclaration();
									List<String> interfaces = new ArrayList<>(field.getSignature().stream().map(typeSystem::resolveSlashed).map(Type::getDottedName).collect(Collectors.toList()));
									interfaces.add(0, "org.springframework.data.mongodb.core.convert.LazyLoadingProxy");
									interfaces.add("org.springframework.aop.SpringProxy");
									interfaces.add("org.springframework.aop.framework.Advised");
									interfaces.add("org.springframework.core.DecoratingProxy");

									JdkProxyDescriptor descriptor = new JdkProxyDescriptor(interfaces);
									hintDeclaration.addProxyDescriptor(descriptor);
									return hintDeclaration;
								}

								HintDeclaration hintDeclaration = new HintDeclaration();
								AotProxyDescriptor descriptor = new AotProxyDescriptor(field.getType().getDottedName(), Collections.singletonList("org.springframework.data.mongodb.core.convert.LazyLoadingProxy"), ProxyBits.IS_STATIC);
								hintDeclaration.addProxyDescriptor(descriptor);
								return hintDeclaration;
							});
				}).collect(Collectors.toList());

		System.out.println("MongoDBRef proxies: " + hints);
		return hints;
	}
}
