/*
 * Copyright 2019 the original author or authors.
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

package org.springframework.graalvm.domain.init;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andy Clement
 */
public class InitializationDescriptor {

	private final List<String> buildtimeClasses;
	
	private final List<String> buildtimePackages;

	private final List<String> runtimeClasses;
	
	private final List<String> runtimePackages;

	public InitializationDescriptor() {
		this.buildtimeClasses = new ArrayList<>();
		this.buildtimePackages = new ArrayList<>();
		this.runtimeClasses = new ArrayList<>();
		this.runtimePackages = new ArrayList<>();
	}

	public InitializationDescriptor(InitializationDescriptor metadata) {
		this.buildtimeClasses = new ArrayList<>(metadata.buildtimeClasses);
		this.buildtimePackages = new ArrayList<>(metadata.buildtimePackages);
		this.runtimeClasses = new ArrayList<>(metadata.runtimeClasses);
		this.runtimePackages = new ArrayList<>(metadata.runtimePackages);
	}

	public List<String> getBuildtimeClasses() {
		return this.buildtimeClasses;
	}

	public List<String> getBuildtimePackages() {
		return this.buildtimePackages;
	}

	public List<String> getRuntimeClasses() {
		return this.runtimeClasses;
	}

	public List<String> getRuntimePackages() {
		return this.runtimePackages;
	}

	public void addBuildtimeClass(String clazz) {
		this.buildtimeClasses.add(clazz);
	}

	public void addBuildtimePackage(String pkg) {
		this.buildtimePackages.add(pkg);
	}

	public void addRuntimeClass(String clazz) {
		this.runtimeClasses.add(clazz);
	}

	public void addRuntimePackage(String pkg) {
		this.runtimePackages.add(pkg);
	}
	
	public String toDetailedString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("#%s buildtime-init-classes   #%s buildtime-init-packages   #%s runtime-init-classes    #%s runtime-init-packages\n",
				buildtimeClasses.size(), buildtimePackages.size(), runtimeClasses.size(), runtimePackages.size()));
		result.append("buildtime classes:\n");
		this.buildtimeClasses.forEach(cd -> {
			result.append(String.format("%s\n",cd));
		});
		result.append("buildtime packages:\n");
		this.buildtimePackages.forEach(cd -> {
			result.append(String.format("%s\n",cd));
		});
		result.append("runtime classes:\n");
		this.runtimeClasses.forEach(cd -> {
			result.append(String.format("%s\n",cd));
		});
		result.append("runtime packages:\n");
		this.runtimePackages.forEach(cd -> {
			result.append(String.format("%s\n",cd));
		});
		return result.toString();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(String.format("#%s buildtime-init-classes   #%s buildtime-init-packages   #%s runtime-init-classes    #%s runtime-init-packages\n",
				buildtimeClasses.size(), buildtimePackages.size(), runtimeClasses.size(), runtimePackages.size()));
		return result.toString();
	}

	public boolean isEmpty() {
		return buildtimeClasses.isEmpty() && runtimeClasses.isEmpty();
	}

	public static InitializationDescriptor of(String jsonString) {
		try {
			return InitializationJsonMarshaller.read(jsonString);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read json:\n"+jsonString, e);
		}
	}

}
