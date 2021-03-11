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

package org.springframework.nativex.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CompilationSummary is created from a file containing lines like:
 * <pre><code>
Compiling Enumeration java.lang.ClassLoader.getSystemResources(String)  [Direct call from boolean ServiceLoader$LazyIterator.hasNextService()]
Compiling boolean com.oracle.svm.core.jdk8.zipfile.ZipFile$ZipEntryIterator.hasMoreElements()  [Virtual call from boolean ServiceLoader$LazyIterator.hasNextService(), callTarget boolean Enumeration.hasMoreElements()]
Compiling String org.springframework.core.MethodParameter.lambda$validateIndex$0(int)  [Direct call from Object MethodParameter$$Lambda$c0f2232e503449199c16c75cdafef268b35dd263.get()]
Compiling void org.springframework.beans.factory.BeanCreationException.&lt;init&gt;(String, String, Throwable)  [Direct call from void ConfigurationPropertiesBindException.&lt;init&gt;(ConfigurationPropertiesBean, Exception)]
Compiling Object org.springframework.core.annotation.MergedAnnotationCollectors$$Lambda$387827c9271df01b9dedb80939c6683492e200b9.get()  [Virtual call from void SystemPropertiesSupport.initializeLazyValue(String), callTarget Object Supplier.get()]
 * </code></pre>
 * This information is output from a native-image run.
 */
public class CompilationSummary {

	private String id;
	private List<Compiled> data;

	private CompilationSummary() {
		this.id = "";
		this.data = new ArrayList<>();
	}

	private CompilationSummary(String id, List<Compiled> data) {
		this.id = id;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public List<Compiled> getData() {
		return data;
	}

	/**
	 * Attempt to load 'Compiled...' lines from the specified file and produce a summary.
	 * 
	 * @param id an arbitrary string used to identify what is being loaded
	 * @param file the file (usually containing output from native-image) that contains 'Compiled...' lines
	 * @return a CompilationSummary containing parses of all the 'Compiled...' lines
	 */
	public static CompilationSummary load(String id, String file) {
		if (file.equals("-")) {
			return new CompilationSummary();
		}
		try {
			List<Compiled> data = new ArrayList<>();
			List<String> lines = Files.readAllLines(Paths.get(new File(file).toURI()));
			for (String line : lines) {
				if (line.startsWith("Compiling ")) {
					data.add(Compiled.from(line));
				}
			}
			return new CompilationSummary(id, data);
		} catch (IOException ioe) {
			throw new IllegalStateException("Problem loading file: " + file, ioe);
		}
	}

	static class Compiled implements Comparable<Compiled> {
		// Compiling void org.springframework.beans.factory.BeanCreationException.<init>(String,
		// String, Throwable) [Direct call from void ConfigurationPropertiesBindException.<init>(ConfigurationPropertiesBean, Exception)]
		private static final Pattern p = Pattern.compile("^Compiling ([^ ]*) ([^ ]*)\\.([^ ]*)(\\(.*\\)) *\\[(.*)\\]$");

		private String returnType;
		private String type;
		private String method;
		private String parameterString;
		private String reason;

		private Compiled(String returnType, String type, String method, String parameterString, String reason) {
			this.returnType = returnType;
			this.type = type;
			this.method = method;
			this.parameterString = parameterString;
			this.reason = reason;
		}

		public static Compiled from(String line) {
			Matcher matcher = p.matcher(line);
			boolean matched = matcher.find();
			if (!matched || matcher.groupCount() != 5) {
				throw new IllegalStateException("Unable to match '" + line + "' "
						+ (!matched ? "" : "(groupCount=" + matcher.groupCount() + ")"));
			}
			String returnType = matcher.group(1);
			String type = matcher.group(2);
			String method = matcher.group(3);
			String parameterString = matcher.group(4);
			String reason = matcher.group(5);
			if (type.startsWith("com.oracle.svm.reflect")) {
				int u = type.lastIndexOf("_");
				if (u !=-1) {
					type = type.substring(0,u);
				}
			}
			return new Compiled(returnType, type, method, parameterString, reason);
		}

		public String getPackageName() {
			int dot = type.lastIndexOf(".");
			if (dot == -1) {
				return "default";
			} else {
				return type.substring(0, dot + 1);
			}
		}

		public String getType() {
			return type;
		}
		
		public String getReason() {
			return reason;
		}

		@Override
		public int compareTo(Compiled o) {
			int rc = returnType.compareTo(o.returnType);
			if (rc != 0) {
				return rc;
			} else {
				rc = type.compareTo(o.type);
				if (rc != 0) {
					return rc;
				} else {
					rc = method.compareTo(o.method);
					if (rc != 0) {
						return rc;
					} else {
						return parameterString.compareTo(o.parameterString);
					}
				}
			}
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append(returnType).append(" ").append(type).append(".").append(method).append(parameterString);
			return s.toString();
		}
	}

	public String getReason(String typename) {
		for (Compiled compiled: data) {
			if (compiled.type.equals(typename)) {
				return compiled.getReason();
			}
		}
		return null;
	}

}
