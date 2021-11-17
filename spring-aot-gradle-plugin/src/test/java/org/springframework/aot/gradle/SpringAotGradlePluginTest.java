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
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import org.graalvm.buildtools.gradle.NativeImagePlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.build.event.BuildEventsListenerRegistry;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.scopes.ProjectScopeServices;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.tooling.events.OperationCompletionListener;
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin;
import org.junit.jupiter.api.Test;

import org.springframework.aot.gradle.dsl.SpringAotExtension;
import org.springframework.aot.gradle.tasks.GenerateAotSources;
import org.springframework.aot.gradle.tasks.GenerateAotTestSources;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringAotGradlePlugin}
 */
public class SpringAotGradlePluginTest {

	@Test
	void pluginRegistersAotSourceSet() {
		Project project = createTestProject();

		JavaPluginExtension java = project.getExtensions().findByType(JavaPluginExtension.class);
		SourceSet aotSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_MAIN_SOURCE_SET_NAME);
		SourceSet mainSourceSet = java.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME);

		assertThat(aotSourceSet).isNotNull();
		assertThat(aotSourceSet.getJava().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "sources" + File.separator + SpringAotGradlePlugin.AOT_MAIN_SOURCE_SET_NAME));
		assertThat(aotSourceSet.getResources().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "resources" + File.separator + SpringAotGradlePlugin.AOT_MAIN_SOURCE_SET_NAME));
		assertThat(aotSourceSet.getCompileClasspath()).containsAll(mainSourceSet.getRuntimeClasspath());
	}

	@Test
	void pluginRegistersAotTestSourceSet() {
		Project project = createTestProject();

		JavaPluginExtension java = project.getExtensions().findByType(JavaPluginExtension.class);
		SourceSet aotTestSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_TEST_SOURCE_SET_NAME);
		SourceSet testSourceSet = java.getSourceSets().findByName(SourceSet.TEST_SOURCE_SET_NAME);

		assertThat(aotTestSourceSet).isNotNull();
		assertThat(aotTestSourceSet.getJava().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "sources" + File.separator + SpringAotGradlePlugin.AOT_TEST_SOURCE_SET_NAME));
		assertThat(aotTestSourceSet.getResources().getSourceDirectories())
				.hasSize(1)
				.allMatch(file -> file.getAbsolutePath().endsWith(File.separator + "build" + File.separator + "generated"
						+ File.separator + "resources" + File.separator + SpringAotGradlePlugin.AOT_TEST_SOURCE_SET_NAME));
		assertThat(aotTestSourceSet.getCompileClasspath()).containsAll(testSourceSet.getRuntimeClasspath());
	}

	@Test
	void pluginRegistersAotTask() {
		Project project = createTestProject();

		JavaPluginExtension java = project.getExtensions().findByType(JavaPluginExtension.class);
		SourceSet aotSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_MAIN_SOURCE_SET_NAME);
		TaskProvider<GenerateAotSources> generateAotSourcesProvider = project.getTasks().withType(GenerateAotSources.class)
				.named(SpringAotGradlePlugin.GENERATE_MAIN_TASK_NAME);
		assertThat(generateAotSourcesProvider.isPresent()).isTrue();
		TaskProvider<Task> compileAotProvider = project.getTasks().named(aotSourceSet.getCompileJavaTaskName());
		assertThat(compileAotProvider.isPresent()).isTrue();
	}

	@Test
	void pluginRegistersAotTestTask() {
		Project project = createTestProject();

		JavaPluginExtension java = project.getExtensions().findByType(JavaPluginExtension.class);
		SourceSet aotTestSourceSet = java.getSourceSets().findByName(SpringAotGradlePlugin.AOT_TEST_SOURCE_SET_NAME);
		TaskProvider<GenerateAotTestSources> generateAotTestSourcesProvider = project.getTasks().withType(GenerateAotTestSources.class)
				.named(SpringAotGradlePlugin.GENERATE_TEST_TASK_NAME);
		assertThat(generateAotTestSourcesProvider.isPresent()).isTrue();
		TaskProvider<Task> compileAotProvider = project.getTasks().named(aotTestSourceSet.getCompileJavaTaskName());
		assertThat(compileAotProvider.isPresent()).isTrue();
	}


	@Test
	void registerDslExtension() {
		Project project = createTestProject();

		assertThat(project.getExtensions().findByType(SpringAotExtension.class)).isNotNull();
		assertThat(project.getExtensions().findByName(SpringAotGradlePlugin.EXTENSION_NAME)).isNotNull();
	}

	@Test
	void nativePluginIsApplied() {
		Project project = createTestProject();
		assertThat(project.getPlugins().getPlugin(NativeImagePlugin.class)).isNotNull();
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
		addFakeService(project);
		project.getPlugins().apply(KotlinPlatformJvmPlugin.class);

		TaskProvider<Task> compileAotKotlin = project.getTasks().named("compileAotMainKotlin");
		assertThat(compileAotKotlin.get().getDependsOn()).extracting("name").contains("generateAot");
	}

	// TODO Workaround for https://github.com/gradle/gradle/issues/17783, removed when fixed
	private void addFakeService(Project project) {
		try {
			ProjectScopeServices gss =
					(ProjectScopeServices) ((DefaultProject) project).getServices();

			Field state = ProjectScopeServices.class.getSuperclass().getDeclaredField("state");
			state.setAccessible(true);
			AtomicReference<Object> stateValue = (AtomicReference<Object>) state.get(gss);
			Class<?> enumClass = Class.forName(DefaultServiceRegistry.class.getName() + "$State");
			stateValue.set(enumClass.getEnumConstants()[0]);

			gss.add(BuildEventsListenerRegistry.class, new FakeBuildEventsListenerRegistry());
			stateValue.set(enumClass.getEnumConstants()[1]);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static class FakeBuildEventsListenerRegistry implements BuildEventsListenerRegistry {
		@Override
		public void onTaskCompletion(Provider<? extends OperationCompletionListener> provider) {}
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
