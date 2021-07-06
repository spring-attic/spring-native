package org.springframework.validated;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ValidatedComponentProcessor;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.nativex.util.NativeTestContext;
import org.springframework.validated.components.ComponentWithValidatedInterface;
import org.springframework.validated.components.ComponentWithValidatedInterfaceAndExtraInterface;
import org.springframework.validated.components.ComponentWithValidatedInterfaceOverProxyClass;
import org.springframework.validated.components.ComponentWithValidatedInterfaceOverProxyInterface;
import org.springframework.validated.components.ComponentWithValidatedInterfaceWithDefault;
import org.springframework.validated.components.ComponentWithValidatedInterfaceWithoutMethod;
import org.springframework.validated.components.ValidatedComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidatedComponentProcessorTest {

    private NativeTestContext nativeContext = new NativeTestContext();
    private TypeSystem typeSystem = nativeContext.getTypeSystem();
    private ValidatedComponentProcessor processor;

    @Before
    public void setUp() {
        processor = new ValidatedComponentProcessor();
    }

    @Test
    public void shouldDescribeProxiesForComponentWithValidatedInterface() {
        checkProxies(ComponentWithValidatedInterface.class,
                "org.springframework.validated.components.ValidatedInterface",
                Arrays.asList(
                        "org.springframework.validated.components.ValidatedInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithValidatedInterfaceAndExtraInterface() {
        checkProxies(ComponentWithValidatedInterfaceAndExtraInterface.class,
                "org.springframework.validated.components.ValidatedInterface",
                Arrays.asList(
                        "org.springframework.validated.components.ValidatedInterface",
                        "org.springframework.validated.components.VoidInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithValidatedInterfaceOverSuperClass() {
        checkProxies(ComponentWithValidatedInterfaceOverProxyClass.class,
                "org.springframework.validated.components.ValidatedInterface",
                Arrays.asList(
                        "org.springframework.validated.components.ValidatedInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithValidatedInterfaceOverSuperInterface() {
        checkProxies(ComponentWithValidatedInterfaceOverProxyInterface.class,
                "org.springframework.validated.components.ProxyInterface",
                Arrays.asList(
                        "org.springframework.validated.components.ProxyInterface",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForComponentWithValidatedInterfaceWithoutMethod() {
        checkProxies(ComponentWithValidatedInterfaceWithoutMethod.class,
                null,
                null,
                "org.springframework.validated.components.ComponentWithValidatedInterfaceWithoutMethod");
    }

    @Test
    public void shouldDescribeProxiesForComponentWithValidatedInterfaceWithDefault() {
        checkProxies(ComponentWithValidatedInterfaceWithDefault.class,
                "org.springframework.validated.components.ValidatedInterfaceWithDefault",
                Arrays.asList(
                        "org.springframework.validated.components.ValidatedInterfaceWithDefault",
                        "org.springframework.aop.SpringProxy",
                        "org.springframework.aop.framework.Advised",
                        "org.springframework.core.DecoratingProxy"),
                null);
    }

    @Test
    public void shouldDescribeProxiesForValidatedComponent() {
        checkProxies(ValidatedComponent.class,
                null,
                null,
                "org.springframework.validated.components.ValidatedComponent");
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
