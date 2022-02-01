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

package org.springframework.nativex.substitutions.boot;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import org.springframework.nativex.substitutions.OnlyIfPresent;

/**
 * Why this substitution exists?
 * To avoid shipping ImageBanner in the native image which is shipping itself a subset of AWT (really bad for the memory footprint).
 *
 * How this substitution workarounds the problem?
 * It disables image banner support and just returns the default one.
 */
@TargetClass(className = "org.springframework.boot.SpringApplicationBannerPrinter", onlyWith = OnlyIfPresent.class)
final class Target_SpringApplicationBannerPrinter {

	@Alias
	private static Banner DEFAULT_BANNER;

	@Substitute
	private Banner getBanner(Environment environment) {
		return DEFAULT_BANNER;
	}
}
