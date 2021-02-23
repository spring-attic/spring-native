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
package org.springframework.boot.autoconfigure.orm.jpa;

import com.zaxxer.hikari.util.ConcurrentBag;
import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.hibernate.Session;
import org.hibernate.annotations.Tuplizer;
import org.hibernate.cache.spi.access.CollectionDataAccess;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQLDialect;
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
import org.hibernate.tuple.entity.AbstractEntityTuplizer;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourcesInfo;
import org.springframework.nativex.hint.TypeInfo;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.EventListener;

@SuppressWarnings("deprecation")
@NativeHint(trigger = HibernateJpaAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {DefaultFlowMessageFactory.class, ConcurrentBag.IConcurrentBagEntry[].class, ConcurrentBag.IConcurrentBagEntry.class})
})
@NativeHint(trigger = HibernateJpaConfiguration.class,
		resourcesInfos = {
				@ResourcesInfo(patterns = {"hibernate.properties", "org/hibernate/.*.xsd", "org/hibernate/.*.dtd"})
		},
		typeInfos = {
				@TypeInfo(types = {
						// petclinic
						// These three are due to org.hibernate.hql.internal.ast.util.TokenPrinters
						HqlTokenTypes.class, SqlTokenTypes.class, GeneratedOrderByFragmentRendererTokenTypes.class,
				}, access = AccessBits.DECLARED_CONSTRUCTORS),
				@TypeInfo(types = {
						// petclinic
						Repository.class,
						PersistenceContext.class, MappedSuperclass.class, Column.class, ManyToOne.class, JoinColumn.class, Table.class, Transient.class
				}, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS),
				@TypeInfo(types = {
						SessionImpl.class,
						EventType.class,

						JdbcResourceLocalTransactionCoordinatorBuilderImpl.class,
						SimpleJpaRepository.class,
						EntityManagerFactory.class,
						EntityManager.class,
						QueryProducer.class,
						HibernateEntityManager.class,
						HibernateEntityManagerFactory.class,
						GeneratedValue.class, Id.class,
						ReusableMessageFactory.class,
						// TODO expose package contents again?
						ParameterizedMessageFactory.class,
						PersistenceAnnotationBeanPostProcessor.class,

						SingleTableEntityPersister.class, CollectionDataAccess.class, PersistentClass.class,
						PersisterCreationContext.class,

						// related to Naming.applyNamingStrategies
						SpringImplicitNamingStrategy.class, SpringPhysicalNamingStrategy.class,

						//	HikariDataSource.class,HikariConfig.class,	MVTableEngine.class // pushed to datasource hints
						NoJtaPlatform.class,
						//org.hibernate.service.jta.platform.internal.NoJtaPlatform.class
						EntityManagerMessageLogger.class, // CoreMessageLogger.class,
						Session.class, EventListener.class
				}, typeNames = {
						"org.hibernate.internal.EntityManagerMessageLogger_$logger",
						"org.hibernate.service.jta.platform.internal.NoJtaPlatform",
						"org.hibernate.annotations.common.util.impl.Log_$logger",
						"org.hibernate.annotations.common.util.impl.Log",
				}),
				@TypeInfo(types = DataSourceInitializedPublisher.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_FIELDS),
				@TypeInfo(types = {org.hibernate.internal.CoreMessageLogger_$logger.class,
						// These from EntityTuplizerFactory
						Tuplizer.class, EntityTuplizer.class, AbstractEntityTuplizer.class, PojoEntityTuplizer.class,
				}, access = AccessBits.DECLARED_CONSTRUCTORS),
				// Persisters
				@TypeInfo(types = {org.hibernate.internal.CoreMessageLogger_$logger.class,
						OneToManyPersister.class, JoinedSubclassEntityPersister.class, SingleTableEntityPersister.class, UnionSubclassEntityPersister.class,
						BasicCollectionPersister.class,
				}, access = AccessBits.DECLARED_CONSTRUCTORS),

				// AST nodes from the org.hibernate.hql.internal.ast.tree package
				// These are necessary when HQL/JPQL queries are used.
				@TypeInfo(types = {
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
				}, access = AccessBits.DECLARED_CONSTRUCTORS),
				// EventListener
				@TypeInfo(types = {
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

				}, access = AccessBits.DECLARED_CONSTRUCTORS),
				// Id Generators
				@TypeInfo(types = {
						IdentityGenerator.class, SequenceStyleGenerator.class,
						SequenceIdentityGenerator.class,
						UUIDGenerator.class, GUIDGenerator.class, UUIDHexGenerator.class, Assigned.class,
						SelectGenerator.class, SequenceHiLoGenerator.class, IncrementGenerator.class,
						ForeignGenerator.class,
						TableGenerator.class,
				}, access = AccessBits.DECLARED_CONSTRUCTORS),
		})
// Dialects
@NativeHint(trigger = org.h2.Driver.class, typeInfos = @TypeInfo(types = H2Dialect.class, access = AccessBits.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = com.mysql.cj.jdbc.Driver.class, typeInfos = @TypeInfo(types = MySQLDialect.class, access = AccessBits.DECLARED_CONSTRUCTORS))
@NativeHint(trigger = org.hsqldb.jdbc.JDBCDriver.class, typeInfos = @TypeInfo(types = HSQLDialect.class, access = AccessBits.DECLARED_CONSTRUCTORS))
public class HibernateJpaHints implements NativeConfiguration {
}
