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
import org.springframework.nativex.hint.Flag;

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

	private final Set<Flag> flags;

	private final String conditionalOnTypeReachable;

	protected NativeReflectionEntry(Builder<?, ?> builder) {
		this.flags = Collections.unmodifiableSet(builder.flags);
		this.conditionalOnTypeReachable = builder.conditionalOnTypeReachable;
	}

	/**
	 * Return the {@link Flag flags} to set.
	 * @return the flags to set
	 */
	public Set<Flag> getFlags() {
		return this.flags;
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
		descriptor.setFlags(this.flags);
		return descriptor;
	}

	protected abstract ClassDescriptor initializerClassDescriptor();

	protected <T> void registerIfNecessary(Iterable<T> members, Flag allFlag,
			Flag publicFlag, Predicate<T> isPublicMember, Consumer<T> memberConsumer) {
		if (!this.flags.contains(allFlag)) {
			boolean checkVisibility = this.flags.contains(publicFlag);
			for (T member : members) {
				if (!checkVisibility || !isPublicMember.test(member)) {
					memberConsumer.accept(member);
				}
			}
		}
	}

	public abstract static class Builder<B extends Builder<B, T>, T extends NativeReflectionEntry> {

		private final Set<Flag> flags = new LinkedHashSet<>();

		protected String conditionalOnTypeReachable;

		/**
		 * Set the specified {@link Flag flags}.
		 * @param flags the flags to set
		 * @return this for method chaining
		 */
		public B withFlags(Flag... flags) {
			this.flags.addAll(Arrays.asList(flags));
			return self();
		}

		@SuppressWarnings("unchecked")
		protected B self() {
			return (B) this;
		}

		abstract T build();

	}

}
