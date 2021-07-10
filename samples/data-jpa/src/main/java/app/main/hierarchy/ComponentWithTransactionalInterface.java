package app.main.hierarchy;

import org.springframework.stereotype.Component;

// needs jdk proxy
@Component
public class ComponentWithTransactionalInterface implements TransactionalInterface {

    @Override
    public void foo() {

    }

}
