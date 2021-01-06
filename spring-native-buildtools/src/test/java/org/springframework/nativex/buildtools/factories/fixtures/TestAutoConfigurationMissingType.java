package org.springframework.nativex.buildtools.factories.fixtures;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@ConditionalOnClass(name = "org.example.MissingType")
public class TestAutoConfigurationMissingType {
}
