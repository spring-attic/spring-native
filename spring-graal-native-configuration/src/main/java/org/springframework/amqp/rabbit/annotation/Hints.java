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
package org.springframework.amqp.rabbit.annotation;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

// These are from the reflect.json but we don't have a rabbit sample any more so can't confirm how they need correct wiring in:
/*{
	"name": "org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor",
	"allDeclaredConstructors": true,
	"allDeclaredMethods": true
},
{
	"name": "org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry",
	"allDeclaredConstructors": true,
	"allDeclaredMethods": true
},
	{
	"name": "org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer",
	"allDeclaredConstructors": true,
	"allDeclaredMethods": true
},
{
	"name": "org.springframework.boot.autoconfigure.amqp.DirectRabbitListenerContainerFactoryConfigurer",
	"allDeclaredConstructors": true,
	"allDeclaredMethods": true
},

	{
		"name": "org.springframework.amqp.rabbit.connection.CachingConnectionFactory",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	},
	{
		"name": "org.springframework.amqp.rabbit.annotation.RabbitListener",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	},
	{
		"name": "org.springframework.amqp.rabbit.connection.ChannelProxy",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	}
	{
		"name": "com.rabbitmq.client.Channel",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	},
	{
		"name": "com.rabbitmq.client.ShutdownNotifier",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	},
	{
		"name": "org.springframework.amqp.core.Queue",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	},
	{
		"name": "org.springframework.amqp.core.AnonymousQueue",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	},
	{
		"name": "org.springframework.amqp.core.AmqpAdmin",
		"allDeclaredConstructors": true,
		"allDeclaredMethods": true
	},,
*/
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