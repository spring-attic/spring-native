package org.springframework.core.annotation;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

@ConfigurationHint( typeInfos = {@TypeInfo(types= {Order.class },access = AccessBits.CLASS|AccessBits.PUBLIC_METHODS)})
public class Hints implements NativeImageConfiguration { }
