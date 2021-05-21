/*
 * Copyright 2021 the original author or authors.
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

package org.springframework.nativex.hint;

/**
 * Specifies the special behaviours that need to supported by class proxies when they are generated.
 * 
 * @author Andy Clement
 */
public class ProxyBits {

	// TODO javadoc
	
	public static final int NONE                  = 0x0000;
	public static final int EXPOSE_PROXY          = 0x0001;
	public static final int IS_STATIC             = 0x0002;
	public static final int IS_FROZEN             = 0x0004;
	public static final int IS_OPAQUE             = 0x0008;

	private int value;

	public int hashCode() {
		return value;
	}

	public boolean equals(Object that) {
		if (that instanceof ProxyBits) {
			return this.value == ((ProxyBits) that).value;
		}
		return false;
	}

	public String toString() {
		return toString(value);
	}

	public ProxyBits() {
		value = 0;
	}

	public ProxyBits(int value) {
		this.value = value;
	}

	public final static ProxyBits forValue(int value) {
		return new ProxyBits(value);
	}

	public final static ProxyBits forBits(int... bits) {
		int value = 0;
		for (int i = 0; i < bits.length; i++) {
			value |= bits[i];
		}
		return new ProxyBits(value);
	}

	public ProxyBits with(ProxyBits accessRequired) {
		return forValue(value | accessRequired.value);
	}

	public static String toString(Integer value) {
		StringBuilder s = new StringBuilder();
		s.append("PBS(");
		if ((value & IS_STATIC) != 0) {
			s.append("IS_STATIC ");
		}
		if ((value & EXPOSE_PROXY) != 0) {
			s.append("EXPOSE_PROXY ");
		}
		if ((value & IS_FROZEN) != 0) {
			s.append("IS_FROZEN ");
		}
		if ((value & IS_OPAQUE) != 0) {
			s.append("IS_OPAQUE ");
		}
		if (value == 0) {
			s.append("NONE");
		}
		return s.toString().trim() + ")";
	}

	public int getValue() {
		return value;
	}

	public static boolean isSet(int value, int mask) {
		return (value&mask)!=0;
	}
}