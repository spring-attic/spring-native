package org.springframework.context.bootstrap.generator.infrastructure;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.context.bootstrap.generator.infrastructure.ProtectedAccessAnalysis.ProtectedElement;
import org.springframework.context.bootstrap.generator.sample.visibility.ProtectedConstructorComponent;
import org.springframework.context.bootstrap.generator.sample.visibility.ProtectedFactoryMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link ProtectedAccessAnalysis}.
 *
 * @author Stephane Nicoll
 */
class ProtectedAccessAnalysisTests {

	@Test
	void isAccessibleWithPublicElements() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.emptyList());
		assertThat(analysis.isAccessible()).isTrue();
	}

	@Test
	void isAccessibleWithProtectedElement() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.singletonList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null)));
		assertThat(analysis.isAccessible()).isFalse();
	}

	@Test
	void getPrivilegedPackageNameWithPublicElements() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.emptyList());
		assertThat(analysis.getPrivilegedPackageName()).isNull();
	}

	@Test
	void getPrivilegedPackageNameWithProtectedElement() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Collections.singletonList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null)));
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedConstructorComponent.class.getPackageName());
	}

	@Test
	void getPrivilegedPackageNameWithProtectedElementsFromSamePackage() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Arrays.asList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null),
				ProtectedElement.of(ProtectedFactoryMethod.class, null)));
		assertThat(analysis.getPrivilegedPackageName()).isEqualTo(ProtectedConstructorComponent.class.getPackageName());
	}

	@Test
	void getPrivilegedPackageNameWithProtectedElementsFromDifferentPackages() {
		ProtectedAccessAnalysis analysis = new ProtectedAccessAnalysis(Arrays.asList(
				ProtectedElement.of(ProtectedConstructorComponent.class, null),
				ProtectedElement.of(String.class, null)));
		assertThatIllegalStateException().isThrownBy(analysis::getPrivilegedPackageName)
				.withMessageContaining(ProtectedConstructorComponent.class.getPackageName())
				.withMessageContaining(String.class.getPackageName());
	}

}
