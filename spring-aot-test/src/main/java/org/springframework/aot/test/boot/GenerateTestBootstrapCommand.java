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

package org.springframework.aot.test.boot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.squareup.javapoet.JavaFile;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.test.context.bootstrap.generator.TestContextBootstrapGenerator;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ClassUtils;

@Command(mixinStandardHelpOptions = true,
		description = "Generate the Java source for the Spring test Bootstrap class.")
public class GenerateTestBootstrapCommand implements Callable<Integer> {

	@Option(names={"--output", "-o"}, description = "Path where generated source files should be written.")
	private Path outputPath;

	@Option(names = {"--debug"}, description = "Enable debug logging.")
	private boolean isDebug;

	@Parameters(index = "0", arity = "1", description = "The package name which should be used for generated source files.")
	private String packageName;

	@Parameters(index = "1", arity = "1..*", description = "Folders containing the application test classes.")
	private Path[] testClassesFolders;


	@Override
	public Integer call() throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<String> testClassesNames = new ArrayList<>();
		for (Path testClassesFolder : testClassesFolders) {
			testClassesNames.addAll(TestClassesFinder.findTestClasses(testClassesFolder));
		}

		ConfigurableEnvironment environment = new StandardEnvironment();
		LogFile logFile = LogFile.get(environment);
		LoggingInitializationContext initializationContext = new LoggingInitializationContext(environment);
		LoggingSystem loggingSystem = LoggingSystem.get(classLoader);
		loggingSystem.initialize(initializationContext, null, logFile);
		if (this.isDebug) {
			loggingSystem.setLogLevel(null, LogLevel.DEBUG);
		}

		List<Class<?>> testClasses = new ArrayList<>(testClassesNames.size());
		for (String testClassName : testClassesNames) {
			testClasses.add(ClassUtils.forName(testClassName, classLoader));
		}
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext(this.packageName, "Test");
		new TestContextBootstrapGenerator(classLoader).generateTestContexts(testClasses, writerContext);
		Path out = this.outputPath != null ? this.outputPath : Path.of(System.getProperty("user.dir"));
		writeSources(out, writerContext.toJavaFiles());
		return 0;
	}

	private void writeSources(Path srcDirectory, List<JavaFile> javaFiles) {
		try {
			for (JavaFile javaFile : javaFiles) {
				javaFile.writeTo(srcDirectory);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to write source code to disk ", ex);
		}
	}

	public static void main(String[] args) throws IOException {
		int exitCode = new CommandLine(new GenerateTestBootstrapCommand()).execute(args);
		System.exit(exitCode);
	}
}
