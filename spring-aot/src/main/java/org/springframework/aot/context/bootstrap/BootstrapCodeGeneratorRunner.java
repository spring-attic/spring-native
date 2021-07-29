/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.aot.context.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.aot.ApplicationStructure;
import org.springframework.aot.BootstrapCodeGenerator;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.nativex.AotOptions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class BootstrapCodeGeneratorRunner {

	/**
	 * <ul>
	 * 	<li>[0] sourcesPath
	 * 	<li>[1] resourcesPath
	 * 	<li>[2] resourcesFolders
	 * 	<li>[3] classesFolder
	 * 	<li>[4] classPathElements
	 * 	<li>[5] Application main class
	 * </ul>
	 */
	public static void main(String[] args) throws IOException {
		Assert.state(args.length >= 4, "Missing argument");
		AotOptions aotOptions = new AotOptions();
		aotOptions.setMode("native");

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		// TODO Manage properly logging, for now just avoid debug logging by default (with Logback)
		LoggingSystem loggingSystem = LoggingSystem.get(classLoader);
		loggingSystem.beforeInitialize();

		BootstrapCodeGenerator generator = new BootstrapCodeGenerator(aotOptions);
		Path sourcesPath = Paths.get(args[0]);
		Path resourcesPath = Paths.get(args[1]);
		String[] folders = StringUtils.tokenizeToStringArray(args[2], File.pathSeparator);
		Set<Path> resourceFolders = Arrays.stream(folders).map(Paths::get).collect(Collectors.toSet());
		Path classesPath = Paths.get(args[3]);
		String[] classPath = StringUtils.tokenizeToStringArray(args[4], File.pathSeparator);
		String mainClass = args.length >= 6 ? args[5] : null;

		ApplicationStructure applicationStructure = new ApplicationStructure(sourcesPath, resourcesPath, resourceFolders,
				classesPath, mainClass, Arrays.asList(classPath), classLoader);
		generator.generate(applicationStructure);
	}
}
