/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.springframework.nativex.hint.AccessBits.*;
import static org.springframework.nativex.util.HintDeclarationsAssert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.example.test.hateoas.Drink;
import com.example.test.hateoas.DrinksModelProcessor;
import com.example.test.hateoas.Person;
import com.example.test.hateoas.PersonRepresentationModel;
import com.example.test.hateoas.TypeUsingJackson;
import com.example.test.hateoas.TypeUsingJacksonOnField;
import com.example.test.hateoas.TypeUsingJacksonOnMethod;
import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Before;
import org.junit.Test;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.TypeSystem;

public class HateoasHintsTests {

	private TypeSystem typeSystem = new TypeSystem(Arrays.asList(new File("./target/classes").toString(), new File("./target/test-classes/com/example/test/hateoas").toString()));

	private HateoasHints hateoasHints;

	@Before
	public void setUp() {
		hateoasHints = new HateoasHints();
	}

	@Test
	public void computesModelAndModelProcessorCorrectly() {

		List<HintDeclaration> hintDeclarations = hateoasHints.computeRepresentationModels(typeSystem);

		assertHints(hintDeclarations)
				.hasSize(4)
				.doesNotContainResourceConfiguration()
				.extractReflectionConfigFor(PersonRepresentationModel.class, it -> it.hasAccessBits(FULL_REFLECTION))
				.extractReflectionConfigFor(Person.class, it -> it.hasAccessBits(FULL_REFLECTION))
				.extractReflectionConfigFor(DrinksModelProcessor.class, it -> it.hasAccessBits(FULL_REFLECTION))
				.extractReflectionConfigFor(Drink.class, it -> it.hasAccessBits(FULL_REFLECTION));
	}

	@Test
	public void jackson() {

		List<HintDeclaration> hintDeclarations = hateoasHints.computeJacksonMappings(typeSystem, Collections.emptySet());
		assertHints(hintDeclarations)
				// Assert only the ones using jackson
				.hasSize(7)
				// The domain types
				.extractReflectionConfigFor(TypeUsingJackson.class, it -> it.hasAccessBits(FULL_REFLECTION))
				.extractReflectionConfigFor(TypeUsingJacksonOnMethod.class, it -> it.hasAccessBits(FULL_REFLECTION))
				.extractReflectionConfigFor(TypeUsingJacksonOnField.class, it -> it.hasAccessBits(FULL_REFLECTION))
				// The used jackson annotations
				.extractReflectionConfigFor(JsonClassDescription.class, it -> it.hasAccessBits(ANNOTATION))
				.extractReflectionConfigFor(JacksonAnnotation.class, it -> it.hasAccessBits(ANNOTATION))
				.extractReflectionConfigFor(JsonProperty.class, it -> it.hasAccessBits(ANNOTATION))
				.extractReflectionConfigFor(JsonAlias.class, it -> it.hasAccessBits(ANNOTATION));
	}
}
