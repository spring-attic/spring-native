/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.endpoint;

import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointExtension;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.Selector.Match;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.convert.ConversionServiceParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;

import com.fasterxml.jackson.databind.ser.BeanSerializer;

@NativeHint(trigger = EndpointAutoConfiguration.class, types = {
	@TypeHint(types = {
			Match.class,
			EndpointFilter.class,
			Link.class,
			BeanSerializer.class,
			ApiVersion.class,
			ShutdownEndpoint.class,
			CloudPlatform.class,
			OperationInvokerAdvisor.class,
			ConversionServiceParameterValueMapper.class,
			LoggersEndpoint.class,
			ThreadDumpEndpoint.class,
			ScheduledTasksEndpoint.class,
			DiskSpaceHealthIndicator.class,
	}, typeNames = {
		"org.springframework.core.LocalVariableTableParameterNameDiscoverer",
	}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
		@TypeHint(types = {
				Endpoint.class,
				EndpointExtension.class,
				ReadOperation.class,
				WriteOperation.class,
				DeleteOperation.class,
				Selector.class,
		})
})
public class EndpointAutoConfigurationHints implements NativeConfiguration {
}