package org.springframework.boot.autoconfigure.cache;

import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheConfigurationImportSelector;
import org.springframework.graal.extension.ConfigurationHint;

@ConfigurationHint(value=CacheConfigurationImportSelector.class,
	types = {GenericCacheConfiguration.class, EhCacheCacheConfiguration.class,
		HazelcastCacheConfiguration.class, InfinispanCacheConfiguration.class,
		JCacheCacheConfiguration.class, CouchbaseCacheConfiguration.class,
		RedisCacheConfiguration.class, CaffeineCacheConfiguration.class,
		SimpleCacheConfiguration.class, NoOpCacheConfiguration.class})

//	proposedHints.put("Lorg/springframework/boot/autoconfigure/cache/CacheAutoConfiguration$CacheConfigurationImportSelector;",
//			new CompilationHint(false,true, new String[] {
//			 	"org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration",
//			 	"org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration"
//			}));
	
public class AnyOldName implements NativeImageConfiguration {
	public List<ConfigurationHint> getHints(ImageBuildContext context) {
		return Collections.singletonList(new ConfigurationHint(CacheConfigurationImportSelector.class)
				.add(TypeHint.forConfigurationClass(GenericCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(EhCacheCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(HazelcastCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(InfinispanCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(JCacheCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(CouchbaseCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(RedisCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(CaffeineCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(SimpleCacheConfiguration.class))
				.add(TypeHint.forConfigurationClass(NoOpCacheConfiguration.class)));
	}
}	
	
//	// ---
///*
//class SpringWebMvcImportSelector implements ImportSelector {
//
//	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
//		boolean webmvcPresent = ClassUtils.isPresent(
//				"org.springframework.web.servlet.DispatcherServlet",
//				getClass().getClassLoader());
//		return webmvcPresent
//				? new String[] {
//						"org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration" }
//				: new String[] {};
//	}
//}
//*/
//	
//	// the hint is:
//	// TODO these should come with the jars themselves really (@CompilationHints on the selectors...)
//	proposedHints.put(SpringWebMvcImportSelector,
//			new CompilationHint(false, true, new String[] {
//				"org.springframework.web.servlet.DispatcherServlet:EXISTENCE_CHECK",
//				"org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration"
//			}));
//	
//	// so two types of thing here:
//	// - something for a pure existence check
//    // - a configuration class that will actually be returned (if that type was around)	
//	// How eagerly can we check this? We could in theory determine that type is missing *RIGHT NOW* as the config is built
//	// rather than returning a result and chasing any further.
//
//	static ConfigurationHint springWebMvcImportSelector(ImageBuildContext context) {
//		if (context.contains(org.springframework.web.servlet.DispatcherServlet.class)) {
//			// but this is us duplicating what that thing does...
//			return new ConfigurationHint(SpringWebMvcImportSelector.class)
//					// Configuration type hints are implicitly followed
//					.add(TypeHint.forConfiguration(org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration.class))
//					.build();
//		}
//		return ConfigurationHint.NONE;
//		
//		// or should it be:
//		return new ConfigurationHint(SpringWebMvcImportSelector.class)
//				.add(TypeHint.for(DispatcherServlet.class))
//				.add(TypeHint.forConfiguration(WebMvcSecurityConfiguration.class))
//				.build();
//	}
//	
//	// ---
//
//	// TODO shouldn't this be on OnWebApplicationCondition?
//	proposedHints.put("Lorg/springframework/boot/autoconfigure/condition/ConditionalOnWebApplication;", 
//			new CompilationHint(true, false, new String[] {
//				"org.springframework.web.context.support.GenericWebApplicationContext",
//				"org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication$Type"
//			}));	