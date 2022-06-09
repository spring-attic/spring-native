/*
 * Copyright 2022 the original author or authors.
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
package app.main;

import java.util.Arrays;
import java.util.EventListener;

import com.zaxxer.hikari.util.ConcurrentBag;
import jakarta.persistence.*;
import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.hibernate.Session;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cache.spi.access.CollectionDataAccess;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;
import org.hibernate.event.spi.*;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlToken;
import org.hibernate.hql.internal.ast.tree.*;
import org.hibernate.id.Assigned;
import org.hibernate.id.ForeignGenerator;
import org.hibernate.id.GUIDGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.IncrementGenerator;
import org.hibernate.id.SelectGenerator;
import org.hibernate.id.SequenceHiLoGenerator;
import org.hibernate.id.SequenceIdentityGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.EntityManagerMessageLogger;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.query.QueryProducer;
import org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorBuilderImpl;
import org.hibernate.sql.ordering.antlr.GeneratedOrderByFragmentRendererTokenTypes;
import org.hibernate.tuple.Tuplizer;
import org.hibernate.tuple.component.DynamicMapComponentTuplizer;
import org.hibernate.tuple.component.PojoComponentTuplizer;
import org.hibernate.tuple.entity.AbstractEntityTuplizer;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.support.RuntimeHintsUtils;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Christoph Strobl
 * @since 2022/06
 */
public class RuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {

		// Embedded DB Support
		hints.reflection().registerTypes(Arrays.asList(TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabase"),
				TypeReference.of("org.springframework.jdbc.support.JdbcAccessor"),
				TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy"),
				TypeReference.of("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory.EmbeddedDataSourceProxy")
		), builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
				MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.DECLARED_FIELDS, MemberCategory.DECLARED_CLASSES));

		// for reading JPA Entities from the index
		hints.resources().registerPattern("META-INF/spring.components");

		// hibernate
		hints.resources().registerPattern("hibernate.properties");
		hints.resources().registerPattern("org/hibernate/.*.xsd");
		hints.resources().registerPattern("org/hibernate/.*.dtd");
		hints.resources().registerPattern("org/hibernate/jpa/.*.xsd");
		hints.resources().registerPattern("org/hibernate/orm/.*.xsd");
		hints.resources().registerPattern("org/hibernate/xsd/.*.xsd");
		hints.resources().registerPattern("org/hibernate/jpa/orm_2_0.xsd");
		hints.resources().registerPattern("org/hibernate/jpa/orm_2_1.xsd");
		hints.resources().registerPattern("org/hibernate/jpa/orm_2_2.xsd");
		hints.resources().registerPattern("org/hibernate/jpa/orm_3_0.xsd");
		hints.resources().registerPattern("org/hibernate/xsd/mapping/legacy-mapping-4.0.xsd");
		hints.resources().registerPattern("org/hibernate/xsd/mapping/mapping-2.1.0.xsd");
		hints.resources().registerPattern("org/hibernate/xsd/cfg/legacy-configuration-4.0.xsd");
		hints.resources().registerPattern("org/hibernate/hibernate-mapping-4.0.xsd");
		hints.resources().registerPattern("org/hibernate/hibernate-configuration-4.0.xsd");
		hints.resources().registerPattern("org/hibernate/hibernate-configuration-3.0.dtd");
		hints.resources().registerPattern("org/hibernate/hibernate-mapping-3.0.dtd");

		RuntimeHintsUtils.registerAnnotation(hints, Id.class);
		RuntimeHintsUtils.registerAnnotation(hints, GeneratedValue.class);
		RuntimeHintsUtils.registerAnnotation(hints, PersistenceContext.class);
		RuntimeHintsUtils.registerAnnotation(hints, MappedSuperclass.class);
		RuntimeHintsUtils.registerAnnotation(hints, Column.class);
		RuntimeHintsUtils.registerAnnotation(hints, ManyToOne.class);
		RuntimeHintsUtils.registerAnnotation(hints, OneToMany.class);
		RuntimeHintsUtils.registerAnnotation(hints, OneToOne.class);
		RuntimeHintsUtils.registerAnnotation(hints, JoinColumn.class);
		RuntimeHintsUtils.registerAnnotation(hints, Table.class);
		RuntimeHintsUtils.registerAnnotation(hints, Transient.class);
		RuntimeHintsUtils.registerAnnotation(hints, TableGenerator.class);
		RuntimeHintsUtils.registerAnnotation(hints, NamedEntityGraph.class);

		hints.reflection().registerTypes(Arrays.asList(
						TypeReference.of(DefaultFlowMessageFactory.class),
						TypeReference.of(ConcurrentBag.IConcurrentBagEntry[].class),
						TypeReference.of(ConcurrentBag.IConcurrentBagEntry.class),
						TypeReference.of(org.hibernate.query.spi.QueryImplementor.class),
						TypeReference.of(HqlTokenTypes.class),
						TypeReference.of(SqlTokenTypes.class),
						TypeReference.of(GeneratedOrderByFragmentRendererTokenTypes.class),
						TypeReference.of(org.hibernate.sql.ordering.antlr.NodeSupport.class),
						TypeReference.of(org.hibernate.sql.ordering.antlr.OrderByFragment.class),
						TypeReference.of(org.hibernate.sql.ordering.antlr.SortSpecification.class),
						TypeReference.of(org.hibernate.sql.ordering.antlr.SortKey.class),
						TypeReference.of(org.hibernate.sql.ordering.antlr.OrderingSpecification.class),
						TypeReference.of(org.hibernate.sql.ordering.antlr.CollationSpecification.class),
						TypeReference.of("antlr.CommonAST"),
						TypeReference.of("antlr.CommonToken"),
						TypeReference.of(EventType.class),
						TypeReference.of(EntityManagerFactory.class),
						TypeReference.of(SessionImpl.class),
						TypeReference.of(JdbcResourceLocalTransactionCoordinatorBuilderImpl.class),
						TypeReference.of(EntityManager.class),
						TypeReference.of(QueryProducer.class),
						TypeReference.of(HibernateEntityManager.class),
						TypeReference.of(HibernateEntityManagerFactory.class),
						TypeReference.of(ReusableMessageFactory.class),
						TypeReference.of(ParameterizedMessageFactory.class),
						TypeReference.of(PersistenceAnnotationBeanPostProcessor.class),
						TypeReference.of(PojoComponentTuplizer.class),
						TypeReference.of(DynamicMapComponentTuplizer.class),
						TypeReference.of(SingleTableEntityPersister.class),
						TypeReference.of(CollectionDataAccess.class),
						TypeReference.of(PersistentClass.class),
						TypeReference.of(PersisterCreationContext.class),
						TypeReference.of(SpringImplicitNamingStrategy.class),
						TypeReference.of("org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy"),
						TypeReference.of(CamelCaseToUnderscoresNamingStrategy.class),
						TypeReference.of(NoJtaPlatform.class),
						TypeReference.of(EntityManagerMessageLogger.class),
						TypeReference.of(Session.class),
						TypeReference.of(EventListener.class),
						TypeReference.of(EnumType.class),
						TypeReference.of(org.hibernate.type.EnumType.class),
						TypeReference.of(InheritanceType.class),
						TypeReference.of("org.hibernate.bytecode.enhance.spi.interceptor.BytecodeInterceptorLogging_$logger"),
						TypeReference.of("org.apache.logging.slf4j.SLF4JLoggerContextFactory"),
						TypeReference.of("org.hibernate.internal.EntityManagerMessageLogger_$logger"),
						TypeReference.of("org.hibernate.service.jta.platform.internal.NoJtaPlatform"),
						TypeReference.of("org.hibernate.annotations.common.util.impl.Log_$logger"),
						TypeReference.of("org.hibernate.annotations.common.util.impl.Log"),
						TypeReference.of("org.hibernate.cfg.beanvalidation.TypeSafeActivator"),
						TypeReference.of(org.hibernate.internal.CoreMessageLogger_$logger.class),
						TypeReference.of(Tuplizer.class),
						TypeReference.of(EntityTuplizer.class),
						TypeReference.of(AbstractEntityTuplizer.class),
						TypeReference.of(PojoEntityTuplizer.class),
						TypeReference.of(org.hibernate.internal.CoreMessageLogger_$logger.class),
						TypeReference.of(OneToManyPersister.class),
						TypeReference.of(JoinedSubclassEntityPersister.class),
						TypeReference.of(SingleTableEntityPersister.class),
						TypeReference.of(UnionSubclassEntityPersister.class),
						TypeReference.of(BasicCollectionPersister.class),
						TypeReference.of(AbstractMapComponentNode.class),
						TypeReference.of(AbstractNullnessCheckNode.class),
						TypeReference.of(AbstractRestrictableStatement.class),
						TypeReference.of(AbstractSelectExpression.class),
						TypeReference.of(AbstractStatement.class),
						TypeReference.of(AggregatedSelectExpression.class),
						TypeReference.of(AggregateNode.class),
						TypeReference.of(AssignmentSpecification.class),
						TypeReference.of(BetweenOperatorNode.class),
						TypeReference.of(BinaryArithmeticOperatorNode.class),
						TypeReference.of(BinaryLogicOperatorNode.class),
						TypeReference.of(BinaryOperatorNode.class),
						TypeReference.of(BooleanLiteralNode.class),
						TypeReference.of(CastFunctionNode.class),
						TypeReference.of(CollectionFunction.class),
						TypeReference.of(CollectionPathNode.class),
						TypeReference.of(CollectionPropertyReference.class),
						TypeReference.of(CollectionSizeNode.class),
						TypeReference.of(ComponentJoin.class),
						TypeReference.of(ConstructorNode.class),
						TypeReference.of(CountNode.class),
						TypeReference.of(DeleteStatement.class),
						TypeReference.of(DisplayableNode.class),
						TypeReference.of(DotNode.class),
						TypeReference.of(EntityJoinFromElement.class),
						TypeReference.of(ExpectedTypeAwareNode.class),
						TypeReference.of(FromClause.class),
						TypeReference.of(FromElement.class),
						TypeReference.of(FromElementFactory.class),
						TypeReference.of(FromReferenceNode.class),
						TypeReference.of(FunctionNode.class),
						TypeReference.of(HqlSqlWalkerNode.class),
						TypeReference.of(IdentNode.class),
						TypeReference.of(ImpliedFromElement.class),
						TypeReference.of(IndexNode.class),
						TypeReference.of(InitializeableNode.class),
						TypeReference.of(InLogicOperatorNode.class),
						TypeReference.of(InsertStatement.class),
						TypeReference.of(IntoClause.class),
						TypeReference.of(IsNotNullLogicOperatorNode.class),
						TypeReference.of(IsNullLogicOperatorNode.class),
						TypeReference.of(JavaConstantNode.class),
						TypeReference.of(LiteralNode.class),
						TypeReference.of(MapEntryNode.class),
						TypeReference.of(MapKeyEntityFromElement.class),
						TypeReference.of(MapKeyNode.class),
						TypeReference.of(MapValueNode.class),
						TypeReference.of(MethodNode.class),
						TypeReference.of(Node.class),
						TypeReference.of(NullNode.class),
						TypeReference.of(OperatorNode.class),
						TypeReference.of(OrderByClause.class),
						TypeReference.of(ParameterContainer.class),
						TypeReference.of(ParameterNode.class),
						TypeReference.of(PathNode.class),
						TypeReference.of(QueryNode.class),
						TypeReference.of(ResolvableNode.class),
						TypeReference.of(RestrictableStatement.class),
						TypeReference.of(ResultVariableRefNode.class),
						TypeReference.of(SearchedCaseNode.class),
						TypeReference.of(SelectClause.class),
						TypeReference.of(SelectExpression.class),
						TypeReference.of(SelectExpressionImpl.class),
						TypeReference.of(SelectExpressionList.class),
						TypeReference.of(SessionFactoryAwareNode.class),
						TypeReference.of(SimpleCaseNode.class),
						TypeReference.of(SqlFragment.class),
						TypeReference.of(SqlNode.class),
						TypeReference.of(Statement.class),
						TypeReference.of(TableReferenceNode.class),
						TypeReference.of(UnaryArithmeticNode.class),
						TypeReference.of(UnaryLogicOperatorNode.class),
						TypeReference.of(UnaryOperatorNode.class),
						TypeReference.of(UpdateStatement.class),
						TypeReference.of(HqlToken.class),
						TypeReference.of(ReplicateEventListener[].class),
						TypeReference.of(PostInsertEventListener[].class),
						TypeReference.of(PostLoadEventListener[].class),
						TypeReference.of(PostUpdateEventListener[].class),
						TypeReference.of(PreCollectionRecreateEventListener[].class),
						TypeReference.of(PreCollectionRemoveEventListener[].class),
						TypeReference.of(PreCollectionUpdateEventListener[].class),
						TypeReference.of(PreLoadEventListener[].class),
						TypeReference.of(PreUpdateEventListener[].class),
						TypeReference.of(RefreshEventListener[].class),
						TypeReference.of(SaveOrUpdateEventListener[].class),
						TypeReference.of(LockEventListener[].class),
						TypeReference.of(InitializeCollectionEventListener[].class),
						TypeReference.of(ResolveNaturalIdEventListener[].class),
						TypeReference.of(LoadEventListener[].class),
						TypeReference.of(FlushEntityEventListener[].class),
						TypeReference.of(FlushEventListener[].class),
						TypeReference.of(EvictEventListener[].class),
						TypeReference.of(DirtyCheckEventListener[].class),
						TypeReference.of(DeleteEventListener[].class),
						TypeReference.of(AutoFlushEventListener[].class),
						TypeReference.of(PersistEventListener[].class),
						TypeReference.of(EventType[].class),
						TypeReference.of(ClearEventListener[].class),
						TypeReference.of(MergeEventListener[].class),
						TypeReference.of(PostCollectionRecreateEventListener[].class),
						TypeReference.of(PostCollectionRemoveEventListener[].class),
						TypeReference.of(PostCollectionUpdateEventListener[].class),
						TypeReference.of(PostDeleteEventListener[].class),
						TypeReference.of(IdentityGenerator.class),
						TypeReference.of(SequenceStyleGenerator.class),
						TypeReference.of(SequenceIdentityGenerator.class),
						TypeReference.of(UUIDGenerator.class),
						TypeReference.of(GUIDGenerator.class),
						TypeReference.of(UUIDHexGenerator.class),
						TypeReference.of(Assigned.class),
						TypeReference.of(SelectGenerator.class),
						TypeReference.of(SequenceHiLoGenerator.class),
						TypeReference.of(IncrementGenerator.class),
						TypeReference.of(ForeignGenerator.class)
				),
				hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));

		hints.proxies().registerJdkProxy(org.hibernate.query.spi.QueryImplementor.class);
		hints.proxies().registerJdkProxy(org.hibernate.Session.class, org.springframework.orm.jpa.EntityManagerProxy.class);
		hints.proxies().registerJdkProxy(org.hibernate.SessionFactory.class, org.springframework.orm.jpa.EntityManagerFactoryInfo.class);
		hints.proxies().registerJdkProxy(org.hibernate.jpa.HibernateEntityManagerFactory.class, org.springframework.orm.jpa.EntityManagerFactoryInfo.class);
		hints.proxies().registerJdkProxy(org.hibernate.query.Query.class, org.hibernate.query.spi.QueryImplementor.class);

		// hibernate H2 support
		hints.reflection().registerType(H2Dialect.class, hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));

		// TX Support
		hints.proxies().registerJdkProxy(TypeReference.of(Transactional.class),
				TypeReference.of("org.springframework.core.annotation.SynthesizedAnnotation"));
		hints.reflection().registerType(TypeReference.of(Transactional.class),
				builder -> builder.withMembers(MemberCategory.PUBLIC_FIELDS, MemberCategory.DECLARED_CLASSES,
						MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
		hints.reflection()
				.registerTypes(Arrays.asList(TypeReference.of(Propagation.class),
						TypeReference.of(org.springframework.transaction.annotation.Isolation.class),
						TypeReference.of(org.springframework.transaction.annotation.Propagation.class),
						TypeReference.of(org.springframework.jdbc.datasource.ConnectionProxy.class),
						TypeReference.of(org.springframework.jdbc.support.JdbcAccessor.class),
						TypeReference.of(org.springframework.jdbc.support.JdbcTransactionManager.class),
						TypeReference.of(TransactionDefinition.class)

				), builder -> builder.withMembers(MemberCategory.PUBLIC_FIELDS, MemberCategory.DECLARED_CLASSES,
						MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_METHODS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));

		hints.reflection().registerTypes(
				Arrays.asList(TypeReference.of(AutoProxyRegistrar.class),
						TypeReference.of(ProxyTransactionManagementConfiguration.class),
						TypeReference
								.of("org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor$1")),
				builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS,
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
	}
}
