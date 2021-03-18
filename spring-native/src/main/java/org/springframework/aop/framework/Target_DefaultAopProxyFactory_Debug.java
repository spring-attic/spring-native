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
import java.util.Arrays;

import org.springframework.core.NativeDetector;
import org.springframework.nativex.substitutions.DebugProxies;
import org.springframework.nativex.substitutions.OnlyIfPresent;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

// In this package due to visibility reasons
@TargetClass(className = "org.springframework.aop.framework.DefaultAopProxyFactory", onlyWith = { OnlyIfPresent.class, DebugProxies.class })
final class Target_DefaultAopProxyFactory_Debug {

	@Substitute
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		System.out.println("DEBUGGING PROXY CREATION");
		System.out.println(" > DefaultAopProxyFactory.createAopProxy "+config+"   (config=0x"+System.identityHashCode(config)+")");
		System.out.println(" = isOptimize? "+config.isOptimize());
		System.out.println(" = isProxyTargetClass? "+config.isProxyTargetClass());
		System.out.println(" = hasNoUserSuppliedProxyInterfaces? "+hasNoUserSuppliedProxyInterfaces(config));
		System.out.println(" = targetclass? "+config.getTargetClass());
		System.out.println(" = targetClass.isInterface? "+(config.getTargetClass()!=null && config.getTargetClass().isInterface()));
		System.out.println(" = targetClass.isProxyClass? "+(config.getTargetClass()!=null && Proxy.isProxyClass(config.getTargetClass())));
		boolean a = config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config);
		Class<?> tc = config.getTargetClass();
		boolean b = tc!=null && (tc.isInterface() || Proxy.isProxyClass(tc));
		System.out.println(" = Would have been a class based proxy? "+(a && !b));
		if (!NativeDetector.inNativeImage() &&
				(config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config))) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				AopProxy ap = new JdkDynamicAopProxy(config);
				System.out.println(" = interfaces: "+Arrays.toString(ap.getProxy().getClass().getInterfaces()));
				System.out.println(" < returning JdkDynamicAopProxy: "+ap+"   (config=0x"+System.identityHashCode(config)+")");
				return ap;
			}
			 ObjenesisCglibAopProxy objenesisCglibAopProxy = new ObjenesisCglibAopProxy(config);
			 System.out.println(" < returning class proxy: "+objenesisCglibAopProxy+"   (config=0x"+System.identityHashCode(config)+")");
			 return objenesisCglibAopProxy;
		}
		else {
			AopProxy ap = new JdkDynamicAopProxy(config);
			System.out.println(" = falling back to JdkDynamicProxy...");
			System.out.println(" = interfaces: "+Arrays.toString(ap.getProxy().getClass().getInterfaces()));
			System.out.println(" < returning JdkDynamicAopProxy: "+ap+"   (config=0x"+System.identityHashCode(config)+")");
			return ap;
		}
	}

 	@Alias
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		return true;
	}

}
