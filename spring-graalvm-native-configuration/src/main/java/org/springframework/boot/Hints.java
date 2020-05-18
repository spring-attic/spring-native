/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.boot;

import java.io.Serializable;
import java.util.logging.LogManager;

import org.springframework.boot.logging.java.JavaLoggingSystem;
import org.springframework.core.io.InputStreamSource;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(typeInfos = { @TypeInfo(types = {
	SpringApplication.class,
	SpringBootConfiguration.class,
	// TODO Handle these more correctly - the need to expose these should be 
	// inferred by recognizing which constructors are being exposed and that the parameters
	// to those constructors have array types (or were varargs). For example see
	// AutoConfigurationPackages$BasePackages. Without String[] exposed you will see:
	// Error creating bean with name 'org.springframework.boot.autoconfigure.AutoConfigurationPackages': Unsatisfied dependency
	// expressed through constructor parameter 0: Could not convert argument value of type [[Ljava.lang.String;] to required 
	// type [[Ljava.lang.String;]: Failed to convert value of type 'java.lang.String[]' to required type 'java.lang.String[]'; 
	// nested exception is java.lang.IllegalArgumentException: Class java.io.Serializable[] is instantiated reflectively but 
	// was never registered. Register the class by using org.graalvm.nativeimage.hosted.RuntimeReflection
	String[].class,Serializable[].class,Comparable[].class,
	// This one: 
	// Caused by: org.springframework.beans.TypeMismatchException: Failed to convert property value of type 'java.lang.String' 
	// to required type 'org.springframework.core.io.Resource[]' for property 'locations'; nested exception is 
	// java.lang.IllegalArgumentException: Class org.springframework.core.io.InputStreamSource[] is instantiated reflectively but was
	// never registered. Register the class by using org.graalvm.nativeimage.hosted.RuntimeReflection
	InputStreamSource[].class,
	// TODO How should these be properly handled?
	LogManager.class,JavaLoggingSystem.class,
	// Caused by: java.lang.IllegalArgumentException: Unable to find field cause on class java.lang.Throwable!
	// at org.springframework.data.util.ReflectionUtils.findRequiredField(ReflectionUtils.java:222)
	// at org.springframework.data.mapping.model.AbstractPersistentProperty.<clinit>(AbstractPersistentProperty.java:51)
	Throwable.class
  })
})
public class Hints implements NativeImageConfiguration {
}
