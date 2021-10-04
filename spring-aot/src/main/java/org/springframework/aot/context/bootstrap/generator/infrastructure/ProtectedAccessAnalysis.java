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

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The analysis of any protected access.
 *
 * @author Stephane Nicoll
 */
public class ProtectedAccessAnalysis {

	private final List<ProtectedElement> protectedElements;

	ProtectedAccessAnalysis(List<ProtectedElement> protectedElements) {
		this.protectedElements = protectedElements;
	}

	/**
	 * Specify if the analysis concluded that the elements are accessible from the
	 * requested package.
	 * @return {@code true} if the elements are accessible, {@link false} otherwise
	 */
	public boolean isAccessible() {
		return this.protectedElements.isEmpty();
	}

	/**
	 * Return the privileged package name to use, or {@code null} if none is required.
	 * @return the priviledged package name to use, or {@code null}
	 */
	public String getPrivilegedPackageName() {
		if (isAccessible()) {
			return null;
		}
		List<String> packageNames = this.protectedElements.stream().map((element) -> element.type.getPackageName())
				.distinct().collect(Collectors.toList());
		if (packageNames.size() == 1) {
			return packageNames.get(0);
		}
		throw new IllegalStateException("Multiple privileged packages: " + packageNames);
	}

	static class ProtectedElement {

		private final Class<?> type;

		private final Object target;

		private ProtectedElement(Class<?> type, Object target) {
			this.type = type;
			this.target = target;
		}

		static ProtectedElement of(Class<?> type, Object target) {
			return new ProtectedElement(type, target);
		}
	}
}
