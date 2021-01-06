package org.springframework.boot.logging;

import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.pattern.LevelConverter;
import ch.qos.logback.classic.pattern.LineSeparatorConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;

import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import org.springframework.boot.logging.logback.ColorConverter;
import org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter;
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;

// TODO Send a PR to Logback to remove reflection usage in ch.qos.logback.classic.PatternLayout
// TODO Initialize ch.qos.logback.classic.PatternLayout at build time?
@NativeImageHint(typeInfos = {
	@TypeInfo(types= {
			DateConverter.class,
			LevelConverter.class,
			LoggerConverter.class,
			MessageConverter.class,
			LineSeparatorConverter.class,
			ThreadConverter.class,
			MDCConverter.class,
			ColorConverter.class,
			WhitespaceThrowableProxyConverter.class,
			ExtendedWhitespaceThrowableProxyConverter.class,
			IntegerTokenConverter.class,
			DateTokenConverter.class
	})
})
public class LogbackHints implements NativeImageConfiguration {
}
