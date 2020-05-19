package org.springframework.aop.framework;

import java.lang.reflect.Proxy;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.aop.SpringProxy;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveCglibSupport;

@TargetClass(className = "org.springframework.aop.framework.DefaultAopProxyFactory", onlyWith = { OnlyPresent.class, RemoveCglibSupport.class })
public final class Target_DefaultAopProxyFactory {

	@Substitute
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			throw new AopConfigException("Can't create an ObjenesisCglibAopProxy since it is unsupported in native images");
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

	@Alias
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class<?>[] ifcs = config.getProxiedInterfaces();
		return ifcs.length == 0 || ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0]);
	}
}
