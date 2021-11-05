/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.nativex.substitutions.framework.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.apache.commons.logging.Log;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.test.context.TestExecutionListener;

/**
 * Native substitution for {@link org.springframework.test.context.support.AbstractTestContextBootstrapper}.
 *
 * <p>Necessary since class instantiation via reflection can result in a
 * {@link NoSuchMethodException} instead of a {@link NoClassDefFoundError}
 * within a native image, IF a {@link NoClassDefFoundError} was thrown during
 * native image build time while attempting to register the class for reflection.
 * In that case, the reflection registration still occurs, and we unfortunately
 * see different behavior at runtime within the native image.
 *
 * <p>In summary, the only change this substitution makes is the following
 * which additionally checks for a {@code NoSuchMethodException}:
 * {@code if (cause instanceof NoClassDefFoundError || cause instanceof NoSuchMethodException)}.
 *
 * @author Sam Brannen
 */
@TargetClass(className = "org.springframework.test.context.support.AbstractTestContextBootstrapper", onlyWith = OnlyIfPresent.class)
final class Target_AbstractTestContextBootstrapper {

	@Alias
	private Log logger;


	@Substitute
	private List<TestExecutionListener> instantiateListeners(Collection<Class<? extends TestExecutionListener>> classes) {
		List<TestExecutionListener> listeners = new ArrayList<>(classes.size());
		for (Class<? extends TestExecutionListener> listenerClass : classes) {
			try {
				listeners.add(BeanUtils.instantiateClass(listenerClass));
			}
			catch (BeanInstantiationException ex) {
				Throwable cause = ex.getCause();
				// Within a native image, NoSuchMethodException may be thrown instead of NoClassDefFoundError.
				if (cause instanceof NoClassDefFoundError || cause instanceof NoSuchMethodException) {
					// TestExecutionListener not applicable due to a missing dependency
					if (logger.isDebugEnabled()) {
						logger.debug(String.format(
								"Skipping candidate TestExecutionListener [%s] due to a missing dependency. " +
								"Specify custom listener classes or make the default listener classes " +
								"and their required dependencies available. Offending class: [%s]",
								listenerClass.getName(), cause.getMessage()));
					}
				}
				else {
					throw ex;
				}
			}
		}
		return listeners;
	}

}
