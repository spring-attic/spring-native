package app.main.hierarchy;

import org.springframework.stereotype.Component;

// no proxy needed
@Component
public class ComponentWithTransactionalInterfaceWithoutMethod implements TransactionalInterfaceWithoutMethod {

}
