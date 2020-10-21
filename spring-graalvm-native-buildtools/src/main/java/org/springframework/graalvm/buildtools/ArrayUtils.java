/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.graalvm.buildtools;

/**
 * @author Dave Syer
 *
 */
public abstract class ArrayUtils {

	public static Object[] merge(Object[] args, Object... type ) {
		if (type.length==0) {
			return args;
		}
		Object[] result = new Object[args.length + type.length];
		System.arraycopy(type, 0, result, 0, type.length);
		System.arraycopy(args, 0, result, type.length, args.length);
		return result;
	}

	public static Object[] merge(Object type, Object... args) {
		Object[] result = new Object[args.length + 1];
		result[0] = type;
		System.arraycopy(args, 0, result, 1, args.length);
		return result;
	}

	public static Object[] merge(Object one, Object two, Object... args) {
		Object[] result = new Object[args.length + 2];
		result[0] = one;
		result[1] = two;
		System.arraycopy(args, 0, result, 2, args.length);
		return result;
	}

}
