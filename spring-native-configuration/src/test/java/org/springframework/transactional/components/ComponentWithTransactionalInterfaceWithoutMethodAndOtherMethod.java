package org.springframework.transactional.components;

import org.springframework.stereotype.Component;

// needs aot proxy
@Component
public class ComponentWithTransactionalInterfaceWithoutMethodAndOtherMethod implements TransactionalInterfaceWithoutMethod {

    public void bar() {

    }

}
