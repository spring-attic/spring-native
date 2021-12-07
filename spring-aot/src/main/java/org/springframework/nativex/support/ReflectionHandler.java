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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.ConditionDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.AccessDescriptor;


/**
 * Loads up the constant data defined in resource file and registers reflective
 * access being necessary with the image build. Also provides an method
 * ({@code addAccess(String typename, Flag... flags)} usable from elsewhere
 * when needing to register reflective access to a type (e.g. used when resource
 * processing).
 * 
 * @author Andy Clement
 */
public class ReflectionHandler extends Handler {
	
	private static Log logger = LogFactory.getLog(ReflectionHandler.class);

	public ReflectionHandler(ConfigurationCollector collector) {
		super(collector);
	}

	public static String[] subarray(String[] array) {
		if (array.length == 1) {
			return null;
		} else {
			return Arrays.copyOfRange(array, 1, array.length);
		}
	}

	public void addAccess(String typename, String typeReachable, String[][] methodsAndConstructors, String[][] queriedMethodsAndConstructors, String[][] fields, boolean silent, TypeAccess... access) {
		if (!silent) {
			logger.debug("Registering reflective access to " + typename+": "+(access ==null?"":Arrays.asList(access)));
		}
		
		ClassDescriptor cd = ClassDescriptor.of(typename);
		if (typeReachable != null && !ts.resolveDotted(typeReachable).isAnnotation()) {
			cd.setCondition(new ConditionDescriptor(typeReachable));
		}
		if (cd == null) {
			cd  = ClassDescriptor.of(typename);
		}
		// Update access...
		for (TypeAccess f : access) {
			cd.setAccess(f);
		}
		if (methodsAndConstructors != null) {
			for (String[] mc: methodsAndConstructors) {
				MethodDescriptor md = MethodDescriptor.of(mc[0], subarray(mc));
				if (!cd.containsMethod(md)) {
					cd.addMethodDescriptor(md);	
				}
			}
		}
		if (queriedMethodsAndConstructors != null) {
			for (String[] mc: queriedMethodsAndConstructors) {
				MethodDescriptor md = MethodDescriptor.of(mc[0], subarray(mc));
				if (!cd.containsQueriedMethod(md)) {
					cd.addQueriedMethodDescriptor(md);
				}
			}
		}
		if (fields != null) {
			for (String[] field: fields) {
				FieldDescriptor fieldDescriptor = FieldDescriptor.of(field);
				FieldDescriptor existingFieldDescriptor = cd.getFieldDescriptorNamed(fieldDescriptor.getName());
				if (existingFieldDescriptor != null) {
					existingFieldDescriptor.merge(fieldDescriptor);
				} else {
					cd.addFieldDescriptor(fieldDescriptor);
				}
			}
		}
		collector.addClassDescriptor(cd);
	}
	
	public ClassDescriptor getClassDescriptor(String typename) {
		return collector.getClassDescriptorFor(typename);
	}

}
