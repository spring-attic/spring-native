package org.springframework.aot.factories.fixtures;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@ConditionalOnClass(String.class)
public class TestAutoConfiguration {
}
