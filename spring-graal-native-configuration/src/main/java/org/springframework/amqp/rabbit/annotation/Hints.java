package org.springframework.amqp.rabbit.annotation;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

@ConfigurationHint(value=RabbitListenerConfigurationSelector.class, typeInfos = {
		@TypeInfo(types= {RabbitBootstrapConfiguration.class},access=AccessBits.CONFIGURATION)},abortIfTypesMissing = true,follow=true)
/*
public final static String RabbitConfigurationImportSelector = "Lorg/springframework/amqp/rabbit/annotation/RabbitListenerConfigurationSelector;";
	proposedHints.put(RabbitConfigurationImportSelector,
			new CompilationHint(true,true, new String[] {
			 	"org.springframework.amqp.rabbit.annotation.RabbitBootstrapConfiguration"
			}));
*/
public class Hints implements NativeImageConfiguration { }