package org.springframework.validated.components;

import org.springframework.validation.annotation.Validated;

@Validated
public interface ValidatedInterfaceWithDefault {

    default void foo() {

    }

}
