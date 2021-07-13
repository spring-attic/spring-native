package com.example.validator.hierarchy;

import org.springframework.stereotype.Component;

@Component
public class ComponentWithValidatedInterfaceOverProxyInterface implements ProxyInterface {

    @Override
    public void foo() {

    }

}
