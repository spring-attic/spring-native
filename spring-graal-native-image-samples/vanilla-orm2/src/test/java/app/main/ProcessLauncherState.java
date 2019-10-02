/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.thin.ArchiveUtils;
import org.springframework.boot.loader.thin.DependencyResolver;
import org.springframework.boot.loader.thin.PathResolver;
import org.springframework.util.ReflectionUtils;

public class ProcessLauncherState {

	private static final Logger log = LoggerFactory.getLogger(ProcessLauncherState.class);

	public static final String CLASS_COUNT_MARKER = "Class count";

	public static final String BEAN_COUNT_MARKER = "Bean count";

	private Process started;

	private List<String> args = new ArrayList<>();

	private List<String> progs = new ArrayList<>();

	private static List<String> DEFAULT_JVM_ARGS = Arrays.asList("-Xmx128m", "-cp", "",
			"-Djava.security.egd=file:/dev/./urandom", "-noverify",
			"-Dspring.data.jpa.repositories.bootstrap-mode=lazy",
			"-Dspring.cache.type=none", "-Dspring.main.lazy-initialization=true",
			"-Dspring.jmx.enabled=false");

	private File home;

	private String mainClass;

	private String name = "thin";

	private String[] profiles = new String[0];

	private BufferedReader buffer;

	private CountDownLatch latch = new CountDownLatch(1);

	private int classes;

	private int beans;

	private long memory;

	private long heap;

	private String classpath;

	public int getClasses() {
		return classes;
	}

	public int getBeans() {
		return beans;
	}

	public double getMemory() {
		return memory / (1024. * 1024);
	}

	public double getHeap() {
		return heap / (1024. * 1024);
	}

	public ProcessLauncherState(String dir, String... args) {
		this.args.addAll(DEFAULT_JVM_ARGS);
		String vendor = System.getProperty("java.vendor", "").toLowerCase();
		if (vendor.contains("ibm") || vendor.contains("j9")) {
			this.args.addAll(Arrays.asList("-Xms32m", "-Xquickstart", "-Xshareclasses",
					"-Xscmx128m"));
		}
		else {
			this.args.addAll(Arrays.asList("-XX:TieredStopAtLevel=1"));
		}
		if (System.getProperty("bench.args") != null) {
			this.args.addAll(Arrays.asList(System.getProperty("bench.args").split(" ")));
		}
		this.progs.addAll(Arrays.asList(args));
		this.home = new File(dir);
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProfiles(String... profiles) {
		this.profiles = profiles;
	}

	public void addArgs(String... args) {
		this.args.addAll(Arrays.asList(args));
	}

	protected String getClasspath() {
		return getClasspath(true);
	}

	protected String getClasspath(boolean includeTargetClasses) {
		if (this.classpath == null) {
			PathResolver resolver = new PathResolver(DependencyResolver.instance());
			Archive root = ArchiveUtils.getArchive(ProcessLauncherState.class);
			List<Archive> resolved = resolver.resolve(root, name, profiles);
			StringBuilder builder = new StringBuilder();
			if (includeTargetClasses) {
				builder.append(new File("target/classes").getAbsolutePath());
			}
			else {
				builder.append(new File("target/orm-0.0.1.BUILD-SNAPSHOT.jar")
						.getAbsolutePath());
			}
			try {
				for (Archive archive : resolved) {
					if (archive.getUrl().equals(root.getUrl())) {
						continue;
					}
					if (builder.length() > 0) {
						builder.append(File.pathSeparator);
					}
					builder.append(file(archive.getUrl().toString()));
				}
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException("Cannot find archive", e);
			}
			log.debug("Classpath: " + builder);
			this.classpath = builder.toString();
		}
		return this.classpath;
	}

	private String file(String path) {
		if (path.endsWith("!/")) {
			path = path.substring(0, path.length() - 2);
		}
		if (path.startsWith("jar:")) {
			path = path.substring("jar:".length());
		}
		if (path.startsWith("file:")) {
			path = path.substring("file:".length());
		}
		return path;
	}

	public String getPid() {
		String pid = null;
		try {
			if (started != null) {
				Field field = ReflectionUtils.findField(started.getClass(), "pid");
				ReflectionUtils.makeAccessible(field);
				pid = "" + ReflectionUtils.getField(field, started);
			}
		}
		catch (Exception e) {
		}
		return pid;
	}

	public void after() throws Exception {
		drain();
		if (started != null && started.isAlive()) {
			latch.await(10, TimeUnit.SECONDS);
			Map<String, Long> metrics = VirtualMachineMetrics.fetch(getPid());
			this.memory = VirtualMachineMetrics.total(metrics);
			this.heap = VirtualMachineMetrics.heap(metrics);
			this.classes = metrics.get("Classes").intValue();
			System.out.println(
					"Stopped " + mainClass + ": " + started.destroyForcibly().waitFor());
		}
	}

	private BufferedReader getBuffer() {
		return this.buffer;
	}

	public void run() throws Exception {
		List<String> jvmArgs = new ArrayList<>(this.args);
		customize(jvmArgs);
		started = exec(jvmArgs.toArray(new String[0]), this.progs.toArray(new String[0]));
		InputStream stream = started.getInputStream();
		this.buffer = new BufferedReader(new InputStreamReader(stream));
		monitor();
	}

	public void before() throws Exception {
		int classpath = args.indexOf("-cp");
		if (classpath >= 0 && args.get(classpath + 1).length() == 0) {
			args.set(classpath + 1, getClasspath());
		}
	}

	protected void customize(List<String> args) {
	}

	protected Process exec(String[] jvmArgs, String... progArgs) {
		List<String> args = new ArrayList<>(Arrays.asList(jvmArgs));
		args.add(0, System.getProperty("java.home") + "/bin/java");
		if (mainClass.length() > 0) {
			args.add(mainClass);
		}
		int classpath = args.indexOf("-cp");
		if (classpath >= 0 && args.get(classpath + 1).length() == 0) {
			args.set(classpath + 1, getClasspath());
		}
		args.addAll(Arrays.asList(progArgs));
		ProcessBuilder builder = new ProcessBuilder(args);
		builder.redirectErrorStream(true);
		builder.directory(getHome());
		if (!"false".equals(System.getProperty("debug", "false"))) {
			System.out.println("Executing: " + builder.command());
		}
		Process started;
		try {
			started = builder.start();
			return started;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Cannot calculate classpath");
		}
	}

	protected void monitor() throws Exception {
		// use this method to wait for an app to start
		output(getBuffer(), StartupApplicationListener.MARKER);
	}

	protected void finish() throws Exception {
		// use this method to wait for an app to stop
		output(getBuffer(), ShutdownApplicationListener.MARKER);
	}

	protected void drain() throws Exception {
		System.out.println("Draining console buffer");
		output(getBuffer(), null);
		latch.countDown();
	}

	protected void output(BufferedReader br, String marker) throws Exception {
		StringBuilder sb = new StringBuilder();
		String line = null;
		if (!"false".equals(System.getProperty("debug", "false"))) {
			System.err.println("Scanning for: " + marker);
		}
		while ((marker != null || br.ready()) && (line = br.readLine()) != null
				&& (marker == null || !line.contains(marker))) {
			sb.append(line + System.getProperty("line.separator"));
			if (!"false".equals(System.getProperty("debug", "false"))) {
				System.out.println(line);
			}
			if (line.contains(CLASS_COUNT_MARKER)) {
				classes = Integer
						.valueOf(line.substring(line.lastIndexOf("=") + 1).trim());
			}
			if (line.contains(BEAN_COUNT_MARKER)) {
				int count = Integer
						.valueOf(line.substring(line.lastIndexOf("=") + 1).trim());
				beans = count > beans ? count : beans;
			}
			line = null;
		}
		if (line != null) {
			sb.append(line + System.getProperty("line.separator"));
		}
		if ("false".equals(System.getProperty("debug", "false"))) {
			System.out.println(sb.toString());
		}
	}

	public File getHome() {
		return home;
	}

}
