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

package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.ConditionDescriptor;
import org.springframework.nativex.hint.TypeAccess;

/**
 * Describe the need for reflection for a particular type.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Reflection/">GraalVM native image reflection documentation</a>
 * @see DefaultNativeReflectionEntry
 * @see GeneratedCodeNativeReflectionEntry
 */
public abstract class NativeReflectionEntry {

	private final Set<TypeAccess> access;

	private final String conditionalOnTypeReachable;

	protected NativeReflectionEntry(Builder<?, ?> builder) {
		this.access = Collections.unmodifiableSet(builder.access);
		this.conditionalOnTypeReachable = builder.conditionalOnTypeReachable;
	}

	/**
	 * Return the {@link TypeAccess access} to set.
	 * @return the access to set
	 */
	public Set<TypeAccess> getAccess() {
		return this.access;
	}

	/**
	 * Create a {@link ClassDescriptor} from this entry.
	 * @return a class descriptor describing this entry
	 */
	public ClassDescriptor toClassDescriptor() {
		ClassDescriptor descriptor = initializerClassDescriptor();
		if (this.conditionalOnTypeReachable != null) {
			descriptor.setCondition(new ConditionDescriptor(this.conditionalOnTypeReachable));
		}
		descriptor.setAccess(this.access);
		return descriptor;
	}

	protected abstract ClassDescriptor initializerClassDescriptor();

	protected <T> void registerIfNecessary(Iterable<T> members, TypeAccess allAccess,
			TypeAccess publicAccess, Predicate<T> isPublicMember, Consumer<T> memberConsumer) {
		if (!this.access.contains(allAccess)) {
			boolean checkVisibility = this.access.contains(publicAccess);
			for (T member : members) {
				if (!checkVisibility || !isPublicMember.test(member)) {
					memberConsumer.accept(member);
				}
			}
		}
	}

	public abstract static class Builder<B extends Builder<B, T>, T extends NativeReflectionEntry> {

		private final Set<TypeAccess> access = new LinkedHashSet<>();

		protected String conditionalOnTypeReachable;

		/**
		 * Set the specified {@link TypeAccess access}.
		 * @param access the access to set
		 * @return this for method chaining
		 */
		public B withAccess(TypeAccess... access) {
			this.access.addAll(Arrays.asList(access));
			return self();
		}

		@SuppressWarnings("unchecked")
		protected B self() {
			return (B) this;
		}

		abstract T build();

	}

}
