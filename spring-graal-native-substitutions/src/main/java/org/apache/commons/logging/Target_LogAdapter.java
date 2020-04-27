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

package org.apache.commons.logging;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.internal.svm.OnlyPresent;

// Workaround for https://github.com/oracle/graal/issues/904 + print the log system used
@TargetClass(className = "org.apache.commons.logging.LogAdapter", onlyWith = OnlyPresent.class)
final class Target_LogAdapter {

	@Alias
	private static String SLF4J_SPI;

	@Alias
	private static String SLF4J_API;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias, isFinal = true)
	private static Target_LogApi logApi;

	static {
		if (isPresent(SLF4J_SPI)) {
			// Full SLF4J SPI including location awareness support
			logApi = Target_LogApi.SLF4J_LAL;
			System.out.println("Full SLF4J SPI logging system used");
		}
		else if (isPresent(SLF4J_API)) {
			// Minimal SLF4J API without location awareness support
			logApi = Target_LogApi.SLF4J;
			System.out.println("Minimal SLF4J API logging system used");
		}
		else {
			// java.util.logging as default
			logApi = Target_LogApi.JUL;
			System.out.println("java.util.logging logging system used");
		}
	}

	@Substitute
	public static Log createLog(String name) {
		if (logApi.equals(Target_LogApi.SLF4J_LAL)) {
			return Target_Slf4jAdapter.createLocationAwareLog(name);
		}
		else if (logApi.equals(Target_LogApi.SLF4J)) {
			return Target_Slf4jAdapter.createLog(name);
		}
		return Target_JavaUtilAdapter.createLog(name);
	}

	@Alias
	private static boolean isPresent(String className) {
		return false;
	}


}

@TargetClass(className = "org.apache.commons.logging.LogAdapter", innerClass = "LogApi")
final class Target_LogApi {

	@Alias
	public static Target_LogApi SLF4J_LAL;

	@Alias
	public static Target_LogApi SLF4J;

	@Alias
	public static Target_LogApi JUL;

}

@TargetClass(className = "org.apache.commons.logging.LogAdapter", innerClass = "Slf4jAdapter")
final class Target_Slf4jAdapter {

	@Alias
	public static Log createLocationAwareLog(String name) {
		return null;
	}

	@Alias
	public static Log createLog(String name) {
		return null;
	}

}

@TargetClass(className = "org.apache.commons.logging.LogAdapter", innerClass = "JavaUtilAdapter")
final class Target_JavaUtilAdapter {

	@Alias
	public static Log createLog(String name) {
		return null;
	}
}
