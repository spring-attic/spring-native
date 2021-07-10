package org.springframework.transactional.components;

import org.springframework.stereotype.Component;

// needs jdk proxy
@Component
public class ComponentWithTransactionalInterfaceAndExtraInterface implements TransactionalInterface, VoidInterface {

    @Override
    public void foo() {

    }

}
