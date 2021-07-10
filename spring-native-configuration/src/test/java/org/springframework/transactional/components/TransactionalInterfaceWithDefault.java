package org.springframework.transactional.components;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TransactionalInterfaceWithDefault {

    default void foo() {

    }

}
