package org.springframework.nativex.substitutions.framework;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.core.DecoratingProxy;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.util.ClassUtils;

// TODO Remove when https://github.com/oracle/graal/issues/3870 will be fixed
@TargetClass(className = "org.springframework.aop.framework.AopProxyUtils", onlyWith = { OnlyIfPresent.class })
final class Target_AopProxyUtils {

	@Substitute
	static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised, boolean decoratingProxy) {
		Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();
		if (specifiedInterfaces.length == 0) {
			// No user-specified interfaces: check whether target class is an interface.
			Class<?> targetClass = advised.getTargetClass();
			if (targetClass != null) {
				if (targetClass.isInterface()) {
					advised.setInterfaces(targetClass);
				}
				else if (Proxy.isProxyClass(targetClass)) {
					advised.setInterfaces(targetClass.getInterfaces());
				}
				specifiedInterfaces = advised.getProxiedInterfaces();
			}
		}
		List<Class<?>> proxiedInterfaces = new ArrayList<>(specifiedInterfaces.length + 3);
		for (Class<?> ifc : specifiedInterfaces) {
			// Only non-sealed interfaces are actually eligible for JDK proxying (on JDK 17)
			//if (isSealedMethod == null || Boolean.FALSE.equals(ReflectionUtils.invokeMethod(isSealedMethod, ifc))) {
				proxiedInterfaces.add(ifc);
			//}
		}
		if (!advised.isInterfaceProxied(SpringProxy.class)) {
			proxiedInterfaces.add(SpringProxy.class);
		}
		if (!advised.isOpaque() && !advised.isInterfaceProxied(Advised.class)) {
			proxiedInterfaces.add(Advised.class);
		}
		if (decoratingProxy && !advised.isInterfaceProxied(DecoratingProxy.class)) {
			proxiedInterfaces.add(DecoratingProxy.class);
		}
		return ClassUtils.toClassArray(proxiedInterfaces);
	}
}
