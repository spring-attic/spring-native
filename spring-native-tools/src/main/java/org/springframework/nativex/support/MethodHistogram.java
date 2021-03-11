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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/*
  Code Size; Nodes Before; Nodes After; Is Trivial; Deopt Target; Code Size; Nodes Before; Nodes After; Deopt Entries; Deopt During Call; Entry Points; Direct Calls; Virtual Calls; Method
     610;    54;   203;  ;  ;      0;     0;     0;    0;    0;    0;    0;  162; app.main.Foo.toString() String
   14672;  2575;  3935;  ;  ;      0;     0;     0;    0;    0;    1;    0;    0; app.main.GeneratedConditionService.<clinit>() void
*/
public class MethodHistogram {
	
	static class Datum implements Comparable<Datum> {
		
		Map<String,Object> data;
		
		public int getCodeSize() {
			return (Integer)data.get("codesize");
		}
		
		private Datum(Map<String,Object> data) {
			this.data = data;
		}
		
		public String getMethod() {
			return (String)data.get("method");
		}

		public static Datum from(String line) {
			StringTokenizer st = new StringTokenizer(line,";");
			Map<String,Object> fieldsCaredAbout = new HashMap<>();
			fieldsCaredAbout.put("codesize",Integer.parseInt(st.nextToken().trim())); // Code Size
			st.nextToken();// Nodes Before
			st.nextToken();// Nodes After
			st.nextToken();// Nodes Is Trivial
			st.nextToken();// Deopt Target
			st.nextToken();// Code Size
			st.nextToken();// Nodes Before
			st.nextToken();// Nodes After
			st.nextToken();// Deopt Entries
			st.nextToken();// Deopt During Call
			int entryPoints = Integer.parseInt(st.nextToken().trim());// Entry Points
			int dCalls = Integer.parseInt(st.nextToken().trim());// Direct Calls
			int vCalls = Integer.parseInt(st.nextToken().trim());// Virtual Calls
			fieldsCaredAbout.put("method",st.nextToken());// Method
			if (entryPoints+dCalls+vCalls==0) {
				System.out.println("Whats this one? "+fieldsCaredAbout.get("method"));
			}
			return new Datum(fieldsCaredAbout);

		}

		public String getPackageName() {
			int lastDot = getMethod().lastIndexOf(".");
			return (lastDot==-1)?getMethod():getMethod().substring(0,lastDot+1);
		}


		@Override
		public int compareTo(Datum o) {
			if (getMethod().equals(o.getMethod())) {
				return getCodeSize()-o.getCodeSize();
			} else {
				return getMethod().compareTo(o.getMethod());
			}
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append(getMethod()).append("::").append(getCodeSize());
			return s.toString();
		}
	}

	private List<Datum> data;
	
	private MethodHistogram() {}
	
	private MethodHistogram(List<Datum> data) {
		this.data = data;
	}
	
	public List<Datum> getData() {
		return data;
	}

	public static MethodHistogram load(String file) {
		try {
			List<Datum> data = new ArrayList<>();
			List<String> lines = Files.readAllLines(Paths.get(new File(file).toURI()));
			boolean processingData = false;
			for (String line: lines) {
				if (processingData) {
					if (line.trim().length()==0) {
						break;
					}
					data.add(Datum.from(line));
				} else if (line.startsWith("Code Size;")) {
					processingData = true;
				}
			}
			return new MethodHistogram(data);
		} catch (IOException ioe) {
			throw new IllegalStateException("Problem loading file: "+file,ioe);
		}
	}
//
//	public Set<String> packagesNotIn(MethodHistogram b) {
//		Set<String> thisPackages = getPackages();
//		Set<String> thatPackages = b.getPackages();
//		Set<String> result = new TreeSet<>();
//		result.addAll(thisPackages);
//		result.removeAll(thatPackages);
//		return result;
//	}

//	public Set<Datum> typesNotIn(MethodHistogram b) {
//		return typesNotIn(b,false);
//	}
//
//	public Set<Datum> typesNotIn(MethodHistogram b,boolean skipReflectiveEntries) {
//		Set<Datum> result = new TreeSet<>();
//		for (Datum d: data) {
//			if (skipReflectiveEntries && d.isReflection) continue;
//			if (!b.containsClassname(d.getClassname())) {
//				result.add(d);
//			}
//		}
//		return result;
//	}
//	
//	private boolean containsClassname(String classname) {
//		for (Datum d: data) {
//			if (d.classname.equals(classname)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private Set<String> packages = null;
//
//	private Set<String> getPackages() {
//		if (this.packages == null) {
//			this.packages = new TreeSet<>();
//			for (Datum d: data) {
//				String packageName = d.getPackageName();
//				packages.add(packageName);
//			}
//		}
//		return this.packages;
//	}
//
//	public List<Datum> getReflectiveEntries() {
//		List<Datum> result = new ArrayList<>();
//		for (Datum d: data) {
//			if (d.isReflection) {
//				result.add(d);
//			}
//		}
//		return result;
//	}
//
//	public int getSizeOf(String classname) {
//		for (Datum d: data) {
//			if (d.getClassname().equals(classname)) {
//				return d.size;
//			}
//		}
//		return 0;
//	}

}
