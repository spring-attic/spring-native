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
package org.springframework.aop.framework;

import java.lang.reflect.Proxy;

import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.substitutions.OnlyIfPresent;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Patch the DefaultAopProxyFactory.createAopProxy() method so that it will look for a class based proxy
 * generated at build time, instead of attempting to generate a class at runtime which will fail. If the class cannot
 * be found, produces a clear @see {@link AotProxyHint} indicating what needs including at build time to cause
 * Spring Native to create the class based proxy early such that it is included in the packaged application.
 * 
 * @author Andy Clement
 */
@TargetClass(className = "org.springframework.aop.framework.DefaultAopProxyFactory", onlyWith = { OnlyIfPresent.class })
final class Target_DefaultAopProxyFactory {

	@Substitute
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		if ((config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config))) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}

			return new ObjenesisBuildTimeAopProxy(config);
			// This won't work in native image as it requires dynamic class definition
			// return new ObjenesisCglibAopProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

 	@Alias
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		return true;
	}

}
