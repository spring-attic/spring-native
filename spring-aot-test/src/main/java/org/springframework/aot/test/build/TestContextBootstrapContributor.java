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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aot.build.AotPhase;
import org.springframework.aot.build.BootstrapContributor;
import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.build.context.SourceFiles;
import org.springframework.aot.context.bootstrap.generator.infrastructure.DefaultBootstrapWriterContext;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.aot.test.context.bootstrap.generator.TestContextAotProcessor;
import org.springframework.nativex.AotOptions;
import org.springframework.util.ClassUtils;

/**
 * Adapter class that calls {@link TestContextAotProcessor}
 *
 * @author Brian Clozel
 */
public class TestContextBootstrapContributor implements BootstrapContributor {

	private static final Log logger = LogFactory.getLog(TestContextBootstrapContributor.class);

	@Override
	public void contribute(BuildContext context, AotOptions aotOptions) {
		ClassLoader classLoader = context.getClassLoader();
		if (!ClassUtils.isPresent("org.springframework.test.context.CacheAwareContextLoaderDelegate", classLoader)) {
			logger.info("Skip TestContextAotProcessor because spring-test dependency is not present in the classpath");
			return;
		}
		List<Class<?>> testClasses = new ArrayList<>(context.getTestClasses().size());
		for (String testClassName : context.getTestClasses()) {
			try {
				testClasses.add(ClassUtils.forName(testClassName, classLoader));
			}
			catch (ClassNotFoundException e) {
				logger.info("Could not load test class: " + testClassName);
			}
		}
		DefaultBootstrapWriterContext writerContext = new DefaultBootstrapWriterContext("org.springframework.aot", "Test");
		new TestContextAotProcessor(classLoader).generateTestContexts(testClasses, writerContext);

		writerContext.toJavaFiles().forEach(javaFile -> context.addSourceFiles(SourceFiles.fromJavaFile(javaFile)));
		NativeConfigurationRegistry nativeConfigurationRegistry = writerContext.getNativeConfigurationRegistry();
		context.getOptions().addAll(nativeConfigurationRegistry.options());
		context.describeReflection(reflectionDescriptor -> nativeConfigurationRegistry.reflection().toClassDescriptors().forEach(reflectionDescriptor::merge));
		context.describeResources(resourcesDescriptor -> resourcesDescriptor.merge(nativeConfigurationRegistry.resources().toResourcesDescriptor()));
		context.describeProxies(proxiesDescriptor -> proxiesDescriptor.merge(nativeConfigurationRegistry.proxy().toProxiesDescriptor()));
		context.describeInitialization(initializationDescriptor -> initializationDescriptor.merge(nativeConfigurationRegistry.initialization().toInitializationDescriptor()));
		context.describeSerialization(serializationDescriptor -> serializationDescriptor.merge(nativeConfigurationRegistry.serialization().toSerializationDescriptor()));
		context.describeJNIReflection(reflectionDescriptor -> nativeConfigurationRegistry.jni().toClassDescriptors().forEach(reflectionDescriptor::merge));
	}

	@Override
	public boolean supportsAotPhase(AotPhase aotPhase) {
		return AotPhase.TEST.equals(aotPhase);
	}
}
