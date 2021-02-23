package org.springframework.boot.autoconfigure.data.mongodb;

import org.springframework.boot.autoconfigure.data.SpringDataReactiveHints;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeSaveCallback;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.config.ReactiveMongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeInfo;


@NativeHint(trigger = MongoRepositoriesAutoConfiguration.class, types = {
		@TypeInfo(types = {
				MongoRepositoryFactoryBean.class,
				MongoRepositoryConfigurationExtension.class,
				MongoConfigurationSupport.class,
				SimpleMongoRepository.class,
				BeforeConvertCallback.class,
				BeforeSaveCallback.class,
				AfterSaveCallback.class,
				AfterConvertCallback.class
		})
})
@NativeHint(trigger = MongoReactiveRepositoriesAutoConfiguration.class,
		imports = SpringDataReactiveHints.class,
		types = {
				@TypeInfo(types = {
						ReactiveMongoRepositoryFactoryBean.class,
						ReactiveMongoRepositoryConfigurationExtension.class,
						MongoConfigurationSupport.class,
						SimpleReactiveMongoRepository.class,
						ReactiveBeforeConvertCallback.class,
						ReactiveBeforeSaveCallback.class,
						ReactiveAfterSaveCallback.class,
						ReactiveAfterConvertCallback.class
				})
		})
public class MongoRepositoriesHints implements NativeConfiguration {

}
