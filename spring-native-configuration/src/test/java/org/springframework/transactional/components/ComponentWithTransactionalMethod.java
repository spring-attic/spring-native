package org.springframework.transactional.components;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// aot proxy
@Component
public class ComponentWithTransactionalMethod {

    @Transactional
    public void foo() {

    }

}
