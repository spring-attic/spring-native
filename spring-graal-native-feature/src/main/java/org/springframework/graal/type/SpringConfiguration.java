/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.graal.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Predicate;

/**
 * @author Andy Clement
 */
public class SpringConfiguration {
    	
	// Spring Security
	public final static String OAuth2ImportSelector = "Lorg/springframework/security/config/annotation/web/configuration/OAuth2ImportSelector;";
	
	public final static String SpringWebMvcImportSelector = "Lorg/springframework/security/config/annotation/web/configuration/SpringWebMvcImportSelector;";
	
	public final static String ImportSelector ="Lorg/springframework/context/annotation/ImportSelector;";
	
	public final static String ImportBeanDefinitionRegistrar ="Lorg/springframework/context/annotation/ImportBeanDefinitionRegistrar;";

	public final static String TransactionManagementConfigurationSelector = "Lorg/springframework/transaction/annotation/TransactionManagementConfigurationSelector;";
	
	public final static String SpringDataWebConfigurationSelector = "Lorg/springframework/data/web/config/EnableSpringDataWebSupport$SpringDataWebConfigurationImportSelector;";

	public final static String SpringDataWebQueryDslSelector = "Lorg/springframework/data/web/config/EnableSpringDataWebSupport$QuerydslActivator;";

	public final static String AdviceModeImportSelector="Lorg/springframework/context/annotation/AdviceModeImportSelector;";

	public final static String EnableConfigurationPropertiesImportSelector = "Lorg/springframework/boot/context/properties/EnableConfigurationPropertiesImportSelector;";
	
	public final static String CacheConfigurationImportSelector = "Lorg/springframework/boot/autoconfigure/cache/CacheAutoConfiguration$CacheConfigurationImportSelector;";
	
	public final static String RabbitConfigurationImportSelector = "Lorg/springframework/amqp/rabbit/annotation/RabbitListenerConfigurationSelector;";
	
	public final static String AtBean = "Lorg/springframework/context/annotation/Bean;";

	public final static String AtImports = "Lorg/springframework/context/annotation/Import;";

	public final static String AtEnableConfigurationProperties = "Lorg/springframework/boot/context/properties/EnableConfigurationProperties;";
	
	public final static String AtConditionalOnClass = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnClass;";

	public final static String AtConditionalOnSingleCandidate = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnSingleCandidate;";

	public final static String AtConfiguration = "Lorg/springframework/context/annotation/Configuration;";
	
	public final static String Condition = "Lorg/springframework/context/annotation/Condition;";
	
	public final static String AtConditional = "Lorg/springframework/context/annotation/Conditional;";

	public final static String HypermediaConfigurationImportSelector = "Lorg/springframework/hateoas/config/HypermediaConfigurationImportSelector;";

	public final static String WebStackImportSelector = "Lorg/springframework/hateoas/config/WebStackImportSelector;";
	
	public final static String AtConditionalOnMissingBean = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean;";

	private final static Map<String, CompilationHint> proposedHints = new HashMap<>();
	
	private final static Map<String, String[]> proposedFactoryGuards = new HashMap<>();
	
	static {
		
		// This specifies that the TemplateAvailabilityProvider key will only be processed if one of the
		// specified types is around. This ensures we don't provide reflective access to the value of this
		// key in apps that don't use mvc or webflux
		proposedFactoryGuards.put(
			"org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider",new String[] {
				"org.springframework.web.reactive.config.WebFluxConfigurer",
				"org.springframework.web.servlet.config.annotation.WebMvcConfigurer"
			});
		
		
		// @ConditionalOnClass has @CompilationHint(skipIfTypesMissing=true, follow=false)
		proposedHints.put(AtConditionalOnClass, new CompilationHint(true,false));

		// If @ConditionalOnSingleCandidate refers to a class that doesn't exist then
		// there cannot be a bean for it in this application.
		proposedHints.put(AtConditionalOnSingleCandidate, new CompilationHint(true,false));
		
		proposedHints.put("Lorg/springframework/boot/autoconfigure/condition/ConditionalOnMissingBean;",
				new CompilationHint(true, false, new String[] {
				 	"org.springframework.boot.autoconfigure.condition.SearchStrategy",
				}));
		
		proposedHints.put("Lorg/springframework/boot/autoconfigure/web/servlet/WebMvcAutoConfiguration;",
				new CompilationHint(true, false, new String[] {
					"java.util.concurrent.Callable:EXISTENCE_MC"
				}));
				
		// TODO can be {@link Configuration}, {@link ImportSelector}, {@link ImportBeanDefinitionRegistrar}
		// @Imports has @CompilationHint(skipIfTypesMissing=false?, follow=true)
		proposedHints.put(AtImports, new CompilationHint(false, true));
		
		// @Conditional has @CompilationHint(skipIfTypesMissing=false, follow=false)
		proposedHints.put(AtConditional, new CompilationHint(false, false));
		
		// TODO do configuration properties chain?
		// @EnableConfigurationProperties has @CompilationHint(skipIfTypesMissing=false, follow=false)
		proposedHints.put(AtEnableConfigurationProperties, new CompilationHint(false, false));
		
		// @EnableConfigurationPropertiesImportSelector has
		// @CompilationHint(skipIfTypesMissing=false, follow=false, name={
		//   ConfigurationPropertiesBeanRegistrar.class.getName(),
		//   ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName() })
		// proposedAnnotations.put(AtEnableConfigurationProperties, new CompilationHint(false, false));
		
		// TODO review this - what is the right way? That type is only needed if (strictly speaking) XMLEventFactory is used
		proposedHints.put("Lorg/springframework/boot/autoconfigure/orm/jpa/HibernateJpaAutoConfiguration;",
				new CompilationHint(false, false, new String[] {
						"com.sun.xml.internal.stream.events.XMLEventFactoryImpl:REGISTRAR",
						"org.apache.logging.log4j.message.DefaultFlowMessageFactory:REGISTRAR",
						"com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry[]:REGISTRAR",
						"com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry:REGISTRAR"
				}));
		
		proposedHints.put("Lorg/springframework/boot/autoconfigure/jdbc/DataSourceInitializationConfiguration$Registrar;",
				new CompilationHint(false, false, new String[] {
						"org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerPostProcessor:EXISTENCE_CMF"
				}));

		proposedHints.put("Lorg/springframework/boot/autoconfigure/jdbc/EmbeddedDataSourceConfiguration;",
				new CompilationHint(false, false, new String[] {
						"org.h2.store.fs.FilePathDisk:REGISTRAR",
						"org.h2.store.fs.FilePathMem:REGISTRAR",
						"org.h2.store.fs.FilePathMemLZF:REGISTRAR",
						"org.h2.store.fs.FilePathNioMem:REGISTRAR",
						"org.h2.store.fs.FilePathNioMemLZF:REGISTRAR",
						"org.h2.store.fs.FilePathSplit:REGISTRAR",
						"org.h2.store.fs.FilePathNio:REGISTRAR",
						"org.h2.store.fs.FilePathNioMapped:REGISTRAR",
						"org.h2.store.fs.FilePathAsync:REGISTRAR",
						"org.h2.store.fs.FilePathZip:REGISTRAR",
						"org.h2.store.fs.FilePathRetryOnInterrupt:REGISTRAR"
				}));

		proposedHints.put(CacheConfigurationImportSelector,
				new CompilationHint(false,true, new String[] {
				 	"org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration",
				 	"org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration"
				}));
		
		proposedHints.put(RabbitConfigurationImportSelector,
				new CompilationHint(true,true, new String[] {
				 	"org.springframework.amqp.rabbit.annotation.RabbitBootstrapConfiguration"
				}));
		
		
		proposedHints.put(TransactionManagementConfigurationSelector,
				new CompilationHint(false, true, new String[] { // TODO really the 'skip if missing' can be different for each one here...
					"org.springframework.context.annotation.AutoProxyRegistrar",
					"org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration",
					"org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration",
					"org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration"
				}));
		

		proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$ReactiveSessionConfigurationImportSelector;",
				new CompilationHint(true, true, new String[] {
						"org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpReactiveSessionConfiguration"
				}));

		proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$SessionConfigurationImportSelector;",
				new CompilationHint(true, true, new String[] {
						"org.springframework.boot.autoconfigure.session.RedisSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoReactiveSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.JdbcSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.HazelcastSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpReactiveSessionConfiguration"
				}));

		proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$ServletSessionConfigurationImportSelector;",
				new CompilationHint(true, true, new String[] {
						"org.springframework.boot.autoconfigure.session.RedisSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.MongoSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.JdbcSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.HazelcastSessionConfiguration",
						"org.springframework.boot.autoconfigure.session.NoOpSessionConfiguration"
				}));
		
		//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
		proposedHints.put(SpringDataWebConfigurationSelector,
				new CompilationHint(true, true, new String[] {
					"org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration",
					"org.springframework.data.web.config.SpringDataWebConfiguration"
				}));
		
		//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
		proposedHints.put(SpringDataWebQueryDslSelector,
				new CompilationHint(true, true, new String[] {
					"org.springframework.data.web.config.QuerydslWebConfiguration"}
				));
		
		// EnableConfigurationPropertiesImportSelector has
		// @CompilationHint(skipIfTypesMissing=true, follow=false, name={
		//	 	"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector$ConfigurationPropertiesBeanRegistrar",
		//	 	"org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar"})
		proposedHints.put(EnableConfigurationPropertiesImportSelector,
				new CompilationHint(false,false, new String[] {
				 	"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector$ConfigurationPropertiesBeanRegistrar:REGISTRAR",
				 	"org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar:REGISTRAR"}
				));
		
		proposedHints.put("Lorg/springframework/boot/autoconfigure/web/servlet/DispatcherServletAutoConfiguration;", 
				new CompilationHint(false,false, new String[] {
						// All from DispatcherServlet.properties
//org.springframework.web.servlet.LocaleResolver=
						"org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver",
//org.springframework.web.servlet.ThemeResolver=
						"org.springframework.web.servlet.theme.FixedThemeResolver",
//org.springframework.web.servlet.HandlerMapping=
						"org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping",
						"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping",
						"org.springframework.web.servlet.function.support.RouterFunctionMapping",
//org.springframework.web.servlet.HandlerAdapter=
						"org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter",
						"org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter",
						"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter",
						"org.springframework.web.servlet.function.support.HandlerFunctionAdapter",
//org.springframework.web.servlet.HandlerExceptionResolver=
						"org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver",
						"org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver",
						"org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver",
//org.springframework.web.servlet.RequestToViewNameTranslator=
						"org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator",
//org.springframework.web.servlet.ViewResolver=
						"org.springframework.web.servlet.view.InternalResourceViewResolver",
//org.springframework.web.servlet.FlashMapManager=
						"org.springframework.web.servlet.support.SessionFlashMapManager"
				}));
		
				
		// Not quite right... this is a superclass of a selector we've already added...
		proposedHints.put(AdviceModeImportSelector,
				new CompilationHint(true, true, new String[0]
				));
		
		// Spring Security!
		// TODO these should come with the jars themselves really (@CompilationHints on the selectors...)
		proposedHints.put(SpringWebMvcImportSelector,
				new CompilationHint(false, true, new String[] {
					"org.springframework.web.servlet.DispatcherServlet:EXISTENCE_CHECK",
					"org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration"
				}));
				
		// TODO this one is actually incomplete, finish it
		proposedHints.put(OAuth2ImportSelector,
				new CompilationHint(false, true, new String[] {
					"org.springframework.security.oauth2.client.registration.ClientRegistration:EXISTENCE_CHECK",
					"org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration"
				}));
		
		
		proposedHints.put("Lorg/springframework/boot/autoconfigure/task/TaskExecutionAutoConfiguration;",
				new CompilationHint(true,false, new String[] {
						"org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor:REGISTRAR"
				}));

		// TODO I am not sure the specific entry here is right, but given that the selector references entries loaded via factories - aren't those already handled? 
		proposedHints.put(HypermediaConfigurationImportSelector,
				new CompilationHint(false, true, new String[] {
						"org.springframework.hateoas.config.HypermediaConfigurationImportSelector"
				}));

		proposedHints.put(WebStackImportSelector,
				new CompilationHint(false, true, new String[] {
					//"org.springframework.hateoas.config.WebStackImportSelector" - why was this here???
					"org.springframework.hateoas.config.WebMvcHateoasConfiguration",
					"org.springframework.hateoas.config.WebFluxHateoasConfiguration"
				}));

		proposedHints.put("Lorg/springframework/boot/autoconfigure/condition/OnWebApplicationCondition;", 
				new CompilationHint(false, false, new String[] {
					"org.springframework.web.context.support.GenericWebApplicationContext",
					
				}));
		// Temporary until hints more flexible...
		proposedHints.put("Lorg/springframework/boot/autoconfigure/condition/ConditionalOnWebApplication;", 
				new CompilationHint(true, false, new String[] {
					"org.springframework.web.context.support.GenericWebApplicationContext",
					"org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication$Type"
				}));
	}

	static class CompilationHint {
		boolean follow;
		boolean skipIfTypesMissing;
		Map<String, AccessRequired> specificTypes;
		Predicate predicate;
		
		public CompilationHint(boolean skipIfTypesMissing, boolean follow) {
			this(skipIfTypesMissing,follow,new String[] {});
		}
		
		public CompilationHint(Predicate p) {
			this.predicate = p;
		}
		
		public CompilationHint(boolean skipIfTypesMissing, boolean follow, String[] specificTypes) {
			this.skipIfTypesMissing = skipIfTypesMissing;
			this.follow = follow;
			if (specificTypes != null) {
				this.specificTypes = new LinkedHashMap<>();
				for (String specificType: specificTypes) {
					AccessRequired access = AccessRequired.ALL;
					StringTokenizer t = new StringTokenizer(specificType,":");
					String type = t.nextToken(); // the type name
					if (t.hasMoreTokens()) { // possible access specified otherwise default to ALL
						access = AccessRequired.valueOf(t.nextToken());
					}
					this.specificTypes.put(type, access);
				}
			} else {
				this.specificTypes = Collections.emptyMap();
			}
		}
	}


	public static CompilationHint findProposedHints(String descriptor) {
        return proposedHints.get(descriptor);     
	}


	public static String[] findProposedFactoryGuards(String key) {
        return proposedFactoryGuards.get(key);
	}
}