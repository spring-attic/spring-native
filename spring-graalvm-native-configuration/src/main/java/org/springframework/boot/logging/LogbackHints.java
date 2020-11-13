package org.springframework.boot.logging;

import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.pattern.LevelConverter;
import ch.qos.logback.classic.pattern.LineSeparatorConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;

import org.springframework.boot.logging.logback.ColorConverter;
import org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter;
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

// TODO Send a PR to Logback to remove reflection usage in ch.qos.logback.classic.PatternLayout
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
			ExtendedWhitespaceThrowableProxyConverter.class
	})
})
public class LogbackHints implements NativeImageConfiguration {
}
