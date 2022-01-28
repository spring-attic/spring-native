package org.springframework.aot.test.samples.tests;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class SpringWithNestedTest {
    @Nested
    public class NestedTest {
        @Nested
        public class NestedTest2 {
        }
    }
}