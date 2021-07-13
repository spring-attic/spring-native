package com.example.validator.hierarchy;

import org.springframework.stereotype.Component;

@Component
public class ComponentWithValidatedInterfaceAndExtraInterface implements ValidatedInterface, VoidInterface {

    @Override
    public void foo() {

    }

}
