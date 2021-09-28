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

package org.springframework.aot.context.annotation;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link ImportAwareInvoker}.
 *
 * @author Stephane Nicoll
 */
class ImportAwareInvokerTests {

	@Test
	@SuppressWarnings("unchecked")
	void registerInvokeSupplier() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ImportAwareInvoker importAwareInvoker = new ImportAwareInvoker(Collections.emptyMap());
		Supplier<ImportAwareInvoker> supplier = mock(Supplier.class);
		given(supplier.get()).willReturn(importAwareInvoker);
		ImportAwareInvoker.register(beanFactory, supplier);
		verifyNoInteractions(supplier);
		ImportAwareInvoker invoker = ImportAwareInvoker.get(beanFactory);
		assertThat(invoker).isSameAs(importAwareInvoker);
		verify(supplier).get();
	}

	@Test
	void registerOnExistingInstanceDoesNotRegister() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		ImportAwareInvoker importAwareInvoker = new ImportAwareInvoker(Collections.emptyMap());
		ImportAwareInvoker.register(beanFactory, () -> importAwareInvoker);
		ImportAwareInvoker.register(beanFactory, () -> {
			throw new IllegalStateException("test");
		});
		assertThat(ImportAwareInvoker.get(beanFactory)).isSameAs(importAwareInvoker);
	}

	@Test
	void setAnnotationMetadataOnMatchingCandidate() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(TestImportAware.class.getName(),
				ImportAwareInvokerTests.class.getName()));
		TestImportAware importAware = new TestImportAware();
		invoker.setAnnotationMetadata(importAware);
		assertThat(importAware.importMetadata).isNotNull();
		assertThat(importAware.importMetadata.getClassName())
				.isEqualTo(ImportAwareInvokerTests.class.getName());
	}

	@Test
	void setAnnotationMetadataOnMatchingCandidateWithNestedClass() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(TestImportAware.class.getName(),
				TestImporting.class.getName()));
		TestImportAware importAware = new TestImportAware();
		invoker.setAnnotationMetadata(importAware);
		assertThat(importAware.importMetadata).isNotNull();
		assertThat(importAware.importMetadata.getClassName())
				.isEqualTo(TestImporting.class.getName());
	}

	@Test
	void setAnnotationMetadataOnNoCandidateDoesNotInvokeCallback() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(String.class.getName(),
				ImportAwareInvokerTests.class.getName()));
		TestImportAware importAware = new TestImportAware();
		invoker.setAnnotationMetadata(importAware);
		assertThat(importAware.importMetadata).isNull();
	}

	@Test
	void setAnnotationMetadataOnMatchingCandidateWithNoMetadata() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(TestImportAware.class.getName(),
				"com.example.invalid.DoesNotExist"));
		TestImportAware importAware = new TestImportAware();
		assertThatIllegalStateException().isThrownBy(() -> invoker.setAnnotationMetadata(importAware))
				.withMessageContaining("Failed to read metadata for 'com.example.invalid.DoesNotExist'");
	}


	static class TestImportAware implements ImportAware {
		private AnnotationMetadata importMetadata;

		@Override
		public void setImportMetadata(AnnotationMetadata importMetadata) {
			this.importMetadata = importMetadata;
		}
	}

	static class TestImporting {

	}

}
