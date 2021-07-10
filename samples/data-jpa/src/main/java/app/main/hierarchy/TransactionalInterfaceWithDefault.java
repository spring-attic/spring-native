package app.main.hierarchy;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TransactionalInterfaceWithDefault {

    default void foo() {

    }

}
