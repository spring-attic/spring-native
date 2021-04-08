package org.apache.commons.pool2;

import org.apache.commons.pool2.impl.BaseGenericObjectPool;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = BaseGenericObjectPool.class, types = @TypeHint(types = DefaultEvictionPolicy.class))
public class CommonsPool2Hints implements NativeConfiguration {
}
