package org.springframework.boot.autoconfigure;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

@ConfigurationHint( typeInfos = {@TypeInfo(types= {AutoConfigureAfter.class,AutoConfigureOrder.class,AutoConfigurationPackage.class },access = AccessBits.CLASS|AccessBits.PUBLIC_METHODS)})
public class Hints implements NativeImageConfiguration { }
