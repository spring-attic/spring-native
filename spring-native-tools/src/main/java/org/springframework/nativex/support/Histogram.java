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
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
=== Total ===
   Count     Size   Size%    Cum% Class
    2524 10294208  35.74%  35.74% byte[]
   64500  5155792  17.90%  53.64% char[]
   11626  3495472  12.14%  65.77% java.lang.Class
*/
public class Histogram {
	
	static class Datum implements Comparable<Datum> {
		//     2524 10294208  35.74%  35.74% byte[]
		static final Pattern p = Pattern.compile("^ *([0-9]*) *([0-9]*) *([0-9\\.]*)% *([0-9\\.]*)% *(.*)$");
		
		// com.oracle.svm.reflect.SimpleModule_setupModule_fe06d5e7c9aea7e4673462ea6f849728958a9dd8
		static final Pattern withoutHex = Pattern.compile("^(.*)_[0-9a-f]*$");
		int count;
		int size;
		double sizePercent;
		double cPercent;
		String classname;
		boolean isReflection;
		
		public int getSize() {
			return size;
		}
		
		private Datum(int count, int size, double sizePercent, double cPercent, String classname) {
			this.count = count;
			this.size = size;
			this.sizePercent = sizePercent;
			this.cPercent = cPercent;
			// Strip hex from the end of entries like this
			// com.oracle.svm.reflect.SimpleModule_setupModule_fe06d5e7c9aea7e4673462ea6f849728958a9dd8
			Matcher matcher = withoutHex.matcher(classname);
			if (matcher.find()) {
				this.classname = matcher.group(1);
				this.isReflection = true;
			} else {
				this.classname = classname;
				this.isReflection = false;
			}
		}
		
		public String getClassname() {
			return classname;
		}

		public static Datum from(String line) {
			Matcher matcher = p.matcher(line);
			boolean matched = matcher.find();
			if (!matched || matcher.groupCount()!=5) {
				throw new IllegalStateException("Unable to match '"+line+
						"' "+(!matched?"":"(groupCount="+matcher.groupCount()+")"));
			}
			return new Datum(
				Integer.parseInt(matcher.group(1)),
				Integer.parseInt(matcher.group(2)),
				Double.parseDouble(matcher.group(3)),
				Double.parseDouble(matcher.group(4)),
				matcher.group(5));
		}

		public String getPackageName() {
			int lastDot = classname.lastIndexOf(".");
			return (lastDot==-1)?classname:classname.substring(0,lastDot+1);
		}

		public boolean isReflection() {
			return isReflection;
		}

		@Override
		public int compareTo(Datum o) {
			if (classname.equals(o.classname)) {
				return size-o.size;
			} else {
				return classname.compareTo(o.classname);
			}
		}

		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append(classname).append("::").append(size);
			return s.toString();
		}
	}

	private String name;
	private List<Datum> data;
	
	private Histogram() {}
	
	private Histogram(String name, List<Datum> data) {
		this.name= name;
		this.data = data;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Datum> getData() {
		return data;
	}

	public static Histogram load(String name, String file) {
		try {
			List<Datum> data = new ArrayList<>();
			List<String> lines = Files.readAllLines(Paths.get(new File(file).toURI()));
			// From marker "=== Total ===", jump one line then read all until next blank line
			boolean processingData = false;
			boolean inTotalSection = false;
			for (String line: lines) {
				if (inTotalSection && processingData) {
					if (line.trim().length()==0) {
						break;
					}
					data.add(Datum.from(line));
				} else if (line.trim().equals("=== Total ===")) {
					inTotalSection = true;
				} else if (line.trim().equals("")) {
					inTotalSection = false;
				} else if (inTotalSection && line.matches(".*Count.*Size.*Size%.*Cum%.*Class.*$")) {//   Count     Size   Size%    Cum% Class
					processingData = true;
				}
			}
			return new Histogram(name, data);
		} catch (IOException ioe) {
			throw new IllegalStateException("Problem loading file: "+file,ioe);
		}
	}

	public Set<String> packagesNotIn(Histogram b) {
		Set<String> thisPackages = getPackages();
		Set<String> thatPackages = b.getPackages();
		Set<String> result = new TreeSet<>();
		result.addAll(thisPackages);
		result.removeAll(thatPackages);
		return result;
	}

	public Set<Datum> typesNotIn(Histogram b) {
		return typesNotIn(b,false);
	}

	public Set<Datum> typesNotIn(Histogram b,boolean skipReflectiveEntries) {
		Set<Datum> result = new TreeSet<>();
		for (Datum d: data) {
			if (skipReflectiveEntries && d.isReflection) continue;
			if (!b.containsClassname(d.getClassname())) {
				result.add(d);
			}
		}
		return result;
	}
	
	private boolean containsClassname(String classname) {
		for (Datum d: data) {
			if (d.classname.equals(classname)) {
				return true;
			}
		}
		return false;
	}

	private Set<String> packages = null;

	private Set<String> getPackages() {
		if (this.packages == null) {
			this.packages = new TreeSet<>();
			for (Datum d: data) {
				String packageName = d.getPackageName();
				packages.add(packageName);
			}
		}
		return this.packages;
	}

	public List<Datum> getReflectiveEntries() {
		List<Datum> result = new ArrayList<>();
		for (Datum d: data) {
			if (d.isReflection) {
				result.add(d);
			}
		}
		return result;
	}

	public int getSizeOf(String classname) {
		for (Datum d: data) {
			if (d.getClassname().equals(classname)) {
				return d.size;
			}
		}
		return 0;
	}

}
