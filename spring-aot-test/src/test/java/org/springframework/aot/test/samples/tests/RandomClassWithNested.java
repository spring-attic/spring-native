package org.springframework.aot.test.samples.tests;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
public class RandomClassWithNested {
    @Nested
    public class NestedTest {
    }
}
