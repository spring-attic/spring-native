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

package org.springframework.nativex.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.type.SpringFactoriesProcessor;

/**
 * A {@link SpringFactoriesProcessor} that is configurable by an option:
 * {@code spring.native.spring-factories-exclusions}. This should be supplied with
 * information of the form 'key=value,value;key2=value;key3=value,value,value' and
 * then when {@code spring.factories} files are processed this configurable processor
 * is given the option to filter out the values for any keys it matches within that file.
 * 
 * @author Andy Clement
 */
public class ConfigurableSpringFactoriesProcessor implements SpringFactoriesProcessor {

	private static Log logger = LogFactory.getLog(ConfigurableSpringFactoriesProcessor.class);	

	private final static String option = "spring.native.spring-factories-exclusions";

	private final static Map<String,List<String>> springFactoriesExclusions = new HashMap<>();
	
	static {
		String optionValue = System.getProperty(option,"");
		// it should be of the form kkk1=vvv1,vvv2;kkk2=vvv3;kkk3=fff
		try {
			String[] exclusions = optionValue.split(";");
			for (String exclusion : exclusions) {
				if (exclusion.trim().length()==0) {
					continue;
				}
				int equals = exclusion.indexOf("=");
				if (equals == -1) {
					throw new IllegalStateException(
							"Problem in format of exclusions (expected 'a=b,c,d;d=e,f;g=i') : '" + optionValue + "'");
				}
				String key = exclusion.substring(0, equals);
				List<String> values = Arrays.asList(exclusion.substring(equals + 1).split(","));
				springFactoriesExclusions.put(key, values);
			}
		} catch (Throwable t) {
			throw new IllegalStateException("Unable to process spring.native.spring-factories-exclusions: "+springFactoriesExclusions,t);
		}
		if (springFactoriesExclusions.size()>0) {
			logger.debug("The following map of spring.factories entries will be removed if encountered: "+springFactoriesExclusions);
		}
	}

	@Override
	public boolean filter(String key, List<String> values) {
		boolean modified = false;
		for (Map.Entry<String, List<String>> exclusion: springFactoriesExclusions.entrySet()) {
			if (exclusion.getKey().equals(key)) {
				modified = values.removeAll(exclusion.getValue()) || modified;
			}
		}
		return modified;
	}

}
