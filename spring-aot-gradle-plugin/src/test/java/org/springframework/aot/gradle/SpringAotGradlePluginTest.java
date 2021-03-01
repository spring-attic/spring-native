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

package org.springframework.aot.gradle;

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import org.springframework.aot.gradle.tasks.GenerateAotSources;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringAotGradlePlugin}
 */
public class SpringAotGradlePluginTest {

	@Test
	public void pluginRegistersAotSourceSet() {
		Project project = createTestProject();

		JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);

		SourceSet aotSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_SOURCE_SET_NAME);
		SourceSet mainSourceSet = java.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME);
		assertThat(aotSourceSet).isNotNull();
		assertThat(aotSourceSet.getJava().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith("/build/generated/sources/aot"));
		assertThat(aotSourceSet.getResources().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith("/build/generated/resources/aot"));
		assertThat(aotSourceSet.getCompileClasspath()).containsAll(mainSourceSet.getRuntimeClasspath());
	}

	@Test
	public void pluginRegistersAotTasks() {
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
	public void pluginRegistersAotTestSourceSet() {
		Project project = createTestProject();

		JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);

		SourceSet aotTestSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_TEST_SOURCE_SET_NAME);
		assertThat(aotTestSourceSet).isNotNull();
		assertThat(aotTestSourceSet.getJava().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith("/build/generated/sources/aotTest"));
		assertThat(aotTestSourceSet.getResources().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith("/build/generated/resources/aotTest"));
	}

	@Test
	public void pluginRegistersAotTestTasks() {
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
