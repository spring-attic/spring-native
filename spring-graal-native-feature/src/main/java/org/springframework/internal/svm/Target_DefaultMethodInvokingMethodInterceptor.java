/*
 * Copyright 2019 Contributors
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
package org.springframework.internal.svm;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;

/**
 * @author Andy Clement
 */
@TargetClass(className="org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor", onlyWith = OnlyPresent.class)
public final class Target_DefaultMethodInvokingMethodInterceptor {

	@Substitute
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		if (!method.isDefault()) {
			return invocation.proceed();
		}
		Object[] arguments = invocation.getArguments();
		Object proxy = ((ProxyMethodInvocation)invocation).getProxy();
		try {
			return method.invoke(proxy,arguments);
		} catch (UndeclaredThrowableException ute) {
			System.out.println("UNDECLARED THROWABLE: "+ute.getUndeclaredThrowable());
			throw ute;
		}
	}

}
