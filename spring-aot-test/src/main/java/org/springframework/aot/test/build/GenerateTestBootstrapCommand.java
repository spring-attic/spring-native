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

package org.springframework.aot.test.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import org.springframework.aot.build.AotPhase;
import org.springframework.aot.build.ApplicationStructure;
import org.springframework.aot.build.BootstrapCodeGenerator;
import org.springframework.aot.build.BootstrapContributor;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.nativex.AotOptions;
import org.springframework.util.StringUtils;

@Command(mixinStandardHelpOptions = true,
		description = "Generate the Java source for the Spring test Bootstrap class.")
public class GenerateTestBootstrapCommand implements Callable<Integer> {

	@Option(names = {"--sources-out"}, required = true, description = "Output path for the generated sources.")
	private Path sourceOutputPath;

	@Option(names = {"--resources-out"}, required = true, description = "Output path for the generated resources.")
	private Path resourcesOutputPath;

	@Option(names = {"--resources"}, required = true, split = "${sys:path.separator}", description = "Paths to the application compiled resources.")
	private Set<Path> resourcesPaths;

	@Option(names = {"--primary-classes"}, split = ",", description = "spring primary classes")
	private Set<String> primaryClasses;

	@Option(names = "--mode", required = true, description = "The mode which could be native or native-agent")
	private String mode;
	
	@Option(names = {"--debug"}, description = "Enable debug logging.")
	private boolean isDebug;

	@Option(names = {"--remove-yaml"}, description = "Remove Yaml support.")
	private boolean removeYaml;

	@Option(names = {"--remove-jmx"}, description = "Remove JMX support.")
	private boolean removeJmx;

	@Option(names = {"--remove-xml"}, description = "Remove XML support.")
	private boolean removeXml;

	@Option(names = {"--remove-spel"}, description = "Remove SpEL support.")
	private boolean removeSpel;

	@Parameters(index = "0", arity = "1..*", description = "Folders containing the application test classes.")
	private Path[] testClassesFolders;


	@Override
	public Integer call() throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<String> testClassesNames = new ArrayList<>();
		for (Path testClassesFolder : testClassesFolders) {
			testClassesNames.addAll(TestClassesFinder.findTestClasses(testClassesFolder));
		}
		AotOptions aotOptions = new AotOptions();
		aotOptions.setMode(this.mode);
		aotOptions.setDebugVerify(this.isDebug);
		aotOptions.setRemoveYamlSupport(this.removeYaml);
		aotOptions.setRemoveJmxSupport(this.removeJmx);
		aotOptions.setRemoveXmlSupport(this.removeXml);
		aotOptions.setRemoveSpelSupport(this.removeSpel);

		ConfigurableEnvironment environment = new StandardEnvironment();
		LogFile logFile = LogFile.get(environment);
		LoggingInitializationContext initializationContext = new LoggingInitializationContext(environment);
		LoggingSystem loggingSystem = LoggingSystem.get(classLoader);
		loggingSystem.initialize(initializationContext, null, logFile);
		if (this.isDebug) {
			loggingSystem.setLogLevel(null, LogLevel.DEBUG);
		}

		List<BootstrapContributor> bootstrapContributors = SpringFactoriesLoader.loadFactories(BootstrapContributor.class, classLoader);
		BootstrapCodeGenerator generator = new BootstrapCodeGenerator(aotOptions, bootstrapContributors);
		String[] classPath = StringUtils.tokenizeToStringArray(System.getProperty("java.class.path"), File.pathSeparator);
		ApplicationStructure applicationStructure = new ApplicationStructure(this.sourceOutputPath, this.resourcesOutputPath, this.resourcesPaths,
				Arrays.asList(this.testClassesFolders), null, null, testClassesNames, Arrays.asList(classPath), classLoader, new ArrayList<>(primaryClasses));
		generator.generate(AotPhase.TEST, applicationStructure);
		return 0;
	}

	public static void main(String[] args) throws IOException {
		int exitCode = new CommandLine(new GenerateTestBootstrapCommand()).execute(args);
		System.exit(exitCode);
	}
}
