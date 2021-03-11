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
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CallTree is created from a file containing lines like:
 * 
 * <pre>
 * <code>
├── entry com.oracle.svm.core.classinitialization.ClassInitializationInfo.initialize(com.oracle.svm.core.classinitialization.ClassInitializationInfo, java.lang.Class):void id=1 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.initializeSuperInterfaces(java.lang.Class):void id=876 @bci=192 
│   │   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.initializeSuperInterfaces(java.lang.Class):void id-ref=876 @bci=53 
│   │   ├── directly calls java.lang.Class.ensureInitialized():void id-ref=883 @bci=66 
│   │   └── directly calls java.lang.Class.getInterfaces():java.lang.Class[] id=1449 @bci=24 
│   │       └── directly calls java.lang.Class.getInterfaces(java.lang.Class, boolean):java.lang.Class[] id=2152 @bci=2 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.invokeClassInitializer():void id=877 @bci=211 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.isBeingInitialized():boolean id=878 @bci=12 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.isBeingInitialized():boolean id-ref=878 @bci=57 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.isInErrorState():boolean id=879 @bci=95 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.isInitialized():boolean id=880 @bci=80 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.isReentrantInitialization(java.lang.Thread):boolean id=881 @bci=65 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.isReentrantInitialization(java.lang.Thread):boolean id-ref=881 @bci=20 
│   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.setInitializationStateAndNotify(com.oracle.svm.core.classinitialization.ClassInitializationInfo$InitState):void id=882 @bci=256 
│   │   ├── virtually calls java.util.concurrent.locks.Condition.signalAll():void @bci=34
│   │   │   └── is overridden by java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.signalAll():void id=1450 
│   │   │       ├── directly calls java.lang.IllegalMonitorStateException.&lt;init&gt;():void id-ref=1520 @bci=14
 * </code>
 * </pre>
 * 
 * This information is output from a native-image run.
 */
public class CallTree {

	static final int IGNORE_OVERRIDDEN = 1;

	private String id;
	private Node data;

	private CallTree() {
		this.id = "";
		this.data = null;
	}

	private CallTree(String id, Node tree) {
		this.id = id;
		this.data = tree;
	}

	public String getId() {
		return id;
	}

	public Node getData() {
		return data;
	}
	
	static class Node {

		Entry e;
		List<Node> kids;
		Node parent;
		private int indent;

		public Node(Entry e, int indent) {
			this.e = e;
			this.indent = indent;
		}
		
		public int getIndent() {
			return indent;
		}
		
		public String toString() {
			StringBuilder s = new StringBuilder();
			if (e!=null) {
				s.append(e);
			}
			if (kids!=null) {
				s.append(" #"+kids.size());
			}
			return s.toString();
		}
		
		public Node() {
		}

		public void addChild(Node n) {
			if (kids == null) {
				kids = new ArrayList<>();
			}
			kids.add(n);
			n.setParent(this);
		}

		private void setParent(Node node) {
			this.parent = node;
		}

		public Node getParent() {
			return this.parent;
		}

		public int size() {
			return this.kids.size();
		}

		public List<Node> getKids() {
			return kids;
		}

		public Entry getValue() {
			return e;
		}

		public void print(int indent,int max) {
			if (indent>max) {
				return;
			}
			StringBuilder s = new StringBuilder();
			for (int i=0;i<indent;i++) {
				s.append(" ");
			}
			if (e!=null) {
				s.append(e);
			}
			System.out.println(s);
			if (kids !=null) {
				for (Node kid: kids) {
					kid.print(indent+2,max);
				}
			}
		}

		public void print(int i) {
			print(i,10000000);
		}

		public int length() {
			int total = 1;
			if (kids != null) {
				for (Node kid: kids) {
					total+=kid.length();
				}
			}
			return total;
		}
		
		public void collect(Predicate<Node> test, List<Node> collector) {
			if (test.test(this)) {
				collector.add(this);
			}
			if (kids != null) {
				for (Node kid: kids) {
					kid.collect(test, collector);
				}
			}
		}
		
		void printRoutes(String string) {

			
		}
		
		public boolean printRoute(int depth, Predicate<Node> test, List<String> collector) {
			boolean kidMatches = false;
			if (kids != null) {
				for (Node kid: kids) {
					kidMatches=kid.printRoute(depth+1,test,collector) || kidMatches;
				}
			}
			if (test.test(this) || kidMatches) {
				collector.add(toString(depth));
				return true;
			} else {
				return false;
			}
		}

		private String toString(int indent) {
			StringBuilder s = new StringBuilder();
			for (int i=0;i<indent;i++) {
				s.append(" ");
			}
			if (e!=null) {
				s.append(e);
			}
			return s.toString();
		}

		public Entry getEntry() {
			return this.e;
		}

		public void printRoute() {
			List<Node> path = new ArrayList<>();
			Node current= this;
			while (current!=null) {
				path.add(0,current);
				current = current.getParent();
			}
			for (int i=0;i<path.size();i++) {
				System.out.print("                                                                                                                      ".substring(0,i*2));
				System.out.println(path.get(i));
			}
		}
		
	}

	/**
	 * Attempt to load 'Compiled...' lines from the specified file and produce a
	 * summary.
	 * 
	 * @param id   an arbitrary string used to identify what is being loaded
	 * @param file the file (usually containing output from native-image) that
	 *             contains 'Compiled...' lines
	 * @return a CompilationSummary containing parses of all the 'Compiled...' lines
	 */
	public static CallTree load(String id, String file) {
		try {
			Node tree = new Node();
			List<String> lines = Files.readAllLines(Paths.get(new File(file).toURI()));
			int entries = 0;
			boolean valid = false;
			Node current = tree;
			int lastIndent = 0;
			for (String line : lines) {
				entries++;
				if (valid) {
					Entry e = null;
					if ((entries % 10000) == 0) {
						System.out.print(".");
						System.out.flush();
					} 
					int indent = line.lastIndexOf("─");
					if (EntryPoint.matches(line)) {
						e = EntryPoint.process(line);
					} else if (DirectlyCalls.matches(line)) {
						e = DirectlyCalls.process(line);
					} else if (VirtuallyCalls.matches(line)) {
						e = VirtuallyCalls.process(line);
					} else if (OverriddenBy.matches(line)) {
						e = OverriddenBy.process(line);
					} else if (line.trim().length()==0) {
						// ...
						continue;
					} else {
						throw new IllegalStateException("What is this? "+line);
					}
					Node n = new Node(e,indent);
//					System.out.println("Next is "+e+" indent="+indent+" (lastIndent="+lastIndent+")");

					if (lastIndent == 0) {
						// special case, first 
						current.addChild(n);
						current = n;
					} else if (indent == lastIndent) {
						current.getParent().addChild(n);
						current = n;
					} else if (indent>lastIndent) {
						// deeper
						current.addChild(n);
						current = n;
					} else if (lastIndent>indent) {
						// shallower
						while (current.getIndent()!=indent) {
							current = current.getParent();
						}
						current = current.getParent();
						current.addChild(n);
						current = n;
					}
					lastIndent = indent;
				}
//				if (entries>100000) {
//					break;
//				}
				if (line.equals("VM Entry Points")) {
					valid = true;
					System.out.print("Loading call tree");
				}
			}
			System.out.println("#"+entries+" entries");
			return new CallTree(id, tree);
		} catch (IOException ioe) {
			throw new IllegalStateException("Problem loading file: " + file, ioe);
		}
	}

	interface Entry {

		default boolean contains(String string) {
			return toString().contains(string);
		}

	}

	static class EntryPoint implements Entry {
		// ├── entry com.example.commandlinerunner.CLR.<clinit>():void id=0
		private static final Pattern p = Pattern.compile("^.* entry ([^:]*):([^ ]*).*$");
		
		private String entryPoint;

		public EntryPoint(String entryPoint) {
			this.entryPoint = entryPoint;
		}
		
		public String toString() {
			return "Entry: "+entryPoint;
		}

		public static boolean matches(String line) {
			return line.contains("─ entry ");
		}

		public static EntryPoint process(String line) {
			Matcher matcher = p.matcher(line);
			boolean matched = matcher.find();
			if (!matched || matcher.groupCount() != 2) {
				throw new IllegalStateException("Unexpectedly unable to match '" + line + "' "
						+ (!matched ? "" : "(groupCount=" + matcher.groupCount() + ")"));
			}
			return new EntryPoint(matcher.group(1));
		}
	}

	static class DirectlyCalls implements Entry {
		// │   ├── directly calls com.oracle.svm.core.classinitialization.ClassInitializationInfo.initializeSuperInterfaces(java.lang.Class):void id=876 @bci=192 
		private static final Pattern p = Pattern.compile("^.* directly calls ([^:]*):([^ ]*).*$");
		
		private String target;

		public DirectlyCalls(String target) {
			this.target = target;
		}

		public String toString() {
			return "DirectlyCalls: "+target;
		}

		public static boolean matches(String line) {
			return line.contains("─ directly calls ");
		}

		public static DirectlyCalls process(String line) {
			Matcher matcher = p.matcher(line);
			boolean matched = matcher.find();
			if (!matched || matcher.groupCount() != 2) {
				throw new IllegalStateException("Unexpectedly unable to match '" + line + "' "
						+ (!matched ? "" : "(groupCount=" + matcher.groupCount() + ")"));
			}
			return new DirectlyCalls(matcher.group(1));
		}

	}

	static class VirtuallyCalls implements Entry {
		// ├── virtually calls java.util.concurrent.locks.Condition.signalAll():void @bci=34
		private static final Pattern p = Pattern.compile("^.* virtually calls ([^:]*):([^ ]*).*$");
		
		private String target;

		public VirtuallyCalls(String target) {
			this.target = target;
		}

		public String toString() {
			return "VirtuallyCalls: "+target;
		}

		public static boolean matches(String line) {
			return line.contains("─ virtually calls ");
		}

		public static VirtuallyCalls process(String line) {
			Matcher matcher = p.matcher(line);
			boolean matched = matcher.find();
			if (!matched || matcher.groupCount() != 2) {
				throw new IllegalStateException("Unexpectedly unable to match '" + line + "' "
						+ (!matched ? "" : "(groupCount=" + matcher.groupCount() + ")"));
			}
			return new VirtuallyCalls(matcher.group(1));
		}

	}
	

	static class OverriddenBy implements Entry {
		// is overridden by java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.signalAll():void 
		private static final Pattern p = Pattern.compile("^.* is overridden by ([^:]*):([^ ]*).*$");
		
		private String target;

		public OverriddenBy(String target) {
			this.target = target;
		}

		public String toString() {
			return "OverriddenBy: "+target;
		}

		public static boolean matches(String line) {
			return line.contains("─ is overridden by ");
		}

		public static OverriddenBy process(String line) {
			Matcher matcher = p.matcher(line);
			boolean matched = matcher.find();
			if (!matched || matcher.groupCount() != 2) {
				throw new IllegalStateException("Unexpectedly unable to match '" + line + "' "
						+ (!matched ? "" : "(groupCount=" + matcher.groupCount() + ")"));
			}
			return new OverriddenBy(matcher.group(1));
		}

	}


	static class Compiled implements Comparable<Compiled> {
		// Compiling void
		// org.springframework.beans.factory.BeanCreationException.<init>(String,
		// String, Throwable) [Direct call from void
		// ConfigurationPropertiesBindException.<init>(ConfigurationPropertiesBean,
		// Exception)]
		private static final Pattern p = Pattern.compile("^.* entry ([^ ]*):([^ ]*).*$");

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
				if (u != -1) {
					type = type.substring(0, u);
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

	public int length() {
		return data.length();
	}

	public void stripOut(CallTree b) {
		// For each 
	}

	public void printRoutes(String string, int flags) {
		List<String> collector = new ArrayList<>();
		data.printRoute(0, (n)-> {
			if ((flags & IGNORE_OVERRIDDEN)!=0) {
				if (n.getEntry() instanceof OverriddenBy) {
					return false;
				}
			}
			return n.getEntry()!=null && n.getEntry().contains(string);
		}, collector);
		for (int i=0;i<collector.size();i++) {
			System.out.println(collector.get(collector.size()-1-i));
		}
	}
	
	public List<Node> findNodes(String string) {
		List<Node> collector = new ArrayList<>();
		data.collect((n)->{
			if (n.getEntry() instanceof OverriddenBy) {
				return false;
			}
			return n.getEntry()!=null && n.getEntry().contains(string);
		}, collector);
		return collector;
	}

	/*
	 * public String getReason(String typename) { for (Compiled compiled: data) { if
	 * (compiled.type.equals(typename)) { return compiled.getReason(); } } return
	 * null; }
	 */

}
