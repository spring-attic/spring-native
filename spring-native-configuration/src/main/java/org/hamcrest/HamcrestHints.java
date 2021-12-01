package org.hamcrest;

import org.hamcrest.beans.HasProperty;
import org.hamcrest.beans.HasPropertyWithValue;

import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = Matcher.class, types =
@TypeHint(types = {
		HasProperty.class,
		HasPropertyWithValue.class,
		TypeSafeMatcher.class,
		TypeSafeDiagnosingMatcher.class
}, access = Flag.allDeclaredMethods))
public class HamcrestHints implements NativeConfiguration {
}
