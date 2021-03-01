package org.hibernate.validator;

import org.hibernate.validator.internal.engine.ValidatorImpl;

import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyHint;

@NativeHint(trigger = ValidatorImpl.class,
		initialization = @InitializationHint(types = {
				org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator.class,
				org.hibernate.validator.internal.engine.ValidatorImpl.class,
				org.hibernate.validator.internal.engine.ValidatorFactoryImpl.class,
				org.hibernate.validator.internal.engine.resolver.TraversableResolvers.class,
				org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory.class,
				org.hibernate.validator.internal.util.CollectionHelper.class,
				org.hibernate.validator.internal.util.Contracts.class,
				org.hibernate.validator.internal.util.logging.Log.class,
				org.hibernate.validator.internal.util.logging.Messages.class,
				org.hibernate.validator.internal.util.privilegedactions.GetMethod.class,
				org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator.class,
				org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator.class,
				org.hibernate.validator.internal.xml.config.ValidationBootstrapParameters.class,
				org.hibernate.validator.internal.xml.config.ValidationXmlParser.class,
				org.hibernate.validator.resourceloading.PlatformResourceBundleLocator.class,
				org.hibernate.validator.internal.engine.ConfigurationImpl.class
		}, typeNames = {
				"org.hibernate.validator.internal.util.logging.Messages_$bundle",
				"org.hibernate.validator.internal.util.privilegedactions.LoadClass",
				"org.hibernate.validator.internal.xml.config.ResourceLoaderHelper",
				"org.hibernate.validator.resourceloading.PlatformResourceBundleLocator$AggregateResourceBundle",
				"org.hibernate.validator.resourceloading.PlatformResourceBundleLocator$AggregateResourceBundleControl",
				"org.hibernate.validator.internal.util.logging.Log_$logger"
		}, packageNames = {
				"org.hibernate.validator.constraints",
				"org.hibernate.validator.internal.engine",
				"org.hibernate.validator.internal.engine.groups",
				"org.hibernate.validator.internal.cfg.context",
				"org.hibernate.validator.internal.engine.constraintvalidation",
				"org.hibernate.validator.internal.engine.valueextraction",
				"org.hibernate.validator.internal.metadata.aggregated.rule",
				"org.hibernate.validator.internal.metadata.core",
				"org.hibernate.validator.internal.metadata.provider",
				"org.hibernate.validator.internal.metadata.aggregated",
				"org.hibernate.validator.internal.metadata.raw",
				"org.hibernate.validator.internal.util"
		}, initTime = InitializationTime.BUILD),
		proxies = @ProxyHint(types = {
				javax.validation.Validator.class,
				org.springframework.aop.SpringProxy.class,
				org.springframework.aop.framework.Advised.class,
				org.springframework.core.DecoratingProxy.class
		})
)
public class ValidatorHints implements NativeConfiguration {
}
