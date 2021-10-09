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

package org.springframework;

import org.springframework.nativex.domain.proxies.AotProxyDescriptor;
import org.springframework.nativex.hint.ProxyBits;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recognize spring.components that use any of @PostAuthorize, @PostFilter, @PreAuthorize or @PreFilter
 * annotation and register proxies for them.
 *
 * @author Eleftheria Stein
 * @author Andy Clement
 */
public class PrePostSecuredComponentProcessor implements ComponentProcessor {

    @Override
    public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {
        Type type = imageContext.getTypeSystem().resolveName(componentType);
        return (type != null && (type.isAtPrePostSecured(true)));
    }

    @Override
    public void process(NativeContext imageContext, String componentType, List<String> classifiers) {
        Type type = imageContext.getTypeSystem().resolveName(componentType);
        // Check if it is an interface or the PrePost annotations are not directly on this 'class' (in which
        // case we assume it is on a super interface - this is not perfect)
        if (type.isInterface() || !type.isAtPrePostSecured(false)) {
	        List<String> prePostSecuredInterfaces = new ArrayList<>();
	        for (Type intface: type.getInterfaces()) {
	            prePostSecuredInterfaces.add(intface.getDottedName());
	        }
	        if (prePostSecuredInterfaces.size()==0) {
	            imageContext.log("PrePostSecuredComponentProcessor: unable to find interfaces to proxy on "+componentType);
	            return;
	        }
	        prePostSecuredInterfaces.add("org.springframework.aop.SpringProxy");
	        prePostSecuredInterfaces.add("org.springframework.aop.framework.Advised");
	        prePostSecuredInterfaces.add("org.springframework.core.DecoratingProxy");
	        imageContext.addProxy(prePostSecuredInterfaces);
	        imageContext.log("PrePostSecuredComponentProcessor: creating proxy for these interfaces: "+prePostSecuredInterfaces);
        } else {
        	AotProxyDescriptor aotDescriptor = new AotProxyDescriptor(componentType, Collections.emptyList(), ProxyBits.IS_STATIC);
        	imageContext.addAotProxy(aotDescriptor);
	        imageContext.log("PrePostSecuredComponentProcessor: creating aot proxy for "+componentType);
        }
    }
}
