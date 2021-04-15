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

package org.springframework.aot.gradle;

import java.io.File;
import java.util.Optional;

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin;
import org.junit.jupiter.api.Test;

import org.springframework.aot.gradle.dsl.SpringAotExtension;
import org.springframework.aot.gradle.tasks.GenerateAotSources;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringAotGradlePlugin}
 */
public class SpringAotGradlePluginTest {

	@Test
	void pluginRegistersAotSourceSet() {
		Project project = createTestProject();

		JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);

		SourceSet aotSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_SOURCE_SET_NAME);
		SourceSet mainSourceSet = java.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME);
		assertThat(aotSourceSet).isNotNull();
		assertThat(aotSourceSet.getJava().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "sources" + File.separator + "aot"));
		assertThat(aotSourceSet.getResources().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "resources" + File.separator + "aot"));
		assertThat(aotSourceSet.getCompileClasspath()).containsAll(mainSourceSet.getRuntimeClasspath());
	}

	@Test
	void pluginRegistersAotTasks() {
		Project project = createTestProject();

		JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet aotSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_SOURCE_SET_NAME);

		TaskProvider<GenerateAotSources> generateAotSourcesProvider = project.getTasks().withType(GenerateAotSources.class)
				.named(SpringAotGradlePlugin.GENERATE_TASK_NAME);
		assertThat(generateAotSourcesProvider.isPresent()).isTrue();

		TaskProvider<Task> compileAotProvider = project.getTasks().named(aotSourceSet.getCompileJavaTaskName());
		assertThat(compileAotProvider.isPresent()).isTrue();
	}


	@Test
	void pluginRegistersAotTestSourceSet() {
		Project project = createTestProject();

		JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);

		SourceSet aotTestSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_TEST_SOURCE_SET_NAME);
		assertThat(aotTestSourceSet).isNotNull();
		assertThat(aotTestSourceSet.getJava().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "sources" + File.separator + "aotTest"));
		assertThat(aotTestSourceSet.getResources().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "resources" + File.separator + "aotTest"));
	}

	@Test
	void pluginRegistersAotTestTasks() {
		Project project = createTestProject();

		JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet aotTestSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_TEST_SOURCE_SET_NAME);
		SourceSet aotSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_SOURCE_SET_NAME);

		TaskProvider<GenerateAotSources> generateAotSourcesProvider = project.getTasks().withType(GenerateAotSources.class)
				.named(SpringAotGradlePlugin.GENERATE_TEST_TASK_NAME);
		assertThat(generateAotSourcesProvider.isPresent()).isTrue();

		TaskProvider<Task> compileAotTestProvider = project.getTasks().named(aotTestSourceSet.getCompileJavaTaskName());
		assertThat(compileAotTestProvider.isPresent()).isTrue();

		assertThat(compileAotTestProvider.get().getDependsOn()).contains(generateAotSourcesProvider.get());

		org.gradle.api.tasks.testing.Test test = project.getTasks()
				.withType(org.gradle.api.tasks.testing.Test.class).named(JavaPlugin.TEST_TASK_NAME).get();
		assertThat(test.getClasspath().getFiles())
				.containsAll(aotTestSourceSet.getOutput().getClassesDirs().getFiles())
				.doesNotContainAnyElementsOf(aotSourceSet.getOutput().getClassesDirs().getFiles());
	}

	@Test
	void registerDslExtension() {
		Project project = createTestProject();

		assertThat(project.getExtensions().findByType(SpringAotExtension.class)).isNotNull();
		assertThat(project.getExtensions().findByName(SpringAotGradlePlugin.EXTENSION_NAME)).isNotNull();
	}

	@Test
	void nativeDependencyIsRegistered() {
		Project project = createTestProject();

		Configuration apiConfiguration = project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);

		Optional<Dependency> springNativeDependency = apiConfiguration.getDependencies().stream()
				.filter(dep -> dep.getGroup().equals("org.springframework.experimental"))
				.filter(dep -> dep.getName().equals("spring-native"))
				.findAny();

		assertThat(springNativeDependency).isPresent();
	}

	@Test
	void configureKotlinTasksDependencies() {
		Project project = createTestProject();
		project.getPlugins().apply(KotlinPlatformJvmPlugin.class);

		TaskProvider<Task> compileAotKotlin = project.getTasks().named("compileAotKotlin");
		assertThat(compileAotKotlin.get().getDependsOn()).extracting("name")
				.contains(SpringAotGradlePlugin.GENERATE_TASK_NAME);
		TaskProvider<Task> compileAotTestKotlin = project.getTasks().named("compileAotTestKotlin");
		assertThat(compileAotTestKotlin.get().getDependsOn()).extracting("name")
				.contains(SpringAotGradlePlugin.GENERATE_TEST_TASK_NAME);
	}


	private Project createTestProject() {
		Project project = ProjectBuilder.builder().build();
		project.getRepositories().mavenLocal();
		project.getRepositories().mavenCentral();
		project.getPlugins().apply("java");
		project.getPlugins().apply(DependencyManagementPlugin.class);
		project.getPlugins().apply(SpringBootPlugin.class);
		project.getPlugins().apply("org.springframework.experimental.aot");
		return project;
	}
}
