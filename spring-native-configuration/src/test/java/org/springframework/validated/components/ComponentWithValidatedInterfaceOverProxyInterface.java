package org.springframework.validated.components;

import org.springframework.stereotype.Component;

@Component
public class ComponentWithValidatedInterfaceOverProxyInterface implements ProxyInterface {

    @Override
    public void foo() {

    }

}
