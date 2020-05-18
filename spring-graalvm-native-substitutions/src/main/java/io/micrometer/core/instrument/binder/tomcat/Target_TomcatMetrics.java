/*
 * Copyright 2020 Contributors
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
package io.micrometer.core.instrument.binder.tomcat;

import org.springframework.graalvm.substitutions.OnlyPresent;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Attempting to prevent TomcatMetrics getting into JMX tinkering.
 * 
 * @author Andy Clement
 */
@TargetClass(className = "io.micrometer.core.instrument.binder.tomcat.TomcatMetrics", 
    onlyWith = {OnlyPresent.class,CatalinaManagerPresent.class})
public final class Target_TomcatMetrics {

	@Substitute
    public void bindTo(MeterRegistry registry) {}
}
