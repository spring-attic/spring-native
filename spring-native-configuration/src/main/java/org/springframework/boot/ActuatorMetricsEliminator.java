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

package org.springframework.boot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.type.SpringFactoriesProcessor;

/**
 * If using actuators purely for a health endpoint, there is a high memory cost to pay
 * for micrometer related infrastructure that is created that is not needed for that.
 */
public class ActuatorMetricsEliminator implements SpringFactoriesProcessor {

	private static Log logger = LogFactory.getLog(ActuatorMetricsEliminator.class);	
	
	private final static boolean active;
	
	static {
		String value = System.getProperty("spring.native.factories.no-actuator-metrics","false");
		active = Boolean.valueOf(value.equalsIgnoreCase("true"));
		if (active) {
			logger.debug("ActuatorMetricsEliminator: active");
		}
	}

	@Override
	public boolean filter(String key, List<String> values) {
		if (!active) {
			return false;
		}
		if (key.equals(SpringFactoriesProcessor.enableAutoConfigurationKey)) {
			List<String> toRemove = new ArrayList<>();
			for (String value: values) {
				if (value.startsWith("org.springframework.boot.actuate.autoconfigure.metrics.")) {
					toRemove.add(value);
				}
			}
			if (toRemove.size()>0) {
				logger.debug("ActuatorMetricsEliminator: removing "+toRemove.size()+" configurations from "+enableAutoConfigurationKey);
				values.removeAll(toRemove);
				return true;
			}
		}
		return false;
	}
}
