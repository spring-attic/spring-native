package org.springframework.boot.autoconfigure.data.mongodb;

import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.data.mongodb.config.MongoConfigurationSupport;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;


@NativeImageHint(trigger = MongoRepositoriesAutoConfiguration.class, typeInfos = {
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
public class MongoRepositoriesHints implements NativeImageConfiguration {

}
