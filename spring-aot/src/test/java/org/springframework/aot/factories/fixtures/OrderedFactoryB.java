package org.springframework.aot.factories.fixtures;

import org.springframework.core.Ordered;

public class OrderedFactoryB implements OrderedFactory, Ordered {

    @Override
    public int getOrder() {
        return 30;
    }
}
