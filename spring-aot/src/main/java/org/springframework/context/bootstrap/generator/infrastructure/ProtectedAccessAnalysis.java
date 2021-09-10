package org.springframework.context.bootstrap.generator.infrastructure;

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
