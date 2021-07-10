package app.main.hierarchy;

import org.springframework.stereotype.Component;

// needs jdk proxy
@Component
public class ComponentWithTransactionalInterfaceAndOtherMethod implements TransactionalInterface {

    @Override
    public void foo() {

    }

    public void bar() {

    }

}
