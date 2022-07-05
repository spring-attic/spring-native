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

package org.springframework.nativex.domain.init;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andy Clement
 */
public class InitializationDescriptor {

	private final Set<String> buildtimeClasses;
	
	private final Set<String> buildtimePackages;

	private final Set<String> runtimeClasses;
	
	private final Set<String> runtimePackages;

	public InitializationDescriptor() {
		this.buildtimeClasses = new TreeSet<>();
		this.buildtimePackages = new TreeSet<>();
		this.runtimeClasses = new TreeSet<>();
		this.runtimePackages = new TreeSet<>();
	}

	public InitializationDescriptor(InitializationDescriptor metadata) {
		this.buildtimeClasses = new TreeSet<>(metadata.buildtimeClasses);
		this.buildtimePackages = new TreeSet<>(metadata.buildtimePackages);
		this.runtimeClasses = new TreeSet<>(metadata.runtimeClasses);
		this.runtimePackages = new TreeSet<>(metadata.runtimePackages);
	}

	public Set<String> getBuildtimeClasses() {
		return this.buildtimeClasses;
	}

	public Set<String> getBuildtimePackages() {
		return this.buildtimePackages;
	}

	public Set<String> getRuntimeClasses() {
		return this.runtimeClasses;
	}

	public Set<String> getRuntimePackages() {
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
		result.append(toString()).append("\n");
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
		result.append(String.format("#%s buildtime-init-classes   #%s buildtime-init-packages   #%s runtime-init-classes   #%s runtime-init-packages\n",
				buildtimeClasses.size(), buildtimePackages.size(), runtimeClasses.size(), runtimePackages.size()));
		return result.toString();
	}

	public boolean isEmpty() {
		return buildtimeClasses.isEmpty() && runtimeClasses.isEmpty() && buildtimePackages.isEmpty() && runtimePackages.isEmpty();
	}

	public static InitializationDescriptor fromJSON(String jsonString) {
		return InitializationJsonMarshaller.read(jsonString);
	}

	public String toJSON() {
		return InitializationJsonMarshaller.write(this);
	}

	public void merge(InitializationDescriptor initializationDescriptor) {
		this.buildtimeClasses.addAll(initializationDescriptor.getBuildtimeClasses());
		this.buildtimePackages.addAll(initializationDescriptor.getBuildtimePackages());
		this.runtimeClasses.addAll(initializationDescriptor.getRuntimeClasses());
		this.runtimePackages.addAll(initializationDescriptor.getRuntimePackages());
	}

	public void mergeTo(InitializationDescriptor initializationDescriptor) {
		initializationDescriptor.getBuildtimeClasses().addAll(this.buildtimeClasses);
		initializationDescriptor.getBuildtimePackages().addAll(this.buildtimePackages);
		initializationDescriptor.getRuntimeClasses().addAll(this.runtimeClasses);
		initializationDescriptor.getRuntimePackages().addAll(this.runtimePackages);
	}

}
