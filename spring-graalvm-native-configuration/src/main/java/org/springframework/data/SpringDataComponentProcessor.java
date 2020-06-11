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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.repository.Repository;
import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.extension.ComponentProcessor;
import org.springframework.graalvm.extension.NativeImageContext;
import org.springframework.graalvm.type.Method;
import org.springframework.graalvm.type.Type;
import org.springframework.graalvm.type.TypeSystem;
import org.springframework.util.StringUtils;

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
 * @author Christoph Strobl
 */
public class SpringDataComponentProcessor implements ComponentProcessor {

	private static final String LOG_PREFIX = "SDCP: ";
	private static final String SPRING_DATA_NAMESPACE = "org.springframework.data";
	private static final String SPRING_DATA_DOMAIN_NAMESPACE = SPRING_DATA_NAMESPACE + ".domain";

	// TODO: is there a way to check via org.springframework.data.repository.query.parser.PartTree
	private static final Pattern REPOSITORY_METHOD_PATTERN = Pattern.compile("^(find|read|get|query|search|stream|count|exists|delete|remove).*");

	private static final Set<String> COMMON_REPOSITORY_DECLARATION_NAMES = new HashSet<>(
			Arrays.asList(

					// Imperative
					"org.springframework.data.repository.Repository",
					"org.springframework.data.repository.CrudRepository",
					"org.springframework.data.repository.PagingAndSortingRepository",

					// Reactive
					"org.springframework.data.repository.reactive.ReactiveCrudRepository",
					"org.springframework.data.repository.reactive.ReactiveSortingRepository"
			));

	private static final Set<String> STORE_REPOSITORY_DECLARATION_NAMES = new HashSet<>(
			Arrays.asList(

					// CASSANDRA
					"org.springframework.data.cassandra.repository.CassandraRepository",
					"org.springframework.data.cassandra.repository.ReactiveCassandraRepository",

					// COUCHBASE
					"org.springframework.data.couchbase.repository.CouchbaseRepository",
					"org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository",

					// ELASTICSEARCH
					"org.springframework.data.elasticsearch.repository.ElasticsearchRepository",
					"org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository",

					// JPA
					"org.springframework.data.jpa.repository.JpaRepository",

					// MONGODB
					"org.springframework.data.mongodb.repository.MongoRepository",
					"org.springframework.data.mongodb.repository.ReactiveMongoRepository",

					// NEO4J
					"org.springframework.data.neo4j.repository.Neo4jRepository",

					// R2DBC
					"org.springframework.data.r2dbc.repository.R2dbcRepository"
			));

	private static String repositoryName;
	private static String queryAnnotationName;

	private final SpringDataComponentLog log = SpringDataComponentLog.instance();
	private Set<String> keysSeen = new HashSet<>();

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

		if (repositoryName != null && values.contains(repositoryName)) {
			return !keysSeen.contains(key);
		}

		return false;
	}

	@Override
	public void process(NativeImageContext imageContext, String key, List<String> values) {

		keysSeen.add(key);

		try {

			Type repositoryType = imageContext.getTypeSystem().resolveName(key);
			Type repositoryDomainType = resolveRepositoryDomainType(repositoryType, imageContext.getTypeSystem());

			if (repositoryDomainType == null) {
				// give up!
				System.out.println(LOG_PREFIX + "Unable to work out repository contents for repository " + key);
				return;
			}

			log.repositoryFoundForType(repositoryType, repositoryDomainType);

			registerRepositoryInterface(repositoryType, imageContext);
			registerDomainType(repositoryDomainType, imageContext);
			registerQueryMethodResultTypes(repositoryType, repositoryDomainType, imageContext);
			detectCustomRepositoryImplementations(repositoryType, imageContext);
		} catch (Throwable t) {
			System.out.println("WARNING: Problem with SpringDataComponentProcessor: " + t.getMessage());
		}
	}

	private void registerRepositoryInterface(Type repositoryType, NativeImageContext imageContext) {

		imageContext.addReflectiveAccess(repositoryType, Flag.allDeclaredMethods, Flag.allDeclaredConstructors, Flag.allPublicMethods);

		{ // proxy configuration

			// as is
			imageContext.addProxy(repositoryType.getDottedName(), "org.springframework.aop.SpringProxy", "org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy");

			// transactional
			imageContext.addProxy(repositoryType.getDottedName(), repositoryName, "org.springframework.transaction.interceptor.TransactionalProxy",
					"org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy");
		}

		// reactive repo
		if (repositoryType.implementsInterface("org/springframework/data/repository/reactive/ReactiveCrudRepository")) {
			imageContext.initializeAtBuildTime(repositoryType); // TODO: check if we really need this!
		}
	}

	private void detectCustomRepositoryImplementations(Type repositoryType, NativeImageContext imageContext) {

		List<Type> customImplementations = new ArrayList<>();

		{
			log.message("Looking for custom repository implementations of " + repositoryType.getDottedName());
			Type customImplementation = imageContext.getTypeSystem().resolveName(repositoryType.getName() + customRepositoryImplementationPostfix(), true);
			if (customImplementation != null) {

				log.customImplementationFound(repositoryType, customImplementation);
				customImplementations.add(customImplementation);
			}
		}

		log.message("Inspecting repository interfaces for potential extensions.");
		for (Type repoInterface : repositoryType.getInterfaces()) {

			if (isPartOfSpringData(repoInterface)) {

				log.message("Skipping spring data interface " + repoInterface.getDottedName());
				continue;
			}

			log.message("Detected non spring data interface " + repoInterface.getDottedName());
			String customImplementationName = repoInterface.getName() + customRepositoryImplementationPostfix();

			Type customImplementation = imageContext.getTypeSystem().resolveName(customImplementationName, true);
			if (customImplementation != null) {

				log.customImplementationFound(repoInterface, customImplementation);
				customImplementations.add(customImplementation);
			}
		}

		for (Type customImpl : customImplementations) {

			imageContext.addReflectiveAccessHierarchy(customImpl, Flag.allDeclaredConstructors,
					Flag.allDeclaredMethods);

			for (Method method : customImpl.getMethods()) {
				for (Type signatureType : method.getSignatureTypes(true)) {
					registerDomainType(signatureType, imageContext);
				}
			}
		}
	}

	private void registerQueryMethodResultTypes(Type repositoryType, Type repositoryDomainType, NativeImageContext imageContext) {

		// Grab all partTreeQueryMethods
		List<Method> methods = repositoryType.getMethods(this::isQueryMethod);

		// Let's add the methods with @QueryAnnotations on (including meta usage of
		// QueryAnnotation)
		methods.addAll(repositoryType.getMethodsWithAnnotationName(queryAnnotationName, true));

		// For each of those, let's ensure reflective access to return types
		for (Method method : methods) {

			registerSpringDataAnnotations(method, imageContext);

			for (Type signatureType : method.getSignatureTypes(true)) {

				registerDomainType(signatureType, imageContext);

				if (isProjectionInterface(repositoryDomainType, signatureType)) {

					log.message(String.format("Registering proxy for '%s'. Might be projection return type of %s#%s", signatureType.getDottedName(), repositoryName, method.getName()));

					imageContext.addProxy(signatureType.getDottedName(), "org.springframework.data.projection.TargetAware",
							"org.springframework.aop.SpringProxy", "org.springframework.core.DecoratingProxy");
				}
			}
		}
	}

	private boolean isProjectionInterface(Type repositoryDomainType, Type signatureType) {
		return signatureType.isInterface() && !signatureType.isPartOfDomain("java.") && !isPartOfSpringData(signatureType) && !signatureType.isAssignableFrom(repositoryDomainType);
	}

	private Type resolveRepositoryDomainType(Type repositoryType, TypeSystem typeSystem) {

		for (String repositoryDeclarationName : repositoryDeclarationNames()) {

			String domainTypeName = repositoryType.findTypeParameterInSupertype(repositoryDeclarationName, 0);

			if (StringUtils.hasText(domainTypeName)) {

				log.message(String.format("Found %s for domain type %s.", repositoryDeclarationName, domainTypeName));
				return typeSystem.resolveName(domainTypeName);
			}
		}

		return null;
	}


	private void registerDomainType(Type domainType, NativeImageContext imageContext) {

		if (domainType.isPartOfDomain(SPRING_DATA_DOMAIN_NAMESPACE) || imageContext.hasReflectionConfigFor(domainType.getDottedName())) {
			return;
		}

		log.message(String.format("Registering reflective access for %s", domainType.getDottedName()));

		imageContext.addReflectiveAccess(domainType.getDottedName(), Flag.allDeclaredMethods,
				Flag.allDeclaredConstructors, Flag.allDeclaredFields);

		domainType.getAnnotations().forEach(it -> registerSpringDataAnnotation(it, imageContext));

		domainType.getFields().forEach(field -> {
			field.getAnnotations().forEach(it -> registerSpringDataAnnotation(it, imageContext));
		});

		List<Method> methods = domainType.getMethods(m -> m.getName().startsWith("get"));
		for (Method method : methods) {

			registerSpringDataAnnotations(method, imageContext);

			Set<Type> signatureTypes = method.getSignatureTypes(true);

			for (Type signatureType : signatureTypes) {

				// cycle guard, no need to do things over and over again
				if (!imageContext.hasReflectionConfigFor(signatureType.getDottedName())) {

					if (!signatureType.isPartOfDomain("java.")) {
						registerDomainType(signatureType, imageContext);
					} else {
						imageContext.addReflectiveAccess(signatureType.getDottedName(), Flag.allDeclaredConstructors,
								Flag.allDeclaredMethods, Flag.allDeclaredFields);
					}
				}
			}
		}
	}

	protected boolean isQueryMethod(Method m) {
		return REPOSITORY_METHOD_PATTERN.matcher(m.getName()).matches();
	}

	private Set<String> repositoryDeclarationNames() {

		LinkedHashSet<String> repositoryDeclarationNames = new LinkedHashSet<>(commonRepositoryDeclarationNames());
		repositoryDeclarationNames.addAll(storeSpecificRepositoryDeclarationNames());

		return repositoryDeclarationNames;
	}

	protected Set<String> commonRepositoryDeclarationNames() {
		return COMMON_REPOSITORY_DECLARATION_NAMES;
	}

	protected Set<String> storeSpecificRepositoryDeclarationNames() {
		return STORE_REPOSITORY_DECLARATION_NAMES;
	}

	protected String customRepositoryImplementationPostfix() {
		return "Impl";
	}

	private void registerSpringDataAnnotations(Method method, NativeImageContext context) {

		for (Type annotation : method.getAnnotationTypes()) {
			registerSpringDataAnnotation(annotation, context);
		}
	}

	private void registerSpringDataAnnotation(Type annotation, NativeImageContext context) {

		if (!context.hasReflectionConfigFor(annotation) && isPartOfSpringData(annotation)) {

			context.addReflectiveAccess(annotation.getDottedName(), Flag.allDeclaredConstructors,
					Flag.allDeclaredMethods, Flag.allDeclaredFields);
			context.addProxy(annotation.getDottedName(), "org.springframework.core.annotation.SynthesizedAnnotation");

			log.annotationFound(annotation);
		}
	}

	@Override
	public void printSummary() {
		log.printSummary();
	}

	private boolean isPartOfSpringData(Type type) {
		return type.isPartOfDomain(SPRING_DATA_NAMESPACE);
	}

	static class SpringDataComponentLog {

		private final boolean verbose;
		private final HashMap<Type, Type> repositoryInterfaces = new LinkedHashMap<>();
		private final Set<Type> annotations = new LinkedHashSet<>();
		private final Set<Type> customImplementations = new LinkedHashSet<>();

		private SpringDataComponentLog(boolean verbose) {
			this.verbose = verbose;
		}

		static SpringDataComponentLog instance() {
			return new SpringDataComponentLog(Boolean.valueOf(System.getProperty("spring.native.verbose", "false")));
		}

		void message(String msg) {

			if (verbose) {
				System.out.println(LOG_PREFIX + msg);
			}
		}

		void repositoryFoundForType(Type repo, Type domainType) {

			repositoryInterfaces.put(repo, domainType);
			message(String.format("Registering repository '%s' for type '%s'.", repo.getDottedName(), domainType.getDottedName()));
		}


		void annotationFound(Type annotation) {

			annotations.add(annotation);
			message(String.format("Registering annotation '%s'.", annotation.getDottedName()));
		}

		void customImplementationFound(Type repositoryInterface, Type customImplementation) {

			customImplementations.add(customImplementation);
			message(String.format("Registering custom repository implementation '%s' for '%s'.", customImplementation.getDottedName(), repositoryInterface.getDottedName()));
		}

		void printSummary() {

			if(repositoryInterfaces.isEmpty()) {
				message("No Spring Data repositories found.");
				return;
			}

			System.out.println(String.format(LOG_PREFIX + "Found %s repositories, %s custom implementations and registered %s annotations used by domain types.",
					repositoryInterfaces.size(), customImplementations.size(), annotations.size()));
		}
	}
}
