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

package org.springframework.boot.autoconfigure.orm.jpa;

import com.zaxxer.hikari.util.ConcurrentBag;
import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.hibernate.Session;
import org.hibernate.annotations.Tuplizer;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cache.spi.access.CollectionDataAccess;
import org.hibernate.dialect.*;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;
import org.hibernate.event.spi.*;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlToken;
import org.hibernate.hql.internal.ast.tree.*;
import org.hibernate.id.*;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.EntityManagerMessageLogger;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jpa.HibernateEntityManager;
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
import org.hibernate.tuple.component.DynamicMapComponentTuplizer;
import org.hibernate.tuple.component.PojoComponentTuplizer;
import org.hibernate.tuple.entity.AbstractEntityTuplizer;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.hibernate.type.EnumType;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.EventListener;

@SuppressWarnings("deprecation")
@NativeHint(trigger = HibernateJpaAutoConfiguration.class, types = {
		@TypeHint(types = {DefaultFlowMessageFactory.class, ConcurrentBag.IConcurrentBagEntry[].class, ConcurrentBag.IConcurrentBagEntry.class})
})
@NativeHint(trigger = HibernateJpaConfiguration.class,
		jdkProxies = @JdkProxyHint(types = org.hibernate.query.spi.QueryImplementor.class),
		resources = {
				@ResourceHint(patterns = {"hibernate.properties", "org/hibernate/.*.xsd", "org/hibernate/.*.dtd"})
		},
		types = {
				@TypeHint(types = {
						// petclinic
						// These three are due to org.hibernate.hql.internal.ast.util.TokenPrinters
						HqlTokenTypes.class, SqlTokenTypes.class, GeneratedOrderByFragmentRendererTokenTypes.class,
				}, access = TypeAccess.DECLARED_CONSTRUCTORS),
				@TypeHint(
						/*  Required types to support maintaining order in element collection.
						 *
						 *	@OrderBy
						 *	@ElementCollection
						 *	private List<String> phoneNumbers;
						 */
						types = {
							org.hibernate.sql.ordering.antlr.NodeSupport.class,
							org.hibernate.sql.ordering.antlr.OrderByFragment.class,
							org.hibernate.sql.ordering.antlr.SortSpecification.class,
							org.hibernate.sql.ordering.antlr.SortKey.class,
							org.hibernate.sql.ordering.antlr.OrderingSpecification.class,
							org.hibernate.sql.ordering.antlr.CollationSpecification.class
						},
						typeNames = {
							"antlr.CommonAST",
							"antlr.CommonToken",
				}),
				@TypeHint(types = {
						Id.class, GeneratedValue.class, Repository.class,
						PersistenceContext.class, MappedSuperclass.class, Column.class, ManyToOne.class, OneToMany.class,
						OneToOne.class, JoinColumn.class, Table.class, Transient.class
				}),
				@TypeHint(types = EventType.class, access = TypeAccess.DECLARED_FIELDS),
				@TypeHint(types = EntityManagerFactory.class, methods = @MethodHint(name = "getMetamodel")),
				@TypeHint(types = {
						SessionImpl.class,
						JdbcResourceLocalTransactionCoordinatorBuilderImpl.class,
						EntityManager.class,
						QueryProducer.class,
						HibernateEntityManager.class,
						HibernateEntityManagerFactory.class,

						ReusableMessageFactory.class,
						// TODO expose package contents again?
						ParameterizedMessageFactory.class,
						PersistenceAnnotationBeanPostProcessor.class,

						// ComponentTuplizerFactory
						PojoComponentTuplizer.class,
						DynamicMapComponentTuplizer.class,

						SingleTableEntityPersister.class, CollectionDataAccess.class, PersistentClass.class,
						PersisterCreationContext.class,

						// related to Naming.applyNamingStrategies
						SpringImplicitNamingStrategy.class, SpringPhysicalNamingStrategy.class, CamelCaseToUnderscoresNamingStrategy.class,

						//	HikariDataSource.class,HikariConfig.class,	MVTableEngine.class // pushed to datasource hints
						NoJtaPlatform.class,
						//org.hibernate.service.jta.platform.internal.NoJtaPlatform.class
						EntityManagerMessageLogger.class, // CoreMessageLogger.class,
						Session.class, EventListener.class,

						EnumType.class,
						InheritanceType.class,
				},typeNames = {
						"org.hibernate.bytecode.enhance.spi.interceptor.BytecodeInterceptorLogging_$logger",
						"org.hibernate.internal.EntityManagerMessageLogger_$logger",
						"org.hibernate.service.jta.platform.internal.NoJtaPlatform",
						"org.hibernate.annotations.common.util.impl.Log_$logger",
						"org.hibernate.annotations.common.util.impl.Log",
				}),
				@TypeHint(typeNames = {
						"org.hibernate.cfg.beanvalidation.TypeSafeActivator"
				}, access = { TypeAccess.PUBLIC_METHODS }),
				@TypeHint(types = {
						SimpleJpaRepository.class
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
				@TypeHint(types = {org.hibernate.internal.CoreMessageLogger_$logger.class,
						// These from EntityTuplizerFactory
						Tuplizer.class, EntityTuplizer.class, AbstractEntityTuplizer.class, PojoEntityTuplizer.class,
				}, access = TypeAccess.DECLARED_CONSTRUCTORS),
				// Persisters
				@TypeHint(types = {org.hibernate.internal.CoreMessageLogger_$logger.class,
						OneToManyPersister.class, JoinedSubclassEntityPersister.class, SingleTableEntityPersister.class, UnionSubclassEntityPersister.class,
						BasicCollectionPersister.class,
				}, access = TypeAccess.DECLARED_CONSTRUCTORS),
				// AST nodes from the org.hibernate.hql.internal.ast.tree package
				// These are necessary when HQL/JPQL queries are used.
				@TypeHint(types = {
						AbstractMapComponentNode.class,
						AbstractNullnessCheckNode.class,
						AbstractRestrictableStatement.class,
						AbstractSelectExpression.class,
						AbstractStatement.class,
						AggregatedSelectExpression.class,
						AggregateNode.class,
						AssignmentSpecification.class,
						BetweenOperatorNode.class,
						BinaryArithmeticOperatorNode.class,
						BinaryLogicOperatorNode.class,
						BinaryOperatorNode.class,
						BooleanLiteralNode.class,
						CastFunctionNode.class,
						CollectionFunction.class,
						CollectionPathNode.class,
						CollectionPropertyReference.class,
						CollectionSizeNode.class,
						ComponentJoin.class,
						ConstructorNode.class,
						CountNode.class,
						DeleteStatement.class,
						DisplayableNode.class,
						DotNode.class,
						EntityJoinFromElement.class,
						ExpectedTypeAwareNode.class,
						FromClause.class,
						FromElement.class,
						FromElementFactory.class,
						FromReferenceNode.class,
						FunctionNode.class,
						HqlSqlWalkerNode.class,
						IdentNode.class,
						ImpliedFromElement.class,
						IndexNode.class,
						InitializeableNode.class,
						InLogicOperatorNode.class,
						InsertStatement.class,
						IntoClause.class,
						IsNotNullLogicOperatorNode.class,
						IsNullLogicOperatorNode.class,
						JavaConstantNode.class,
						LiteralNode.class,
						MapEntryNode.class,
						MapKeyEntityFromElement.class,
						MapKeyNode.class,
						MapValueNode.class,
						MethodNode.class,
						Node.class,
						NullNode.class,
						OperatorNode.class,
						OrderByClause.class,
						ParameterContainer.class,
						ParameterNode.class,
						PathNode.class,
						QueryNode.class,
						ResolvableNode.class,
						RestrictableStatement.class,
						ResultVariableRefNode.class,
						SearchedCaseNode.class,
						SelectClause.class,
						SelectExpression.class,
						SelectExpressionImpl.class,
						SelectExpressionList.class,
						SessionFactoryAwareNode.class,
						SimpleCaseNode.class,
						SqlFragment.class,
						SqlNode.class,
						Statement.class,
						TableReferenceNode.class,
						UnaryArithmeticNode.class,
						UnaryLogicOperatorNode.class,
						UnaryOperatorNode.class,
						UpdateStatement.class,
						// other parsing related classes that seem necessary.
						HqlToken.class
				}, access = TypeAccess.DECLARED_CONSTRUCTORS),
				// EventListener
				@TypeHint(types = {
						ReplicateEventListener[].class,
						PostInsertEventListener[].class, PostLoadEventListener[].class, PostUpdateEventListener[].class,
						PreCollectionRecreateEventListener[].class, PreCollectionRemoveEventListener[].class, PreCollectionUpdateEventListener[].class,
						PreLoadEventListener[].class, PreUpdateEventListener[].class, RefreshEventListener[].class, SaveOrUpdateEventListener[].class,
						LockEventListener[].class, InitializeCollectionEventListener[].class, ResolveNaturalIdEventListener[].class,
						LoadEventListener[].class, FlushEntityEventListener[].class, FlushEventListener[].class,
						EvictEventListener[].class, DirtyCheckEventListener[].class, DeleteEventListener[].class,
						AutoFlushEventListener[].class, PersistEventListener[].class, EventType[].class,
						ClearEventListener[].class, MergeEventListener[].class, PostCollectionRecreateEventListener[].class,
						PostCollectionRemoveEventListener[].class, PostCollectionUpdateEventListener[].class, PostDeleteEventListener[].class,

				}, access = TypeAccess.DECLARED_CONSTRUCTORS),
				// Id Generators
				@TypeHint(types = {
						IdentityGenerator.class, SequenceStyleGenerator.class,
						SequenceIdentityGenerator.class,
						UUIDGenerator.class, GUIDGenerator.class, UUIDHexGenerator.class, Assigned.class,
						SelectGenerator.class, SequenceHiLoGenerator.class, IncrementGenerator.class,
						ForeignGenerator.class,
						TableGenerator.class,
				}, access = TypeAccess.DECLARED_CONSTRUCTORS),
		})
// Dialects
// In memory
@NativeHint(trigger = org.h2.Driver.class, types = @TypeHint(types = H2Dialect.class, access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = org.hsqldb.jdbc.JDBCDriver.class, types = @TypeHint(types = HSQLDialect.class, access = TypeAccess.DECLARED_CONSTRUCTORS))
// Open Source
@NativeHint(trigger = com.mysql.cj.jdbc.Driver.class, types = @TypeHint(types = {MySQL8Dialect.class, MySQL57Dialect.class, MySQL55Dialect.class, MySQL5Dialect.class, MySQLDialect.class}, access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = org.mariadb.jdbc.Driver.class, types = @TypeHint(types = {MariaDB103Dialect.class, MariaDB102Dialect.class, MariaDB10Dialect.class, MariaDB53Dialect.class, MariaDBDialect.class}, access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = org.postgresql.Driver.class, types = @TypeHint(types = {PostgreSQL10Dialect.class,
		PostgreSQL95Dialect.class, PostgreSQL94Dialect.class, PostgreSQL93Dialect.class, PostgreSQL92Dialect.class, PostgreSQL91Dialect.class, PostgreSQL9Dialect.class,
		PostgresPlusDialect.class,
		PostgreSQL82Dialect.class, PostgreSQL81Dialect.class,
}, access = TypeAccess.DECLARED_CONSTRUCTORS))
// Commercial
@NativeHint(trigger = com.microsoft.sqlserver.jdbc.SQLServerDriver.class, types = @TypeHint(types = {SQLServer2012Dialect.class, SQLServer2008Dialect.class, SQLServer2005Dialect.class, SQLServerDialect.class,}, access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = oracle.jdbc.OracleDriver.class, types = @TypeHint(types = {Oracle12cDialect.class, Oracle10gDialect.class, Oracle9iDialect.class, Oracle8iDialect.class,}, access = TypeAccess.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = com.ibm.db2.jcc.DB2Driver.class, types = @TypeHint(types = {DB297Dialect.class,
		DB2390V8Dialect.class, DB2390Dialect.class,
		DB2400V7R3Dialect.class, DB2400Dialect.class,
		DB2Dialect.class,
}, access = TypeAccess.DECLARED_CONSTRUCTORS))
public class HibernateJpaHints implements NativeConfiguration {
}
