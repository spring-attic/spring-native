/*
 * Copyright 2019 Contributors
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
package org.springframework.boot.validation;

import javax.validation.MessageInterpolator;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.graalvm.substitutions.MessageInterpolatorIsAround;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.util.ClassUtils;

/**
 * Substitution for MessageInterpolatorFactory. The code pattern in there misbehaves under graal so let's just fallback straight away... needs review
 * 
 * @author Andy Clement
 */
@TargetClass(className="org.springframework.boot.validation.MessageInterpolatorFactory", onlyWith = { MessageInterpolatorIsAround.class, OnlyPresent.class})
public final class Target_MessageInterpolatorFactory {

	@Substitute
	public MessageInterpolator getObject() throws BeansException {
		Class<?> interpolatorClass = ClassUtils.resolveClassName("org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator", null);
		Object interpolator = BeanUtils.instantiateClass(interpolatorClass);
		return (MessageInterpolator) interpolator;
	}
}
