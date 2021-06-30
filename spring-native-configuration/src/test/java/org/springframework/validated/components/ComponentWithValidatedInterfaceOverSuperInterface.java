package org.springframework.validated.components;

import org.springframework.stereotype.Component;

@Component
public class ComponentWithValidatedInterfaceOverSuperInterface implements MiddleInterface {

    @Override
    public void x() {

    }

}
