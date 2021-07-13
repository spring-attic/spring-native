package com.example.validator.hierarchy;

import org.springframework.validation.annotation.Validated;

@Validated
public interface ValidatedInterfaceWithDefault {

    default void foo() {

    }

}
