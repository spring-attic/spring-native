package org.springframework.boot.autoconfigure.orm.jpa;

import java.util.EventListener;

import javax.persistence.TableGenerator;

import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.hibernate.Session;
import org.hibernate.cache.spi.access.CollectionDataAccess;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.ClearEventListener;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.DirtyCheckEventListener;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.InitializeCollectionEventListener;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.event.spi.LockEventListener;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreCollectionRecreateEventListener;
import org.hibernate.event.spi.PreCollectionRemoveEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.event.spi.RefreshEventListener;
import org.hibernate.event.spi.ReplicateEventListener;
import org.hibernate.event.spi.ResolveNaturalIdEventListener;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
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
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.EntityManagerMessageLogger;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.xml.config.ValidationBootstrapParameters;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;

import com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry;

/*
// TODO review this - what is the right way? That type is only needed if (strictly speaking) XMLEventFactory is used
proposedHints.put("Lorg/springframework/boot/autoconfigure/orm/jpa/HibernateJpaAutoConfiguration;",
		new CompilationHint(false, false, new String[] {
				"com.sun.xml.internal.stream.events.XMLEventFactoryImpl:REGISTRAR",
				"org.apache.logging.log4j.message.DefaultFlowMessageFactory:REGISTRAR",
				"com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry[]:REGISTRAR",
				"com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry:REGISTRAR"
		}));
		*/
@ConfigurationHint(value=HibernateJpaAutoConfiguration.class,typeInfos= {
	@TypeInfo(typeNames= {"com.sun.xml.internal.stream.events.XMLEventFactoryImpl"},types= {DefaultFlowMessageFactory.class,IConcurrentBagEntry[].class,IConcurrentBagEntry.class})	
})
@ConfigurationHint(value=HibernateJpaConfiguration.class,typeInfos= {
		@TypeInfo(types= { H2Dialect.class,
				EventType.class,
				// Related to EventListenerRegistry...
				SaveOrUpdateEventListener.class,ResolveNaturalIdEventListener.class,
				ReplicateEventListener.class,RefreshEventListener.class,PreUpdateEventListener.class,
				PreLoadEventListener.class,PreInsertEventListener.class,PreDeleteEventListener.class,
				PreCollectionUpdateEventListener.class,PreCollectionRecreateEventListener.class,
				PreCollectionRemoveEventListener.class,PostUpdateEventListener.class,
				PostLoadEventListener.class,PostInsertEventListener.class,PostDeleteEventListener.class,
				PostCollectionUpdateEventListener.class,PostCollectionRemoveEventListener.class,
				PostCollectionRecreateEventListener.class,PersistEventListener.class,MergeEventListener.class,
				LockEventListener.class,LoadEventListener.class,InitializeCollectionEventListener.class,
				FlushEventListener.class,FlushEntityEventListener.class,EvictEventListener.class,
				DirtyCheckEventListener.class,DeleteEventListener.class,ClearEventListener.class,
				AutoFlushEventListener.class,
				ReplicateEventListener[].class,
				PostInsertEventListener[].class,PostLoadEventListener[].class,PostUpdateEventListener[].class,
				PreCollectionRecreateEventListener[].class,PreCollectionRemoveEventListener[].class,PreCollectionUpdateEventListener[].class,
				PreLoadEventListener[].class,PreUpdateEventListener[].class,RefreshEventListener[].class,SaveOrUpdateEventListener[].class,
				LockEventListener[].class,InitializeCollectionEventListener[].class,ResolveNaturalIdEventListener[].class,
				LoadEventListener[].class,FlushEntityEventListener[].class,FlushEventListener[].class,
				EvictEventListener[].class,DirtyCheckEventListener[].class,DeleteEventListener[].class,
				AutoFlushEventListener[].class,PersistEventListener[].class,EventType[].class,
				ClearEventListener[].class,MergeEventListener[].class,PostCollectionRecreateEventListener[].class,
				PostCollectionRemoveEventListener[].class,PostCollectionUpdateEventListener[].class,PostDeleteEventListener[].class,
				
// Are these needed to?
//				{
//					"name": "org.hibernate.cache.spi.access.NaturalIdDataAccess",
//					"allDeclaredConstructors": true,
//					"allDeclaredMethods": true
//				},
//				{
//					"name": "org.hibernate.cache.spi.access.EntityDataAccess",
//					"allDeclaredConstructors": true,
//					"allDeclaredMethods": true
//				},
//				{
//					"name": "org.hibernate.persister.entity.AbstractEntityPersister",
//					"allDeclaredConstructors": true,
//					"allDeclaredMethods": true
//				},
//				{
//					"name": "org.hibernate.persister.internal.PersisterClassResolverInitiator",
//					"allDeclaredConstructors": true,
//					"allDeclaredMethods": true
//				},
//				{
//					"name": "org.hibernate.persister.spi.PersisterClassResolver",
//					"allDeclaredConstructors": true,
//					"allDeclaredMethods": true
//				},
				// From DefaultIdentifierGeneratorFactory?
				UUIDGenerator.class,GUIDGenerator.class,UUIDHexGenerator.class,Assigned.class,IdentityGenerator.class,
				SelectGenerator.class,SequenceStyleGenerator.class,SequenceHiLoGenerator.class,IncrementGenerator.class,
				ForeignGenerator.class,SequenceIdentityGenerator.class,TableGenerator.class,

			SingleTableEntityPersister.class,CollectionDataAccess.class,PersistentClass.class,
			PersisterCreationContext.class,
			
			// related to hibernate validator
			ParameterMessageInterpolator.class,AbstractMessageInterpolator.class,ValidationBootstrapParameters.class,
			HibernateValidatorConfiguration.class,ConfigurationImpl.class,
			
			// related to Naming.applyNamingStrategies
			SpringImplicitNamingStrategy.class,SpringPhysicalNamingStrategy.class,
			
		//	HikariDataSource.class,HikariConfig.class,	MVTableEngine.class // pushed to datasource hints
			NoJtaPlatform.class,
			//org.hibernate.service.jta.platform.internal.NoJtaPlatform.class
			EntityManagerMessageLogger.class,CoreMessageLogger.class,
			Session.class,EventListener.class
		}, typeNames = {
			"org.hibernate.internal.EntityManagerMessageLogger_$logger",
			"org.hibernate.internal.CoreMessageLogger_$logger",
			"org.hibernate.service.jta.platform.internal.NoJtaPlatform"
		})
})
// These look to be hibernate validator related ... where should they live? Or can we rely on the hibernate feature now?
//{
//"name": "org.hibernate.validator.internal.engine.resolver.JPATraversableResolver",
//"allDeclaredConstructors": true,
//"allDeclaredMethods": true
//},
//{
//"name": "org.hibernate.validator.internal.engine.resolver.TraversableResolvers",
//"allDeclaredConstructors": true,
//"allDeclaredMethods": true
//},
//{
//"name": "org.hibernate.validator.resourceloading.PlatformResourceBundleLocator",
//"allDeclaredConstructors": true,
//"allDeclaredMethods": true
//},
//{
//"name": "org.hibernate.validator.internal.util.logging.Log_$logger",
//"allDeclaredConstructors": true,
//"allDeclaredMethods": true
//},
//{
//"name": "org.hibernate.validator.internal.util.logging.Messages",
//"allDeclaredConstructors": true,
//"allDeclaredMethods": true
//},
//{
//"name": "org.hibernate.validator.internal.util.logging.Log",
//"allDeclaredConstructors": true,
//"allDeclaredMethods": true
//},
public class Hints implements NativeImageConfiguration {
}
