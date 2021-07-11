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
package org.springframework.transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.TransactionalComponentProcessor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.nativex.util.NativeTestContext;
import org.springframework.transactional.components.ComponentWithTransactionalInterface;
import org.springframework.transactional.components.ComponentWithTransactionalInterfaceAndExtraInterface;
import org.springframework.transactional.components.ComponentWithTransactionalInterfaceAndOtherMethod;
import org.springframework.transactional.components.ComponentWithTransactionalInterfaceOverProxyClass;
import org.springframework.transactional.components.ComponentWithTransactionalInterfaceOverProxyInterface;
import org.springframework.transactional.components.ComponentWithTransactionalInterfaceWithDefault;
import org.springframework.transactional.components.ComponentWithTransactionalInterfaceWithoutMethod;
import org.springframework.transactional.components.ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod;
import org.springframework.transactional.components.ComponentWithTransactionalMethod;
import org.springframework.transactional.components.TransactionalComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionalComponentProcessorTest {

    private NativeTestContext nativeContext = new NativeTestContext();
    private TypeSystem typeSystem = nativeContext.getTypeSystem();
    private TransactionalComponentProcessor processor;

    @Before
    public void setUp() {
        processor = new TransactionalComponentProcessor();
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterface() {
        checkProxies(ComponentWithTransactionalInterface.class,
                "org.springframework.transactional.components.TransactionalInterface",
                Arrays.asList(
                        "org.springframework.transactional.components.TransactionalInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterfaceAndExtraInterface() {
        checkProxies(ComponentWithTransactionalInterfaceAndExtraInterface.class,
                "org.springframework.transactional.components.TransactionalInterface",
                Arrays.asList(
                        "org.springframework.transactional.components.TransactionalInterface",
                        "org.springframework.transactional.components.VoidInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterfaceAndOtherMethod() {
        checkProxies(ComponentWithTransactionalInterfaceAndOtherMethod.class,
                "org.springframework.transactional.components.TransactionalInterface",
                Arrays.asList(
                        "org.springframework.transactional.components.TransactionalInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterfaceOverSuperClass() {
        checkProxies(ComponentWithTransactionalInterfaceOverProxyClass.class,
                "org.springframework.transactional.components.TransactionalInterface",
                Arrays.asList(
                        "org.springframework.transactional.components.TransactionalInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterfaceOverSuperInterface() {
        checkProxies(ComponentWithTransactionalInterfaceOverProxyInterface.class,
                "org.springframework.transactional.components.ProxyInterface",
                Arrays.asList(
                        "org.springframework.transactional.components.ProxyInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterfaceWithoutMethod() {
        checkProxies(ComponentWithTransactionalInterfaceWithoutMethod.class,
                null,
                null,
                "org.springframework.transactional.components.ComponentWithTransactionalInterfaceWithoutMethod");
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod() {
        checkProxies(ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod.class,
                null,
                null,
                "org.springframework.transactional.components.ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod");
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalInterfaceWithDefault() {
        checkProxies(ComponentWithTransactionalInterfaceWithDefault.class,
                "org.springframework.transactional.components.TransactionalInterfaceWithDefault",
                Arrays.asList(
                        "org.springframework.transactional.components.TransactionalInterfaceWithDefault",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithTransactionalMethod() {
        checkProxies(ComponentWithTransactionalMethod.class,
                null,
                null,
                "org.springframework.transactional.components.ComponentWithTransactionalMethod");
    }

    @Test
    public void shouldDescribeProxiesForTransactionalComponent() {
        checkProxies(TransactionalComponent.class,
                null,
                null,
                "org.springframework.transactional.components.TransactionalComponent");
    }

    private void checkProxies(Class component, String key, List<String> jdkProxies, String aotProxy) {
        String componentType = typeSystem.resolve(component).getDottedName();

        // handled
        assertThat(processor.handle(nativeContext, componentType, Collections.emptyList())).isTrue();

        // processed
        processor.process(nativeContext, componentType, Collections.emptyList());

        // and has correct proxies
        if (key != null) {
            assertThat(nativeContext.getProxyEntries()).isNotEmpty();
            assertThat(nativeContext.getProxyEntries().get(key).get(0))
                    .isEqualTo(jdkProxies);
        } else {
            assertThat(nativeContext.getProxyEntries()).isEmpty();
        }
        if (aotProxy == null) {
            assertThat(nativeContext.getClassProxyEntries()).isEmpty();
        } else {
            assertThat(nativeContext.getClassProxyEntries().keySet()).isEqualTo(new HashSet<>(Arrays.asList(
                    aotProxy)));
        }
    }

}
