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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeProcessor;
import org.springframework.nativex.type.TypeSystem;
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
 * {@link org.springframework.nativex.type.ComponentProcessor} and that is how it
 * is picked up by the feature runtime.
 *
 * @author Andy Clement
 * @author Christoph Strobl
 * @author Jens Schauder
 */
public class SpringDataComponentProcessor implements ComponentProcessor {

	private static Log logger = LogFactory.getLog(SpringDataComponentProcessor.class);

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
					"org.springframework.data.repository.reactive.ReactiveSortingRepository",

					// Kotlin
					"org.springframework.data.repository.kotlin.CoroutineCrudRepository",
					"org.springframework.data.repository.kotlin.CoroutineSortingRepository"
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

	public static final String REPOSITORY_REST_RESOURCE_DESCRIPTOR = "Lorg/springframework/data/rest/core/annotation/RepositoryRestResource;";

	private static String repositoryName;
	private static String queryAnnotationName;

	private final SpringDataComponentLog log = SpringDataComponentLog.instance();
	private Set<String> keysSeen = new HashSet<>();

	private final TypeProcessor typeProcessor = new TypeProcessor(

			(type, nativeContext) -> {

				// TODO: should we filter out "javax.persistence.Entity" because those are handled by the JpaComponentProcessor?

				if (type.isPartOfDomain(SPRING_DATA_DOMAIN_NAMESPACE) || TypeProcessor.isExcludedByDefault(type) || type.isPartOfDomain("kotlinx.coroutines.")) {
					return false;
				}
				return true;
			},
			this::registerDomainTypeInConfiguration,
			this::registerSpringDataAnnotationInConfiguration
	).named(LOG_PREFIX);

	static {
		try {
			repositoryName = "org.springframework.data.repository.Repository";
			queryAnnotationName = "org.springframework.data.annotation.QueryAnnotation";
		} catch (NoClassDefFoundError ncdfe) {
			// This component processor isn't useful anyway in this run, so OK that these
			// aren't here
		}
	}

	@Override
	public boolean handle(NativeContext imageContext, String key, List<String> values) {
		return repositoryName != null && values.contains(repositoryName);
	}

	@Override
	public void process(NativeContext imageContext, String key, List<String> values) {

		keysSeen.add(key);

		try {

			Type repositoryType = imageContext.getTypeSystem().resolveName(key);
			Type repositoryDomainType = resolveRepositoryDomainType(repositoryType, imageContext.getTypeSystem());

			if (repositoryDomainType == null) {
				// give up!
				logger.debug(LOG_PREFIX + "Unable to work out repository contents for repository " + key);
				return;
			}

			log.repositoryFoundForType(repositoryType, repositoryDomainType);

			registerRepositoryInterface(repositoryType, imageContext);
			registerDomainType(repositoryDomainType, imageContext);
			registerQueryMethodResultTypes(repositoryType, repositoryDomainType, imageContext);
			detectCustomRepositoryImplementations(repositoryType, imageContext);
		} catch (Throwable t) {
			logger.debug("WARNING: Problem with SpringDataComponentProcessor: " + t.getMessage());
		}
	}

	private void registerRepositoryInterface(Type repositoryType, NativeContext imageContext) {

		imageContext.addReflectiveAccess(repositoryType, Flag.allPublicMethods, Flag.allDeclaredConstructors);

		{ // proxy configuration

			// as is
			imageContext.addProxy(repositoryType.getDottedName(), "org.springframework.aop.SpringProxy", "org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy");

			// transactional
			imageContext.addProxy(repositoryType.getDottedName(), repositoryName, "org.springframework.transaction.interceptor.TransactionalProxy",
					"org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy");

			/*
			 * Generated proxies extend Proxy and Proxy implements Serializable. So the call:
			 * 	    protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
			 * 		    Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, getProxyClassLoader());
			 * is going to find targetInterfaces contains Serializable when called on the beanClass.
			 */
			if (repositoryType.isAtComponent()) {
				imageContext.addProxy(repositoryType.getDottedName(), repositoryName, "org.springframework.transaction.interceptor.TransactionalProxy",
						"org.springframework.aop.framework.Advised", "org.springframework.core.DecoratingProxy", "java.io.Serializable");
			}
		}

		// reactive repo
		if (repositoryType.implementsInterface("org/springframework/data/repository/reactive/ReactiveCrudRepository")) {
			imageContext.initializeAtBuildTime(repositoryType); // TODO: check if we really need this!
		}

		// kotlin
		if (repositoryType.implementsInterface("org/springframework/data/repository/kotlin/CoroutineCrudRepository")) {

			logger.debug(LOG_PREFIX + "kotlin.Coroutine repository detected. Adding kotlin Flow, Iterable, Unit, Long and Boolean.");

			imageContext.addReflectiveAccess("java.lang.Iterable", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
			imageContext.addReflectiveAccess("kotlinx.coroutines.flow.Flow", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
			imageContext.addReflectiveAccess("kotlin.collections.Iterable", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
			imageContext.addReflectiveAccess("kotlin.Unit", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
			imageContext.addReflectiveAccess("kotlin.Long", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
			imageContext.addReflectiveAccess("kotlin.Boolean", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
		}
	}

	private void detectCustomRepositoryImplementations(Type repositoryType, NativeContext imageContext) {

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

			imageContext.addReflectiveAccessHierarchy(customImpl, AccessBits.DECLARED_CONSTRUCTORS | AccessBits.DECLARED_METHODS);

			for (Method method : customImpl.getMethods()) {
				for (Type signatureType : method.getSignatureTypes(true)) {
					registerDomainType(signatureType, imageContext);
				}
			}
		}
	}

	private void registerQueryMethodResultTypes(Type repositoryType, Type repositoryDomainType, NativeContext imageContext) {

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


	private void registerDomainType(Type domainType, NativeContext imageContext) {
		typeProcessor.process(domainType, imageContext);
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

	private void registerSpringDataAnnotations(Method method, NativeContext context) {

		for (Type annotation : method.getAnnotationTypes()) {
			registerSpringDataAnnotationInConfiguration(annotation, context);
		}

		if (method.getParameterCount() == 0) {
			return;
		}

		// lookup for parameter annotations like @Param
		for (int i = 0; i < method.getParameterCount(); i++) {
			method.getParameterAnnotationTypes(i).forEach(it -> registerSpringDataAnnotationInConfiguration(it, context));
		}
	}

	private void registerDomainTypeInConfiguration(Type type, NativeContext nativeContext) {

		if (type.isPartOfDomain("sun.") || type.isPartOfDomain("jdk.")) {
			return;
		}

		log.message("Adding reflective access to " + type.getDottedName());

		nativeContext.addReflectiveAccess(type.getDottedName(), Flag.allDeclaredConstructors, Flag.allDeclaredMethods,
				Flag.allDeclaredFields);
	}

	private void registerSpringDataAnnotationInConfiguration(Type annotation, NativeContext context) {

		if (!context.hasReflectionConfigFor(annotation)) {

			if (isPartOfSpringData(annotation)) {

				context.addReflectiveAccess(annotation.getDottedName(), AccessBits.ANNOTATION);
				context.addProxy(annotation.getDottedName(), "org.springframework.core.annotation.SynthesizedAnnotation");
			}

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
				logger.debug(LOG_PREFIX + msg);
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

			if (repositoryInterfaces.isEmpty()) {
				message("No Spring Data repositories found.");
				return;
			}

			logger.debug(String.format(LOG_PREFIX + "Found %s repositories, %s custom implementations and registered %s annotations used by domain types.",
					repositoryInterfaces.size(), customImplementations.size(), annotations.size()));
		}
	}
}
