package org.springframework.aot.beans.factory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Resolved attributes of an injected element.
 *
 * @author Stephane Nicoll
 */
public class InjectedElementAttributes {

	private final List<Object> attributes;

	InjectedElementAttributes(List<Object> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Specify if the attributes have been resolved, or {@code null} to indicate that
	 * the injection should not happen.
	 * @return the resolution of the injection
	 */
	public boolean isResolved() {
		return (this.attributes != null);
	}

	/**
	 * Run the specified {@link Runnable task} only if this instance is
	 * {@link #isResolved() resolved}.
	 * @param task the task to invoke if this instance is resolved
	 */
	public void ifResolved(Runnable task) {
		if (isResolved()) {
			task.run();
		}
	}

	/**
	 * Invoke the specified {@link Consumer} if this instance is {@link #isResolved()
	 * resolved}.
	 * @param attributes the consumer to invoke if this instance is resolved
	 */
	public void ifResolved(SmartConsumer<InjectedElementAttributes> attributes) {
		ifResolved(() -> attributes.accept(this));
	}


	@SuppressWarnings("unchecked")
	public <T> T get(int index) {
		return (T) attributes.get(index);
	}

}
