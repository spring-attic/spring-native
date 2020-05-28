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
package org.springframework.data;

import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.repository.Repository;
import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.extension.ComponentProcessor;
import org.springframework.graalvm.extension.NativeImageContext;
import org.springframework.graalvm.type.Method;
import org.springframework.graalvm.type.Type;
import org.springframework.graalvm.type.TypeSystem;

// This is an example from the mongodb sample.
//
// This is spring.components:
//  com.example.data.mongo.Order=org.springframework.data.annotation.Persistent
//  com.example.data.mongo.OrderRepository=org.springframework.data.repository.Repository
//  com.example.data.mongo.OrderRepositoryImpl=com.example.data.mongo.OrderRepositoryImpl
// 
// (The third looks a bit unusual - force fit IIRC because there is no classpath scanning)
//
// These are the reflective accesses to compute: (the letters indicate how 'computed' from the analysis below):
//[
//(E)  "name":"com.example.data.mongo.LineItem",
//(B)  "name":"com.example.data.mongo.Order",
//(C)  "name":"com.example.data.mongo.OrderProjection",
//(A)  "name":"com.example.data.mongo.OrderRepository",
//(G)  "name":"com.example.data.mongo.OrderRepositoryCustom",
//(F)  "name":"com.example.data.mongo.OrderRepositoryImpl",
//(D) "name":"com.example.data.mongo.OrdersPerCustomer",
//]
// 
// These are the proxies to compute:
//
//[
//(C) ["com.example.data.mongo.OrderProjection","org.springframework.data.projection.TargetAware","org.springframework.aop.SpringProxy","org.springframework.core.DecoratingProxy"],
//(A) ["com.example.data.mongo.OrderRepository","org.springframework.data.repository.Repository","org.springframework.transaction.interceptor.TransactionalProxy","org.springframework.aop.framework.Advised","org.springframework.core.DecoratingProxy"]
//]
//
// Reflective access to OrderRepository (A) because it is a Repository which is Indexed
// OrderRepository extends CrudRepository<Order, String> which gives us Order (B)
//
// Within the repository are find methods and other methods marked (meta) with @QueryAnnotation.
// The find* methods have return types like List<OrderProjection> with gives us OrderProjection(C)
// Also because that ends with 'Projection' we add the proxy for it.
//
// There are also some methods marked @Aggregation (this is @QueryAnnotation meta usage),
// including totalOrdersPerCustomerId that returns a List<OrdersPerCustomer> giving us OrdersPerCustomer (D).
//
// The Order type has a getter that returns List<LineItem> giving us LineItem(E)
//
// Via a naming scheme we see that there is an OrderRepositoryImpl (repository name with Impl on the end)
// so we register the hierarchy for that which gives us OrderRepositoryImpl (F) and OrderRepositoryCustom (G)
//
/**
 * Basic spring.components entry processor for Spring Data. Crafted initially to
 * handle the mongodb case. This ComponentProcessor implementation is listed in
 * the META-INF/services file
 * org.springframework.graalvm.extension.ComponentProcessor and that is how it
 * is picked up by the feature runtime.
 * 
 * @author Andy Clement
 */
public class SpringDataComponentProcessor implements ComponentProcessor {

	private static String repositoryName;
	private static String queryAnnotationName;

	static {
		try {
			repositoryName = Repository.class.getName();
			queryAnnotationName = QueryAnnotation.class.getName();
		} catch (NoClassDefFoundError ncdfe) {
			// This component processor isn't useful anyway in this run, so OK that these
			// aren't here
		}
	}

	@Override
	public boolean handle(String key, List<String> values) {
		return repositoryName != null && values.contains(repositoryName);
	}

	@Override
	public void process(NativeImageContext imageContext, String key, List<String> values) {

		try {
			TypeSystem typeSystem = imageContext.getTypeSystem();
			Type applicationRepositoryType = typeSystem.resolveName(key);
			String thingInApplicationRepository = null;
			// This is a little crude - works for the code seen so far but I'm sure
			// insufficient
			thingInApplicationRepository = applicationRepositoryType.fetchJpaRepositoryType();
			if (thingInApplicationRepository == null) {
				thingInApplicationRepository = applicationRepositoryType.fetchCrudRepositoryType();
			}

			if (thingInApplicationRepository == null) {
				// give up!
				System.out.println("SDCP: Unable to work out repository contents for repository " + key);
				return;
			}

			imageContext.addProxy(key, repositoryName, "org.springframework.transaction.interceptor.TransactionalProxy",
					"org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy");
			imageContext.addReflectiveAccess(key, Flag.allDeclaredMethods, Flag.allDeclaredConstructors);


			Type thingInApplicationRepositoryType = typeSystem.resolveName(thingInApplicationRepository);

			// Grab get* methods from the type of the thing in the repository type
			// TODO recurse into types where they are application types? (e.g. LineItem
			// where get returns List<LineItem>)
			List<Method> methods = thingInApplicationRepositoryType.getMethods(m -> m.getName().startsWith("get"));
			for (Method method : methods) {
				Set<Type> signatureTypes = method.getSignatureTypes(true);
				// System.out.println("SDCP: method "+method.getName()+" has return types
				// "+signatureTypes);
				for (Type signatureType : signatureTypes) {
					imageContext.addReflectiveAccess(signatureType.getDottedName(), Flag.allDeclaredConstructors,
							Flag.allDeclaredMethods, Flag.allDeclaredFields);
				}
			}

			imageContext.addReflectiveAccess(thingInApplicationRepository, Flag.allDeclaredMethods,
					Flag.allDeclaredConstructors, Flag.allDeclaredFields);

			// Grab the find* methods
			methods = applicationRepositoryType.getMethods(m -> m.getName().startsWith("find"));

			// Let's add the methods with @QueryAnnotations on (including meta usage of
			// QueryAnnotation)
			methods.addAll(applicationRepositoryType.getMethodsWithAnnotationName(queryAnnotationName, true));

			// For each of those, let's ensure reflective access to return types
			for (Method method : methods) {
				Set<Type> signatureTypes = method.getSignatureTypes(true);
				// System.out.println("SDCP: method "+method.getName()+" has return types
				// "+signatureTypes);
				for (Type signatureType : signatureTypes) {
					String signatureTypeName = signatureType.getDottedName();
					imageContext.addReflectiveAccess(signatureTypeName, Flag.allDeclaredConstructors,
							Flag.allDeclaredMethods, Flag.allDeclaredFields);
					if (signatureTypeName.endsWith("Projection")) {
						imageContext.addProxy(signatureTypeName, "org.springframework.data.projection.TargetAware",
								"org.springframework.aop.SpringProxy", "org.springframework.core.DecoratingProxy");
					}
				}
			}

			// Check for an Impl of the repository
			Type applicationRepositoryImplType = typeSystem.resolveName(key + "Impl", true);
			if (applicationRepositoryImplType != null) {
				imageContext.addReflectiveAccessHierarchy(applicationRepositoryImplType, Flag.allDeclaredConstructors,
						Flag.allDeclaredMethods);
				for (Method method : applicationRepositoryImplType.getMethods()) {

					Set<Type> signatureTypes = method.getSignatureTypes(true);
					for (Type signatureType : signatureTypes) {
						String signatureTypeName = signatureType.getDottedName();
						imageContext.addReflectiveAccess(signatureTypeName, Flag.allDeclaredConstructors,
								Flag.allDeclaredMethods, Flag.allDeclaredFields);
					}
				}
			}
		} catch (Throwable t) {
			System.out.println("WARNING: Problem with SpringDataComponentProcessor: " + t.getMessage());
		}
	}

}
