package org.springframework.boot.autoconfigure.cache;

import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheConfigurationImportSelector;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

@ConfigurationHint(value=CacheConfigurationImportSelector.class,follow=true,
	typeInfos = {@TypeInfo(types= {GenericCacheConfiguration.class, EhCacheCacheConfiguration.class,
		HazelcastCacheConfiguration.class, InfinispanCacheConfiguration.class,
		JCacheCacheConfiguration.class, CouchbaseCacheConfiguration.class,
		RedisCacheConfiguration.class, CaffeineCacheConfiguration.class,
		SimpleCacheConfiguration.class, NoOpCacheConfiguration.class},access=AccessBits.CONFIGURATION)})
public class Hints implements NativeImageConfiguration { }