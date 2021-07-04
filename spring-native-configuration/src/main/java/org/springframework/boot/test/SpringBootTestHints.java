package org.springframework.boot.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeProcessor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@NativeHint(trigger = org.junit.jupiter.api.Test.class, types = {
		@TypeHint(typeNames = {
				"org.springframework.boot.test.context.ImportsContextCustomizer$ImportsSelector",
				"org.springframework.boot.autoconfigure.ImportAutoConfigurationImportSelector"
		}, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.RESOURCE),
		@TypeHint(types = {
				SpringBootTest.WebEnvironment.class,
				org.springframework.test.context.junit.jupiter.SpringExtension.class,
				org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.class,
				org.springframework.test.context.support.DefaultBootstrapContext.class,
				org.springframework.boot.test.context.SpringBootTestContextBootstrapper.class,
				org.springframework.boot.test.context.SpringBootContextLoader.class,
				org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener.class,
				org.springframework.boot.test.mock.mockito.MockitoPostProcessor.class,
				ImportAutoConfiguration.class,
				OverrideAutoConfiguration.class,
				TypeExcludeFilters.class
		}, typeNames = {
				"org.springframework.boot.autoconfigure.test.ImportAutoConfiguration",
				"org.springframework.boot.test.mock.mockito.MockitoPostProcessor$SpyPostProcessor",
				"org.springframework.boot.test.context.ImportsContextCustomizer$ImportsCleanupPostProcessor"
		}),
		@TypeHint(types = {
				org.springframework.boot.test.context.SpringBootTest.class,
				ActiveProfiles.class,
				org.springframework.test.context.web.WebAppConfiguration.class,
				org.springframework.test.context.BootstrapWith.class,
				AutoConfigureMockMvc.class,
				SpringBootConfiguration.class,
				TestPropertySource.class
		}, access = AccessBits.ANNOTATION)
}, jdkProxies = {
		@JdkProxyHint(types = { org.springframework.test.context.BootstrapWith.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.boot.test.context.SpringBootTest.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.context.annotation.Import.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(types = { org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters.class, org.springframework.core.annotation.SynthesizedAnnotation.class }),
		@JdkProxyHint(typeNames = { "org.springframework.context.annotation.ComponentScan$Filter", "org.springframework.core.annotation.SynthesizedAnnotation" })
})
@NativeHint(trigger = TestDatabaseAutoConfiguration.class, types = @TypeHint(typeNames = "org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration$EmbeddedDataSourceFactoryBean"))
// TODO Move to Spring Security (test) hint
@NativeHint(trigger = WithSecurityContext.class, types = {
		@TypeHint(types = {
				SecurityContextPersistenceFilter.class
		}, access = AccessBits.FULL_REFLECTION),
		@TypeHint(types = org.springframework.security.web.csrf.CsrfFilter.class, access = AccessBits.FULL_REFLECTION, fields = @FieldHint(name = "tokenRepository", allowWrite = true)),
		@TypeHint(types = FilterChainProxy.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_METHODS),
		@TypeHint(types = WithSecurityContext.class, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS),
		@TypeHint(typeNames = "org.springframework.security.test.context.support.WithMockUserSecurityContextFactory")
}, jdkProxies = {@JdkProxyHint(types = { WithSecurityContext.class, org.springframework.core.annotation.SynthesizedAnnotation.class })
})
public class SpringBootTestHints implements NativeConfiguration {

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {
		Type test = typeSystem.resolve("org/junit/jupiter/api/Test", true);
		List<HintDeclaration> hints = new ArrayList<>();
		if (test != null)  {
			if ((typeSystem.resolve("org/springframework/data/cassandra/SessionFactory", true) != null) ||
					(typeSystem.resolve("org/springframework/data/cassandra/ReactiveSession", true) != null)) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest", new AccessDescriptor(AccessBits.RESOURCE));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/data/jdbc/repository/config/AbstractJdbcConfiguration", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/ldap/core/ContextSource", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if ((typeSystem.resolve("com/mongodb/client/MongoClient", true) != null) ||
					(typeSystem.resolve("com/mongodb/reactivestreams/client/ClientSession", true) != null)) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/neo4j/driver/Driver", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/data/r2dbc/core/R2dbcEntityTemplate", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if ((typeSystem.resolve("org/springframework/data/redis/core/RedisOperations", true) != null) ||
					(typeSystem.resolve("org/springframework/data/redis/core/ReactiveRedisTemplate", true) != null)) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.redis.DataRedisTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.data.redis.DataRedisTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/jdbc/core/JdbcTemplate", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.jdbc.JdbcTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.jdbc.JdbcTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.jdbc.JdbcTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/jooq/ConnectionProvider", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.jooq.JooqTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.jooq.JooqTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.jooq.JooqTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if ((typeSystem.resolve("com/google/gson/Gson", true) != null) ||
					(typeSystem.resolve("com/fasterxml/jackson/databind/ObjectMapper", true) != null) ||
					(typeSystem.resolve("javax/json/bind/Jsonb", true) != null)) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.json.JsonTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.json.JsonTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.json.JsonTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/web/reactive/function/client/WebClient", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.client.RestClientTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.client.RestClientTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.client.RestClientTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/ws/client/core/WebServiceTemplate", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/data/jpa/repository/JpaRepository", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest", new AccessDescriptor(AccessBits.ANNOTATION));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/web/servlet/DispatcherServlet", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("org/springframework/web/reactive/result/view/ViewResolver", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest", new AccessDescriptor(AccessBits.ALL));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTestContextBootstrapper", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTypeExcludeFilter", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
			if (typeSystem.resolve("reactor/core/publisher/Traces", true) != null) {
				HintDeclaration hintDeclaration = new HintDeclaration();
				hintDeclaration.addDependantType("reactor.core.publisher.Traces$StackWalkerCallSiteSupplierFactory", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("reactor.core.publisher.Traces$SharedSecretsCallSiteSupplierFactory", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hintDeclaration.addDependantType("reactor.core.publisher.Traces$ExceptionCallSiteSupplierFactory", new AccessDescriptor(AccessBits.LOAD_AND_CONSTRUCT));
				hints.add(hintDeclaration);
			}
		}
		List<HintDeclaration> testHints = TypeProcessor.namedProcessor("Tests")
				.skipTypesMatching(type -> type.getMethodsWithAnnotationName("org.junit.jupiter.api.Test", false).isEmpty())
				.skipAnnotationInspection()
				.limitInspectionDepth(0)
				.onTypeDiscovered((type, context) -> {
					context.addReflectiveAccess(type, new AccessDescriptor(AccessBits.ALL));
				})
				.use(typeSystem)
				.processTypes();
		hints.addAll(testHints);
		return hints;
	}
}
