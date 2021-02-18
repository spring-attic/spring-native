package org.springframework.aot.factories.fixtures;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@ConditionalOnClass(name = "org.example.MissingType")
public class TestAutoConfigurationMissingType {
}
