package org.springframework.boot.autoconfigure.data.mongodb;

import java.util.Properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFragmentsFactoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;


@NativeImageHint(trigger= MongoRepositoriesAutoConfiguration.class, typeInfos= {
		@TypeInfo(types = {MongoRepositoryFactoryBean.class, RepositoryFactoryBeanSupport.class, MongoRepositoryConfigurationExtension.class,
				MappingContext.class, PropertiesBasedNamedQueries.class, RepositoryFragmentsFactoryBean.class,
				QueryByExampleExecutor.class, MongoConfigurationSupport.class, SimpleMongoRepository.class,
				BeforeConvertCallback.class, BeforeSaveCallback.class, AfterSaveCallback.class, AfterConvertCallback.class,
				Query.class, Aggregation.class }),
		@TypeInfo(types = { Properties.class, BeanFactory.class },access = AccessBits.CLASS)
})
public class Hints implements NativeImageConfiguration {
}
