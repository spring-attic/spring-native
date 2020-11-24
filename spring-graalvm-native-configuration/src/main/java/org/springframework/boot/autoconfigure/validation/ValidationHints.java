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
package org.springframework.boot.autoconfigure.validation;

import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.GenericBootstrap;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForCharSequence;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.resolver.JPATraversableResolver;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.logging.Messages;
import org.hibernate.validator.internal.xml.config.ValidationBootstrapParameters;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(trigger=ValidationAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {
			ParameterMessageInterpolator.class,
			HibernateValidatorConfiguration.class,
			AbstractMessageInterpolator.class,
			JPATraversableResolver.class,
			TraversableResolvers.class,
			PlatformResourceBundleLocator.class,
			ConfigurationImpl.class,
			TraversableResolvers.class,
			Messages.class,
			ParameterizedMessageFactory.class,
			DefaultFlowMessageFactory.class,
			ValidationBootstrapParameters.class,
			HibernateValidatorConfiguration.class,
			ConfigurationImpl.class,
			ConstraintDescriptorImpl.class,
			NotEmptyValidatorForCharSequence.class,
			DigitsValidatorForCharSequence.class,
			GenericBootstrap.class,
			PatternValidator.class
		}, typeNames = {
				"org.hibernate.validator.internal.engine.resolver.TraverseAllTraversableResolver",
				"org.hibernate.validator.internal.util.logging.Log_$logger",
				"org.hibernate.validator.internal.util.logging.Log"}
		, access = AccessBits.LOAD_AND_CONSTRUCT),
		@TypeInfo(types = {
				ValidatorFactory.class,
				Pattern.class,
				AssertFalse.class,
				AssertTrue.class,
				DecimalMax.class,
				DecimalMin.class,
				Digits.class,
				Email.class,
				Future.class,
				FutureOrPresent.class,
				Max.class,
				Min.class,
				Negative.class,
				NegativeOrZero.class,
				NotBlank.class,
				NotEmpty.class,
				NotNull.class,
				Null.class,
				Past.class,
				PastOrPresent.class,
				Pattern.class,
				Positive.class,
				PositiveOrZero.class,
				Size.class
		}, access = AccessBits.LOAD_AND_CONSTRUCT|AccessBits.DECLARED_METHODS)
})
public class ValidationHints implements NativeImageConfiguration {
}
