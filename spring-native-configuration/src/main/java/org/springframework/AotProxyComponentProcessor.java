/*
 * Copyright 2021 the original author or authors.
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

package org.springframework;

import java.util.Collections;
import java.util.List;

import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

/**
 * Recognize spring.components that need an AotProxy generating in order to work. This is 
 * a simple version that triggers proxy creation based on one of a number of annotations
 * appearing on a method in a component. For simple cases this might be sufficient, if
 * the annotations presence needs something deeper to happen, maybe it would spin off
 * into its own component processor.
 *
 * @author Andy Clement
 */
public class AotProxyComponentProcessor implements ComponentProcessor {

	// A list of annotations that, if found on a method in a component, should trigger
	// creation of an AotProxy for the class
	public static String[] MethodLevelAnnotations = new String[]{
			"Lorg/springframework/scheduling/annotation/Async;"
	};

	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
		TypeSystem ts = imageContext.getTypeSystem();
		Type component = ts.resolveDotted(componentType);
		for (String annotationName: MethodLevelAnnotations) {
			if (ts.Lresolve(annotationName,true)!=null) {
				List<Method> interestingMethods = component.getMethods(m -> m.hasAnnotation(annotationName,false));
				if (!interestingMethods.isEmpty()) {
					imageContext.log("AotProxyComponentProcessor: found annotation "+annotationName+" on component "+componentType+": creating class proxy");
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
		// TODO is IS_STATIC always right here?
		imageContext.addAotProxy(componentType, Collections.emptyList(), ProxyBits.IS_STATIC);
		imageContext.log("AotProxyComponentProcessor: creating proxy for this class: "+componentType);
	}

}
