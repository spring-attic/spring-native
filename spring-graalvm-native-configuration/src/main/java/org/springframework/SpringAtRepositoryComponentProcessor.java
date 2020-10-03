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
package org.springframework;

import java.util.ArrayList;
import java.util.List;

import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.extension.ComponentProcessor;
import org.springframework.graalvm.extension.NativeImageContext;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.Method;
import org.springframework.graalvm.type.Type;

// Example input code:
//
// @Repository
// public class JdbcFooRepository implements FooRepository {
//
// public interface FooRepository {
//   Foo find(long id);
//   void save(Foo foo);
// }
// 
// public class Foo {
// 	@JsonIgnore private Long id;
// 	private String value;
// 	public Foo() { }
// 	public Foo(String value) { this.value = value; }
// 	public Long getId() { return id; }
// 	...
// 
// and the result:
//    {
//            "name": "app.main.foo.Foo",
//            "allDeclaredConstructors": true,
//            "allPublicMethods": true,
//            "allDeclaredFields": true
//    },
//    {
//            "name": "app.main.foo.FooRepository",
//            "allDeclaredMethods": true
//    },
//    {
//            "name": "app.main.foo.JdbcFooRepository",
//            "allDeclaredFields": true,
//            "allDeclaredConstructors": true,
//            "allDeclaredMethods": true,
//            "allPublicMethods": true,
//            "allDeclaredClasses": true
//    },

/**
 * Basic spring.components entry processor for classes marked @Repository.
 * 
 * @author Andy Clement
 * @author Christoph Strobl
 */ 
public class SpringAtRepositoryComponentProcessor implements ComponentProcessor {
	private static final String LOG_PREFIX = "SARCP: ";

	private static String repositoryName;
	private static String queryAnnotationName;

	@Override
	public boolean handle(NativeImageContext imageContext, String key, List<String> values) {
		Type resolvedKey = imageContext.getTypeSystem().resolveDotted(key);
		if (resolvedKey.hasAnnotationInHierarchy("Lorg/springframework/stereotype/Repository;")) {
			imageContext.log(LOG_PREFIX+" handling @Repository "+key);
			return true;
		}
		return false;
	}

	@Override
	public void process(NativeImageContext imageContext, String key, List<String> values) {
		try {
			Type repositoryType = imageContext.getTypeSystem().resolveDotted(key);
			List<Method> publicRepositoryMethods = repositoryType.getMethods(m->m.isPublic());
			// Assumption... the return values of these need reflective access... this is likely too broad
			for (Method publicRepositoryMethod: publicRepositoryMethods) {
				Type returnType = publicRepositoryMethod.getReturnType();
				if (returnType != null) {
					registerDomainType(returnType, imageContext);
				}
			}
			imageContext.log(LOG_PREFIX+" registering repository type (and its hierarchy): "+repositoryType);
			imageContext.addReflectiveAccessHierarchy(repositoryType, AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS|AccessBits.DECLARED_FIELDS);
			registerRepositoryProxy(imageContext, repositoryType);
		} catch (Throwable t) {
			imageContext.log(LOG_PREFIX+"WARNING: Problem with SpringAtRepositoryComponentProcessor: " + t.getMessage());
		}
	}
	
	public void registerRepositoryProxy(NativeImageContext imageContext, Type repositoryType) {
		List<String> repositoryInterfacesStrings = repositoryType.getInterfacesStrings();
		if (repositoryInterfacesStrings.size()!=0) {
			List<String> repositoryInterfaces = new ArrayList<>();
			imageContext.log(LOG_PREFIX+" creating proxy for repository "+repositoryType.getDottedName());
			for (String s: repositoryInterfacesStrings) {
				repositoryInterfaces.add(s.replace("/", "."));
			}
			repositoryInterfaces.add("org.springframework.aop.SpringProxy");
			repositoryInterfaces.add("org.springframework.aop.framework.Advised");
			repositoryInterfaces.add("org.springframework.core.DecoratingProxy");
			imageContext.addProxy(repositoryInterfaces);
		} else {
			imageContext.log(LOG_PREFIX+"WARNING: unable to create proxy for repository "+repositoryType.getDottedName()+" because it has no interfaces");
		}
	}

	private void registerDomainType(Type domainType, NativeImageContext imageContext) {
		imageContext.log(String.format(LOG_PREFIX+"registering reflective access for domain type %s", domainType.getDottedName()));
		imageContext.addReflectiveAccess(domainType.getDottedName(), Flag.allDeclaredMethods,
				Flag.allDeclaredConstructors, Flag.allDeclaredFields);
	}

	@Override
	public void printSummary() {
	}

}
